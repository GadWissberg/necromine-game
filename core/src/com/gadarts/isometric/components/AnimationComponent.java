package com.gadarts.isometric.components;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import lombok.Getter;

@Getter
public class AnimationComponent implements GameComponent {
	private Animation<TextureAtlas.AtlasRegion> animation;
	private float stateTime;

	@Override
	public void reset() {

	}

	public void init(final float frameDuration, final Animation<TextureAtlas.AtlasRegion> animation) {
		this.animation = animation;
		animation.setFrameDuration(frameDuration);
		animation.setPlayMode(Animation.PlayMode.LOOP);
	}

	public TextureAtlas.AtlasRegion getCurrentFrame(final float deltaTime) {
		stateTime += deltaTime;
		return animation.getKeyFrame(stateTime, true);
	}
}
