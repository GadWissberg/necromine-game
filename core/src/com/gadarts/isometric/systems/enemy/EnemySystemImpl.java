package com.gadarts.isometric.systems.enemy;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.TimeUtils;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.ObstacleComponent;
import com.gadarts.isometric.components.character.CharacterComponent;
import com.gadarts.isometric.components.character.data.CharacterHealthData;
import com.gadarts.isometric.components.decal.HudDecalComponent;
import com.gadarts.isometric.components.decal.RelatedDecal;
import com.gadarts.isometric.components.enemy.EnemyComponent;
import com.gadarts.isometric.systems.GameEntitySystem;
import com.gadarts.isometric.systems.character.CharacterSystem;
import com.gadarts.isometric.systems.character.CharacterSystemEventsSubscriber;
import com.gadarts.isometric.systems.character.commands.CharacterCommand;
import com.gadarts.isometric.systems.character.commands.CharacterCommands;
import com.gadarts.isometric.systems.render.RenderSystemEventsSubscriber;
import com.gadarts.isometric.systems.turns.TurnsSystem;
import com.gadarts.isometric.systems.turns.TurnsSystemEventsSubscriber;
import com.gadarts.isometric.utils.SoundPlayer;
import com.gadarts.isometric.utils.map.MapGraphNode;
import com.gadarts.isometric.utils.map.MapGraphPath;
import com.gadarts.necromine.assets.Assets;
import com.gadarts.necromine.model.characters.SpriteType;

import java.util.List;

/**
 * Handles enemy AI.
 */
