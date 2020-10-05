package com.gadarts.isometric.systems.enemy;

import com.badlogic.ashley.core.Entity;

public interface EnemySystemEventsSubscriber {
	void onEnemyFinishedTurn(Entity character);
}
