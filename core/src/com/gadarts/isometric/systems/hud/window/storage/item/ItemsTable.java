package com.gadarts.isometric.systems.hud.window.storage.item;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.gadarts.isometric.systems.hud.window.GameWindowEvent;
import com.gadarts.isometric.systems.hud.window.GameWindowEventType;

public abstract class ItemsTable extends Table {
	protected final ItemSelectionHandler itemSelectionHandler;

	public ItemsTable(final ItemSelectionHandler itemSelectionHandler) {
		this.itemSelectionHandler = itemSelectionHandler;
	}

	protected void onRightClick() {
		fire(new GameWindowEvent(this, GameWindowEventType.CLICK_RIGHT));
	}

	public abstract void removeItem(ItemDisplay item);
}
