package com.gadarts.isometric.components;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import lombok.AccessLevel;
import lombok.Getter;

@Getter
public class BulletComponent implements GameComponent {

	@Getter(AccessLevel.NONE)
	private final Vector2 initialPosition = new Vector2();

	private Entity owner;
	private Vector2 direction;

	public Vector2 getInitialPosition(final Vector2 output) {
		return output.set(initialPosition);
	}

	@Override
	public void reset() {

	}

	public void init(final Vector2 initialPosition, final Vector2 direction, final Entity owner) {
		this.initialPosition.set(initialPosition);
		this.direction = direction;
		this.owner = owner;
	}
}
