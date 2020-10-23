package com.gadarts.isometric.utils.assets.definitions;

import com.gadarts.isometric.utils.assets.Assets;

public interface TextureDefinition {
	String TEXTURES_FOLDER = "textures";
	String TEXTURE_FORMAT = "png";

	default String getFilePath() {
		return TEXTURES_FOLDER + Assets.PATH_SEPARATOR + getName().toLowerCase() + "." + TEXTURE_FORMAT;
	}

	String getName();
}
