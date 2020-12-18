package com.gadarts.isometric.components;

import com.badlogic.gdx.math.Vector3;
import lombok.AccessLevel;
import lombok.Getter;

@Getter
public class LightComponent implements GameComponent {
	public static final float LIGHT_RADIUS = 8f;

	private float intensity;
	private float radius;

	@Getter(AccessLevel.NONE)
	private final Vector3 position = new Vector3();

	@Override
	public void reset() {

	}

	public void init(final Vector3 position, final float intensity, final float radius) {
		this.position.set(position);
		this.intensity = intensity;
		this.radius = radius;
	}

	public Vector3 getPosition(final Vector3 output) {
		return output.set(position);
	}
}
