package com.gadarts.isometric.components.player;

import com.gadarts.isometric.utils.assets.Assets;

public enum WeaponsDefinitions implements ItemDefinition {
	AXE_PICK(4, 5, Assets.UiTextures.WEAPON_AXE_PICK, new int[]{
			1, 1, 1, 1,
			0, 1, 0, 0,
			0, 1, 0, 0,
			0, 1, 0, 0,
			0, 1, 0, 0,
	}),
	COLT(2, 2, Assets.UiTextures.WEAPON_COLT, new int[]{
			1, 1,
			1, 0
	});

	private final int width;
	private final int height;
	private final Assets.UiTextures image;
	private final int[] mask;

	WeaponsDefinitions(final int width, final int height, final Assets.UiTextures image, final int[] mask) {
		this.width = width;
		this.height = height;
		this.image = image;
		this.mask = mask;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int[] getMask() {
		return mask;
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
