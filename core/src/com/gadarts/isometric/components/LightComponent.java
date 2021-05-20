package com.gadarts.isometric.components;

import com.badlogic.gdx.math.Vector3;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
public class LightComponent implements GameComponent {
	public static final float LIGHT_MAX_RADIUS = 7f;

	@Getter(AccessLevel.NONE)
	private final Vector3 position = new Vector3();

	@Setter
	private float intensity;

	@Setter
	private float radius;

	@Setter
	private long nextFlicker;
	private boolean flicker;
	private float originalRadius;
	private float originalIntensity;

	@Override
	public void reset() {

	}

	public void init(final Vector3 position, final float intensity, final float radius, final boolean flicker) {
		this.position.set(position);
		this.originalIntensity = intensity;
		this.intensity = intensity;
		this.originalRadius = radius;
		this.radius = radius;
		this.flicker = flicker;
	}

	public Vector3 getPosition(final Vector3 output) {
		return output.set(position);
	}
}
