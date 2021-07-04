package com.gadarts.isometric.components;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
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

	@Setter
	private Color color;

	private boolean flicker;
	private float originalRadius;
	private float originalIntensity;
	private Entity parent;

	@Override
	public void reset( ) {

	}

	public void init(final Vector3 position,
					 final float intensity,
					 final float radius,
					 final boolean flicker,
					 final Entity parent) {
		this.position.set(position);
		this.originalIntensity = intensity;
		this.intensity = intensity;
		this.originalRadius = radius;
		this.radius = radius;
		this.flicker = flicker;
		this.parent = parent;
	}

	public void setPosition(final Vector3 newPosition) {
		position.set(newPosition);
	}

	public Vector3 getPosition(final Vector3 output) {
		return output.set(position);
	}
}
