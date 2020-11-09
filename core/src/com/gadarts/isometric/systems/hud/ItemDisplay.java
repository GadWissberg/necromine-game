package com.gadarts.isometric.systems.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.gadarts.isometric.components.player.Item;
import com.gadarts.isometric.components.player.Weapon;
import lombok.Getter;

@Getter
public class ItemDisplay extends Image {

	private static final float FLICKER_DURATION = 0.2f;
	private final Item item;

	@Override
	public void clearActions() {
		super.clearActions();
		setColor(Color.WHITE);
	}

	public ItemDisplay(final Weapon item, final StorageWindow window) {
		super(item.getImage());
		this.item = item;
		addListener(new ClickListener() {
			@Override
			public void clicked(final InputEvent event, final float x, final float y) {
				super.clicked(event, x, y);
				window.applySelectedItem(ItemDisplay.this);
			}
		});
		addListener(new InputListener() {
			@Override
			public void enter(final InputEvent event, final float x, final float y, final int pointer, final Actor fromActor) {
				super.enter(event, x, y, pointer, fromActor);
				if (window.getSelectedItem() == null) {
					clearActions();
					applyFlickerAction();
				}
			}

			@Override
			public void exit(final InputEvent event, final float x, final float y, final int pointer, final Actor toActor) {
				super.exit(event, x, y, pointer, toActor);
				if (window.getSelectedItem() == null) {
					clearActions();
					addAction(Actions.color(Color.WHITE, FLICKER_DURATION, Interpolation.smooth2));
				}
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
