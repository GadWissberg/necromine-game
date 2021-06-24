package com.gadarts.isometric.systems.hud;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.gadarts.isometric.GlobalGameService;
import com.gadarts.isometric.NecromineGame;
import com.gadarts.isometric.services.GameServices;
import com.gadarts.isometric.utils.DefaultGameSettings;
import lombok.Getter;

import java.util.Arrays;

import static com.gadarts.isometric.systems.hud.HudHandler.TABLE_NAME_HUD;


@Getter
public class MenuHandler {
	private static final String TABLE_NAME_MENU = "menu";
	private Table menuTable;

	public void applyMenuOptions(final MenuOptionDefinition[] options,
								 final GameServices services,
								 final Entity player,
								 final InterfaceSystem interfaceSystem) {
		menuTable.clear();
		BitmapFont smallFont = services.getAssetManager().get("chubgothic_40.ttf", BitmapFont.class);
		Label.LabelStyle style = new Label.LabelStyle(smallFont, MenuOption.FONT_COLOR_REGULAR);
		GlobalGameService global = services.getGlobalGameService();
		Arrays.stream(options).forEach(o -> {
			if (o.getValidation().validate(player)) {
				menuTable.add(new MenuOption(o, style, global, interfaceSystem)).row();
			}
		});
	}

	private Label createLogo(final GameServices services) {
		BitmapFont largeFont = services.getAssetManager().get("chubgothic_72.ttf", BitmapFont.class);
		Label.LabelStyle logoStyle = new Label.LabelStyle(largeFont, MenuOption.FONT_COLOR_REGULAR);
		return new Label(NecromineGame.TITLE, logoStyle);
	}


	void addMenuTable(final GameStage stage,
					  final Entity player,
					  final Table table,
					  final GameServices services,
					  final InterfaceSystem interfaceSystem) {
		menuTable = table;
		menuTable.setName(TABLE_NAME_MENU);
		menuTable.add(createLogo(services)).row();
		applyMenuOptions(MainMenuOptions.values(), services, player, interfaceSystem);
		menuTable.toFront();
		toggleMenu(DefaultGameSettings.MENU_ON_STARTUP, stage);
	}

	public void toggleMenu(final boolean active, final GameStage stage) {
		getMenuTable().setVisible(active);
		stage.getRoot().findActor(TABLE_NAME_HUD).setTouchable(active ? Touchable.disabled : Touchable.enabled);
	}
}
