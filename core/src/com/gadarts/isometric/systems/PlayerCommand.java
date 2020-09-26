package com.gadarts.isometric.systems;

import com.badlogic.gdx.math.Vector3;

public class PlayerCommand {
	private Commands type;
	private Vector3 destination = new Vector3();

	public Vector3 getDestination(final Vector3 output) {
		return output.set(destination);
	}

	public void init(final Commands type, final Vector3 destination) {
		this.type = type;
		this.destination.set(destination);
	}
}
