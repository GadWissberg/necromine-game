package com.gadarts.isometric.systems.player;

import com.gadarts.isometric.systems.SystemEventsSubscriber;
import com.gadarts.isometric.utils.map.MapGraphNode;

import java.util.List;

public interface PlayerSystemEventsSubscriber extends SystemEventsSubscriber {
	default void onPlayerFinishedTurn() {

	}

	default void onPathCreated(final boolean pathToEnemy) {

	}

	default void onEnemySelectedWithRangeWeapon(final MapGraphNode node) {

	}

	default void onPlayerSystemReady(final PlayerSystem playerSystem) {

	}

	default void onAttackModeActivated(final List<MapGraphNode> availableNodes) {

	}

	default void onAttackModeDeactivated() {

	}

}
