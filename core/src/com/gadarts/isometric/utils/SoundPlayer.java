package com.gadarts.isometric.utils;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.MathUtils;
import com.gadarts.isometric.utils.assets.Assets;
import com.gadarts.isometric.utils.assets.GameAssetsManager;

public class SoundPlayer {
	private static final float MELODY_VOLUME = 0.7f;
	private static final float PITCH_OFFSET = 0.1f;
	private final GameAssetsManager assetManager;

	public SoundPlayer(final GameAssetsManager assetManager) {
		this.assetManager = assetManager;
	}

	public void playMusic(final Assets.Melody melody) {
		if (!DefaultGameSettings.MELODY_ENABLED) return;
		Music music = assetManager.getMelody(melody);
		music.setVolume(MELODY_VOLUME);
		music.play();
	}

	public void playRandomSound(final Assets.Sounds... sounds) {
		if (!DefaultGameSettings.SFX_ENABLED) return;
		int randomSound = MathUtils.random(sounds.length - 1);
		playSound(sounds[randomSound]);
	}

	public void playSound(final Assets.Sounds sound) {
		if (!DefaultGameSettings.SFX_ENABLED) return;
		boolean randPitch = sound.isRandomPitch();
		boolean randomBoolean = MathUtils.randomBoolean();
		float pitch = 1 + (randPitch ? (randomBoolean ? 1 : -1) : 0) * MathUtils.random(-PITCH_OFFSET, PITCH_OFFSET);
		assetManager.getSound(sound).play(1f, pitch, 0);
	}
}
