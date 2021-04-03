package com.gadarts.isometric.systems.hud;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum NewGameMenuOptions implements MenuOptionDefinition {
	CITY("City", (globalGameService, hudSystem) -> {
		globalGameService.startNewGame("city");
	}),
	COAL_MINE("Coal Mine", (globalGameService, hudSystem) -> {
		globalGameService.startNewGame("coalmine");
	});
	private final String label;
	private final MenuOptionAction action;

	@Override
	public MenuOptionDefinition[] getSubOptions() {
		return null;
	}
}
