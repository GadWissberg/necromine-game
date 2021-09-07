package com.gadarts.isometric.systems.bullets;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;
import com.gadarts.isometric.components.BulletComponent;
import com.gadarts.isometric.components.CollisionComponent;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.enemy.EnemyComponent;
import com.gadarts.isometric.components.player.Weapon;
import com.gadarts.isometric.systems.GameEntitySystem;
import com.gadarts.isometric.systems.character.CharacterSystemEventsSubscriber;
import com.gadarts.isometric.utils.EntityBuilder;
import com.gadarts.isometric.utils.map.MapGraph;
import com.gadarts.isometric.utils.map.MapGraphNode;
import com.gadarts.necromine.model.MapNodesTypes;
import com.gadarts.necromine.model.characters.attributes.Accuracy;
import com.gadarts.necromine.model.pickups.WeaponsDefinitions;

import static com.badlogic.gdx.math.Vector3.Zero;

/**
 * Handles weapons bullets behaviour.
 */
public class BulletsSystemImpl extends GameEntitySystem<BulletsSystemEventsSubscriber> implements BulletSystem, CharacterSystemEventsSubscriber {

	private final static Vector2 auxVector2_1 = new Vector2();
	private final static Vector3 auxVector3_1 = new Vector3();
	private final static Vector3 auxVector3_2 = new Vector3();
	private final static Vector3 auxVector3_3 = new Vector3();
	private final static Vector3 auxVector3_4 = new Vector3();
	private final static float BULLET_SPEED = 0.2f;
	private final static float BULLET_MAX_DISTANCE = 10;
	private final static float CHAR_RAD = 0.3f;
	private final static float OBST_RAD = 0.5f;
	private static final float PROJECTILE_LIGHT_INTENSITY = 0.4F;
	private static final float PROJECTILE_LIGHT_RADIUS = 2F;
	private static final Color PROJECTILE_LIGHT_COLOR = Color.valueOf("#8396FF");
	private static final Bresenham2 bresenham = new Bresenham2();
	private static final float HIT_SCAN_MAX_DISTANCE = 5;
	private static final float PROJECTILE_RELATIVE_HEIGHT = 0.5F;

	private ImmutableArray<Entity> bullets;
	private ImmutableArray<Entity> collidables;

	@Override
	public void activate( ) {

	}

	@Override
	public void onCharacterEngagesPrimaryAttack(final Entity character,
												final Vector3 direction,
												final Vector3 charPos) {
		if (ComponentsMapper.enemy.has(character)) {
			enemyEngagesPrimaryAttack(character, direction, charPos);
		} else {
			Weapon selectedWeapon = ComponentsMapper.player.get(character).getStorage().getSelectedWeapon();
			if (selectedWeapon.isHitScan()) {
				MapGraph map = services.getMapService().getMap();
				MapGraphNode posNode = map.getNode(ComponentsMapper.characterDecal.get(character).getNodePosition(auxVector3_1));
				Vector3 posNodeCenterPosition = posNode.getCenterPosition(auxVector3_4);
				int maxAngle = ComponentsMapper.character.get(character).getSkills().getAccuracy().getMaxAngle();
				direction.rotate(Vector3.Y, MathUtils.random(-maxAngle, maxAngle));
				Vector3 step = auxVector3_3.setZero().add(direction.setLength(HIT_SCAN_MAX_DISTANCE));
				Vector3 maxRangePos = auxVector3_2.set(posNodeCenterPosition).add(step);
				Array<GridPoint2> nodes = bresenham.line((int) posNodeCenterPosition.x, (int) posNodeCenterPosition.z, (int) maxRangePos.x, (int) maxRangePos.z);
				for (GridPoint2 n : nodes) {
					MapGraphNode node = map.getNode(n.x, n.y);
					if (node.getHeight() > map.getNode((int) posNodeCenterPosition.x, (int) posNodeCenterPosition.z).getHeight() + 1) {
						break;
					}
					Entity enemy = map.getAliveEnemyFromNode(node);
					if (enemy != null) {
						onHitScanCollisionWithAnotherEntity((WeaponsDefinitions) selectedWeapon.getDefinition(), enemy);
						break;
					}
				}
			}
		}
	}

	private void enemyEngagesPrimaryAttack(final Entity character, final Vector3 direction, final Vector3 charPos) {
		EnemyComponent enemyComponent = ComponentsMapper.enemy.get(character);
		Accuracy accuracy = enemyComponent.getEnemyDefinition().getAccuracy()[enemyComponent.getSkill() - 1];
		int maxAngle = accuracy.getMaxAngle();
		direction.rotate(Vector3.Y, MathUtils.random(-maxAngle, maxAngle));
		EnemyComponent enemyComp = ComponentsMapper.enemy.get(character);
		Animation<TextureAtlas.AtlasRegion> bulletAnim = enemyComp.getBulletAnimation();
		createBullet(character, direction, charPos, enemyComp, bulletAnim);
	}

