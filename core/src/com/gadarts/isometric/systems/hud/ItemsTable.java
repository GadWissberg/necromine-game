package com.gadarts.isometric.systems.hud;

import com.badlogic.gdx.scenes.scene2d.ui.Table;

public abstract class ItemsTable extends Table {
	protected final ItemSelectionHandler itemSelectionHandler;

	public ItemsTable(final ItemSelectionHandler itemSelectionHandler) {
		this.itemSelectionHandler = itemSelectionHandler;
	}

	protected void onRightClick() {
		fire(new GameWindowEvent(this, GameWindowEventType.CLICK_RIGHT));
	}

	protected abstract void removeItem(ItemDisplay item);
}
