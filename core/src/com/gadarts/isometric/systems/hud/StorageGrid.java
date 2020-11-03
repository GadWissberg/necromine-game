package com.gadarts.isometric.systems.hud;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.gadarts.isometric.components.player.Item;
import com.gadarts.isometric.utils.assets.GameAssetsManager;

import java.util.List;

public class StorageGrid extends Image {
	private final List<Item> playerStorage;
	private final GameAssetsManager assetsManager;

	public StorageGrid(final Texture gridTexture, final List<Item> playerStorage, final GameAssetsManager assetsManager) {
		super(gridTexture);
		this.playerStorage = playerStorage;
		this.assetsManager = assetsManager;
	}

	@Override
	public void draw(final Batch batch, final float parentAlpha) {
		super.draw(batch, parentAlpha);
		for (Item item : playerStorage) {
			assetsManager.getTexture(item.getDefinition().getImage());
		}
	}
}
