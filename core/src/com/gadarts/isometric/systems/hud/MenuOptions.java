package com.gadarts.isometric.systems.hud;

import com.badlogic.gdx.Gdx;
import com.gadarts.isometric.GlobalApplicationService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum MenuOptions {
	CONTINUE("Continue", (GlobalApplicationService globalApplicationService, HudSystem hudSystem) -> hudSystem.toggleMenu(false)),
	NEW("New Game", (GlobalApplicationService globalApplicationService, HudSystem hudSystem) -> globalApplicationService.restartGame()),
	LOAD("Load Game"),
	SAVE("Save Game"),
	OPTIONS("Options"),
	INFO("Info"),
	QUIT("Quit", (GlobalApplicationService globalApplicationService, HudSystem hudSystem) -> Gdx.app.exit());

	private final String label;
	private final MenuOptionAction action;

	MenuOptions(final String label) {
		this(label, null);
	}
}
