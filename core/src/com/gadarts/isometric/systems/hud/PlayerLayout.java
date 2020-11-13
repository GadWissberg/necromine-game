package com.gadarts.isometric.systems.hud;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.gadarts.isometric.components.player.Weapon;
import lombok.Getter;

public class PlayerLayout extends Table {
	public static final int WEAPON_POSITION_PARENT_X = 100;
	public static final int WEAPON_POSITION_PARENT_Y = 200;
	static final String NAME = "player_layout";
	private final static Vector2 auxVector = new Vector2();
	private static final float SPOT_RADIUS = 25;
	private ItemSelectionHandler itemSelectionHandler;
	@Getter
	private ItemDisplay weaponChoice;

	public PlayerLayout(final Texture texture, final Weapon weaponChoice, final ItemSelectionHandler itemSelectionHandler) {
		this.itemSelectionHandler = itemSelectionHandler;
		this.weaponChoice = new ItemDisplay(weaponChoice, this.itemSelectionHandler);
		setTouchable(Touchable.enabled);
		setName(NAME);
		setBackground(new TextureRegionDrawable(texture));
		addListener(event -> {
			boolean result = false;
			if (event instanceof GameWindowEvent) {
				result = onGameWindowEvent(event);
			}
			return result;
		});
		addListener(new ClickListener() {
			@Override
			public boolean touchDown(final InputEvent event,
									 final float x,
									 final float y,
									 final int pointer,
									 final int button) {
				super.touchDown(event, x, y, pointer, button);
				boolean result = false;
				if (button == Input.Buttons.RIGHT) {
					onRightClick(itemSelectionHandler);
					result = true;
				} else if (button == Input.Buttons.LEFT) {
					result = onLeftClick(x, y);
				}
				return result;
			}

		});
	}

	private boolean onLeftClick(final float x, final float y) {
		Vector2 local = auxVector.set(WEAPON_POSITION_PARENT_X, WEAPON_POSITION_PARENT_Y);
		float distance = parentToLocalCoordinates(local).dst(x, y);
		boolean result = false;
		if (itemSelectionHandler != null && PlayerLayout.this.weaponChoice == null && distance < SPOT_RADIUS) {
			PlayerLayout.this.fire(new GameWindowEvent(PlayerLayout.this, GameWindowEventType.ITEM_PLACED));
			result = true;
		}
		return result;
	}

	private void onRightClick(final ItemSelectionHandler itemSelectionHandler) {
		if (itemSelectionHandler.getSelection() != null) {
			itemSelectionHandler.setSelection(null);
		}
	}

	private boolean onGameWindowEvent(final com.badlogic.gdx.scenes.scene2d.Event event) {
		GameWindowEventType type = ((GameWindowEvent) event).getType();
		boolean result = false;
		if (type == GameWindowEventType.ITEM_PLACED) {
			if (event.getTarget() instanceof PlayerLayout) {
				PlayerLayout.this.weaponChoice = itemSelectionHandler.getSelection();
				placeWeapon();
			} else {
				PlayerLayout.this.weaponChoice = null;
			}
			result = true;
		}
		return result;
	}

	@Override
	public void draw(final Batch batch, final float parentAlpha) {
		super.draw(batch, parentAlpha);
		ItemDisplay selection = itemSelectionHandler.getSelection();
		if (selection != null && selection.isWeapon() && weaponChoice == null) {
			Texture image = selection.getItem().getImage();
			batch.setColor(1f, 1f, 1f, 0.5f);
			float x = WEAPON_POSITION_PARENT_X - image.getWidth() / 2f;
			float y = WEAPON_POSITION_PARENT_Y - image.getHeight() / 2f;
			batch.draw(image, x, y);
			batch.setColor(1f, 1f, 1f, 1f);
		}
	}

	@Override
	protected void setParent(final Group parent) {
		super.setParent(parent);
		getParent().addActor(this.weaponChoice);
		placeWeapon();
	}

	private void placeWeapon() {
		Texture weaponImage = this.weaponChoice.getItem().getImage();
		float weaponX = WEAPON_POSITION_PARENT_X - weaponImage.getWidth() / 2f;
		float weaponY = WEAPON_POSITION_PARENT_Y - weaponImage.getHeight() / 2f;
		this.weaponChoice.setPosition(weaponX, weaponY);
	}


}
