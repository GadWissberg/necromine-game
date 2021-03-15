package com.gadarts.isometric.components;

import lombok.Getter;
import lombok.Setter;

@Getter
public class FloorComponent implements GameComponent {
	public static final int AMBIENT_OCCLUSION_MASK_EAST = 0B00000001;
	public static final int AMBIENT_OCCLUSION_MASK_SOUTH = 0B00000010;
	public static final int AMBIENT_OCCLUSION_MASK_WEST = 0B00000100;
	public static final int AMBIENT_OCCLUSION_MASK_NORTH = 0B00001000;
	@Setter
	private int ambientOcclusion;

	@Override
	public void reset() {

	}
}
