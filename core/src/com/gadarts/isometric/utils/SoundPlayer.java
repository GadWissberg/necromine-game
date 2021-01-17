package com.gadarts.isometric.utils;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.MathUtils;
import com.gadarts.isometric.utils.assets.GameAssetsManager;
import com.gadarts.necromine.Assets;
import lombok.Getter;
import lombok.Setter;

public class SoundPlayer {
	private static final float MELODY_VOLUME = 0.7f;
	private static final float PITCH_OFFSET = 0.1f;
	private final GameAssetsManager assetManager;

	@Getter
	@Setter
	private boolean sfxEnabled;

	@Getter
	private boolean musicEnabled;

	public SoundPlayer(final GameAssetsManager assetManager) {
		this.assetManager = assetManager;
		setSfxEnabled(DefaultGameSettings.SFX_ENABLED);
		setMusicEnabled(DefaultGameSettings.MELODY_ENABLED);
	}

	public void setMusicEnabled(final boolean musicEnabled) {
		this.musicEnabled = musicEnabled;
		if (musicEnabled) {
			playMusic(Assets.Melody.TEST);
		} else {
			stopMusic(Assets.Melody.TEST);
		}
	}

	public void playMusic(final Assets.Melody melody) {
		if (!isMusicEnabled()) return;
		Music music = assetManager.getMelody(melody);
		music.setVolume(MELODY_VOLUME);
		music.play();
	}

	public void stopMusic(final Assets.Melody melody) {
		Music music = assetManager.getMelody(melody);
		music.stop();
	}

	public void playRandomSound(final Assets.Sounds... sounds) {
		if (!isSfxEnabled()) return;
		int randomSound = MathUtils.random(sounds.length - 1);
		playSound(sounds[randomSound]);
	}

	public void playSound(final Assets.Sounds sound) {
		if (!isSfxEnabled()) return;
		boolean randPitch = sound.isRandomPitch();
		boolean randomBoolean = MathUtils.randomBoolean();
		float pitch = 1 + (randPitch ? (randomBoolean ? 1 : -1) : 0) * MathUtils.random(-PITCH_OFFSET, PITCH_OFFSET);
		assetManager.getSound(sound).play(1f, pitch, 0);
	}

}
