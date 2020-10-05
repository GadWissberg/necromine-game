package com.gadarts.isometric.components.character;

import com.badlogic.gdx.graphics.g2d.Animation;
import lombok.Getter;

@Getter
public enum SpriteType {
	IDLE(0.5f),
	RUN(0.15f),
	ATTACK(0.15f, Animation.PlayMode.NORMAL);

	private final float animationDuration;
	private final Animation.PlayMode playMode;

	SpriteType(final float animationDuration) {
		this(animationDuration, Animation.PlayMode.LOOP);
	}

	SpriteType(final float animationDuration, final Animation.PlayMode playMode) {
		this.animationDuration = animationDuration;
		this.playMode = playMode;
	}
}
