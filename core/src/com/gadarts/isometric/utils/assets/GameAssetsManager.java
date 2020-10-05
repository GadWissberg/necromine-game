package com.gadarts.isometric.utils.assets;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.gadarts.isometric.utils.TextureDefinition;
import com.gadarts.isometric.utils.assets.Assets.Atlases;
import com.gadarts.isometric.utils.assets.Assets.Textures.FloorTextures;

import java.util.Arrays;

public class GameAssetsManager extends AssetManager {
	private static final String ATLAS_FOLDER = "atlases";

	public void loadGameFiles() {
		Arrays.stream(FloorTextures.values()).forEach(floor -> load(
				Gdx.files.getFileHandle(floor.getFilePath(), FileType.Internal).path(),
				Texture.class)
		);
		Arrays.stream(Atlases.values()).forEach(atlas -> load(
				Gdx.files.getFileHandle(
						ATLAS_FOLDER + "/" + atlas.name().toLowerCase() + ".txt",
						FileType.Internal
				).path(), TextureAtlas.class)
		);
		finishLoading();
	}

	public TextureAtlas getAtlas(final Atlases atlas) {
		return get(ATLAS_FOLDER + "/" + atlas.name().toLowerCase() + ".txt", TextureAtlas.class);
	}

	public Texture getTexture(final TextureDefinition definition) {
		return get(definition.getFilePath());
	}
}
