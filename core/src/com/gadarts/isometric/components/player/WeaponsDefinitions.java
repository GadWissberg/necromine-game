package com.gadarts.isometric.components.player;

import com.gadarts.isometric.utils.assets.Assets;

public enum WeaponsDefinitions implements ItemDefinition {
	AXE_PICK(2, 2, Assets.UiTextures.WEAPON_AXE_PICK);

	private final int width;
	private final int height;
	private final Assets.UiTextures image;

	WeaponsDefinitions(final int width, final int height, final Assets.UiTextures image) {
		this.width = width;
		this.height = height;
		this.image = image;
	}

	@Override
	public int getWidth() {
		return width;
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
