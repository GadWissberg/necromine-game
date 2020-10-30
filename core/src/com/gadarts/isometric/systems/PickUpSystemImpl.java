package com.gadarts.isometric.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.ModelInstanceComponent;
import com.gadarts.isometric.components.PickUpComponent;
import com.gadarts.isometric.systems.pickup.PickUpSystem;
import com.gadarts.isometric.systems.pickup.PickupSystemEventsSubscriber;

public class PickUpSystemImpl extends GameEntitySystem<PickupSystemEventsSubscriber> implements PickUpSystem {
	private static final float PICK_UP_ROTATION = 10;
	private ImmutableArray<Entity> pickupsEntities;
	private float[] hsvArray = new float[3];


	@Override
	public void addedToEngine(final Engine engine) {
		super.addedToEngine(engine);
		pickupsEntities = engine.getEntitiesFor(Family.all(PickUpComponent.class).get());
	}

	@Override
	public void update(final float deltaTime) {
		super.update(deltaTime);
		for (Entity pickup : pickupsEntities) {
			rotatePickup(deltaTime, pickup);
			PickUpComponent pickUpComponent = ComponentsMapper.pickup.get(pickup);
			flickerPickup(pickup, pickUpComponent);
		}
	}

	private void flickerPickup(final Entity pickup, final PickUpComponent pickUpComponent) {
		float flickerValue = pickUpComponent.getFlicker();
		Color color = ComponentsMapper.modelInstance.get(pickup).getColorAttribute().color;
		color.toHsv(hsvArray);
		float value = hsvArray[2];
		if (flickerValue > 0) {
			fadeOut(pickup, value < 1, Math.min(value + flickerValue, 1), value >= 1);
		} else {
			fadeOut(pickup, value > 0, Math.max(value + flickerValue, 0), value <= 0);
		}
	}

	private void fadeOut(final Entity pickup,
						 final boolean insideRange,
						 final float newValue,
						 final boolean reachedBound) {
		Color color = ComponentsMapper.modelInstance.get(pickup).getColorAttribute().color;
		if (insideRange) {
			hsvArray[2] = newValue;
			color.fromHsv(hsvArray);
		} else if (reachedBound) {
			PickUpComponent pickUpComponent = ComponentsMapper.pickup.get(pickup);
			pickUpComponent.setFlicker(-pickUpComponent.getFlicker());
		}
	}

	private void rotatePickup(final float deltaTime, final Entity pickup) {
		ModelInstanceComponent modelInstanceComponent = ComponentsMapper.modelInstance.get(pickup);
		ModelInstance modelInstance = modelInstanceComponent.getModelInstance();
		modelInstance.transform.rotate(Vector3.Y, deltaTime * PICK_UP_ROTATION);
	}

	@Override
	public void dispose() {

	}

	@Override
	public void onItemPickedUp(final Entity pickup) {
		PooledEngine engine = (PooledEngine) getEngine();
		engine.removeEntity(pickup);
	}

	@Override
	public void activate() {
		subscribers.forEach(sub -> sub.onPickUpSystemReady(PickUpSystemImpl.this));
	}
}
