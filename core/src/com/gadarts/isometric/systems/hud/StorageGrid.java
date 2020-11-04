package com.gadarts.isometric.systems.hud;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.gadarts.isometric.components.player.Item;

import java.util.List;

public class StorageGrid extends Image {
	private final List<Item> playerStorage;

	public StorageGrid(final Texture gridTexture, final List<Item> playerStorage) {
		super(gridTexture);
		this.playerStorage = playerStorage;
	}

	@Override
	public void draw(final Batch batch, final float parentAlpha) {
		super.draw(batch, parentAlpha);
		for (Item item : playerStorage) {
			drawItem(batch, item);
		}
	}

	private void drawItem(final Batch batch, final Item item) {
		batch.draw(
				item.getImage(),
				getX() + item.getX() * GameStage.GRID_CELL_SIZE,
				getY() + item.getY() * GameStage.GRID_CELL_SIZE
		);
	}
}
