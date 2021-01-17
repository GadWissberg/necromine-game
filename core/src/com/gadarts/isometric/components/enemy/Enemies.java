package com.gadarts.isometric.components.enemy;

import com.gadarts.necromine.Assets;
import lombok.Getter;

@Getter
public enum Enemies {
	ZEALOT("Zealot", Assets.Sounds.ATTACK_CLAW);

	private final String displayName;
	private final Assets.Sounds attackSound;

	Enemies(final String displayName, final Assets.Sounds attackSound) {
		this.displayName = displayName;
		this.attackSound = attackSound;
	}
}
