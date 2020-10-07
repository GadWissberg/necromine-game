package com.gadarts.isometric.systems.turns;

import com.gadarts.isometric.systems.SystemEventsSubscriber;

public interface TurnsSystemEventsSubscriber extends SystemEventsSubscriber {
	void onEnemyTurn();

	void onPlayerTurn();

	void onTurnsSystemReady(TurnsSystem turnsSystem);
}
