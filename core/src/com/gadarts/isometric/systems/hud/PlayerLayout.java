package com.gadarts.isometric.systems.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.gadarts.isometric.components.player.Weapon;

public class PlayerLayout extends Table {
	public static final int WEAPON_POSITION_X = 38;
	public static final int WEAPON_POSITION_Y = 152;
	private static final float FLICKER_DURATION = 0.2f;
	private final WeaponDisplay weaponDisplay;

	public PlayerLayout(final Texture texture, final Weapon selectedWeapon) {
		setBackground(new TextureRegionDrawable(texture));
		this.weaponDisplay = new WeaponDisplay(selectedWeapon);
		weaponDisplay.addListener(new InputListener() {
			@Override
			public void enter(final InputEvent event, final float x, final float y, final int pointer, final Actor fromActor) {
				super.enter(event, x, y, pointer, fromActor);
				weaponDisplay.clearActions();
				weaponDisplay.addAction(
						Actions.forever(
								Actions.sequence(
										Actions.color(Color.BLACK, FLICKER_DURATION, Interpolation.smooth2),
										Actions.color(Color.WHITE, FLICKER_DURATION, Interpolation.smooth2)
								)
						)
				);
			}

			@Override
			public void exit(final InputEvent event, final float x, final float y, final int pointer, final Actor toActor) {
				super.exit(event, x, y, pointer, toActor);
				weaponDisplay.clearActions();
				weaponDisplay.addAction(Actions.color(Color.WHITE, FLICKER_DURATION, Interpolation.smooth2));
			}

		});
		addActor(this.weaponDisplay);
		Texture weaponImage = weaponDisplay.getWeapon().getImage();
		float weaponX = getX() + WEAPON_POSITION_X - weaponImage.getWidth() / 2f;
		float weaponY = getY() + WEAPON_POSITION_Y - weaponImage.getHeight() / 2f;
		weaponDisplay.setPosition(weaponX, weaponY);
	}


}
