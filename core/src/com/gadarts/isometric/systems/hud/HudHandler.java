package com.gadarts.isometric.systems.hud;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.services.GameServices;
import com.gadarts.isometric.systems.hud.console.ConsoleCommandResult;
import com.gadarts.isometric.systems.hud.console.ConsoleCommands;
import com.gadarts.isometric.systems.hud.console.commands.ConsoleCommandsList;
import com.gadarts.isometric.utils.DefaultGameSettings;
import com.gadarts.necromine.assets.Assets;
import com.gadarts.necromine.assets.GameAssetsManager;
import lombok.Getter;

import static com.badlogic.gdx.Application.LOG_DEBUG;
import static com.gadarts.isometric.NecronemesGame.*;
import static com.gadarts.isometric.utils.DefaultGameSettings.FULL_SCREEN;


@Getter
public class HudHandler {
	public static final String MSG_BORDERS = "UI borders are %s.";
	static final String TABLE_NAME_HUD = "hud";
	private static final float BUTTON_PADDING = 40;
	private static final String BUTTON_NAME_STORAGE = "button_storage";
	private boolean showBorders = DefaultGameSettings.DISPLAY_HUD_OUTLINES;
	private GameStage stage;

	private void createStage(final GameServices services, final Entity player) {
		int width = FULL_SCREEN ? FULL_SCREEN_RESOLUTION_WIDTH : WINDOWED_RESOLUTION_WIDTH;
		int height = FULL_SCREEN ? FULL_SCREEN_RESOLUTION_HEIGHT : WINDOWED_RESOLUTION_HEIGHT;
		FitViewport fitViewport = new FitViewport(width, height);
		stage = new GameStage(fitViewport, ComponentsMapper.player.get(player), services.getSoundPlayer());
	}

	private void addStorageButton(final Table table, final GameServices services) {
		Button.ButtonStyle buttonStyle = new Button.ButtonStyle();
		GameAssetsManager assetsManager = services.getAssetManager();
		buttonStyle.up = new TextureRegionDrawable(assetsManager.getTexture(Assets.UiTextures.BUTTON_STORAGE));
		buttonStyle.down = new TextureRegionDrawable(assetsManager.getTexture(Assets.UiTextures.BUTTON_STORAGE_DOWN));
		buttonStyle.over = new TextureRegionDrawable(assetsManager.getTexture(Assets.UiTextures.BUTTON_STORAGE_HOVER));
		Button button = new Button(buttonStyle);
		button.setName(BUTTON_NAME_STORAGE);
		button.addListener(new ClickListener() {
			@Override
			public void clicked(final InputEvent event, final float x, final float y) {
				super.clicked(event, x, y);
				stage.openStorageWindow(assetsManager);
				services.getSoundPlayer().playSound(Assets.Sounds.UI_CLICK);
			}
		});
		table.add(button).expand().left().bottom().pad(BUTTON_PADDING);
	}

	Table addTable( ) {
		Table table = new Table();
		stage.setDebugAll(Gdx.app.getLogLevel() == LOG_DEBUG && showBorders);
		table.setFillParent(true);
		stage.addActor(table);
		return table;
	}

	public void init(final GameServices services, final Entity player) {
		createStage(services, player);
		Table hudTable = addTable();
		hudTable.setName(TABLE_NAME_HUD);
		addStorageButton(hudTable, services);
		stage.addActor(services.getConsoleImpl());
	}

	public boolean onCommandRun(final ConsoleCommands command, final ConsoleCommandResult consoleCommandResult) {
		if (command == ConsoleCommandsList.BORDERS) {
			this.showBorders = !showBorders;
			stage.setDebugAll(showBorders);
			String msg = showBorders ? String.format(MSG_BORDERS, "displayed") : String.format(MSG_BORDERS, "hidden");
			consoleCommandResult.setMessage(msg);
			return true;
		}
		return false;
	}

	public void onEnemyTurn( ) {
		Button button = getStage().getRoot().findActor(BUTTON_NAME_STORAGE);
		button.setTouchable(Touchable.disabled);
	}

	public void onPlayerTurn( ) {
		Button button = getStage().getRoot().findActor(BUTTON_NAME_STORAGE);
		button.setTouchable(Touchable.enabled);
	}
}
