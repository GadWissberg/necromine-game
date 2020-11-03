package com.gadarts.isometric.components.player;

import com.gadarts.isometric.utils.assets.Assets;
import lombok.Getter;

@Getter
public enum ItemsDefinitions {
	AXE_PICK(2, 2, Assets.UiTextures.WEAPON_AXE_PICK);

	private final int width;
	private final int height;
	private final Assets.UiTextures image;

	ItemsDefinitions(final int width, final int height, final Assets.UiTextures image) {
		this.width = width;
		this.height = height;
		this.image = image;
	}
}
