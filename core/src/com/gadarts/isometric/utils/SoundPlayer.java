package com.gadarts.isometric.utils;

import com.badlogic.gdx.audio.Music;
import com.gadarts.necromine.assets.Assets;
import com.gadarts.necromine.assets.GameAssetsManager;
import lombok.Getter;
import lombok.Setter;

import static com.badlogic.gdx.math.MathUtils.random;
import static com.badlogic.gdx.math.MathUtils.randomBoolean;

public class SoundPlayer {
	private static final float MELODY_VOLUME = 0.4f;
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
		music.setLooping(true);
		music.play();
	}

	public void stopMusic(final Assets.Melody melody) {
		Music music = assetManager.getMelody(melody);
		music.stop();
	}

	public void playSound(final Assets.Sounds soundDef) {
		playSound(soundDef, 1F);
	}

	public void playSound(final Assets.Sounds soundDef, final float volume) {
		if (!isSfxEnabled()) return;
		String filePath = getRandomSound(soundDef);
		boolean randomPitch = soundDef.isRandomPitch();
		float pitch = 1 + (randomPitch ? (randomBoolean() ? 1 : -1) : 0) * random(-PITCH_OFFSET, PITCH_OFFSET);
		if (!soundDef.isLoop()) {
			assetManager.getSound(filePath).play(volume, pitch, 0);
		} else {
			assetManager.getSound(filePath).loop(volume, 1, 0);
		}
	}

	private String getRandomSound(final Assets.Sounds soundDef) {
		String filePath = soundDef.getFilePath();
		if (soundDef.getFiles().length > 0) {
			filePath = Utils.getRandomRoadSound(soundDef);
		}
		return filePath;
	}

}
