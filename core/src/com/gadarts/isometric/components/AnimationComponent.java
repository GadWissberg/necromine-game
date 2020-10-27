package com.gadarts.isometric.components;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import lombok.Getter;
import lombok.Setter;

@Getter
public class AnimationComponent implements GameComponent {
	private CharacterAnimation animation;
	private float stateTime;

	@Setter
	@Getter
	private boolean doReverse;

	@Override
	public void reset() {
		doReverse = false;
		stateTime = 0;
	}

	public void init(final float frameDuration, final CharacterAnimation animation) {
		this.animation = animation;
		animation.setFrameDuration(frameDuration);
	}

	public TextureAtlas.AtlasRegion calculateFrame(final float deltaTime) {
		stateTime += deltaTime;
		return animation.getKeyFrame(stateTime, animation.getPlayMode() == Animation.PlayMode.LOOP);
	}

	public void resetStateTime() {
		stateTime = 0;
	}
}
