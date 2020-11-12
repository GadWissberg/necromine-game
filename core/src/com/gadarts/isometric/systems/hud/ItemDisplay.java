package com.gadarts.isometric.systems.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.gadarts.isometric.components.player.Item;
import lombok.Getter;

@Getter
public class ItemDisplay extends Image {

	static final float FLICKER_DURATION = 0.2f;
	private final Item item;

	@Override
	public void clearActions() {
		super.clearActions();
		setColor(Color.WHITE);
	}

	public ItemDisplay(final Item item) {
		super(item.getImage());
		this.item = item;
		addListener(new ClickListener() {
			@Override
			public boolean touchDown(final InputEvent event, final float x, final float y, final int pointer, final int button) {
				fire(new GameWindowEvent(ItemDisplay.this, GameWindowEventType.ITEM_SELECTED));
				return super.touchDown(event, x, y, pointer, button);
			}

		});
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

}
