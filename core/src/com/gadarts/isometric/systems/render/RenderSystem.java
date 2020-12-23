package com.gadarts.isometric.systems.render;

import com.gadarts.isometric.systems.GameSystem;

public interface RenderSystem extends GameSystem {
	int getNumberOfVisible();

	int getNumberOfModelInstances();
}
