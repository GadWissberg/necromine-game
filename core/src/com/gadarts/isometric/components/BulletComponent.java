package com.gadarts.isometric.components;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import lombok.AccessLevel;
import lombok.Getter;

@Getter
public class BulletComponent implements GameComponent {

	@Getter(AccessLevel.NONE)
	private final Vector2 initialPosition = new Vector2();

	@Getter(AccessLevel.NONE)
	private Vector2 direction;

	private Entity owner;
	private Integer damage;

	public Vector2 getDirection(final Vector2 output) {
		return output.set(direction);
	}

	public Vector2 getInitialPosition(final Vector2 output) {
		return output.set(initialPosition);
	}

	@Override
	public void reset( ) {

	}

	public void init(final Vector2 initialPosition,
					 final Vector2 direction,
					 final Entity owner,
					 final Integer damagePoints) {
		this.initialPosition.set(initialPosition);
		this.direction = direction;
		this.owner = owner;
		this.damage = damagePoints;
	}
}
