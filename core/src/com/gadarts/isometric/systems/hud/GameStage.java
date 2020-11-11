package com.gadarts.isometric.systems.hud;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.SnapshotArray;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.gadarts.isometric.components.player.PlayerComponent;
import com.gadarts.isometric.utils.assets.Assets;
import com.gadarts.isometric.utils.assets.GameAssetsManager;

import java.util.HashMap;
import java.util.Map;

public class GameStage extends Stage {
	public static final int GRID_SIZE = 256;
	public static final int GRID_CELL_SIZE = 32;
	static final String WINDOW_NAME_STORAGE = "storage";
	private final Map<String, Window> windows = new HashMap<>();
	private final PlayerComponent playerComponent;

	public GameStage(final FitViewport fitViewport, final PlayerComponent playerComponent) {
		super(fitViewport);
		this.playerComponent = playerComponent;
		addListener(event -> {
			boolean result = false;
			if (event instanceof GameWindowEvent) {
				GameWindowEvent gameWindowEvent = (GameWindowEvent) event;
				if (gameWindowEvent.getType() == GameWindowEventType.WINDOW_CLOSED) {
					Actor window = gameWindowEvent.getTarget();
					windows.remove(window.getName());
					result = true;
				}
			}
			return result;
		});
	}


	void openStorageWindow(final GameAssetsManager assetsManager) {
		if (!windows.containsKey(WINDOW_NAME_STORAGE)) {
			Texture ninePatchTexture = assetsManager.getTexture(Assets.UiTextures.NINEPATCHES);
			NinePatch patch = new NinePatch(ninePatchTexture, 12, 12, 12, 12);
			Window.WindowStyle style = new Window.WindowStyle(new BitmapFont(), Color.BLACK, new NinePatchDrawable(patch));
			StorageWindow window = new StorageWindow(WINDOW_NAME_STORAGE, style, assetsManager, playerComponent);
			defineStorageWindow(window, assetsManager);
			addActor(window);
			windows.put(WINDOW_NAME_STORAGE, window);
		}
	}



	private void defineStorageWindow(final StorageWindow window, final GameAssetsManager assetsManager) {
		window.setName(WINDOW_NAME_STORAGE);
		window.setSize(100, 100);
		window.pack();
		window.initialize();
		window.setPosition(
				getWidth() / 2 - window.getPrefWidth() / 2,
				getHeight() / 2 - window.getPrefHeight() / 2
		);
	}


	@Override
	public boolean mouseMoved(final int screenX, final int screenY) {
		return super.mouseMoved(screenX, screenY) || !windows.isEmpty();
	}

	@Override
	public boolean touchDown(final int screenX, final int screenY, final int pointer, final int button) {
		Group root = getRoot();
		SnapshotArray<Actor> children = root.getChildren();
		if (button == Input.Buttons.RIGHT) {
			children.forEach(child -> child.notify(new GameWindowEvent(root, GameWindowEventType.MOUSE_CLICK_RIGHT), false));
		} else if (button == Input.Buttons.LEFT) {
			children.forEach(child -> child.notify(new GameWindowEvent(root, GameWindowEventType.MOUSE_CLICK_LEFT), false));
		}
		return super.touchDown(screenX, screenY, pointer, button) || !windows.isEmpty();
	}

	public boolean hasOpenWindows() {
		return !windows.isEmpty();
	}


}
