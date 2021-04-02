package com.gadarts.isometric.systems.player;

import com.badlogic.ashley.core.Entity;
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

	default void onPlayerSystemReady(final PlayerSystem playerSystem, Entity player) {

	}

	default void onAttackModeActivated(final List<MapGraphNode> availableNodes) {

	}

	default void onAttackModeDeactivated() {

	}

	default void onPlayerStatusChanged(boolean disabled) {

	}
}
