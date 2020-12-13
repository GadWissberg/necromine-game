package com.gadarts.isometric.systems.hud.window;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.gadarts.isometric.systems.hud.window.storage.item.ItemSelectionHandler;
import com.gadarts.isometric.utils.SoundPlayer;

public interface OnEvent {
	boolean execute(Event event, SoundPlayer soundPlayer, ItemSelectionHandler selectedItem, Table target);
}
