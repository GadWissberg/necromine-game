package com.gadarts.isometric.systems.hud.window.storage;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.gadarts.isometric.components.player.Item;
import com.gadarts.isometric.components.player.PlayerStorage;
import com.gadarts.isometric.components.player.PlayerStorageEventsSubscriber;
import com.gadarts.isometric.components.player.Weapon;
import com.gadarts.isometric.systems.hud.GameStage;
import com.gadarts.isometric.systems.hud.window.GameWindowEvent;
import com.gadarts.isometric.systems.hud.window.GameWindowEventType;
import com.gadarts.isometric.systems.hud.window.storage.item.ItemDisplay;
import com.gadarts.isometric.systems.hud.window.storage.item.ItemSelectionHandler;
import com.gadarts.isometric.systems.hud.window.storage.item.ItemsTable;
import com.gadarts.isometric.utils.Utils;
import com.gadarts.necromine.model.pickups.ItemDefinition;

import java.util.Optional;
import java.util.stream.IntStream;

import static com.gadarts.isometric.systems.hud.GameStage.GRID_CELL_SIZE;
import static com.gadarts.isometric.systems.hud.GameStage.GRID_SIZE;

@SuppressWarnings("SameParameterValue")
public class StorageGrid extends ItemsTable implements PlayerStorageEventsSubscriber {
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
	private boolean invalidLocation;

	public StorageGrid(final Texture gridTexture,
					   final PlayerStorage playerStorage,
					   final Texture gridCellTexture,
					   final ItemSelectionHandler itemSelectionHandler) {
		super(itemSelectionHandler);
		setTouchable(Touchable.enabled);
		setName(NAME);
		setBackground(new TextureRegionDrawable(gridTexture));
		this.playerStorage = playerStorage;
		this.gridCellTexture = gridCellTexture;
		addListener(event -> {
			boolean result = false;
			if (event instanceof GameWindowEvent) {
				GameWindowEvent gameWindowEvent = (GameWindowEvent) event;
				GameWindowEventType type = gameWindowEvent.getType();
				if (type == GameWindowEventType.ITEM_PLACED) {
					ItemsTable itemsTable = (ItemsTable) event.getTarget();
					ItemDisplay selectedItem = itemSelectionHandler.getSelection();
					if (itemsTable instanceof StorageGrid) {
						calculateSelectedItemRectangle();
						itemsTable.removeItem(selectedItem);
						selectedItem.setLocatedIn(StorageGrid.class);
						Item item = selectedItem.getItem();
						playerStorage.getItems().add(item);
						int cellsInRow = GRID_SIZE / GRID_CELL_SIZE;
						int numberOfCells = cellsInRow * cellsInRow;
						int minRow = Integer.MAX_VALUE;
						int minCol = Integer.MAX_VALUE;
						for (int i = 0; i < numberOfCells; i++) {
							if ((checkIfCellIsBehindSelection(i, auxCoords))) {
								playerStorage.getStorageMap()[auxCoords.row * cellsInRow + auxCoords.col] = item.getDefinition().getId();
								minRow = Math.min(auxCoords.row, minRow);
								minCol = Math.min(auxCoords.col, minCol);
							}
						}
						item.setRow(minRow);
						item.setCol(minCol);
						selectedItem.setPosition(
								getX() + item.getCol() * GameStage.GRID_CELL_SIZE,
								getY() + item.getRow() * GameStage.GRID_CELL_SIZE
						);
						selectedItem.toFront();
						selectedItem.clearActions();
					} else {
						removeItem(selectedItem);
					}
					result = true;
				}
			}
			return result;
		});
		addListener(new ClickListener() {
			@Override
			public boolean touchDown(final InputEvent event, final float x, final float y, final int pointer, final int button) {
				boolean result = super.touchDown(event, x, y, pointer, button);
				if (button == Input.Buttons.LEFT) {
					result |= onLeftClick();
				} else if (button == Input.Buttons.RIGHT) {
					onRightClick();
					result = true;
				}
				return result;
			}

		});
		playerStorage.subscribeForEvents(this);
	}

	private boolean onLeftClick() {
		boolean result = false;
		if (!invalidLocation && itemSelectionHandler.getSelection() != null) {
			fire(new GameWindowEvent(StorageGrid.this, GameWindowEventType.ITEM_PLACED));
			result = true;
		}
		return result;
	}

	@Override
	protected void drawChildren(final Batch batch, final float parentAlpha) {
		super.drawChildren(batch, parentAlpha);
	}

	@Override
	public void draw(final Batch batch, final float parentAlpha) {
		drawCells(batch);
		super.draw(batch, parentAlpha);
	}

