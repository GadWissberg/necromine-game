package com.gadarts.isometric.systems.turns;

import com.gadarts.isometric.systems.SystemEventsSubscriber;

public interface TurnsSystemEventsSubscriber extends SystemEventsSubscriber {
	default void onEnemyTurn(final long currentTurnId) {

	}

	default void onPlayerTurn(final long currentTurnId) {

	}

	default void onTurnsSystemReady(final TurnsSystem turnsSystem) {

	}
}
