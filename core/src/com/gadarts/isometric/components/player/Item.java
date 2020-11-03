package com.gadarts.isometric.components.player;

import lombok.Getter;

@Getter
public class Item {
	private final ItemsDefinitions definition;
	private final int x;
	private final int y;

	public Item(final ItemsDefinitions definition, final int x, final int y) {
		this.definition = definition;
		this.x = x;
		this.y = y;
	}
}
