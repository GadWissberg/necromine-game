package com.gadarts.isometric.components;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.TimeUtils;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnemyComponent implements GameComponent {
	public static final int ROAM_SOUND_INTERVAL_MINIMUM = 5000;
	public static final int ROAM_SOUND_INTERVAL_MAXIMUM = 10000;
	private long nextRoamSound;

	public void init() {
		calculateNextRoamSound();
	}

	@Override
	public void reset() {

	}

	public void calculateNextRoamSound() {
		nextRoamSound = TimeUtils.millis() + MathUtils.random(ROAM_SOUND_INTERVAL_MINIMUM, ROAM_SOUND_INTERVAL_MAXIMUM);
	}
}
