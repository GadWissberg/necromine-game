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
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.batches.PointSpriteParticleBatch;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;
import com.gadarts.isometric.components.BulletComponent;
import com.gadarts.isometric.components.CollisionComponent;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.enemy.EnemyComponent;
import com.gadarts.isometric.components.player.Weapon;
import com.gadarts.isometric.systems.GameEntitySystem;
import com.gadarts.isometric.systems.character.CharacterSystemEventsSubscriber;
import com.gadarts.isometric.systems.particles.ParticleEffectsSystemEventsSubscriber;
import com.gadarts.isometric.utils.EntityBuilder;
import com.gadarts.isometric.utils.map.MapGraph;
import com.gadarts.isometric.utils.map.MapGraphNode;
import com.gadarts.necromine.assets.Assets;
import com.gadarts.necromine.assets.Assets.ParticleEffects;
import com.gadarts.necromine.model.characters.attributes.Accuracy;
import com.gadarts.necromine.model.map.MapNodesTypes;
import com.gadarts.necromine.model.pickups.WeaponsDefinitions;

import static com.badlogic.gdx.math.Vector3.Zero;

/**
 * Handles weapons bullets behaviour.
 */
public class BulletsSystemImpl extends GameEntitySystem<BulletsSystemEventsSubscriber> implements
		BulletSystem,
		CharacterSystemEventsSubscriber, ParticleEffectsSystemEventsSubscriber {

	private final static Vector2 auxVector2_1 = new Vector2();
	private final static Vector2 auxVector2_2 = new Vector2();
	private final static Vector2 auxVector2_3 = new Vector2();
	private final static Vector2 auxVector2_4 = new Vector2();
	private final static Vector2 auxVector2_5 = new Vector2();
	private final static Vector2 auxVector2_6 = new Vector2();
	private final static Vector3 auxVector3_1 = new Vector3();
	private final static Vector3 auxVector3_2 = new Vector3();
	private final static Vector3 auxVector3_3 = new Vector3();
	private final static Vector3 auxVector3_4 = new Vector3();
	private final static float BULLET_SPEED = 0.2f;
	private final static float BULLET_MAX_DISTANCE = 14;
	private final static float CHAR_RAD = 0.3f;
	private final static float OBST_RAD = 0.5f;
	private final static float PROJECTILE_LIGHT_INTENSITY = 0.2F;
	private final static float PROJECTILE_LIGHT_RADIUS = 2F;
	private final static Color PROJECTILE_LIGHT_COLOR = Color.valueOf("#8396FF");
	private final static float HITSCAN_COL_LIGHT_INTENSITY = 0.1F;
	private final static float HITSCAN_COL_LIGHT_RADIUS = 1.2F;
	private final static Color HITSCAN_COL_LIGHT_COLOR = Color.YELLOW;
	private final static float HITSCAN_COL_LIGHT_DURATION = 0.1F;
	private final static Bresenham2 bresenham = new Bresenham2();
	private final static float HIT_SCAN_MAX_DISTANCE = 10F;
	private final static float BULLET_EXPLOSION_LIGHT_INTENSITY = 0.3F;
	private final static float BULLET_EXPLOSION_LIGHT_DURATION = 0.2F;
	private final static float BULLET_EXPLOSION_LIGHT_RADIUS = 1F;
	private ImmutableArray<Entity> bullets;
	private ImmutableArray<Entity> collidables;
	private ParticleEffect bulletRicochetEffect;

	@Override
	public void activate() {
	}

	@Override
	public void onCharacterEngagesPrimaryAttack(final Entity character,
												final Vector3 direction,
												final Vector3 charPos) {

		createBulletCreationLight(charPos);
		if (ComponentsMapper.enemy.has(character)) {
			enemyEngagesPrimaryAttack(character, direction, charPos);
		} else {
			playerEngagesPrimaryAttack(character, direction);
		}
	}

	private void createBulletCreationLight(final Vector3 charPos) {
		Vector3 position = auxVector3_1.set(charPos.x, charPos.y + 1F, charPos.z);
		EntityBuilder.beginBuildingEntity((PooledEngine) getEngine())
				.addLightComponent(position,
						HITSCAN_COL_LIGHT_INTENSITY,
						HITSCAN_COL_LIGHT_RADIUS,
						HITSCAN_COL_LIGHT_COLOR,
						HITSCAN_COL_LIGHT_DURATION)
				.finishAndAddToEngine();
	}

	private void playerEngagesPrimaryAttack(final Entity character, final Vector3 direction) {
		Weapon selectedWeapon = ComponentsMapper.player.get(character).getStorage().getSelectedWeapon();
		if (selectedWeapon.isHitScan()) {
			playerEngagesHitScanAttack(character, direction, selectedWeapon);
		}
	}

	private void playerEngagesHitScanAttack(final Entity character, final Vector3 direction, final Weapon selectedWeapon) {
		MapGraph map = services.getMapService().getMap();
		MapGraphNode posNode = map.getNode(ComponentsMapper.characterDecal.get(character).getNodePosition(auxVector3_1));
		Vector3 posNodeCenterPos = posNode.getCenterPosition(auxVector3_4);
		affectAimByAccuracy(character, direction);
		Vector3 maxRangePos = calculateHitScanMaxPosition(direction, posNodeCenterPos);
		Array<GridPoint2> nodes = findAllNodesOnTheWayOfTheHitScan(posNodeCenterPos, maxRangePos);
		for (GridPoint2 node : nodes) {
			if (applyHitScanThroughNodes(selectedWeapon, map, posNodeCenterPos, node, maxRangePos)) {
				return;
			}
		}
	}

	private boolean applyHitScanThroughNodes(final Weapon selectedWeapon,
											 final MapGraph map,
											 final Vector3 posNodeCenterPos,
											 final GridPoint2 n,
											 final Vector3 maxRangePos) {
		MapGraphNode node = map.getNode(n.x, n.y);
		if (node.getHeight() > map.getNode((int) posNodeCenterPos.x, (int) posNodeCenterPos.z).getHeight() + 1) {
			Vector2 intersectionPosition = findNodeSegmentIntersection(posNodeCenterPos, maxRangePos, n);
			if (!intersectionPosition.equals(Vector2.Zero)) {
				Vector3 position = auxVector3_1.set(intersectionPosition.x, posNodeCenterPos.y + 1F, intersectionPosition.y);
				EntityBuilder.beginBuildingEntity((PooledEngine) getEngine())
						.addParticleEffectComponent((PooledEngine) getEngine(), bulletRicochetEffect, position)
						.addLightComponent(position,
								HITSCAN_COL_LIGHT_INTENSITY,
								HITSCAN_COL_LIGHT_RADIUS,
								HITSCAN_COL_LIGHT_COLOR,
								HITSCAN_COL_LIGHT_DURATION)
						.finishAndAddToEngine();
				return true;
			} else {
				return false;
			}
		}
		Entity enemy = map.getAliveEnemyFromNode(node);
		if (enemy != null) {
			onHitScanCollisionWithAnotherEntity((WeaponsDefinitions) selectedWeapon.getDefinition(), enemy);
			Vector3 position = auxVector3_1.set(ComponentsMapper.characterDecal.get(enemy).getDecal().getPosition());
			EntityBuilder.beginBuildingEntity((PooledEngine) getEngine())
					.addParticleEffectComponent((PooledEngine) getEngine(), bulletRicochetEffect, position)
					.addLightComponent(position,
							HITSCAN_COL_LIGHT_INTENSITY,
							HITSCAN_COL_LIGHT_RADIUS,
							HITSCAN_COL_LIGHT_COLOR,
							HITSCAN_COL_LIGHT_DURATION)
					.finishAndAddToEngine();
			return true;
		}
		return false;
	}

	private Vector2 findNodeSegmentIntersection(final Vector3 posNodeCenterPos,
												final Vector3 maxRangePos,
												final GridPoint2 node) {
		Vector2 src = auxVector2_1.set(posNodeCenterPos.x, posNodeCenterPos.z);
		Vector2 dst = auxVector2_2.set(maxRangePos.x, maxRangePos.z);
		Vector2 closest = auxVector2_5.setZero();
		float min = Integer.MAX_VALUE;
		min = intersectSegments(posNodeCenterPos, src, dst, closest, min, auxVector2_3.set(node.x, node.y), auxVector2_4.set(node.x + 1F, node.y));
		min = intersectSegments(posNodeCenterPos, src, dst, closest, min, auxVector2_3.set(auxVector2_4), auxVector2_4.set(node.x + 1F, node.y + 1F));
		min = intersectSegments(posNodeCenterPos, src, dst, closest, min, auxVector2_3.set(auxVector2_4), auxVector2_4.set(node.x, node.y + 1F));
		intersectSegments(posNodeCenterPos, src, dst, closest, min, auxVector2_3.set(auxVector2_4), auxVector2_4.set(node.x, node.y));
		return closest;
	}

	private float intersectSegments(final Vector3 posNodeCenterPos,
									final Vector2 src,
									final Vector2 dst,
									final Vector2 closest,
									final float min,
									final Vector2 lineVertex1, final Vector2 lineVertex2) {
		Vector2 candidate = BulletsSystemImpl.auxVector2_6;
		Intersector.intersectSegments(src, dst, lineVertex1, lineVertex2, candidate.set(closest));
		if (!candidate.isZero()) {
			float distance = posNodeCenterPos.dst(candidate.x, posNodeCenterPos.y, candidate.y);
			if (distance < min) {
				closest.set(candidate);
				return distance;
			}
		}
		return min;
	}

	private Array<GridPoint2> findAllNodesOnTheWayOfTheHitScan(final Vector3 posNodeCenterPos,
															   final Vector3 maxRangePos) {
		return bresenham.line(
				(int) posNodeCenterPos.x, (int) posNodeCenterPos.z,
				(int) maxRangePos.x, (int) maxRangePos.z);
	}

	private Vector3 calculateHitScanMaxPosition(final Vector3 direction, final Vector3 posNodeCenterPosition) {
		Vector3 step = auxVector3_3.setZero().add(direction.setLength(HIT_SCAN_MAX_DISTANCE));
		return auxVector3_2.set(posNodeCenterPosition).add(step);
	}

	private void affectAimByAccuracy(final Entity character, final Vector3 direction) {
		int maxAngle = ComponentsMapper.character.get(character).getSkills().getAccuracy().getMaxAngle();
		direction.rotate(Vector3.Y, MathUtils.random(-maxAngle, maxAngle));
	}

	private void enemyEngagesPrimaryAttack(final Entity character, final Vector3 direction, final Vector3 charPos) {
		EnemyComponent enemyComponent = ComponentsMapper.enemy.get(character);
		Accuracy accuracy = enemyComponent.getEnemyDefinition().getAccuracy()[enemyComponent.getSkill() - 1];
		direction.rotate(Vector3.Y, MathUtils.random(-accuracy.getMaxAngle(), accuracy.getMaxAngle()));
		direction.rotate(Vector3.X, MathUtils.random(-accuracy.getMaxAngle(), accuracy.getMaxAngle()));
		EnemyComponent enemyComp = ComponentsMapper.enemy.get(character);
		Animation<TextureAtlas.AtlasRegion> bulletAnim = enemyComp.getBulletAnimation();
		services.getSoundPlayer().playSound(Assets.Sounds.ATTACK_ENERGY_BALL);
		createEnemyBullet(character, direction, charPos, enemyComp, bulletAnim);
	}

	private void createEnemyBullet(final Entity character,
								   final Vector3 direction,
								   final Vector3 charPos,
								   final EnemyComponent enemyComp,
								   final Animation<TextureAtlas.AtlasRegion> bulletAnim) {
		charPos.y += ComponentsMapper.enemy.get(character).getEnemyDefinition().getHeight() / 2F;
		Integer[] damagePoints = enemyComp.getEnemyDefinition().getPrimaryAttack().getDamagePoints();
		ParticleEffect effect = services.getAssetManager().getParticleEffect(ParticleEffects.ENERGY_BALL_TRAIL);
		Entity bullet = EntityBuilder.beginBuildingEntity((PooledEngine) getEngine())
				.addBulletComponent(charPos, direction, character, damagePoints[enemyComp.getSkill() - 1])
				.addAnimationComponent(enemyComp.getEnemyDefinition().getPrimaryAttack().getFrameDuration(), bulletAnim)
				.addSimpleDecalComponent(charPos, bulletAnim.getKeyFrames()[0], Zero.setZero(), true, true)
				.addLightComponent(charPos, PROJECTILE_LIGHT_INTENSITY, PROJECTILE_LIGHT_RADIUS, PROJECTILE_LIGHT_COLOR)
				.finishAndAddToEngine();
		EntityBuilder.beginBuildingEntity((PooledEngine) getEngine())
				.addParticleEffectComponent((PooledEngine) getEngine(), effect, auxVector3_1.set(charPos), bullet)
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
		for (BulletsSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onProjectileCollisionWithAnotherEntity(bullet, collidable);
		}
		destroyBullet(bullet);
	}

	private void onHitScanCollisionWithAnotherEntity(final WeaponsDefinitions definition, final Entity collidable) {
		for (BulletsSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onHitScanCollisionWithAnotherEntity(definition, collidable);
		}
	}

	private void onCollisionWithWall(final Entity bullet, final MapGraphNode node) {
		for (BulletsSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onBulletCollisionWithWall(bullet, node);
		}
		destroyBullet(bullet);
	}

	private void destroyBullet(final Entity bullet) {
		bullet.remove(BulletComponent.class);
		getEngine().removeEntity(bullet);
		ParticleEffect effect = services.getAssetManager().getParticleEffect(ParticleEffects.ENERGY_BALL_EXPLOSION);
		Vector3 pos = auxVector3_1.set(ComponentsMapper.simpleDecal.get(bullet).getDecal().getPosition());
		createExplosion(effect, pos);
	}

	private void createExplosion(final ParticleEffect effect, final Vector3 pos) {
		EntityBuilder.beginBuildingEntity((PooledEngine) getEngine())
				.addParticleEffectComponent((PooledEngine) getEngine(), effect, pos).addLightComponent(pos,
						BULLET_EXPLOSION_LIGHT_INTENSITY,
						BULLET_EXPLOSION_LIGHT_RADIUS,
						PROJECTILE_LIGHT_COLOR,
						BULLET_EXPLOSION_LIGHT_DURATION)
				.finishAndAddToEngine();
		services.getSoundPlayer().playSound(Assets.Sounds.SMALL_EXP);
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
			destroyBullet(bullet);
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
	public void dispose() {

	}

	@Override
	public void onParticleEffectsSystemReady(final PointSpriteParticleBatch pointSpriteBatch) {
		bulletRicochetEffect = services.getAssetManager().getParticleEffect(ParticleEffects.BULLET_RICOCHET);
	}
}