public class EnemySystemImpl extends GameEntitySystem<EnemySystemEventsSubscriber> implements
		TurnsSystemEventsSubscriber,
		RenderSystemEventsSubscriber,
		CharacterSystemEventsSubscriber {

	public static final float SKILL_FLOWER_HEIGHT = 2.5F;
	private static final Vector3 auxVector3_1 = new Vector3();
	private static final Vector2 auxVector2_1 = new Vector2();
	private static final Vector2 auxVector2_2 = new Vector2();
	private static final MapGraphPath auxPath = new MapGraphPath();
	private static final CharacterCommand auxCommand = new CharacterCommand();
	private static final float MAX_SIGHT = 7;
	private static final Rectangle auxRect = new Rectangle();
	private static final float ENEMY_HALF_FOV_ANGLE = 75F;
	private static final int NUMBER_OF_SKILL_FLOWER_LEAF = 8;
	private ImmutableArray<Entity> enemies;
	private CharacterSystem characterSystem;
	private TurnsSystem turnsSystem;
	private ImmutableArray<Entity> walls;

	@Override
	public void update(final float deltaTime) {
		super.update(deltaTime);
		for (Entity enemy : enemies) {
			EnemyComponent enemyComponent = ComponentsMapper.enemy.get(enemy);
			if (enemyComponent.isAwaken()) {
				if (TimeUtils.timeSinceMillis(enemyComponent.getNextRoamSound()) >= 0) {
					enemyComponent.calculateNextRoamSound();
					services.getSoundPlayer().playSound(Assets.Sounds.ENEMY_ROAM);
				}
			}
		}
	}

	@Override
	public void dispose() {

	}

	@Override
	public void addedToEngine(final Engine engine) {
		super.addedToEngine(engine);
		enemies = engine.getEntitiesFor(Family.all(EnemyComponent.class).get());
		walls = engine.getEntitiesFor(Family.all(ObstacleComponent.class).get());
	}


	@Override
	public void onEnemyTurn(final long currentTurnId) {
		if (invokeTurnForUnplayedEnemy(currentTurnId)) return;
		enemyFinishedTurn();
	}

	private boolean invokeTurnForUnplayedEnemy(final long currentTurnId) {
		for (Entity enemy : enemies) {
			int hp = ComponentsMapper.character.get(enemy).getSkills().getHealthData().getHp();
			EnemyComponent enemyComponent = ComponentsMapper.enemy.get(enemy);
			if (enemyComponent.getLastTurn() < currentTurnId) {
				if (hp > 0 && enemyComponent.isAwaken()) {
					invokeEnemyTurn(enemy);
					return true;
				}
			}
		}
		return false;
	}

	private void invokeEnemyTurn(final Entity enemy) {
		Vector2 enemyPosition = ComponentsMapper.characterDecal.get(enemy).getNodePosition(auxVector2_1);
		Entity target = ComponentsMapper.character.get(enemy).getTarget();
		MapGraphNode enemyNode = services.getMap().getNode(enemyPosition);
		applyGoToMelee(enemy, enemyNode, target);
	}

	private void applyGoToMelee(final Entity enemy,
								final MapGraphNode enemyNode,
								final Entity target) {
		if (characterSystem.calculatePathToCharacter(enemyNode, target, auxPath)) {
			auxPath.nodes.removeIndex(auxPath.getCount() - 1);
			auxCommand.init(CharacterCommands.GO_TO_MELEE, auxPath, enemy);
			characterSystem.applyCommand(auxCommand, enemy);
		} else {
			onCharacterCommandDone(enemy);
		}
	}


	@Override
	public void onTurnsSystemReady(final TurnsSystem turnsSystem) {
		this.turnsSystem = turnsSystem;
	}


	@Override
	public void onCharacterCommandDone(final Entity character) {
		if (ComponentsMapper.enemy.has(character)) {
			long currentTurnId = turnsSystem.getCurrentTurnId();
			ComponentsMapper.enemy.get(character).setLastTurn(currentTurnId);
			onEnemyTurn(currentTurnId);
		}
	}

	private void enemyFinishedTurn() {
		for (EnemySystemEventsSubscriber subscriber : subscribers) {
			subscriber.onEnemyFinishedTurn();
		}
	}


	@Override
	public void onCharacterSystemReady(final CharacterSystem characterSystem) {
		this.characterSystem = characterSystem;
	}

	@Override
	public void onCharacterGotDamage(final Entity entity) {
		if (ComponentsMapper.enemy.has(entity)) {
			awakeEnemy(entity);
			refreshSkillFlower(entity);
		}
	}

	private void refreshSkillFlower(final Entity entity) {
		List<RelatedDecal> relatedDecals = ComponentsMapper.simpleDecal.get(entity).getRelatedDecals();
		CharacterHealthData healthData = ComponentsMapper.character.get(entity).getSkills().getHealthData();
		float div = (((float) healthData.getHp()) / ((float) healthData.getInitialHp()));
		int numberOfVisibleLeaf = (int) (div * NUMBER_OF_SKILL_FLOWER_LEAF);
		for (int i = 0; i < relatedDecals.size(); i++) {
			relatedDecals.get(i).setVisible(i < numberOfVisibleLeaf);
		}
	}


	@Override
	public void onCharacterDies(final Entity character) {
		if (ComponentsMapper.enemy.has(character)) {
			ComponentsMapper.enemy.get(character).setAwaken(false);
			refreshSkillFlower(character);
		}
	}


	@Override
	public void onFrameChanged(final Entity entity, final float deltaTime, final TextureAtlas.AtlasRegion newFrame) {
		SpriteType spriteType = ComponentsMapper.character.get(entity).getCharacterSpriteData().getSpriteType();
		if (ComponentsMapper.enemy.has(entity)) {
			if (spriteType == SpriteType.ATTACK) {
				onFrameChangedOfAttack(entity, newFrame);
			} else if (spriteType == SpriteType.RUN) {
				onFrameChangedOfRun(entity);
			}
		} else if (ComponentsMapper.player.has(entity) && spriteType == SpriteType.RUN) {
			checkLineOfSightForEnemies(entity);
		}
	}

	private void onFrameChangedOfRun(final Entity entity) {
		Vector3 position = ComponentsMapper.characterDecal.get(entity).getDecal().getPosition();
		HudDecalComponent hudDecalComponent = ComponentsMapper.simpleDecal.get(entity);
		hudDecalComponent.getDecal().setPosition(position.x, SKILL_FLOWER_HEIGHT, position.z);
		List<RelatedDecal> relatedDecals = hudDecalComponent.getRelatedDecals();
		for (RelatedDecal decal : relatedDecals) {
			if (decal.isVisible()) {
				decal.setPosition(position.x, SKILL_FLOWER_HEIGHT, position.z);
			}
		}
	}

	private void onFrameChangedOfAttack(final Entity entity, final TextureAtlas.AtlasRegion newFrame) {
		if (newFrame.index == ComponentsMapper.character.get(entity).getCharacterSpriteData().getHitFrameIndex()) {
			SoundPlayer soundPlayer = services.getSoundPlayer();
			soundPlayer.playSound(ComponentsMapper.enemy.get(entity).getEnemyDefinition().getAttackSound());
		}
	}

	private void checkLineOfSightForEnemies(final Entity entity) {
		for (Entity enemy : enemies) {
			int hp = ComponentsMapper.character.get(enemy).getSkills().getHealthData().getHp();
			if (hp > 0 && !ComponentsMapper.enemy.get(enemy).isAwaken()) {
				Decal enemyDecal = ComponentsMapper.characterDecal.get(enemy).getDecal();
				Vector3 enemyPosition = auxVector3_1.set(enemyDecal.getPosition());
				Vector3 playerPosition = ComponentsMapper.characterDecal.get(entity).getDecal().getPosition();
				if (enemyPosition.dst2(playerPosition) <= Math.pow(MAX_SIGHT, 2)) {
					checkLineOfSightForEnemy(enemy);
				}
			}
		}
	}

	private void checkLineOfSightForEnemy(final Entity enemy) {
		if (!isTargetInFov(enemy)) return;
		for (Entity wall : walls) {
			if (checkIfWallBlocksLineOfSightToTarget(enemy, wall)) {
				return;
			}
		}
		awakeEnemy(enemy);
		services.getSoundPlayer().playSound(Assets.Sounds.ENEMY_AWAKE);
	}

	private void awakeEnemy(final Entity enemy) {
		ComponentsMapper.enemy.get(enemy).setAwaken(true);
		for (EnemySystemEventsSubscriber subscriber : subscribers) {
			subscriber.onEnemyAwaken(enemy);
		}
	}

	private boolean isTargetInFov(final Entity enemy) {
		Vector3 enemyPos = ComponentsMapper.characterDecal.get(enemy).getDecal().getPosition();
		CharacterComponent charComponent = ComponentsMapper.character.get(enemy);
		Vector3 targetPosition = ComponentsMapper.characterDecal.get(charComponent.getTarget()).getDecal().getPosition();
		Vector2 directionToTarget = auxVector2_2.set(targetPosition.x, targetPosition.z).sub(enemyPos.x, enemyPos.z).nor();
		Vector2 enemyDirection = charComponent.getCharacterSpriteData().getFacingDirection().getDirection(auxVector2_1);
		return !(Math.abs(directionToTarget.angleDeg() - enemyDirection.angleDeg()) > ENEMY_HALF_FOV_ANGLE);
	}

	private boolean checkIfWallBlocksLineOfSightToTarget(final Entity enemy, final Entity wall) {
		Rectangle rect = initializeRectOfWall(wall);
		Vector3 enemyPos = ComponentsMapper.characterDecal.get(enemy).getDecal().getPosition();
		Entity target = ComponentsMapper.character.get(enemy).getTarget();
		Vector3 targetPos = ComponentsMapper.characterDecal.get(target).getDecal().getPosition();
		return Intersector.intersectSegmentRectangle(
				auxVector2_1.set(enemyPos.x, enemyPos.z),
				auxVector2_2.set(targetPos.x, targetPos.z),
				rect);
	}

	private Rectangle initializeRectOfWall(final Entity wall) {
		ObstacleComponent wallComp = ComponentsMapper.obstacle.get(wall);
		return auxRect.set(wallComp.getTopLeftX(), wallComp.getTopLeftY(),
				Math.abs(wallComp.getTopLeftX() - (wallComp.getBottomRightX() + 1)),
				Math.abs(wallComp.getTopLeftY() - (wallComp.getBottomRightY() + 1)));
	}

	@Override
	public void activate() {

	}
}
