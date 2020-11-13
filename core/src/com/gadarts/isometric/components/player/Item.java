package com.gadarts.isometric.components.player;

import com.badlogic.gdx.graphics.Texture;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Item {
	private final ItemDefinition definition;
	private final Texture image;

	@Setter
	private int row;

	@Setter
	private int col;

	public Item(final ItemDefinition definition, final int row, final int col, final Texture image) {
		this.definition = definition;
		this.row = row;
		this.col = col;
		this.image = image;
	}

	public boolean isWeapon() {
		return false;
	}
}
