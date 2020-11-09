package com.gadarts.isometric.systems.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.gadarts.isometric.systems.EventsNotifier;
import com.gadarts.isometric.utils.assets.GameAssetsManager;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StorageWindow extends GameWindow implements EventsNotifier<StorageWindowEventsSubscriber> {
	private final static Color auxColor = new Color();
	private final List<StorageWindowEventsSubscriber> subscribers = new ArrayList<>();
	@Getter
	private ItemDisplay selectedItem;

	public StorageWindow(final String windowNameStorage,
						 final WindowStyle windowStyle,
						 final GameAssetsManager assetsManager,
						 final Map<String, Window> windows) {
		super(windowNameStorage, windowStyle, assetsManager, windows);
	}

	@Override
	public void draw(final Batch batch, final float parentAlpha) {
		super.draw(batch, parentAlpha);
		if (selectedItem != null) {
			drawSelectedItemOnCursor(batch);
		}
	}


	private void drawSelectedItemOnCursor(final Batch batch) {
		Texture image = selectedItem.getItem().getImage();
		float x = Gdx.input.getX(0) - image.getWidth() / 2f;
		float y = getStage().getHeight() - Gdx.input.getY(0) - image.getHeight() / 2f;
		auxColor.set(batch.getColor());
		batch.setColor(auxColor.r, auxColor.g, auxColor.b, 0.5f);
		batch.draw(image, x, y);
		batch.setColor(auxColor.r, auxColor.g, auxColor.b, 1f);
	}

	public void applySelectedItem(final ItemDisplay itemDisplay) {
		if (selectedItem != null) {
			selectedItem.clearActions();
		}
		selectedItem = itemDisplay;
		selectedItem.applyFlickerAction();
		closeButton.setDisabled(true);
		subscribers.forEach(sub -> sub.itemHasBeenSelected(itemDisplay));
	}

	@Override
	public void subscribeForEvents(final StorageWindowEventsSubscriber sub) {
		if (subscribers.contains(sub)) return;
		subscribers.add(sub);
	}

	public void onRightMouseButtonClicked() {
		if (selectedItem != null) {
			closeButton.setDisabled(false);
			selectedItem = null;
			PlayerLayout playerLayout = findActor(PlayerLayout.NAME);
			if (playerLayout != null) {
				playerLayout.getSelectedWeapon().clearActions();
			}
		}
	}
}
