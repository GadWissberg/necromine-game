package com.gadarts.isometric.systems.hud.window.storage;

import com.gadarts.isometric.systems.hud.window.GameWindowEvent;
import com.gadarts.isometric.systems.hud.window.GameWindowEventType;
import com.gadarts.isometric.systems.hud.window.OnEvent;
import com.gadarts.isometric.systems.hud.window.storage.item.ItemDisplay;
import com.gadarts.isometric.systems.hud.window.storage.item.ItemSelectionHandler;
import com.gadarts.isometric.utils.SoundPlayer;
import com.gadarts.necromine.assets.Assets;
import com.gadarts.necromine.model.WeaponsDefinitions;

enum StorageWindowOnEvents {

	ITEM_SELECTED(GameWindowEventType.ITEM_SELECTED, (event, soundPlayer, selectedItem, toBeAppliedOn) -> {
		soundPlayer.playSound(Assets.Sounds.UI_ITEM_SELECT);
		ItemDisplay target = (ItemDisplay) event.getTarget();
		StorageWindow storageWindow = (StorageWindow) toBeAppliedOn;
		if (selectedItem.getSelection() != target) {
			storageWindow.applySelectedItem(target);
		} else {
			storageWindow.clearSelectedItem();
		}
		return false;
	}),

	ITEM_PLACED(GameWindowEventType.ITEM_PLACED, (event, soundPlayer, selectedItem, toBeAppliedOn) -> {
		StorageWindow storageWindow = (StorageWindow) toBeAppliedOn;
		soundPlayer.playSound(Assets.Sounds.UI_ITEM_PLACED);
		if (event.getTarget() instanceof PlayerLayout) {
			storageWindow.findActor(StorageGrid.NAME).notify(event, false);
		} else {
			storageWindow.findActor(PlayerLayout.NAME).notify(event, false);
		}
		storageWindow.clearSelectedItem();
		return true;
	}),

	CLICK_RIGHT(GameWindowEventType.CLICK_RIGHT, (event, soundPlayer, selectedItem, toBeAppliedOn) -> {
		StorageWindow storageWindow = (StorageWindow) toBeAppliedOn;
		return storageWindow.onRightClick();
	}),

	WINDOW_CLOSED(GameWindowEventType.WINDOW_CLOSED, (event, soundPlayer, selectedItem, toBeAppliedOn) -> {
		StorageWindow storageWindow = (StorageWindow) toBeAppliedOn;
		if (event.getTarget() == storageWindow) {
			if (storageWindow.getPlayerLayout().getWeaponChoice() == null) {
				StorageGrid storageGrid = storageWindow.getStorageGrid();
				ItemDisplay itemDisplay = storageGrid.findItemDisplay(WeaponsDefinitions.AXE_PICK.getId());
				storageWindow.getPlayerLayout().applySelectionToSelectedWeapon(storageGrid, itemDisplay);
			}
		}
		return false;
	});

	private final GameWindowEventType type;
	private final OnEvent onEvent;

	StorageWindowOnEvents(final GameWindowEventType type, final OnEvent onEvent) {
		this.type = type;
		this.onEvent = onEvent;
	}

	public static boolean execute(final GameWindowEvent event,
								  final SoundPlayer soundPlayer,
								  final ItemSelectionHandler selectedItem,
								  final StorageWindow storageWindow) {
		StorageWindowOnEvents[] values = values();
		for (StorageWindowOnEvents e : values) {
			if (e.type == event.getType()) {
				return e.onEvent.execute(event, soundPlayer, selectedItem, storageWindow);
			}
		}
		return false;
	}
}
