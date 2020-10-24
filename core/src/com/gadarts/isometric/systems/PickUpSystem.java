package com.gadarts.isometric.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.PickUpComponent;
import com.gadarts.isometric.utils.map.MapGraph;

public class PickUpSystem extends GameEntitySystem<SystemEventsSubscriber> {
	private static final float PICK_UP_ROTATION = 10;
	private ImmutableArray<Entity> pickupsEntities;

	public PickUpSystem(final MapGraph map) {
		super(map);
	}

	@Override
	public void init() {

	}

	@Override
	public void addedToEngine(final Engine engine) {
		super.addedToEngine(engine);
		pickupsEntities = engine.getEntitiesFor(Family.all(PickUpComponent.class).get());
	}

	@Override
	public void update(final float deltaTime) {
		super.update(deltaTime);
		for (Entity pickup : pickupsEntities) {
			ComponentsMapper.modelInstance
					.get(pickup)
					.getModelInstance()
					.transform.rotate(Vector3.Y, deltaTime * PICK_UP_ROTATION);
		}
	}

	@Override
	public void dispose() {

	}
}
