package com.gadarts.isometric.components.player;

import com.gadarts.isometric.components.GameComponent;
import lombok.Getter;

@Getter
public class PlayerComponent implements GameComponent {
	private final PlayerStorage storage = new PlayerStorage();
	private Weapon selectedWeapon;

	@Override
	public void reset() {
		storage.clear();
	}

	public void init(final Weapon selectedWeapon) {
		this.selectedWeapon = selectedWeapon;
	}
}