	@Override
	public void act(final float delta) {
		super.act(delta);
		if (itemSelectionHandler.getSelection() != null) {
			calculateSelectedItemRectangle();
			Rectangle storageGridRectangle = auxRectangle_1.set(getX(), getY(), getPrefWidth(), getPrefHeight());
			invalidLocation = !Utils.rectangleContainedInRectangleWithBoundaries(storageGridRectangle, selectedItemRectangle);
		}
	}

	private void calculateSelectedItemRectangle() {
		ItemDisplay selection = itemSelectionHandler.getSelection();
		float prefWidth = selection.getPrefWidth();
		float prefHeight = selection.getPrefHeight();
		selectedItemRectangle.set(0, 0, prefWidth, prefHeight);
		Group parent = getParent();
		float mouseX = Gdx.input.getX(0) - parent.getX();
		float mouseY = getStage().getHeight() - Gdx.input.getY(0) - parent.getY();
		float x = getX();
		int selectedItemCol = MathUtils.round(MathUtils.map(x, x + getPrefWidth(), 0, PlayerStorage.WIDTH, mouseX - selectedItemRectangle.getWidth() / 2));
		float y = getY();
		int selectedItemRow = MathUtils.round(MathUtils.map(y, y + getPrefHeight(), 0, PlayerStorage.HEIGHT, mouseY - selectedItemRectangle.getHeight() / 2));
		selectedItemRectangle.setPosition(x + selectedItemCol * GRID_CELL_SIZE, y + selectedItemRow * GRID_CELL_SIZE);
	}

	private void drawCells(final Batch batch) {
		int cellsInRow = GRID_SIZE / GRID_CELL_SIZE;
		int numberOfCells = cellsInRow * cellsInRow;
		for (int i = 0; i < numberOfCells; i++) {
			boolean cellIsBehindSelection = checkIfCellIsBehindSelection(i, auxCoords);
			Color color = cellIsBehindSelection ? (!invalidLocation ? COLOR_HIGHLIGHT : COLOR_INVALID) : COLOR_REGULAR;
			int valueInMap = playerStorage.getStorageMap()[auxCoords.row * PlayerStorage.WIDTH + auxCoords.col];
			ItemDisplay selection = itemSelectionHandler.getSelection();
			if (selection != null && cellIsBehindSelection && valueInMap > 0 && valueInMap != selection.getItem().getDefinition().getId()) {
				invalidLocation = true;
				color = COLOR_INVALID;
			}
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
		if (itemSelectionHandler.getSelection() != null) {
			if (cellRectangle.overlaps(selectedItemRectangle)) {
				ItemDisplay selection = itemSelectionHandler.getSelection();
				ItemDefinition definition = selection.getItem().getDefinition();
				int col = ((int) (MathUtils.map(selectedItemRectangle.x, selectedItemRectangle.x + selection.getPrefWidth(), 0, definition.getWidth(), cellRectangle.x)));
				int row = ((int) (MathUtils.map(selectedItemRectangle.y, selectedItemRectangle.y + selection.getPrefHeight(), 0, definition.getHeight(), cellRectangle.y)));
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
		int rowIndex = index / cellsInRow;
		float cellX = getX() + (index % cellsInRow) * (width);
		float cellY = getY() + rowIndex * (height);
		return output.set(cellX, cellY);
	}

	@Override
	public void itemAddedToStorage(final Item item) {
		addItemToStorage(item);
	}

	@Override
	public void onSelectedWeaponChanged(final Weapon selectedWeapon) {

	}

	public void initialize() {
		playerStorage.getItems().forEach(this::addItemToStorage);
	}

	private void addItemToStorage(final Item item) {
		ItemDisplay itemDisplay = new ItemDisplay(item, itemSelectionHandler, StorageGrid.class);
		float weaponX = getX() + item.getCol() * GRID_CELL_SIZE;
		float weaponY = getY() + item.getRow() * GRID_CELL_SIZE;
		itemDisplay.setPosition(weaponX, weaponY);
		StorageGrid.this.getParent().addActor(itemDisplay);
	}

	@Override
	public void removeItem(final ItemDisplay item) {
		playerStorage.removeItem(item.getItem().getDefinition().getId());
	}

	public ItemDisplay findItemDisplay(final int id) {
		Actor[] items = StorageGrid.this.getParent().getChildren().items;
		Optional<ItemDisplay> item = IntStream.range(0, items.length)
				.filter(i -> {
					Actor actor = items[i];
					return actor instanceof ItemDisplay && ((ItemDisplay) actor).getItem().getDefinition().getId() == id;
				})
				.mapToObj(value -> ((ItemDisplay) items[value]))
				.findFirst();
		return item.orElse(null);
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
