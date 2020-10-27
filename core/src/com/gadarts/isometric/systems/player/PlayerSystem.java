package com.gadarts.isometric.systems.player;

import com.badlogic.ashley.core.Entity;
import com.gadarts.isometric.systems.turns.GameSystem;
import com.gadarts.isometric.utils.map.MapGraphNode;
import com.gadarts.isometric.utils.map.MapGraphPath;

import java.util.List;

public interface PlayerSystem extends GameSystem {

    Entity getPlayer();

    void activateAttackMode(Entity enemyAtNode, List<MapGraphNode> availableNodes);

    void applyGoToCommand(MapGraphPath path);

    void deactivateAttackMode();

    void applyGoToMeleeCommand(MapGraphPath path);

    void applyGoToPickupCommand(MapGraphPath path, Entity itemToPickup);
}
