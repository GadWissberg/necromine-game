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
import com.gadarts.isometric.components.EnemyComponent;
import com.gadarts.isometric.components.WallComponent;
import com.gadarts.isometric.components.character.CharacterComponent;
import com.gadarts.isometric.components.character.SpriteType;
import com.gadarts.isometric.systems.GameEntitySystem;
import com.gadarts.isometric.systems.character.CharacterCommand;
import com.gadarts.isometric.systems.character.CharacterSystem;
import com.gadarts.isometric.systems.character.CharacterSystemEventsSubscriber;
import com.gadarts.isometric.systems.character.Commands;
import com.gadarts.isometric.systems.render.RenderSystem;
import com.gadarts.isometric.systems.render.RenderSystemEventsSubscriber;
import com.gadarts.isometric.systems.turns.TurnsSystem;
import com.gadarts.isometric.systems.turns.TurnsSystemEventsSubscriber;
import com.gadarts.isometric.utils.assets.Assets;
import com.gadarts.isometric.utils.map.MapGraphNode;
import com.gadarts.isometric.utils.map.MapGraphPath;

/**
 * Handles enemy AI.
 */
public class EnemySystemImpl extends GameEntitySystem<EnemySystemEventsSubscriber> implements
		TurnsSystemEventsSubscriber,
		RenderSystemEventsSubscriber,
		CharacterSystemEventsSubscriber {

	private static final Vector3 auxVector3_1 = new Vector3();
	private static final Vector2 auxVector2_1 = new Vector2();
	private static final Vector2 auxVector2_2 = new Vector2();
	private static final MapGraphPath auxPath = new MapGraphPath();
	private static final CharacterCommand auxCommand = new CharacterCommand();
	private static final float MAX_SIGHT = 10;
	private static final Rectangle auxRect = new Rectangle();
	private static final float ENEMY_HALF_FOV_ANGLE = 75f;

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
					soundPlayer.playSound(Assets.Sounds.ENEMY_ROAM);
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
		walls = engine.getEntitiesFor(Family.all(WallComponent.class).get());
	}


	@Override
	public void onEnemyTurn(final long currentTurnId) {
		for (Entity enemy : enemies) {
			int hp = ComponentsMapper.character.get(enemy).getHealthData().getHp();
			EnemyComponent enemyComponent = ComponentsMapper.enemy.get(enemy);
			if (enemyComponent.getLastTurn() < currentTurnId) {
				if (hp > 0 && enemyComponent.isAwaken()) {
					invokeEnemyTurn(enemy);
					return;
				}
			}
		}
		enemyFinishedTurn();
	}

	private void invokeEnemyTurn(final Entity enemy) {
		Vector2 enemyPosition = ComponentsMapper.characterDecal.get(enemy).getNodePosition(auxVector2_1);
		Entity target = ComponentsMapper.character.get(enemy).getTarget();
		MapGraphNode enemyNode = map.getNode(enemyPosition);
		applyGoToMelee(enemy, enemyNode, target);
	}

	private void applyGoToMelee(final Entity enemy,
								final MapGraphNode enemyNode,
								final Entity target) {
		if (characterSystem.calculatePathToCharacter(enemyNode, target, auxPath)) {
			auxPath.nodes.removeIndex(auxPath.getCount() - 1);
			auxCommand.init(Commands.GO_TO_MELEE, auxPath, enemy);
			characterSystem.applyCommand(auxCommand, enemy);
		}
	}

	@Override
	public void onPlayerTurn(final long currentTurnId) {

	}

	@Override
	public void onTurnsSystemReady(final TurnsSystem turnsSystem) {
		this.turnsSystem = turnsSystem;
	}

	@Override
	public void onDestinationReached(final Entity character) {
	}

	@Override
	public void onCommandDone(final Entity character) {
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
	public void onNewCommandSet(final CharacterCommand command) {

	}

	@Override
	public void onCharacterSystemReady(final CharacterSystem characterSystem) {
		this.characterSystem = characterSystem;
	}

	@Override
	public void onCharacterGotDamage(final Entity entity) {
		if (ComponentsMapper.enemy.has(entity)) {
			ComponentsMapper.enemy.get(entity).setAwaken(true);
		}
	}

	@Override
	public void onItemPickedUp(final Entity itemPickedUp) {

	}

	@Override
	public void onCharacterDies(final Entity character) {
		if (ComponentsMapper.enemy.has(character)) {
			ComponentsMapper.enemy.get(character).setAwaken(false);
		}
	}

	@Override
	public void onCharacterNodeChanged(Entity entity, MapGraphNode oldNode, MapGraphNode newNode) {

	}

	@Override
	public void activate() {

	}

	@Override
	public void onFrameChanged(final Entity entity, final float deltaTime, final TextureAtlas.AtlasRegion newFrame) {
		SpriteType spriteType = ComponentsMapper.character.get(entity).getCharacterSpriteData().getSpriteType();
		if (ComponentsMapper.enemy.has(entity)) {
			if (spriteType == SpriteType.ATTACK) {
				if (newFrame.index == ComponentsMapper.character.get(entity).getCharacterSpriteData().getHitFrameIndex()) {
					soundPlayer.playSound(ComponentsMapper.enemy.get(entity).getEnemyDefinition().getAttackSound());
				}
			}
		} else if (ComponentsMapper.player.has(entity) && spriteType == SpriteType.RUN) {
			checkLineOfSightForEnemies(entity);
		}
	}

	private void checkLineOfSightForEnemies(final Entity entity) {
		for (Entity enemy : enemies) {
			int hp = ComponentsMapper.character.get(enemy).getHealthData().getHp();
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
		ComponentsMapper.enemy.get(enemy).setAwaken(true);
		soundPlayer.playSound(Assets.Sounds.ENEMY_AWAKE);
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
		WallComponent wallComponent = ComponentsMapper.wall.get(wall);
		auxRect.set(wallComponent.getTopLeftX(), wallComponent.getTopLeftY(),
				Math.abs(wallComponent.getTopLeftX() - (wallComponent.getBottomRightX() + 1)),
				Math.abs(wallComponent.getTopLeftY() - (wallComponent.getBottomRightY() + 1))
		);
		Vector3 enemyPosition = ComponentsMapper.characterDecal.get(enemy).getDecal().getPosition();
		Entity target = ComponentsMapper.character.get(enemy).getTarget();
		Vector3 targetPosition = ComponentsMapper.characterDecal.get(target).getDecal().getPosition();
		auxVector2_1.set(enemyPosition.x, enemyPosition.z);
		auxVector2_2.set(targetPosition.x, targetPosition.z);
		return Intersector.intersectSegmentRectangle(auxVector2_1, auxVector2_2, auxRect);
	}

	@Override
	public void onRenderSystemReady(final RenderSystem renderSystem) {

	}
}
