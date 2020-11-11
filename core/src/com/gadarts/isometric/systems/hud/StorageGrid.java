package com.gadarts.isometric.systems.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.gadarts.isometric.components.player.Item;
import com.gadarts.isometric.components.player.ItemDefinition;
import com.gadarts.isometric.components.player.PlayerStorage;
import com.gadarts.isometric.utils.Utils;

import java.util.List;

import static com.gadarts.isometric.systems.hud.GameStage.GRID_CELL_SIZE;
import static com.gadarts.isometric.systems.hud.GameStage.GRID_SIZE;
import static com.gadarts.isometric.systems.hud.StorageWindow.CELL_PADDING;

@SuppressWarnings("SameParameterValue")
public class StorageGrid extends Table {
	static final String NAME = "storage_grid";
	private final static Rectangle auxRectangle_1 = new Rectangle();
	private final static Rectangle selectedItemRectangle = new Rectangle();
	private static final Color COLOR_REGULAR = Color.DARK_GRAY;
	private static final Color COLOR_HIGHLIGHT = Color.YELLOW;
	private static final Color COLOR_INVALID = Color.RED;
	private static final Vector2 auxVector = new Vector2();
	private final static Coords auxCoords = new Coords();
	private final Texture gridCellTexture;
	private final PlayerStorage playerStorage;
	private ItemDisplay currentSelectedItem;
	private boolean invalidLocation;

	public StorageGrid(final Texture gridTexture,
					   final PlayerStorage playerStorage,
					   final Texture gridCellTexture) {
		super();
		setName(NAME);
		setBackground(new TextureRegionDrawable(gridTexture));
		this.playerStorage = playerStorage;
		this.gridCellTexture = gridCellTexture;
		addListener(event -> {
			boolean result = false;
			if (event instanceof GameWindowEvent) {
				GameWindowEvent gameWindowEvent = (GameWindowEvent) event;
				GameWindowEventType type = gameWindowEvent.getType();
				if (type == GameWindowEventType.ITEM_SELECTED) {
					currentSelectedItem = (ItemDisplay) gameWindowEvent.getTarget();
					result = true;
				} else if (type == GameWindowEventType.ITEM_SELECTION_CLEARED) {
					currentSelectedItem = null;
					result = true;
				} else if (type == GameWindowEventType.MOUSE_CLICK_LEFT) {
					if (!invalidLocation && currentSelectedItem != null) {
						calculateSelectedItemRectangle();
						Item item = currentSelectedItem.getItem();
						playerStorage.getItems().add(item);
						int cellsInRow = GRID_SIZE / GRID_CELL_SIZE;
						int numberOfCells = cellsInRow * cellsInRow;
						int minRow = Integer.MAX_VALUE;
						int minCol = Integer.MAX_VALUE;
						for (int i = 0; i < numberOfCells; i++) {
							if ((checkIfCellIsBehindSelection(i, auxCoords))) {
								playerStorage.getStorageMap()[auxCoords.row * cellsInRow + auxCoords.col] = 1;
								minRow = Math.min(auxCoords.row, minRow);
								minCol = Math.min(auxCoords.col, minCol);
							}
						}
						item.setRow(minRow);
						item.setCol(minCol);
						fire(new GameWindowEvent(currentSelectedItem, GameWindowEventType.ITEM_PLACED));
						result = true;
					}
				}
			}
			return result;
		});
	}

	@Override
	public void draw(final Batch batch, final float parentAlpha) {
		super.draw(batch, parentAlpha);
		drawCells(batch);
		List<Item> items = playerStorage.getItems();
		for (Item item : items) {
			drawItem(batch, item);
		}
	}

	@Override
	public void act(final float delta) {
		super.act(delta);
		if (currentSelectedItem != null) {
			calculateSelectedItemRectangle();
			Rectangle storageGridRectangle = auxRectangle_1.set(getX(), getY(), getPrefWidth(), getPrefHeight());
			invalidLocation = !Utils.rectangleContainedInRectangleWithBoundaries(storageGridRectangle, selectedItemRectangle);
		}
	}

