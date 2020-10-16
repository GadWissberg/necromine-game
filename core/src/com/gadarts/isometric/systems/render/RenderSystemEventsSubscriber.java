package com.gadarts.isometric.systems.render;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.gadarts.isometric.systems.SystemEventsSubscriber;

public interface RenderSystemEventsSubscriber extends SystemEventsSubscriber {
	void onFrameChanged(Entity entity, float deltaTime, TextureAtlas.AtlasRegion newFrame);

	void onRenderSystemReady(RenderSystem renderSystem);
}
