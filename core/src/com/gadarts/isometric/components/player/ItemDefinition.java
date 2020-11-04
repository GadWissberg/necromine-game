package com.gadarts.isometric.components.player;

import com.gadarts.isometric.utils.assets.Assets;

public interface ItemDefinition {
	int getWidth();

	int getHeight();

	Assets.UiTextures getImage();
}