	private void calculateSelectedItemRectangle() {
		float prefWidth = currentSelectedItem.getPrefWidth();
		float prefHeight = currentSelectedItem.getPrefHeight();
		selectedItemRectangle.set(0, 0, prefWidth, prefHeight);
		Group parent = getParent();
		float mouseX = Gdx.input.getX(0) - parent.getX();
		float mouseY = getStage().getHeight() - Gdx.input.getY(0) - parent.getY();
		float selectedItemX = Utils.closestMultiplication(mouseX - selectedItemRectangle.getWidth() / 2, 32);
		float selectedItemY = Utils.closestMultiplication(mouseY - selectedItemRectangle.getHeight() / 2, 32);
		selectedItemRectangle.setPosition(selectedItemX, selectedItemY);
	}

	private void drawCells(final Batch batch) {
		int cellsInRow = GRID_SIZE / GRID_CELL_SIZE;
		int numberOfCells = cellsInRow * cellsInRow;
		for (int i = 0; i < numberOfCells; i++) {
			Color color = checkIfCellIsBehindSelection(i, auxCoords) ? (!invalidLocation ? COLOR_HIGHLIGHT : COLOR_INVALID) : COLOR_REGULAR;
			batch.setColor(color);
			Vector2 cellPosition = calculateCellPosition(i, auxVector);
			batch.draw(gridCellTexture, cellPosition.x, cellPosition.y);
		}
		batch.setColor(Color.WHITE);
	}

	private boolean checkIfCellIsBehindSelection(final int index, final Coords auxCoords) {
		int width = gridCellTexture.getWidth();
		int height = gridCellTexture.getHeight();
		Vector2 cellPosition = calculateCellPosition(index, auxVector);
		float x = getX();
		float y = getY();
		auxCoords.set(
				MathUtils.round(MathUtils.map(y, y + getPrefHeight(), 0, PlayerStorage.HEIGHT, cellPosition.y)),
				MathUtils.round(MathUtils.map(x, x + getPrefWidth(), 0, PlayerStorage.WIDTH, cellPosition.x))
		);
		Rectangle cellRectangle = auxRectangle_1.set(cellPosition.x, cellPosition.y, width, height);
		boolean result = false;
		if (currentSelectedItem != null) {
			if (cellRectangle.overlaps(selectedItemRectangle)) {
				ItemDefinition definition = currentSelectedItem.getItem().getDefinition();
				int col = ((int) (MathUtils.map(selectedItemRectangle.x, selectedItemRectangle.x + currentSelectedItem.getPrefWidth(), 0, definition.getWidth(), cellRectangle.x)));
				int row = (definition.getHeight() - 1) - ((int) (MathUtils.map(selectedItemRectangle.y, selectedItemRectangle.y + currentSelectedItem.getPrefHeight(), 0, definition.getHeight(), cellRectangle.y)));
				int mask = definition.getMask()[(row * definition.getWidth() + col)];
				result = mask == 1;
			}
		}
		return result;
	}

	private Vector2 calculateCellPosition(final int index, final Vector2 output) {
		int width = gridCellTexture.getWidth();
		int height = gridCellTexture.getHeight();
		int cellsInRow = GRID_SIZE / GRID_CELL_SIZE;
		int paddingBothSides = CELL_PADDING * 2;
		int rowIndex = index / cellsInRow;
		float cellX = getX() + CELL_PADDING + (index % cellsInRow) * (width + paddingBothSides);
		float cellY = getY() + CELL_PADDING + rowIndex * (height + paddingBothSides);
		return output.set(cellX, cellY);
	}

	private void drawItem(final Batch batch, final Item item) {
		batch.draw(
				item.getImage(),
				getX() + item.getCol() * GameStage.GRID_CELL_SIZE,
				getY() + item.getRow() * GameStage.GRID_CELL_SIZE
		);
	}

	private static class Coords {
		private int row;
		private int col;

		public void set(final int row, final int col) {
			this.row = row;
			this.col = col;
		}
	}
}
