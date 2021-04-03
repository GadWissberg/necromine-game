package com.gadarts.isometric.systems.hud;

public interface MenuOptionDefinition {
	String getLabel();

	MenuOptionAction getAction();

	MenuOptionValidation getValidation();
}
