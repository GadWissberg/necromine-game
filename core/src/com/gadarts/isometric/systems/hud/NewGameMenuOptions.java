package com.gadarts.isometric.systems.hud;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum NewGameMenuOptions implements MenuOptionDefinition {
	MASTABA("Mastaba - Test Map", (globalGameService, hudSystem) -> {
		globalGameService.startNewGame("mastaba");
	});
	private final String label;
	private final MenuOptionAction action;

	@Override
	public MenuOptionDefinition[] getSubOptions( ) {
		return null;
	}
}
