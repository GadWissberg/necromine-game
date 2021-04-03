package com.gadarts.isometric.systems.hud;

public interface MenuOptionDefinition {
	String getLabel();

	MenuOptionAction getAction();

	MenuOptionDefinition[] getSubOptions();

	default MenuOptionValidation getValidation() {
		return player -> true;
	}
}
