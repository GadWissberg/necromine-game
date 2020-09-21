package com.gadarts.isometric.utils;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;

import java.io.File;

public class GameAssetsManager extends AssetManager {
	public void loadGameFiles() {
		load(Gdx.files.getFileHandle("textures" + File.separator + "floor.png", Files.FileType.Internal).path(), Texture.class);
		finishLoading();
	}
}
