package com.gadarts.isometric.systems.turns;

import com.gadarts.isometric.systems.SystemEventsSubscriber;

public interface TurnsSystemEventsSubscriber extends SystemEventsSubscriber {
	void onEnemyTurn(long currentTurnId);

	void onPlayerTurn(long currentTurnId);

	void onTurnsSystemReady(TurnsSystem turnsSystem);
}
