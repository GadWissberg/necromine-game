package com.gadarts.isometric.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.PickUpComponent;
import com.gadarts.isometric.systems.pickup.PickUpSystem;
import com.gadarts.isometric.systems.pickup.PickupSystemEventsSubscriber;
import com.gadarts.isometric.utils.map.MapGraph;

public class PickUpSystemImpl extends GameEntitySystem<PickupSystemEventsSubscriber> implements PickUpSystem {
    private static final float PICK_UP_ROTATION = 10;
    private ImmutableArray<Entity> pickupsEntities;

    public PickUpSystemImpl(final MapGraph map) {
        super(map);
    }

    @Override
    public void init() {
        subscribers.forEach(sub -> sub.onPickUpSystemReady(PickUpSystemImpl.this));
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

    @Override
    public void onItemPickedUp(Entity pickup) {
        getEngine().removeEntity(pickup);
    }
}
