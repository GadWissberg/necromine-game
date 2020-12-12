package com.gadarts.isometric.systems.hud.window.storage;

import com.gadarts.isometric.systems.hud.window.storage.item.ItemDisplay;

public interface StorageWindowEventsSubscriber {
	void itemHasBeenSelected(ItemDisplay itemDisplay);
}
