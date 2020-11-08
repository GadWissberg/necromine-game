package com.gadarts.isometric.systems.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.gadarts.isometric.components.player.ItemDefinition;
import com.gadarts.isometric.components.player.Weapon;
import com.gadarts.isometric.utils.Utils;

import java.util.List;

import static com.gadarts.isometric.systems.hud.GameStage.*;

public class StorageGrid extends Table {
	private final static Rectangle auxRectangle_1 = new Rectangle();
	private final static Rectangle auxRectangle_2 = new Rectangle();
	private final static Rectangle auxRectangle_3 = new Rectangle();
	private final List<Weapon> playerStorage;
	private final Texture gridCellTexture;
	private final StorageWindow storageWindow;

	public StorageGrid(final Texture gridTexture,
					   final List<Weapon> playerStorage,
					   final Texture gridCellTexture,
					   final StorageWindow window) {
		super();
		setBackground(new TextureRegionDrawable(gridTexture));
		this.playerStorage = playerStorage;
		this.gridCellTexture = gridCellTexture;
		this.storageWindow = window;
	}

	@Override
	public void draw(final Batch batch, final float parentAlpha) {
		super.draw(batch, parentAlpha);
		drawCells(batch);
		for (Weapon item : playerStorage) {
			drawItem(batch, item);
		}
	}

	private void drawCells(final Batch batch) {
		int cellsInRow = GRID_SIZE / GRID_CELL_SIZE;
		int numberOfCells = cellsInRow * cellsInRow;
		for (int i = 0; i < numberOfCells; i++) {
			int rowIndex = i / cellsInRow;
			int width = gridCellTexture.getWidth();
			int height = gridCellTexture.getHeight();
			int paddingBothSides = CELL_PADDING * 2;
			float cellX = getX() + CELL_PADDING + (i % cellsInRow) * (width + paddingBothSides);
			float cellY = getY() + CELL_PADDING + rowIndex * (height + paddingBothSides);
			auxRectangle_1.set(cellX, cellY, width - 2, height - 2);
			ItemDisplay selectedItem = storageWindow.getSelectedItem();
			Color color = Color.BLUE;
			if (selectedItem != null) {
				Group parent = getParent();
				float mouseX = Gdx.input.getX(0) - parent.getX();
				float mouseY = getStage().getHeight() - Gdx.input.getY(0) - parent.getY();
				float prefWidth = selectedItem.getPrefWidth();
				float selectedItemX = Utils.closestMultiplication(mouseX - prefWidth / 2, 32);
				float prefHeight = selectedItem.getPrefHeight();
				float selectedItemY = Utils.closestMultiplication(mouseY - prefHeight / 2, 32);
				auxRectangle_2.set(selectedItemX, selectedItemY, prefWidth, prefHeight);
				if (auxRectangle_1.overlaps(auxRectangle_2)) {
					Intersector.intersectRectangles(auxRectangle_1, auxRectangle_2, auxRectangle_3);
					ItemDefinition definition = selectedItem.getItem().getDefinition();
					int col = ((int) (MathUtils.map(auxRectangle_2.x, auxRectangle_2.x + selectedItem.getPrefWidth(), 0, definition.getWidth(), auxRectangle_1.x)));
					int row = (definition.getHeight() - 1) - ((int) (MathUtils.map(auxRectangle_2.y, auxRectangle_2.y + selectedItem.getPrefHeight(), 0, definition.getHeight(), auxRectangle_1.y)));
					int mask = definition.getMask()[(row * definition.getWidth() + col)];
					color = mask == 1 ? Color.YELLOW : Color.DARK_GRAY;
				} else {
					color = Color.DARK_GRAY;
				}
			}
			batch.setColor(color);
			batch.draw(gridCellTexture, cellX, cellY);
		}
		batch.setColor(Color.WHITE);
	}

	private void drawItem(final Batch batch, final Weapon item) {
		batch.draw(
				item.getImage(),
				getX() + item.getX() * GameStage.GRID_CELL_SIZE,
				getY() + item.getY() * GameStage.GRID_CELL_SIZE
		);
	}

}
