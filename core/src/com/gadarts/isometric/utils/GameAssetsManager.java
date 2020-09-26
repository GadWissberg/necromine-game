package com.gadarts.isometric.utils;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.gadarts.isometric.utils.Assets.Textures.FloorTextures;

import java.util.Arrays;

public class GameAssetsManager extends AssetManager {
	private static final String ATLAS_FOLDER = "atlases";

	public void loadGameFiles() {
		Arrays.stream(FloorTextures.values()).forEach(floor -> load(
				Gdx.files.getFileHandle(floor.getFilePath(), Files.FileType.Internal).path(),
				Texture.class)
		);
		String path = ATLAS_FOLDER + "/" + Assets.Atlases.PLAYER.name().toLowerCase() + ".txt";
		load(Gdx.files.getFileHandle(path, Files.FileType.Internal).path(), TextureAtlas.class);
		finishLoading();
	}

	public TextureAtlas getAtlas(final Assets.Atlases atlas) {
		return get(ATLAS_FOLDER + "/" + atlas.name().toLowerCase() + ".txt", TextureAtlas.class);
	}

	public Texture getTexture(final TextureDefinition definition) {
		return get(definition.getFilePath());
	}
}
