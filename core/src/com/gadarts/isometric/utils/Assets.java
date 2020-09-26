package com.gadarts.isometric.utils;

public final class Assets {

	public static final String PATH_SEPERATOR = "/";

	public enum Atlases {PLAYER}

	public static final class Textures {
		public enum FloorTextures implements TextureDefinition {
			FLOOR_0,
			FLOOR_1,
			FLOOR_2;

			private final String filePath;

			FloorTextures() {
				filePath = TEXTURES_FOLDER + PATH_SEPERATOR + name().toLowerCase() + "." + TEXTURE_FORMAT;
			}

			@Override
			public String getFilePath() {
				return filePath;
			}
		}
	}
}
