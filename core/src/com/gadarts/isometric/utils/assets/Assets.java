package com.gadarts.isometric.utils.assets;

import com.gadarts.isometric.utils.TextureDefinition;

public final class Assets {

	public static final String PATH_SEPARATOR = "/";

	public enum Atlases {PLAYER, ZEALOT}

	public static final class Textures {
		public enum FloorTextures implements TextureDefinition {
			FLOOR_0,
			FLOOR_1,
			FLOOR_2;

			private final String filePath;

			FloorTextures() {
				filePath = TEXTURES_FOLDER + PATH_SEPARATOR + name().toLowerCase() + "." + TEXTURE_FORMAT;
			}

			@Override
			public String getFilePath() {
				return filePath;
			}
		}
	}
}
