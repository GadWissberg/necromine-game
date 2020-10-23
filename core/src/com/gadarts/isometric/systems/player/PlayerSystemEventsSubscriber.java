package com.gadarts.isometric.systems.player;

import com.gadarts.isometric.systems.SystemEventsSubscriber;
import com.gadarts.isometric.utils.map.MapGraphNode;

import java.util.List;

public interface PlayerSystemEventsSubscriber extends SystemEventsSubscriber {
	void onPlayerFinishedTurn();

	void onPlayerSystemReady(PlayerSystem playerSystem);

	void onAttackModeActivated(List<MapGraphNode> availableNodes);

	void onAttackModeDeactivated();
}
