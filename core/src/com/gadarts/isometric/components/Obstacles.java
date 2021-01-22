package com.gadarts.isometric.components;

import com.gadarts.necromine.assets.Assets;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Obstacles {
	PILLAR("Pillar", Assets.Models.PILLAR),
	CAVE_SUPPORTER_1("Wall Supporter", Assets.Models.CAVE_SUPPORTER_1, false),
	CAVE_SUPPORTER_2("Wall Supporter", Assets.Models.CAVE_SUPPORTER_2, false),
	CAVE_SUPPORTER_3("Wall Supporter", Assets.Models.CAVE_SUPPORTER_3, false);

	private final String displayName;
	private final Assets.Models model;
	private final boolean blocksPath;

	Obstacles(final String displayName, final Assets.Models model) {
		this(displayName, model, true);
	}
}
