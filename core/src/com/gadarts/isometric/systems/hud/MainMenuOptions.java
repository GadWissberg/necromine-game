package com.gadarts.isometric.systems.hud;

import com.badlogic.gdx.Gdx;
import com.gadarts.isometric.GlobalGameService;
import com.gadarts.isometric.components.ComponentsMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum MainMenuOptions implements MenuOptionDefinition {
	CONTINUE("Continue",
			(GlobalGameService globalGameService, UserInterfaceSystem userInterfaceSystem) -> userInterfaceSystem.toggleMenu(false),
			player -> !ComponentsMapper.player.get(player).isDisabled()),
	NEW("New Game", NewGameMenuOptions.values()),
	LOAD("Load Game"),
	SAVE("Save Game"),
	OPTIONS("Options"),
	INFO("Info"),
	QUIT("Quit", (GlobalGameService globalGameService, UserInterfaceSystem userInterfaceSystem) -> Gdx.app.exit());

	private final String label;
	private final MenuOptionAction action;
	private final MenuOptionValidation validation;
	private final MenuOptionDefinition[] subOptions;

	MainMenuOptions(final String label) {
		this(label, null, player -> true);
	}

	MainMenuOptions(final String label, final MenuOptionAction action) {
		this(label, action, player -> true);
	}

	MainMenuOptions(final String label, final MenuOptionAction action, final MenuOptionValidation validation) {
		this(label, action, validation, null);
	}

	MainMenuOptions(final String label, final NewGameMenuOptions[] subOptions) {
		this(label, null, player -> true, subOptions);
	}
}
