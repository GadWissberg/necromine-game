package com.gadarts.isometric.systems.enemy;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.math.Bresenham2;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.character.CharacterComponent;
import com.gadarts.isometric.components.character.data.CharacterHealthData;
import com.gadarts.isometric.components.decal.RelatedDecal;
import com.gadarts.isometric.components.decal.SimpleDecalComponent;
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
import com.gadarts.isometric.utils.map.MapGraph;
import com.gadarts.isometric.utils.map.MapGraphNode;
import com.gadarts.isometric.utils.map.MapGraphPath;
import com.gadarts.necromine.model.characters.Enemies;
import com.gadarts.necromine.model.characters.SpriteType;
import com.gadarts.necromine.model.characters.attributes.Range;

import java.util.List;

import static com.gadarts.necromine.assets.Assets.Sounds.ENEMY_AWAKE;
import static com.gadarts.necromine.assets.Assets.Sounds.ENEMY_ROAM;
import static com.gadarts.necromine.model.characters.attributes.Accuracy.NONE;

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
	private static final float ENEMY_HALF_FOV_ANGLE = 75F;
	private static final int NUMBER_OF_SKILL_FLOWER_LEAF = 8;
	private static final Bresenham2 bresenham = new Bresenham2();
	private static final float RANGE_ATTACK_MIN_RADIUS = 1.7F;
	private ImmutableArray<Entity> enemies;
	private CharacterSystem characterSystem;
	private TurnsSystem turnsSystem;

	@Override
	public void update(final float deltaTime) {
		super.update(deltaTime);
		for (Entity enemy : enemies) {
			EnemyComponent enemyComponent = ComponentsMapper.enemy.get(enemy);
			if (enemyComponent.isAwaken()) {
				if (TimeUtils.timeSinceMillis(enemyComponent.getNextRoamSound()) >= 0) {
					if (enemyComponent.getNextRoamSound() != 0) {
						SoundPlayer soundPlayer = services.getSoundPlayer();
						soundPlayer.playSound(ENEMY_ROAM);
					}
					enemyComponent.calculateNextRoamSound();
				}
			}
		}
	}


	@Override
	public void dispose( ) {

	}

	@Override
	public void addedToEngine(final Engine engine) {
		super.addedToEngine(engine);
		enemies = engine.getEntitiesFor(Family.all(EnemyComponent.class).get());
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
		MapGraphNode enemyNode = services.getMapService().getMap().getNode(enemyPosition);
		EnemyComponent enemyComponent = ComponentsMapper.enemy.get(enemy);
		Enemies enemyDefinition = enemyComponent.getEnemyDefinition();
		if (!considerPrimaryAttack(enemy, enemyComponent, enemyDefinition, enemyComponent.getSkill() - 1)) {
			applyGoToMelee(enemy, enemyNode, target);
		}
	}

	private boolean considerPrimaryAttack(final Entity enemy,
										  final EnemyComponent enemyCom,
										  final Enemies def,
										  final int skillIndex) {
		if (def.getAccuracy().get(skillIndex) != NONE && def.getRange().get(skillIndex) != Range.NONE) {
			float disToTarget = calculateDistanceToTarget(enemy);
			if (disToTarget <= def.getRange().get(skillIndex).getMaxDistance() && disToTarget > RANGE_ATTACK_MIN_RADIUS) {
				int turnsDiff = def.getReloadTime().get(skillIndex).getNumberOfTurns();
				if (checkIfPrimaryAttackIsReady(enemyCom, turnsDiff) && checkLineOfSightForEnemy(enemy, false)) {
					applyCommand(enemy, CharacterCommands.ATTACK_PRIMARY);
					return true;
				}
			}
		}
		return false;
	}

	private boolean checkIfPrimaryAttackIsReady(final EnemyComponent enemyComponent, final int turnsDiff) {
		return turnsSystem.getCurrentTurnId() - enemyComponent.getLastPrimaryAttack() > turnsDiff;
	}

	private void applyCommand(final Entity enemy, final CharacterCommands attackPrimary) {
		auxCommand.init(attackPrimary, auxPath, enemy);
		characterSystem.applyCommand(auxCommand, enemy);
	}

	private float calculateDistanceToTarget(final Entity enemy) {
		Entity target = ComponentsMapper.character.get(enemy).getTarget();
		Vector3 targetPosition = ComponentsMapper.characterDecal.get(target).getDecal().getPosition();
		Vector3 position = ComponentsMapper.characterDecal.get(enemy).getDecal().getPosition();
		return position.dst(targetPosition);
	}

	private void applyGoToMelee(final Entity enemy,
								final MapGraphNode enemyNode,
								final Entity target) {
		if (characterSystem.calculatePathToCharacter(enemyNode, target, auxPath)) {
			auxPath.nodes.removeIndex(auxPath.getCount() - 1);
			applyCommand(enemy, CharacterCommands.GO_TO_MELEE);
		} else {
			onCharacterCommandDone(enemy, null);
		}
	}


	@Override
	public void onTurnsSystemReady(final TurnsSystem turnsSystem) {
		this.turnsSystem = turnsSystem;
	}


	@Override
	public void onCharacterCommandDone(final Entity character, final CharacterCommand executedCommand) {
		if (ComponentsMapper.enemy.has(character)) {
			long currentTurnId = turnsSystem.getCurrentTurnId();
			EnemyComponent enemyComponent = ComponentsMapper.enemy.get(character);
			if (executedCommand != null && executedCommand.getType() == CharacterCommands.ATTACK_PRIMARY) {
				enemyComponent.setLastPrimaryAttack(currentTurnId);
			}
			enemyComponent.setLastTurn(currentTurnId);
			onEnemyTurn(currentTurnId);
		}
	}

	private void enemyFinishedTurn( ) {
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
		SimpleDecalComponent simpleDecalComponent = ComponentsMapper.simpleDecal.get(entity);
		simpleDecalComponent.getDecal().setPosition(position.x, SKILL_FLOWER_HEIGHT, position.z);
		List<RelatedDecal> relatedDecals = simpleDecalComponent.getRelatedDecals();
		for (RelatedDecal decal : relatedDecals) {
			if (decal.isVisible()) {
				decal.setPosition(position.x, SKILL_FLOWER_HEIGHT, position.z);
			}
		}
	}

	private void onFrameChangedOfAttack(final Entity entity, final TextureAtlas.AtlasRegion newFrame) {
		if (newFrame.index == ComponentsMapper.character.get(entity).getCharacterSpriteData().getMeleeHitFrameIndex()) {
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
					awakeEnemyIfSpotsTarget(enemy);
				}
			}
		}
	}

	private void awakeEnemyIfSpotsTarget(final Entity enemy) {
		boolean spotted = checkLineOfSightForEnemy(enemy, true);
		if (spotted) {
			awakeEnemy(enemy);
			services.getSoundPlayer().playSound(ENEMY_AWAKE);
		}
	}

	private boolean checkLineOfSightForEnemy(final Entity enemy, final boolean checkFov) {
		if (checkFov && !isTargetInFov(enemy)) return false;
		return !checkIfFloorNodesBlockSight(enemy);
	}

	private boolean checkIfFloorNodesBlockSight(final Entity enemy) {
		Vector2 pos = ComponentsMapper.characterDecal.get(enemy).getNodePosition(auxVector2_1);
		Entity target = ComponentsMapper.character.get(enemy).getTarget();
		Vector2 targetPos = ComponentsMapper.characterDecal.get(target).getNodePosition(auxVector2_2);
		Array<GridPoint2> nodes = bresenham.line((int) pos.x, (int) pos.y, (int) targetPos.x, (int) targetPos.y);
		MapGraph map = services.getMapService().getMap();
		for (GridPoint2 n : nodes) {
			if (map.getNode(n.x, n.y).getHeight() > map.getNode((int) pos.x, (int) pos.y).getHeight() + 1) {
				return true;
			}
		}
		return false;
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

	@Override
	public void activate( ) {

	}
}
