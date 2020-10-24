package com.gadarts.isometric.components;

import lombok.Getter;

@Getter
public class ObstacleComponent implements GameComponent {
	private int x;
	private int y;

	public void init(final int x, final int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public void reset() {

	}
}