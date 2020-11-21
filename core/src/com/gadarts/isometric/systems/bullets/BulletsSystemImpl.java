package com.gadarts.isometric.systems.bullets;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.gadarts.isometric.components.BulletComponent;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.systems.GameEntitySystem;

public class BulletsSystemImpl extends GameEntitySystem<BulletsSystemEventsSubscriber> implements BulletSystem {

	private ImmutableArray<Entity> bullets;

	@Override
	public void activate() {

	}

	@Override
	public void update(final float deltaTime) {
		super.update(deltaTime);
		for (Entity bullet : bullets) {
			ComponentsMapper.simpleDecal.get(bullet).getDecal().translate(1, 0, 0);
		}
	}

	@Override
	public void addedToEngine(final Engine engine) {
		super.addedToEngine(engine);
		bullets = engine.getEntitiesFor(Family.all(BulletComponent.class).get());
	}

	@Override
	public void dispose() {

	}
}
