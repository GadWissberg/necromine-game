package com.gadarts.isometric.systems;

import com.badlogic.ashley.core.Entity;

public interface EnemySystemEventsSubscriber {
	void onEnemyFinishedTurn(Entity character);
}
