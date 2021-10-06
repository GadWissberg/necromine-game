package com.gadarts.isometric.systems.render;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.gadarts.isometric.systems.SystemEventsSubscriber;

public interface RenderSystemEventsSubscriber extends SystemEventsSubscriber {
	default void onFrameChanged(final Entity entity, final float deltaTime, final TextureAtlas.AtlasRegion newFrame) {

	}

	default void onRenderSystemReady(final RenderSystem renderSystem) {

	}

	default void onFullScreenToggle(final boolean fullScreen) {

	}

	default void onBeginRenderingModels(ModelBatch modelBatch) {

	}
}
