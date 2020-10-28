package com.gadarts.isometric.components;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PickUpComponent implements GameComponent {
	public static final float FLICKER_DELTA = 0.01f;
	private float flicker = FLICKER_DELTA;

	@Override
	public void reset() {
		flicker = FLICKER_DELTA;
	}
}
