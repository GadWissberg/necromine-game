package com.gadarts.isometric.systems.enemy;

import com.gadarts.isometric.systems.SystemEventsSubscriber;

public interface EnemySystemEventsSubscriber extends SystemEventsSubscriber {
	void onEnemyFinishedTurn();
}
