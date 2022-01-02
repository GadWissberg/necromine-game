package com.gadarts.isometric.systems.hud;

import com.gadarts.isometric.systems.SystemEventsSubscriber;
import com.gadarts.isometric.utils.map.MapGraphNode;

public interface UserInterfaceSystemEventsSubscriber extends SystemEventsSubscriber {
	default void onHudSystemReady(final UserInterfaceSystem userInterfaceSystem) {

	}


	default void onUserSelectedNodeToApplyTurn(final MapGraphNode cursorNode, final AttackNodesHandler attackNodesHandler) {

	}

	default void onMenuToggled(boolean active) {

	}
}
