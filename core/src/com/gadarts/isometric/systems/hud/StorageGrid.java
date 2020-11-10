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

import static com.gadarts.isometric.systems.hud.GameStage.GRID_CELL_SIZE;
import static com.gadarts.isometric.systems.hud.GameStage.GRID_SIZE;
import static com.gadarts.isometric.systems.hud.StorageWindow.CELL_PADDING;

public class StorageGrid extends Table {
	private final static Rectangle auxRectangle_1 = new Rectangle();
	private final static Rectangle auxRectangle_2 = new Rectangle();
	private final static Rectangle auxRectangle_3 = new Rectangle();
	private static final Color COLOR_REGULAR = Color.DARK_GRAY;
	private static final Color COLOR_HIGHLIGHT = Color.YELLOW;
	private static final Color COLOR_INVALID = Color.RED;
	private final List<Weapon> playerStorage;
	private final Texture gridCellTexture;
	private ItemDisplay currentSelectedItem;

	public StorageGrid(final Texture gridTexture,
					   final List<Weapon> playerStorage,
					   final Texture gridCellTexture) {
		super();
		setBackground(new TextureRegionDrawable(gridTexture));
		this.playerStorage = playerStorage;
		this.gridCellTexture = gridCellTexture;
		addListener(event -> {
			boolean result = false;
			if (event instanceof GameWindowEvent) {
				GameWindowEvent gameWindowEvent = (GameWindowEvent) event;
				if (gameWindowEvent.getType() == GameWindowEventType.ITEM_SELECTED) {
					currentSelectedItem = (ItemDisplay) gameWindowEvent.getTarget();
					result = true;
				}
			}
			return result;
		});
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
		Rectangle selectedItemRectangle = null;
		ItemDisplay selectedItem = currentSelectedItem;
		float selectedItemX = 0;
		float selectedItemY = 0;
		Color color = COLOR_REGULAR;
		boolean invalid = false;
		if (selectedItem != null) {
			float prefWidth = selectedItem.getPrefWidth();
			float prefHeight = selectedItem.getPrefHeight();
			selectedItemRectangle = auxRectangle_2.set(0, 0, prefWidth, prefHeight);
			Group parent = getParent();
			float mouseX = Gdx.input.getX(0) - parent.getX();
			float mouseY = getStage().getHeight() - Gdx.input.getY(0) - parent.getY();
			selectedItemX = Utils.closestMultiplication(mouseX - selectedItemRectangle.getWidth() / 2, 32);
			selectedItemY = Utils.closestMultiplication(mouseY - selectedItemRectangle.getHeight() / 2, 32);
			selectedItemRectangle.setPosition(selectedItemX, selectedItemY);
			Rectangle storageGridRectangle = auxRectangle_1.set(getX(), getY(), getPrefWidth(), getPrefHeight());
			if (!Utils.rectangleContainedInRectangleWithBoundaries(storageGridRectangle, selectedItemRectangle)) {
				invalid = true;
			}
		}
		for (int i = 0; i < numberOfCells; i++) {
			int rowIndex = i / cellsInRow;
			int width = gridCellTexture.getWidth();
			int height = gridCellTexture.getHeight();
			int paddingBothSides = CELL_PADDING * 2;
			float cellX = getX() + CELL_PADDING + (i % cellsInRow) * (width + paddingBothSides);
			float cellY = getY() + CELL_PADDING + rowIndex * (height + paddingBothSides);
			auxRectangle_1.set(cellX, cellY, width - 2, height - 2);
			if (selectedItem != null) {
				if (auxRectangle_1.overlaps(selectedItemRectangle)) {
					Intersector.intersectRectangles(auxRectangle_1, selectedItemRectangle, auxRectangle_3);
					ItemDefinition definition = selectedItem.getItem().getDefinition();
					int col = ((int) (MathUtils.map(selectedItemRectangle.x, selectedItemRectangle.x + selectedItem.getPrefWidth(), 0, definition.getWidth(), auxRectangle_1.x)));
					int row = (definition.getHeight() - 1) - ((int) (MathUtils.map(selectedItemRectangle.y, selectedItemRectangle.y + selectedItem.getPrefHeight(), 0, definition.getHeight(), auxRectangle_1.y)));
					int mask = definition.getMask()[(row * definition.getWidth() + col)];
					color = mask == 1 ? (!invalid ? COLOR_HIGHLIGHT : COLOR_INVALID) : COLOR_REGULAR;
				} else {
					color = COLOR_REGULAR;
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
