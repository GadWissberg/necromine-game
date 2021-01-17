package com.gadarts.isometric.components.player;

import com.gadarts.necromine.Assets;
import com.gadarts.necromine.ItemDefinition;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ItemsDefinitions implements ItemDefinition {
	;


	private final int width;
	private final int height;
	private final Assets.UiTextures image;
	private final String displayName;

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
	public int getId() {
		return 0;
	}

	@Override
	public Assets.UiTextures getImage() {
		return image;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}


}
