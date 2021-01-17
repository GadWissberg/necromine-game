package com.gadarts.isometric.components.character;

import com.gadarts.necromine.Assets;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CharacterSoundData {
	private Assets.Sounds painSound;
	private Assets.Sounds deathSound;

	public void set(final CharacterSoundData soundData) {
		set(soundData.getPainSound(), soundData.getDeathSound());
	}

	public void set(final Assets.Sounds painSound, final Assets.Sounds deathSound) {
		this.painSound = painSound;
		this.deathSound = deathSound;
	}
}
