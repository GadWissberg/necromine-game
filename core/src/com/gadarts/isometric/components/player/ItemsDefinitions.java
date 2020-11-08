package com.gadarts.isometric.components.player;

import com.gadarts.isometric.utils.assets.Assets;

public enum ItemsDefinitions implements ItemDefinition {
	;


	private final int width;
	private final int height;
	private final Assets.UiTextures image;

	ItemsDefinitions(final int width, final int height, final Assets.UiTextures image) {
		this.width = width;
		this.height = height;
		this.image = image;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int[] getMask() {
		return new int[0];
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public Assets.UiTextures getImage() {
		return image;
	}


}
