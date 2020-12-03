package com.gadarts.isometric.components;

import com.badlogic.gdx.math.Vector3;
import lombok.AccessLevel;
import lombok.Getter;

@Getter
public class LightComponent implements GameComponent {
	public static final float LIGHT_RADIUS = 7f;
	@Getter(AccessLevel.NONE)
	private final Vector3 position = new Vector3();

	private float intensity;

	@Override
	public void reset() {

	}

	public void init(final Vector3 position, final float intensity) {
		this.position.set(position);
		this.intensity = intensity;
	}

	public Vector3 getPosition(final Vector3 output) {
		return output.set(position);
	}
}
