package com.gadarts.isometric.systems.render;

import com.badlogic.ashley.core.Entity;
import com.gadarts.isometric.systems.SystemEventsSubscriber;

public interface RenderSystemEventsSubscriber extends SystemEventsSubscriber {
	void onRunFrameChanged(Entity entity, float deltaTime);

	void onRenderSystemReady(RenderSystem renderSystem);
}
