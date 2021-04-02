package com.gadarts.isometric.systems.hud;

import com.badlogic.gdx.Gdx;
import com.gadarts.isometric.GlobalGameService;
import com.gadarts.isometric.components.ComponentsMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum MenuOptions {
	CONTINUE("Continue",
			(GlobalGameService globalGameService, HudSystem hudSystem) -> hudSystem.toggleMenu(false),
			player -> !ComponentsMapper.player.get(player).isDisabled()),
	NEW("New Game", (GlobalGameService globalGameService, HudSystem hudSystem) -> globalGameService.startNewGame()),
	LOAD("Load Game"),
	SAVE("Save Game"),
	OPTIONS("Options"),
	INFO("Info"),
	QUIT("Quit", (GlobalGameService globalGameService, HudSystem hudSystem) -> Gdx.app.exit());

	private final String label;
	private final MenuOptionAction action;
	private final MenuOptionValidation validation;

	MenuOptions(final String label) {
		this(label, null, player -> true);
	}

	MenuOptions(final String label, final MenuOptionAction action) {
		this(label, action, player -> true);
	}
}
