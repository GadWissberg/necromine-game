package com.gadarts.isometric.utils.assets;

import com.gadarts.isometric.utils.AtlasDefinition;
import com.gadarts.isometric.utils.ModelDefinition;
import com.gadarts.isometric.utils.TextureDefinition;
import lombok.Getter;

public final class Assets {

	public static final String PATH_SEPARATOR = "/";

	@Getter
	public enum Atlases implements AtlasDefinition {
		PLAYER, ZEALOT;
		private final String filePath;

		Atlases() {
			this.filePath = AtlasDefinition.FOLDER + PATH_SEPARATOR + name().toLowerCase() + "." + AtlasDefinition.FORMAT;
		}
	}

	@Getter
	public enum Models implements ModelDefinition {
		WALL_1,
		PILLAR;

		private final String filePath;

		Models() {
			filePath = ModelDefinition.FOLDER + PATH_SEPARATOR + name().toLowerCase() + "." + ModelDefinition.FORMAT;
		}
	}

	public static final class Textures {
		@Getter
		public enum FloorTextures implements TextureDefinition {
			FLOOR_0,
			FLOOR_1,
			FLOOR_2,
			FLOOR_3;

			private final String filePath;

			FloorTextures() {
				filePath = TEXTURES_FOLDER + PATH_SEPARATOR + name().toLowerCase() + "." + TEXTURE_FORMAT;
			}

		}
	}
}
