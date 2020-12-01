package com.gadarts.isometric.components;

import com.badlogic.gdx.math.Vector3;

public class LightComponent implements GameComponent {
	private final Vector3 position = new Vector3();
	private float radius;

	@Override
	public void reset() {

	}

	public void init(final float x, final float y, final float z, final float radius) {
		this.position.set(x, y, z);
		this.radius = radius;
	}
}
