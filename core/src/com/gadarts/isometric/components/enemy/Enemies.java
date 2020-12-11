package com.gadarts.isometric.components.enemy;

import com.gadarts.isometric.utils.assets.Assets;
import lombok.Getter;

@Getter
public enum Enemies {
	ZEALOT(Assets.Sounds.ATTACK_CLAW);

	private final Assets.Sounds attackSound;

	Enemies(final Assets.Sounds attackSound) {
		this.attackSound = attackSound;
	}
}
