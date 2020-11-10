package com.gadarts.isometric.systems.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.gadarts.isometric.components.player.PlayerComponent;
import com.gadarts.isometric.systems.EventsNotifier;
import com.gadarts.isometric.utils.Utils;
import com.gadarts.isometric.utils.assets.GameAssetsManager;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static com.gadarts.isometric.systems.hud.GameStage.GRID_CELL_SIZE;
import static com.gadarts.isometric.systems.hud.GameStage.GRID_SIZE;

public class StorageWindow extends GameWindow implements EventsNotifier<StorageWindowEventsSubscriber> {
	public static final int CELL_PADDING = 2;

	private final static Color auxColor = new Color();
	private final List<StorageWindowEventsSubscriber> subscribers = new ArrayList<>();
	private final Texture gridTexture;
	private final Texture gridCellTexture;
	private final PlayerComponent playerComponent;
	private StorageGrid grid;
	@Getter
	private ItemDisplay selectedItem;

	public StorageWindow(final String windowNameStorage,
						 final WindowStyle windowStyle,
						 final GameAssetsManager assetsManager,
						 final PlayerComponent playerComponent) {
		super(windowNameStorage, windowStyle, assetsManager);
		this.gridTexture = createGridTexture();
		this.gridCellTexture = createGridCellTexture();
		this.playerComponent = playerComponent;
		addStorageGrid();
		addListener(event -> {
			boolean result = false;
			if (event instanceof GameWindowEvent) {
				GameWindowEvent gameWindowEvent = (GameWindowEvent) event;
				GameWindowEventType type = gameWindowEvent.getType();
				if (type == GameWindowEventType.ITEM_SELECTED) {
					applySelectedItem((ItemDisplay) gameWindowEvent.getTarget());
					result = true;
				} else if (type == GameWindowEventType.MOUSE_CLICK_LEFT) {
					onLeftMouseButtonClicked();
					result = true;
				} else if (type == GameWindowEventType.MOUSE_CLICK_RIGHT) {
					onRightMouseButtonClicked();
				}
			}
			return result;
		});
		addListener(new InputListener() {
			@Override
			public void enter(final InputEvent event, final float x, final float y, final int pointer, final Actor fromActor) {
				super.enter(event, x, y, pointer, fromActor);
				Actor target = event.getTarget();
				if (target instanceof ItemDisplay) {
					if (selectedItem == null) {
						ItemDisplay item = (ItemDisplay) target;
						item.clearActions();
						item.applyFlickerAction();
					}
				}
			}

			@Override
			public void exit(final InputEvent event, final float x, final float y, final int pointer, final Actor toActor) {
				super.exit(event, x, y, pointer, toActor);
				Actor target = event.getTarget();
				if (target instanceof ItemDisplay) {
					if (selectedItem == null) {
						ItemDisplay item = (ItemDisplay) target;
						item.clearActions();
						item.addAction(Actions.color(Color.WHITE, ItemDisplay.FLICKER_DURATION, Interpolation.smooth2));
					}
				}
			}

		});
	}

	private Texture createGridTexture() {
		Pixmap gridPixmap = new Pixmap(GRID_SIZE, GRID_SIZE, Pixmap.Format.RGBA8888);
		paintGrid(gridPixmap);
		Texture gridTexture = new Texture(gridPixmap);
		gridPixmap.dispose();
		return gridTexture;
	}

	private void paintGrid(final Pixmap gridPixmap) {
		gridPixmap.setColor(Color.DARK_GRAY);
		gridPixmap.fill();
		gridPixmap.setColor(Color.BLACK);
		gridPixmap.drawRectangle(0, 0, GRID_SIZE, GRID_SIZE);
		IntStream.range(0, GRID_SIZE / GRID_CELL_SIZE).forEach(i -> {
			int division = i * GRID_CELL_SIZE;
			gridPixmap.drawLine(division, 0, division, GRID_SIZE);
			gridPixmap.drawLine(0, division, GRID_SIZE, division);
		});
	}

	private Texture createGridCellTexture() {
		int size = GRID_CELL_SIZE - CELL_PADDING * 2;
		Pixmap gridPixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
		gridPixmap.setColor(Color.WHITE);
		gridPixmap.fill();
		Texture gridTexture = new Texture(gridPixmap);
		gridPixmap.dispose();
		return gridTexture;
	}

	@Override
	public void draw(final Batch batch, final float parentAlpha) {
		super.draw(batch, parentAlpha);
		if (selectedItem != null) {
			drawSelectedItemOnCursor(batch);
		}
	}


	private void drawSelectedItemOnCursor(final Batch batch) {
		Texture image = selectedItem.getItem().getImage();
		float x = Gdx.input.getX(0) - image.getWidth() / 2f;
		float y = getStage().getHeight() - Gdx.input.getY(0) - image.getHeight() / 2f;
		batch.setColor(1f, 1f, 1f, 0.5f);
		batch.draw(image, x, y);
		batch.setColor(1f, 1f, 1f, 1f);
	}

	private void applySelectedItem(final ItemDisplay itemDisplay) {
		if (selectedItem != null) {
			selectedItem.clearActions();
		}
		selectedItem = itemDisplay;
		selectedItem.applyFlickerAction();
		closeButton.setDisabled(true);
		subscribers.forEach(sub -> sub.itemHasBeenSelected(itemDisplay));
	}

	@Override
	public void subscribeForEvents(final StorageWindowEventsSubscriber sub) {
		if (subscribers.contains(sub)) return;
		subscribers.add(sub);
	}

	private StorageGrid addStorageGrid() {
		grid = new StorageGrid(gridTexture, playerComponent.getStorage(), gridCellTexture);
		add(grid);
		return grid;
	}

	private void onRightMouseButtonClicked() {
		if (selectedItem != null) {
			closeButton.setDisabled(false);
			selectedItem = null;
			PlayerLayout playerLayout = findActor(PlayerLayout.NAME);
			if (playerLayout != null) {
				playerLayout.getSelectedWeapon().clearActions();
			}
		}
	}

	private void onLeftMouseButtonClicked() {
		if (selectedItem != null) {
			closeButton.setDisabled(false);
			selectedItem = null;
			PlayerLayout playerLayout = findActor(PlayerLayout.NAME);
			if (playerLayout != null) {
				playerLayout.getSelectedWeapon().clearActions();
			}
		}
	}

	public void initialize() {
		grid.setPosition(Utils.closestMultiplication(grid.getX(), 32), Utils.closestMultiplication(grid.getY(), 32));
	}
}
