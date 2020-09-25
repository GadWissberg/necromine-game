package com.gadarts.isometric.utils;

import lombok.Getter;

public final class Assets {
	public enum Atlases {PLAYER}

	@Getter
	public enum CharacterDirectionsRegions {
		SOUTH_IDLE("south_idle"),
		SOUTH_WEST_IDLE("south_west_idle"),
		WEST_IDLE("west_idle"),
		NORTH_WEST_IDLE("north_west_idle"),
		NORTH_IDLE("north_idle"),
		NORTH_EAST_IDLE("north_east_idle"),
		EAST_IDLE("east_idle"),
		SOUTH_EAST_IDLE("south_east_idle");

		private final String regionName;

		CharacterDirectionsRegions(final String regionName) {
			this.regionName = regionName;
		}
	}
}
