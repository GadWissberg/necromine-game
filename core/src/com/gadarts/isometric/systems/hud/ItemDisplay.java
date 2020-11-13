package com.gadarts.isometric.systems.hud;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.gadarts.isometric.components.player.Item;
import lombok.Getter;

@Getter
public class ItemDisplay extends Image {

	static final float FLICKER_DURATION = 0.2f;
	private final Item item;
	private static final Vector2 auxVector = new Vector2();
	private final ItemSelectionHandler itemSelectionHandler;

	@Override
	public void clearActions() {
		super.clearActions();
		setColor(Color.WHITE);
	}

	public ItemDisplay(final Item item, final ItemSelectionHandler itemSelectionHandler) {
		super(item.getImage());
		this.item = item;
		this.itemSelectionHandler = itemSelectionHandler;
		addListener(new InputListener() {
			@Override
			public boolean touchDown(final InputEvent event, final float x, final float y, final int pointer, final int button) {
				boolean result = false;
				if (event.getKeyCode() == Input.Buttons.LEFT) {
					result = onLeftClick(event, x, y);
				} else if (event.getKeyCode() == Input.Buttons.RIGHT) {
					onRightClick();
					result = true;
				}
				return result;
			}
		});
	}

	private void onRightClick() {
		if (itemSelectionHandler.getSelection() != null) {
			itemSelectionHandler.setSelection(null);
		}
	}

	private boolean onLeftClick(final InputEvent event, final float x, final float y) {
		boolean result = false;
		if (itemSelectionHandler.getSelection() == null) {
			fire(new GameWindowEvent(ItemDisplay.this, GameWindowEventType.ITEM_SELECTED));
			result = true;
		} else {
			passClickToBehind(event, x, y);
		}
		return result;
	}

	private void passClickToBehind(final InputEvent event, final float x, final float y) {
		Vector2 stageCoordinates = localToStageCoordinates(auxVector.set(x, y));
		setTouchable(Touchable.disabled);
		Actor behind = getStage().hit(stageCoordinates.x, stageCoordinates.y, true);
		if (behind != null) {
			behind.notify(event, false);
		}
		setTouchable(Touchable.enabled);
	}

	public void applyFlickerAction() {
		addAction(
				Actions.forever(
						Actions.sequence(
								Actions.color(Color.BLACK, FLICKER_DURATION, Interpolation.smooth2),
								Actions.color(Color.WHITE, FLICKER_DURATION, Interpolation.smooth2)
						)
				)
		);
	}

	public boolean isWeapon() {
		return item.isWeapon();
	}
}
