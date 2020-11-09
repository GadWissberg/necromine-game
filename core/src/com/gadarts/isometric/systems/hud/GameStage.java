package com.gadarts.isometric.systems.hud;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.gadarts.isometric.components.player.PlayerComponent;
import com.gadarts.isometric.utils.Utils;
import com.gadarts.isometric.utils.assets.Assets;
import com.gadarts.isometric.utils.assets.GameAssetsManager;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class GameStage extends Stage {
	public static final int GRID_SIZE = 256;
	public static final int GRID_CELL_SIZE = 32;
	public static final int PLAYER_LAYOUT_PADDING = 40;
	public static final int CELL_PADDING = 2;
	static final String WINDOW_NAME_STORAGE = "storage";
	private final Map<String, Window> windows = new HashMap<>();
	private final Texture gridTexture;
	private final PlayerComponent playerComponent;
	private final Texture gridCellTexture;

	public GameStage(final FitViewport fitViewport, final PlayerComponent playerComponent) {
		super(fitViewport);
		this.gridTexture = createGridTexture();
		this.gridCellTexture = createGridCellTexture();
		this.playerComponent = playerComponent;
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

	void openStorageWindow(final GameAssetsManager assetsManager) {
		if (!windows.containsKey(WINDOW_NAME_STORAGE)) {
			Texture ninePatchTexture = assetsManager.getTexture(Assets.UiTextures.NINEPATCHES);
			NinePatch patch = new NinePatch(ninePatchTexture, 12, 12, 12, 12);
			Window.WindowStyle style = new Window.WindowStyle(new BitmapFont(), Color.BLACK, new NinePatchDrawable(patch));
			StorageWindow window = new StorageWindow(WINDOW_NAME_STORAGE, style, assetsManager, windows);
			defineStorageWindow(window, assetsManager);
			addActor(window);
			windows.put(WINDOW_NAME_STORAGE, window);
		}
	}


	private void addPlayerLayout(final StorageWindow window, final GameAssetsManager assetsManager) {
		Texture texture = assetsManager.getTexture(Assets.UiTextures.PLAYER_LAYOUT);
		PlayerLayout playerLayout = new PlayerLayout(texture, playerComponent.getSelectedWeapon(), window);
		window.add(playerLayout).pad(PLAYER_LAYOUT_PADDING);
	}

	private void defineStorageWindow(final StorageWindow window, final GameAssetsManager assetsManager) {
		window.setName(WINDOW_NAME_STORAGE);
		window.setSize(100, 100);
		addPlayerLayout(window, assetsManager);
		StorageGrid grid = addStorageGrid(window);
		window.pack();
		grid.setPosition(Utils.closestMultiplication(grid.getX(), 32), Utils.closestMultiplication(grid.getY(), 32));
		window.setPosition(
				getWidth() / 2 - window.getPrefWidth() / 2,
				getHeight() / 2 - window.getPrefHeight() / 2
		);
	}

	private StorageGrid addStorageGrid(final StorageWindow window) {
		StorageGrid actor = new StorageGrid(gridTexture, playerComponent.getStorage(), gridCellTexture, window);
		window.add(actor);
		return actor;
	}

	private Texture createGridTexture() {
		Pixmap gridPixmap = new Pixmap(GRID_SIZE, GRID_SIZE, Pixmap.Format.RGBA8888);
		drawGrid(gridPixmap);
		Texture gridTexture = new Texture(gridPixmap);
		gridPixmap.dispose();
		return gridTexture;
	}

	private void drawGrid(final Pixmap gridPixmap) {
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

	@Override
	public boolean mouseMoved(final int screenX, final int screenY) {
		return super.mouseMoved(screenX, screenY) || !windows.isEmpty();
	}

	@Override
	public boolean touchDown(final int screenX, final int screenY, final int pointer, final int button) {
		if (windows.containsKey(WINDOW_NAME_STORAGE) && Input.Buttons.RIGHT == button) {
			StorageWindow window = (StorageWindow) windows.get(WINDOW_NAME_STORAGE);
			window.onRightMouseButtonClicked();
		}
		return super.touchDown(screenX, screenY, pointer, button) || !windows.isEmpty();
	}

	public boolean hasOpenWindows() {
		return !windows.isEmpty();
	}


}
