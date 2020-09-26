package com.gadarts.isometric.components;

import lombok.Getter;

public enum SpriteType {
	IDLE(0.5f),
	RUN(0.2f);
	@Getter
	private final float animationDuration;

	SpriteType(final float animationDuration) {
		this.animationDuration = animationDuration;
	}
}
