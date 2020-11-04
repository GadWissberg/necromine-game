package com.gadarts.isometric.systems.hud;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.gadarts.isometric.components.player.Weapon;
import lombok.Getter;

@Getter
public class WeaponDisplay extends Image {

	private final Weapon weapon;


	public WeaponDisplay(final Weapon weapon) {
		super(weapon.getImage());
		this.weapon = weapon;

	}

}
