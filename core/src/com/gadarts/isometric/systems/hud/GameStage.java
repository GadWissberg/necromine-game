package com.gadarts.isometric.systems.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.gadarts.isometric.components.player.PlayerComponent;
import com.gadarts.isometric.utils.assets.Assets;
import com.gadarts.isometric.utils.assets.GameAssetsManager;

import java.util.HashMap;
import java.util.Map;


public class GameStage extends Stage {
    public static final int GRID_SIZE = 256;
    public static final int GRID_CELL_SIZE = 32;
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
                    window.setVisible(false);
                    result = true;
                }
            }
            return result;
        });
    }


    void openStorageWindow(final GameAssetsManager assetsManager) {
        if (!windows.containsKey(StorageWindow.NAME)) {
            createStorageWindow(assetsManager);
        } else {
            Window window = windows.get(StorageWindow.NAME);
            window.setVisible(true);
        }
    }

    private void createStorageWindow(GameAssetsManager assetsManager) {
        Texture ninePatchTexture = assetsManager.getTexture(Assets.UiTextures.NINEPATCHES);
        NinePatch patch = new NinePatch(ninePatchTexture, 12, 12, 12, 12);
        Window.WindowStyle style = new Window.WindowStyle(new BitmapFont(), Color.BLACK, new NinePatchDrawable(patch));
        StorageWindow window = new StorageWindow(StorageWindow.NAME, style, assetsManager, playerComponent);
        defineStorageWindow(window);
        addActor(window);
        windows.put(StorageWindow.NAME, window);
    }


    private void defineStorageWindow(final StorageWindow window) {
        window.setName(StorageWindow.NAME);
        window.setSize(100, 100);
        window.pack();
        window.setPosition(
                getWidth() / 2 - window.getPrefWidth() / 2,
                getHeight() / 2 - window.getPrefHeight() / 2
        );
        window.initialize();
    }


    @Override
    public boolean mouseMoved(final int screenX, final int screenY) {
        return super.mouseMoved(screenX, screenY) || !windows.isEmpty();
    }

    @Override
    public boolean touchDown(final int screenX, final int screenY, final int pointer, final int button) {
        return super.touchDown(screenX, screenY, pointer, button) || !windows.isEmpty();
    }

    public boolean hasOpenWindows() {
        return !windows.isEmpty();
    }

}
