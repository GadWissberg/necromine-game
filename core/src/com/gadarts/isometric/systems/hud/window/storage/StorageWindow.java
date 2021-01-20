package com.gadarts.isometric.systems.hud.window.storage;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.gadarts.isometric.components.player.PlayerComponent;
import com.gadarts.isometric.components.player.Weapon;
import com.gadarts.isometric.systems.hud.window.GameWindow;
import com.gadarts.isometric.systems.hud.window.GameWindowEvent;
import com.gadarts.isometric.systems.hud.window.storage.item.ItemDisplay;
import com.gadarts.isometric.systems.hud.window.storage.item.ItemSelectionHandler;
import com.gadarts.isometric.utils.SoundPlayer;
import com.gadarts.necromine.Assets;
import com.gadarts.necromine.assets.GameAssetsManager;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.stream.IntStream;

import static com.gadarts.isometric.systems.hud.GameStage.GRID_CELL_SIZE;
import static com.gadarts.isometric.systems.hud.GameStage.GRID_SIZE;

/**
 * The player's storage management GUI.
 */
public class StorageWindow extends GameWindow {

	/**
	 * Window identifier.
	 */
	public static final String NAME = "storage";
	private static final int PLAYER_LAYOUT_PADDING = 40;

	@Getter
	private final ItemSelectionHandler selectedItem = new ItemSelectionHandler();

	private final Texture gridTexture;
	private final Texture gridCellTexture;
	private final PlayerComponent playerComponent;

	@Getter(AccessLevel.PACKAGE)
	private StorageGrid storageGrid;

	@Getter(AccessLevel.PACKAGE)
	private PlayerLayout playerLayout;

	public StorageWindow(final WindowStyle windowStyle,
						 final GameAssetsManager assetsManager,
						 final PlayerComponent playerComponent,
						 final SoundPlayer soundPlayer) {
		super(StorageWindow.NAME, windowStyle, assetsManager);
		this.gridTexture = createGridTexture();
		this.gridCellTexture = createGridCellTexture();
		this.playerComponent = playerComponent;
		addPlayerLayout(assetsManager);
		setTouchable(Touchable.enabled);
		addStorageGrid();
		initializeListeners(soundPlayer);
	}

	private void initializeListeners(final SoundPlayer soundPlayer) {
		addListener(event -> {
			boolean result = false;
			if (event instanceof GameWindowEvent) {
				GameWindowEvent gameWindowEvent = (GameWindowEvent) event;
				result = StorageWindowOnEvents.execute(gameWindowEvent, soundPlayer, selectedItem, StorageWindow.this);
			}
			return result;
		});
		addListener(new InputListener() {
			@Override
			public void enter(final InputEvent event, final float x, final float y, final int pointer, final Actor fromActor) {
				super.enter(event, x, y, pointer, fromActor);
				Actor target = event.getTarget();
				if (target instanceof ItemDisplay) {
					if (selectedItem.getSelection() == null) {
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
					if (selectedItem.getSelection() == null) {
						ItemDisplay item = (ItemDisplay) target;
						item.clearActions();
						item.addAction(Actions.color(Color.WHITE, ItemDisplay.FLICKER_DURATION, Interpolation.smooth2));
					}
				}
			}
		});
		addListener(new ClickListener() {
			@Override
			public boolean touchDown(final InputEvent event,
									 final float x,
									 final float y,
									 final int pointer,
									 final int button) {
				boolean result = super.touchDown(event, x, y, pointer, button);
				if (button == Input.Buttons.RIGHT) {
					result = onRightClick();
				}
				return result;
			}

		});
	}

	boolean onRightClick() {
		boolean result = false;
		if (selectedItem.getSelection() != null) {
			clearSelectedItem();
			result = true;
		}
		return result;
	}

	private void addPlayerLayout(final GameAssetsManager assetsManager) {
		Texture texture = assetsManager.getTexture(Assets.UiTextures.PLAYER_LAYOUT);
		Weapon selectedWeapon = playerComponent.getStorage().getSelectedWeapon();
		playerLayout = new PlayerLayout(texture, selectedWeapon, selectedItem, playerComponent);
		add(playerLayout).pad(PLAYER_LAYOUT_PADDING);
	}

	private Texture createGridTexture() {
		Pixmap gridPixmap = new Pixmap(GRID_SIZE, GRID_SIZE, Pixmap.Format.RGBA8888);
		paintGrid(gridPixmap);
		Texture gridTexture = new Texture(gridPixmap);
		gridPixmap.dispose();
		return gridTexture;
	}

	private void paintGrid(final Pixmap gridPixmap) {
		gridPixmap.setColor(Color.BLACK);
		gridPixmap.drawRectangle(0, 0, GRID_SIZE, GRID_SIZE);
		IntStream.range(0, GRID_SIZE / GRID_CELL_SIZE).forEach(i -> {
			int division = i * GRID_CELL_SIZE;
			gridPixmap.drawLine(division, 0, division, GRID_SIZE);
			gridPixmap.drawLine(0, division, GRID_SIZE, division);
		});
	}

	private Texture createGridCellTexture() {
		int size = GRID_CELL_SIZE;
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
		if (selectedItem.getSelection() != null) {
			drawSelectedItemOnCursor(batch);
		}
	}


	private void drawSelectedItemOnCursor(final Batch batch) {
		Texture image = selectedItem.getSelection().getItem().getImage();
		float x = Gdx.input.getX(0) - image.getWidth() / 2f;
		float y = getStage().getHeight() - Gdx.input.getY(0) - image.getHeight() / 2f;
		batch.setColor(1f, 1f, 1f, 0.5f);
		batch.draw(image, x, y);
		batch.setColor(1f, 1f, 1f, 1f);
	}

	void applySelectedItem(final ItemDisplay itemDisplay) {
		if (itemDisplay != null) {
			itemDisplay.clearActions();
		}
		selectedItem.setSelection(itemDisplay);
		if (itemDisplay != null) {
			itemDisplay.applyFlickerAction();
		}
		closeButton.setDisabled(true);
	}

	private void addStorageGrid() {
		storageGrid = new StorageGrid(gridTexture, playerComponent.getStorage(), gridCellTexture, selectedItem);
		add(storageGrid);
	}

	void clearSelectedItem() {
		if (selectedItem.getSelection() != null) {
			closeButton.setDisabled(false);
			selectedItem.setSelection(null);
		}
	}

	/**
	 * Initializes the storage grid.
	 */
	public void initialize() {
		storageGrid.initialize();
	}
}
