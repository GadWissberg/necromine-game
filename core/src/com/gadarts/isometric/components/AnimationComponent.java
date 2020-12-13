package com.gadarts.isometric.components;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.TimeUtils;
import lombok.Getter;
import lombok.Setter;

@Getter
public class AnimationComponent implements GameComponent {

	@Setter
	@Getter
	private boolean doingReverse;

	private CharacterAnimation animation;
	private float stateTime;
	private long lastFrameChange;

	@Override
	public void reset() {
		doingReverse = false;
		stateTime = 0;
	}

	public void init(final float frameDuration, final CharacterAnimation animation) {
		this.animation = animation;
		animation.setFrameDuration(frameDuration);
	}

	public TextureAtlas.AtlasRegion calculateFrame() {
		float frameDuration = animation.getFrameDuration();
		boolean looping = animation.getPlayMode() == Animation.PlayMode.LOOP;
		TextureAtlas.AtlasRegion result = animation.getKeyFrame(stateTime, looping);
		if (TimeUtils.timeSinceMillis(lastFrameChange) >= frameDuration * 1000f) {
			lastFrameChange = TimeUtils.millis();
			stateTime += frameDuration;
		}
		return result;
	}

	public void resetStateTime() {
		stateTime = 0;
		lastFrameChange = TimeUtils.millis();
	}
}