	private void createBullet(final Entity character,
							  final Vector3 direction,
							  final Vector3 charPos,
							  final EnemyComponent enemyComp,
							  final Animation<TextureAtlas.AtlasRegion> bulletAnim) {
		charPos.y += PROJECTILE_RELATIVE_HEIGHT;
		Integer[] damagePoints = enemyComp.getEnemyDefinition().getPrimaryAttack().getDamagePoints();
		EntityBuilder.beginBuildingEntity((PooledEngine) getEngine())
				.addBulletComponent(charPos, direction, character, damagePoints[enemyComp.getSkill() - 1])
				.addAnimationComponent(enemyComp.getEnemyDefinition().getPrimaryAttack().getFrameDuration(), bulletAnim)
				.addSimpleDecalComponent(charPos, bulletAnim.getKeyFrames()[0], Zero.setZero(), true, true)
				.addLightComponent(charPos, PROJECTILE_LIGHT_INTENSITY, PROJECTILE_LIGHT_RADIUS, PROJECTILE_LIGHT_COLOR)
				.finishAndAddToEngine();
	}


	@Override
	public void update(final float deltaTime) {
		super.update(deltaTime);
		for (Entity bullet : bullets) {
			Decal decal = ComponentsMapper.simpleDecal.get(bullet).getDecal();
			BulletComponent bulletComponent = ComponentsMapper.bullet.get(bullet);
			handleCollisions(decal, bullet);
			handleBulletMovement(decal, bulletComponent);
			handleBulletMaxDistance(bullet, decal, bulletComponent);
		}
	}

	private void handleCollisions(final Decal decal, final Entity bullet) {
		if (!handleCollisionsWithWalls(bullet)) {
			handleCollisionsWithOtherEntities(decal, bullet);
		}
	}

	private void handleCollisionsWithOtherEntities(final Decal decal, final Entity bullet) {
		for (Entity collidable : collidables) {
			if (ComponentsMapper.bullet.get(bullet).getOwner() != collidable) {
				if (checkCollision(decal, collidable)) {
					onProjectileCollisionWithAnotherEntity(bullet, collidable);
					break;
				}
			}
		}
	}

	private boolean handleCollisionsWithWalls(final Entity bullet) {
		MapGraph map = services.getMapService().getMap();
		Vector3 position = ComponentsMapper.simpleDecal.get(bullet).getDecal().getPosition();
		MapGraphNode node = map.getNode(position);
		MapNodesTypes nodeType = node.getType();
		if (nodeType != MapNodesTypes.PASSABLE_NODE || node.getHeight() >= position.y) {
			onCollisionWithWall(bullet, node);
			return true;
		}
		return false;
	}

	private void onProjectileCollisionWithAnotherEntity(final Entity bullet, final Entity collidable) {
		getEngine().removeEntity(bullet);
		for (BulletsSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onProjectileCollisionWithAnotherEntity(bullet, collidable);
		}
	}

	private void onHitScanCollisionWithAnotherEntity(final WeaponsDefinitions definition, final Entity collidable) {
		for (BulletsSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onHitScanCollisionWithAnotherEntity(definition, collidable);
		}
	}

	private void onCollisionWithWall(final Entity bullet, final MapGraphNode node) {
		getEngine().removeEntity(bullet);
		for (BulletsSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onBulletCollisionWithWall(bullet, node);
		}
	}

	private boolean checkCollision(final Decal decal, final Entity collidable) {
		if (ComponentsMapper.characterDecal.has(collidable)) {
			return checkCollisionWithCharacter(decal, collidable);
		} else if (ComponentsMapper.modelInstance.has(collidable)) {
			return checkCollisionWithObstacle(decal, collidable);
		}
		return false;
	}

	private boolean checkCollisionWithObstacle(final Decal decal, final Entity collidable) {
		if (!ComponentsMapper.obstacle.get(collidable).getType().isWall()) return false;
		ModelInstance modelInstance = ComponentsMapper.modelInstance.get(collidable).getModelInstance();
		Vector3 collPos = modelInstance.transform.getTranslation(auxVector3_1);
		Vector3 position = decal.getPosition();
		return auxVector2_1.set(collPos.x + 0.5f, collPos.z + 0.5f).dst(position.x, position.z) < OBST_RAD;
	}

	private boolean checkCollisionWithCharacter(final Decal decal, final Entity collidable) {
		Vector3 colPos = ComponentsMapper.characterDecal.get(collidable).getDecal().getPosition();
		return ComponentsMapper.character.get(collidable).getSkills().getHealthData().getHp() > 0
				&& auxVector2_1.set(colPos.x, colPos.z).dst(decal.getPosition().x, decal.getPosition().z) < CHAR_RAD;
	}

	private void handleBulletMaxDistance(final Entity bullet, final Decal decal, final BulletComponent bulletComponent) {
		Vector3 position = decal.getPosition();
		float dst = bulletComponent.getInitialPosition(auxVector2_1).dst(position.x, position.z);
		if (dst >= BULLET_MAX_DISTANCE) {
			getEngine().removeEntity(bullet);
		}
	}

	private void handleBulletMovement(final Decal decal, final BulletComponent bulletComponent) {
		Vector3 velocity = bulletComponent.getDirection(auxVector3_1).nor().scl(BULLET_SPEED);
		decal.translate(velocity.x, 0, velocity.z);
	}

	@Override
	public void addedToEngine(final Engine engine) {
		super.addedToEngine(engine);
		bullets = engine.getEntitiesFor(Family.all(BulletComponent.class).get());
		collidables = engine.getEntitiesFor(Family.all(CollisionComponent.class).get());
	}

	@Override
	public void dispose( ) {

	}
}
