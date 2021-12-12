package com.gadarts.isometric.components;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.isometric.systems.render.GameFrameBufferCubeMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
public class ShadowLightComponent implements GameComponent {

	@Getter(AccessLevel.NONE)
	private final Vector3 position = new Vector3();
	private final Color color = new Color(Color.WHITE);
	@Setter
	private float intensity;
	@Setter
	private float radius;

	@Setter
	private GameFrameBufferCubeMap ShadowFrameBuffer;
	private Entity parent;

	@Override
	public void reset( ) {
	}

	public void init(final Vector3 position,
					 final float intensity,
					 final float radius,
					 final Entity parent) {
		this.position.set(position);
		this.intensity = intensity;
		this.radius = radius;
		this.parent = parent;
		color.set(Color.WHITE);
	}

	public void setPosition(final Vector3 newPosition) {
		position.set(newPosition);
	}

	public Vector3 getPosition(final Vector3 output) {
		return output.set(position);
	}


	public void applyColor(final Color color) {
		color.set(color);
	}

}
