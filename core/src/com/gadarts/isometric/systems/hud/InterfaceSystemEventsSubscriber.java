package com.gadarts.isometric.systems.hud;

import com.gadarts.isometric.systems.SystemEventsSubscriber;
import com.gadarts.isometric.utils.map.MapGraphNode;

public interface InterfaceSystemEventsSubscriber extends SystemEventsSubscriber {
	default void onHudSystemReady(final InterfaceSystem interfaceSystem) {

	}


	default void onUserSelectedNodeToApplyTurn(final MapGraphNode cursorNode, final AttackNodesHandler attackNodesHandler) {

	}
}
