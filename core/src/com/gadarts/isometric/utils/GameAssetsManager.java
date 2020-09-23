package com.gadarts.isometric.utils;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class GameAssetsManager extends AssetManager {
	private static final String ATLAS_FOLDER = "atlases";
	private static final String TEXTURES_FOLDER = "textures";

	public void loadGameFiles() {
		load(Gdx.files.getFileHandle(TEXTURES_FOLDER + "/" + "floor.png", Files.FileType.Internal).path(), Texture.class);
		load(Gdx.files.getFileHandle(ATLAS_FOLDER + "/" + Assets.Atlases.PLAYER.name().toLowerCase() + ".txt", Files.FileType.Internal).path(), TextureAtlas.class);
		finishLoading();
	}

	public TextureAtlas getAtlas(final Assets.Atlases atlas) {
		return get(ATLAS_FOLDER + "/" + atlas.name().toLowerCase() + ".txt", TextureAtlas.class);
	}
}
