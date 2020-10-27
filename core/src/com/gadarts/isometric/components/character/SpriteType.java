package com.gadarts.isometric.components.character;

import com.badlogic.gdx.graphics.g2d.Animation;
import lombok.Getter;

@Getter
public enum SpriteType {
	IDLE(0.5f),
	RUN(0.15f),
	ATTACK(0.15f, Animation.PlayMode.NORMAL),
	PAIN(),
	PICKUP(0.2f, Animation.PlayMode.NORMAL, false, true),
	DIE(0.15f, Animation.PlayMode.NORMAL, true);

	private final float animationDuration;
	private final Animation.PlayMode playMode;
	private final boolean singleAnimation;
	private final boolean addReverse;

	SpriteType() {
		this(0);
	}

	SpriteType(final float animationDuration) {
		this(animationDuration, Animation.PlayMode.LOOP);
	}

	SpriteType(final float animationDuration, final Animation.PlayMode playMode) {
		this(animationDuration, playMode, false, false);
	}

	SpriteType(final float animationDuration,
			   final Animation.PlayMode playMode,
			   final boolean singleAnimation) {
		this(animationDuration, playMode, singleAnimation, false);
	}

	SpriteType(final float animationDuration,
			   final Animation.PlayMode playMode,
			   final boolean singleAnimation,
			   final boolean addReverse) {
		this.animationDuration = animationDuration;
		this.playMode = playMode;
		this.singleAnimation = singleAnimation;
		this.addReverse = addReverse;
	}
}
