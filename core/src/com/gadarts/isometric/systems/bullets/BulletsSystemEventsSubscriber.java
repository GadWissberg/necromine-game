package com.gadarts.isometric.systems.bullets;

import com.badlogic.ashley.core.Entity;
import com.gadarts.isometric.systems.SystemEventsSubscriber;

public interface BulletsSystemEventsSubscriber extends SystemEventsSubscriber {
	default void onBulletCollision(final Entity bullet, final Entity collidable) {

	}
}
