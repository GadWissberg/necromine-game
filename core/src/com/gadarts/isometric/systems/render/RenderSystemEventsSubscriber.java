package com.gadarts.isometric.systems.render;

import com.badlogic.ashley.core.Entity;

public interface RenderSystemEventsSubscriber {
	void onRunFrameChanged(Entity entity, float deltaTime);
}
