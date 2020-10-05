package com.gadarts.isometric.systems.player;

import com.gadarts.isometric.utils.map.MapGraphNode;

import java.util.List;

public interface PlayerSystemEventsSubscriber {
	void onPlayerFinishedTurn();

	void onAttackModeActivated(List<MapGraphNode> availableNodes);

	void onAttackModeDeactivated();
}
