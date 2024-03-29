package com.gadarts.isometric.components;

import lombok.Getter;
import lombok.Setter;

@Getter
public class FloorComponent implements GameComponent {
	public static final int AO_MASK_EAST = 0B00000001;
	public static final int AO_MASK_SOUTH_EAST = 0B00000010;
	public static final int AO_MASK_SOUTH = 0B00000100;
	public static final int AO_MASK_SOUTH_WEST = 0B00001000;
	public static final int AO_MASK_WEST = 0B00010000;
	public static final int AO_MASK_NORTH_WEST = 0B00100000;
	public static final int AO_MASK_NORTH = 0B01000000;
	public static final int AO_MASK_NORTH_EAST = 0B10000000;
	@Setter
	private int ambientOcclusion;

	@Override
	public void reset() {

	}
}
