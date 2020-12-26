package com.gadarts.isometric.systems.hud;

import com.gadarts.isometric.systems.SystemEventsSubscriber;
import com.gadarts.isometric.utils.map.MapGraphNode;

public interface HudSystemEventsSubscriber extends SystemEventsSubscriber {
	void onHudSystemReady(HudSystem hudSystem);


	void onUserSelectedNodeToApplyTurn(MapGraphNode cursorNode, AttackNodesHandler attackNodesHandler);
}
