package com.gadarts.isometric.systems.bullets;

import com.badlogic.ashley.core.Entity;
import com.gadarts.isometric.systems.SystemEventsSubscriber;

public interface BulletsSystemEventsSubscriber extends SystemEventsSubscriber {
	void onBulletCollision(Entity bullet, Entity collidable);
}
