package com.gadarts.isometric.components.player;

import com.gadarts.isometric.components.GameComponent;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class PlayerComponent implements GameComponent {
	private final List<Item> storage = new ArrayList<>();
	private Weapon selectedWeapon;

	@Override
	public void reset() {
		storage.clear();
	}

	public void init(final Item item) {
		storage.add(item);
	}

	public void init(final Weapon selectedWeapon) {
		this.selectedWeapon = selectedWeapon;
	}
}
