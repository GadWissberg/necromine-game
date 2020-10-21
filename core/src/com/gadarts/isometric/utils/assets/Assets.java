package com.gadarts.isometric.utils.assets;

import com.gadarts.isometric.utils.assets.definitions.*;
import lombok.Getter;

/**
 * Definitions of the game content.
 */
public final class Assets {

	private static final String PATH_SEPARATOR = "/";

	private Assets() {
	}

	/**
	 * Texture atlases.
	 */
	@Getter
	public enum Atlases implements AtlasDefinition {
		PLAYER, ZEALOT;
		private final String filePath;

		Atlases() {
			this.filePath = AtlasDefinition.FOLDER + PATH_SEPARATOR + name().toLowerCase() + "." + AtlasDefinition.FORMAT;
		}
	}

	/**
	 * Ogg files.
	 */
	@Getter
	public enum Melody implements MelodyDefinition {
		TEST;
		private final String filePath;

		Melody() {
			this.filePath = MelodyDefinition.FOLDER + PATH_SEPARATOR + name().toLowerCase() + "." + MelodyDefinition.FORMAT;
		}
	}

	/**
	 * Wave files.
	 */
	@Getter
	public enum Sounds implements SoundDefinition {
		STEP_1,
		STEP_2,
		STEP_3,
		ENEMY_ROAM,
		ATTACK_CLAW,
		ENEMY_DIE;

		private final String filePath;

		Sounds() {
			this.filePath = SoundDefinition.FOLDER + PATH_SEPARATOR + name().toLowerCase() + "." + SoundDefinition.FORMAT;
		}
	}

	/**
	 * 3D models.
	 */
	@Getter
	public enum Models implements ModelDefinition {
		WALL_1,
		COLT,
		PILLAR;

		private final String filePath;

		Models() {
			filePath = ModelDefinition.FOLDER + PATH_SEPARATOR + name().toLowerCase() + "." + ModelDefinition.FORMAT;
		}
	}

	/**
	 * Images - mainly floors.
	 */
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
