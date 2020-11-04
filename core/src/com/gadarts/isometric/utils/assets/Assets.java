package com.gadarts.isometric.utils.assets;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.Model;
import com.gadarts.isometric.utils.assets.definitions.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

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
		TEXTURE(TexturesTypes.getAllDefinitionsInSingleArray());

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
		PILLAR,
		CAVE_SUPPORTER_1,
		CAVE_SUPPORTER_2,
		CAVE_SUPPORTER_3;

		private final String filePath;

		Models() {
			filePath = ModelDefinition.FOLDER + PATH_SEPARATOR + name().toLowerCase() + "." + ModelDefinition.FORMAT;
		}

		@Override
		public Class<Model> getTypeClass() {
			return Model.class;
		}
	}

	@Getter
	public enum TexturesTypes {
		Floors(FloorsTextures.values()),
		UI(UiTextures.values());

		private final TextureDefinition[] definitions;

		TexturesTypes(final TextureDefinition[] definitions) {
			this.definitions = definitions;
		}

		public static TextureDefinition[] getAllDefinitionsInSingleArray() {
			ArrayList<TextureDefinition> list = new ArrayList<>();
			Arrays.stream(values()).forEach(defs -> list.addAll(Arrays
					.stream(defs.getDefinitions())
					.collect(Collectors.toList()))
			);
			return list.toArray(new TextureDefinition[0]);
		}
	}

	/**
	 * Image files of floors.
	 */
	public enum FloorsTextures implements TextureDefinition {
		FLOOR_0,
		FLOOR_1,
		FLOOR_2,
		FLOOR_3;

		@Override
		public String getSubFolderName() {
			return "floors";
		}

		@Override
		public String getName() {
			return name();
		}

	}

	/**
	 * Image files of UI components.
	 */
	@Getter
	public enum UiTextures implements TextureDefinition {
		PATH_ARROW,
		BUTTON_STORAGE(null, "buttons"),
		BUTTON_STORAGE_DOWN(null, "buttons"),
		BUTTON_STORAGE_HOVER(null, "buttons"),
		BUTTON_CLOSE(null, "buttons"),
		BUTTON_CLOSE_DOWN(null, "buttons"),
		BUTTON_CLOSE_HOVER(null, "buttons"),
		NINEPATCHES("ninepatches.9"),
		WEAPON_AXE_PICK(null, "weapons"),
		PLAYER_LAYOUT;

		public static final String SUB_FOLDER_NAME = "ui";
		private final String specialFileName;
		private final String subSubFolder;

		UiTextures() {
			this(null);
		}

		UiTextures(final String specialFileName) {
			this(specialFileName, null);
		}

		UiTextures(final String specialFileName, final String subSubFolder) {
			this.specialFileName = specialFileName;
			this.subSubFolder = subSubFolder;
		}

		@Override
		public String getSubFolderName() {
			return subSubFolder != null ? SUB_FOLDER_NAME + PATH_SEPARATOR + subSubFolder : SUB_FOLDER_NAME;
		}

		@Override
		public String getName() {
			return specialFileName != null ? specialFileName : name();
		}

	}

}
