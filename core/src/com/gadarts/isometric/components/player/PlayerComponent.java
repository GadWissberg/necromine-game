package com.gadarts.isometric.components.player;

import com.badlogic.ashley.core.Entity;
import com.gadarts.isometric.components.GameComponent;
import com.gadarts.isometric.components.character.CharacterAnimations;
import lombok.Getter;

@Getter
public class PlayerComponent implements GameComponent {
	private final PlayerStorage storage = new PlayerStorage();
	private CharacterAnimations generalAnimations;

	@Override
	public void reset() {
		storage.clear();
	}

	public void init(final Weapon selectedWeapon, final CharacterAnimations general) {
		this.generalAnimations = general;
		storage.setSelectedWeapon(selectedWeapon);
	}

	public void onCollisionWithBullet(final Entity bullet) {

	}
}
