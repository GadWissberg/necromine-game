package com.gadarts.isometric.utils;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.MathUtils;
import com.gadarts.isometric.utils.assets.Assets;
import com.gadarts.isometric.utils.assets.GameAssetsManager;

public class SoundPlayer {
	private static final float MELODY_VOLUME = 0.7f;
	private static final float PITCH_OFFSET = 0.2f;
	private final GameAssetsManager assetManager;

	public SoundPlayer(final GameAssetsManager assetManager) {
		this.assetManager = assetManager;
	}

	public void playMusic(final Assets.Melody melody) {
		if (DefaultGameSettings.MUTE_MELODY) return;
		Music music = assetManager.getMelody(melody);
		music.setVolume(MELODY_VOLUME);
		music.play();
	}

	public void playRandomSound(final Assets.Sounds... sounds) {
		if (DefaultGameSettings.MUTE_SFX) return;
		int randomSound = MathUtils.random(sounds.length - 1);
		playSound(sounds[randomSound]);
	}

	public void playSound(final Assets.Sounds sound) {
		if (DefaultGameSettings.MUTE_SFX) return;
		assetManager.getSound(sound).play(1f, 1 + (MathUtils.randomBoolean() ? 1 : -1) * PITCH_OFFSET, 0);
	}
}
