package com.gadarts.isometric.utils.assets;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.Model;
import com.gadarts.isometric.utils.assets.definitions.*;
import lombok.Getter;

/**
 * Definitions of the game content.
 */
public final class Assets {

	@Getter
	public enum AssetsTypes {
		ATLAS(Atlases.values()),
		MELODY(Melody.values()),
		SOUND(Sounds.values()),
		MODEL(Models.values()),
		TEXTURE(Textures.values());

		private final AssetDefinition[] assetDefinitions;

		AssetsTypes(final AssetDefinition[] assetDefinitions) {
			this.assetDefinitions = assetDefinitions;
		}

	}

	public static final String PATH_SEPARATOR = "/";

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

		@Override
		public Class<TextureAtlas> getTypeClass() {
			return TextureAtlas.class;
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

		@Override
		public Class<Music> getTypeClass() {
			return Music.class;
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

		@Override
		public Class<Sound> getTypeClass() {
			return Sound.class;
		}
	}

	/**
	 * 3D models.
	 */
	@Getter
	public enum Models implements ModelDefinition {
		WALL_1,
		WALL_2,
		COLT,
		PILLAR;

		private final String filePath;

		Models() {
			filePath = ModelDefinition.FOLDER + PATH_SEPARATOR + name().toLowerCase() + "." + ModelDefinition.FORMAT;
		}

		@Override
		public Class<Model> getTypeClass() {
			return Model.class;
		}
	}

	/**
	 * Images - mainly floors.
	 */
	public enum Textures implements TextureDefinition {
		FLOOR_0,
		FLOOR_1,
		FLOOR_2,
		FLOOR_3,
		PATH_ARROW;

		@Override
		public String getName() {
			return name();
		}

		@Override
		public Class<Texture> getTypeClass() {
			return Texture.class;
		}
	}

}
