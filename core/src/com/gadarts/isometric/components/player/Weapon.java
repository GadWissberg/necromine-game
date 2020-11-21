package com.gadarts.isometric.components.player;

public class Weapon extends Item {

	@Override
	public boolean isWeapon() {
		return true;
	}

	public boolean isMelee() {
		WeaponsDefinitions definition = (WeaponsDefinitions) getDefinition();
		return definition.isMelee();
	}
}
