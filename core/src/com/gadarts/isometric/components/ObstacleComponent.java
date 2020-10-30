package com.gadarts.isometric.components;

import lombok.Getter;

@Getter
public class ObstacleComponent implements GameComponent {
	private int x;
	private int y;
	private boolean blockPath;

	public void init(final int x, final int y, final boolean blockPath) {
		this.x = x;
		this.y = y;
		this.blockPath = blockPath;
	}

	@Override
	public void reset() {

	}
}
