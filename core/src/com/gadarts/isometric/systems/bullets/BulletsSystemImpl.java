package com.gadarts.isometric.systems.bullets;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.isometric.components.BulletComponent;
import com.gadarts.isometric.components.CollisionComponent;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.systems.GameEntitySystem;

public class BulletsSystemImpl extends GameEntitySystem<BulletsSystemEventsSubscriber> implements BulletSystem {

	private final static Vector2 auxVector2_1 = new Vector2();
	private final static Vector3 auxVector3 = new Vector3();
	private static final float BULLET_SPEED = 0.8f;
	private static final float BULLET_MAX_DISTANCE = 10;
	private static final float CHAR_RAD = 0.3f;
	private static final float OBST_RAD = 0.5f;
	private ImmutableArray<Entity> bullets;
	private ImmutableArray<Entity> collidables;

	@Override
	public void activate() {

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
		for (Entity collidable : collidables) {
			if (ComponentsMapper.bullet.get(bullet).getOwner() != collidable) {
				if (checkCollision(decal, collidable)) {
					onCollision(bullet, collidable);
					break;
				}
			}
		}
	}

	private void onCollision(final Entity bullet, final Entity collidable) {
		getEngine().removeEntity(bullet);
		for (BulletsSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onBulletCollision(bullet, collidable);
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
		ModelInstance modelInstance = ComponentsMapper.modelInstance.get(collidable).getModelInstance();
		Vector3 collPos = modelInstance.transform.getTranslation(auxVector3);
		Vector3 position = decal.getPosition();
		return auxVector2_1.set(collPos.x + 0.5f, collPos.z + 0.5f).dst(position.x, position.z) < OBST_RAD;
	}

	private boolean checkCollisionWithCharacter(final Decal decal, final Entity collidable) {
		Vector3 colPos = ComponentsMapper.characterDecal.get(collidable).getDecal().getPosition();
		return ComponentsMapper.character.get(collidable).getHealthData().getHp() > 0
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
		Vector2 velocity = bulletComponent.getDirection().nor().scl(BULLET_SPEED);
		decal.translate(velocity.x, 0, velocity.y);
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
}
