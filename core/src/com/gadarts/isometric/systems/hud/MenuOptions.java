package com.gadarts.isometric.systems.hud;

import com.badlogic.gdx.Gdx;
import com.gadarts.isometric.GlobalApplicationService;
import com.gadarts.isometric.components.ComponentsMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum MenuOptions {
	CONTINUE("Continue",
			(GlobalApplicationService globalApplicationService, HudSystem hudSystem) -> hudSystem.toggleMenu(false),
			player -> !ComponentsMapper.player.get(player).isDisabled()),
	NEW("New Game", (GlobalApplicationService globalApplicationService, HudSystem hudSystem) -> globalApplicationService.restartGame()),
	LOAD("Load Game"),
	SAVE("Save Game"),
	OPTIONS("Options"),
	INFO("Info"),
	QUIT("Quit", (GlobalApplicationService globalApplicationService, HudSystem hudSystem) -> Gdx.app.exit());

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
