package com.gadarts.isometric.components.player;

import com.badlogic.gdx.graphics.Texture;

public class Weapon extends Item {
	public Weapon(final WeaponsDefinitions definition, final int x, final int y, final Texture image) {
		super(definition, x, y, image);
	}

	@Override
	public boolean isWeapon() {
		return true;
	}
}
