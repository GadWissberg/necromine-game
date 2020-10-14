package com.gadarts.isometric.components;

import lombok.Getter;

@Getter
public class WallComponent implements GameComponent {
	private int topLeftX;
	private int topLeftY;
	private int bottomRightX;
	private int bottomRightY;

	@Override
	public void reset() {

	}

	public void init(final int topLeftX, final int topLeftY, final int bottomRightX, final int bottomRightY) {
		this.topLeftX = topLeftX;
		this.topLeftY = topLeftY;
		this.bottomRightX = bottomRightX;
		this.bottomRightY = bottomRightY;
	}
}
