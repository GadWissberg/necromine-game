package com.gadarts.isometric.systems.hud;

import com.gadarts.isometric.systems.SystemEventsSubscriber;
import com.gadarts.isometric.utils.map.MapGraphNode;

public interface HudSystemEventsSubscriber extends SystemEventsSubscriber {
	default void onHudSystemReady(final HudSystem hudSystem) {

	}


	default void onUserSelectedNodeToApplyTurn(final MapGraphNode cursorNode, final AttackNodesHandler attackNodesHandler) {

	}
}
