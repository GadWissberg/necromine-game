package com.gadarts.isometric.components.player;

import com.gadarts.isometric.utils.assets.Assets;

public interface ItemDefinition {
	int getWidth();

	int[] getMask();

	int getHeight();

	int getId();

	Assets.UiTextures getImage();

	String getDisplayName();
}
