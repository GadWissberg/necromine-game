package com.gadarts.isometric.systems.player;

import com.badlogic.ashley.core.Entity;
import com.gadarts.isometric.utils.map.MapGraphNode;

import java.util.List;

public interface PlayerSystem {

	Entity getPlayer();

	void activateAttackMode(Entity enemyAtNode, List<MapGraphNode> availableNodes);

	void applyGoToCommand(MapGraphNode selectedNode);

	void deactivateAttackMode();

	void applyGoToMeleeCommand(MapGraphNode selectedNode);
}
