package com.gadarts.isometric.components.player;

public interface PlayerStorageEventsSubscriber {
	void itemAddedToStorage(Item item);

	void onSelectedWeaponChanged(Weapon selectedWeapon);
}
