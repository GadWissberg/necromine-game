package com.gadarts.isometric.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector3;
import lombok.Getter;

public class CharacterCommand {
	private Commands type;
	private final Vector3 destination = new Vector3();

	@Getter
	private Entity character;

	public Vector3 getDestination(final Vector3 output) {
		return output.set(destination);
	}

	public void init(final Commands type, final Vector3 destination, final Entity character) {
		this.type = type;
		this.destination.set(destination);
		this.character = character;
	}
}
