package com.gadarts.isometric.systems.hud;

import com.gadarts.isometric.systems.SystemEventsSubscriber;

public interface HudSystemEventsSubscriber extends SystemEventsSubscriber {
	void onHudSystemReady(HudSystem hudSystem);

	void onPathCreated(boolean pathToEnemy);
}
