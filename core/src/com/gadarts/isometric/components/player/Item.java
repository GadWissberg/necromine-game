package com.gadarts.isometric.components.player;

import com.badlogic.gdx.graphics.Texture;
import lombok.Getter;

@Getter
public class Item {
	private final ItemDefinition definition;
	private final int x;
	private final int y;
	private final Texture image;

	public Item(final ItemDefinition definition, final int x, final int y, final Texture image) {
		this.definition = definition;
		this.x = x;
		this.y = y;
		this.image = image;
	}
}
