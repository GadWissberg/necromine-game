package com.gadarts.isometric.components.character;

import com.badlogic.ashley.core.Entity;
import com.gadarts.isometric.utils.assets.Assets;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CharacterAttackData {
	private Entity target;
	private Assets.Sounds attackSound;

	public void reset() {
		target = null;
		attackSound = null;
	}
}
