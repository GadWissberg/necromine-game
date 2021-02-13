package com.gadarts.isometric.systems.enemy;

import com.badlogic.ashley.core.Entity;
import com.gadarts.isometric.systems.SystemEventsSubscriber;

public interface EnemySystemEventsSubscriber extends SystemEventsSubscriber {
	default void onEnemyFinishedTurn() {

	}

	default void onEnemyAwaken(final Entity enemy) {

	}
}
