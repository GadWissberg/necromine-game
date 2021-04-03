package com.gadarts.isometric.systems.hud;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum NewGameMenuOptions implements MenuOptionDefinition {
	COAL_MINE("Coal-Mine", (globalGameService, hudSystem) -> {
		globalGameService.startNewGame("coalmine");
	});
	private final String label;
	private final MenuOptionAction action;


	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public MenuOptionAction getAction() {
		return action;
	}

	@Override
	public MenuOptionValidation getValidation() {
		return null;
	}
}
