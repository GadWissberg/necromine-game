package com.gadarts.isometric.systems.bullets;

import com.badlogic.ashley.core.Entity;
import com.gadarts.isometric.systems.SystemEventsSubscriber;
import com.gadarts.isometric.utils.map.MapGraphNode;

public interface BulletsSystemEventsSubscriber extends SystemEventsSubscriber {
	default void onBulletCollisionWithAnotherEntity(final Entity bullet, final Entity collidable) {

	}

	default void onBulletCollisionWithWall(final Entity bullet, final MapGraphNode node) {

	}
}
