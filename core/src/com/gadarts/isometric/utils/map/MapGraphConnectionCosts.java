package com.gadarts.isometric.utils.map;

public enum MapGraphConnectionCosts {
	CLEAN, HEIGHT_DIFF;

	public float getCostValue() {
		return ordinal() + 1;
	}
}
