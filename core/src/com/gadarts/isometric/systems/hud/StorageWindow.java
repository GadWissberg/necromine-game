package com.gadarts.isometric.systems.hud;

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
import com.gadarts.isometric.utils.assets.Assets;
import com.gadarts.isometric.utils.assets.GameAssetsManager;
import lombok.Getter;

import java.util.stream.IntStream;

import static com.gadarts.isometric.systems.hud.GameStage.GRID_CELL_SIZE;
import static com.gadarts.isometric.systems.hud.GameStage.GRID_SIZE;

public class StorageWindow extends GameWindow {
	public static final int PLAYER_LAYOUT_PADDING = 40;

	private final Texture gridTexture;
	private final Texture gridCellTexture;
	private final PlayerComponent playerComponent;
	private StorageGrid grid;
	@Getter
	private ItemSelectionHandler selectedItem = new ItemSelectionHandler();

	public StorageWindow(final String windowNameStorage,
						 final WindowStyle windowStyle,
						 final GameAssetsManager assetsManager,
						 final PlayerComponent playerComponent) {
		super(windowNameStorage, windowStyle, assetsManager);
		this.gridTexture = createGridTexture();
		this.gridCellTexture = createGridCellTexture();
		this.playerComponent = playerComponent;
		addPlayerLayout(assetsManager);
		setTouchable(Touchable.enabled);
		addStorageGrid();
		addListener(event -> {
			boolean result = false;
			if (event instanceof GameWindowEvent) {
				GameWindowEvent gameWindowEvent = (GameWindowEvent) event;
				GameWindowEventType type = gameWindowEvent.getType();
				if (type == GameWindowEventType.ITEM_SELECTED) {
					ItemDisplay target = (ItemDisplay) event.getTarget();
					if (selectedItem.getSelection() != target) {
						applySelectedItem(target);
					} else {
						clearSelectedItem();
					}
				} else if (type == GameWindowEventType.ITEM_PLACED) {
					if (event.getTarget() instanceof PlayerLayout) {
						findActor(StorageGrid.NAME).notify(event, false);
					} else {
						findActor(PlayerLayout.NAME).notify(event, false);
					}
					clearSelectedItem();
					result = true;
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
					if (selectedItem.getSelection() != null) {
						selectedItem.setSelection(null);
						result = true;
					}
				}
				return result;
			}

		});
	}

	private void addPlayerLayout(final GameAssetsManager assetsManager) {
		Texture texture = assetsManager.getTexture(Assets.UiTextures.PLAYER_LAYOUT);
		PlayerLayout playerLayout = new PlayerLayout(texture, playerComponent.getSelectedWeapon(), selectedItem);
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

	private void applySelectedItem(final ItemDisplay itemDisplay) {
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
		grid = new StorageGrid(gridTexture, playerComponent.getStorage(), gridCellTexture, selectedItem);
		add(grid);
	}

	private void clearSelectedItem() {
		if (selectedItem.getSelection() != null) {
			closeButton.setDisabled(false);
			selectedItem.setSelection(null);
		}
	}

}
