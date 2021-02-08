package com.gadarts.isometric.components;

import com.gadarts.necromine.model.EnvironmentDefinitions;
import lombok.Getter;

@Getter
public class ObstacleComponent implements GameComponent {
	private int x;
	private int y;
	private EnvironmentDefinitions definition;

	public void init(final int x, final int y, final EnvironmentDefinitions definition) {
		this.x = x;
		this.y = y;
		this.definition = definition;
	}

	@Override
	public void reset() {

	}
}
