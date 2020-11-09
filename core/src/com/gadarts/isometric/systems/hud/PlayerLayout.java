package com.gadarts.isometric.systems.hud;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.gadarts.isometric.components.player.Weapon;
import lombok.Getter;

public class PlayerLayout extends Table {
	public static final int WEAPON_POSITION_X = 38;
	public static final int WEAPON_POSITION_Y = 152;
	static final String NAME = "player_layout";

	@Getter
	private final ItemDisplay selectedWeapon;

	public PlayerLayout(final Texture texture, final Weapon selectedWeapon, final StorageWindow window) {
		setName(NAME);
		setBackground(new TextureRegionDrawable(texture));
		this.selectedWeapon = new ItemDisplay(selectedWeapon, window);
		addActor(this.selectedWeapon);
		Texture weaponImage = this.selectedWeapon.getItem().getImage();
		float weaponX = getX() + WEAPON_POSITION_X - weaponImage.getWidth() / 2f;
		float weaponY = getY() + WEAPON_POSITION_Y - weaponImage.getHeight() / 2f;
		this.selectedWeapon.setPosition(weaponX, weaponY);
	}


}
