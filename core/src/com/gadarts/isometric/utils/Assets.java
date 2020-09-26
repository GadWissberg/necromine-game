package com.gadarts.isometric.utils;

import lombok.Getter;

public final class Assets {

	public static final String PATH_SEPERATOR = "/";

	@Getter
	public enum CharacterDirRegions {
		SOUTH_IDLE("south_idle"),
		SOUTH_WEST_IDLE("south_west_idle"),
		WEST_IDLE("west_idle"),
		NORTH_WEST_IDLE("north_west_idle"),
		NORTH_IDLE("north_idle"),
		NORTH_EAST_IDLE("north_east_idle"),
		EAST_IDLE("east_idle"),
		SOUTH_EAST_IDLE("south_east_idle");

		private final String regionName;

		CharacterDirRegions(final String regionName) {
			this.regionName = regionName;
		}
	}

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
