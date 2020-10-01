package com.gadarts.isometric.systems;

import com.badlogic.gdx.math.Vector3;

public enum Commands {
	GO_TO,
	GO_TO_MELEE(new MeleeAction());

	private final static Vector3 auxVector = new Vector3();
	private final ToDoAfterDestinationReached toDoAfterDestinationReached;

	Commands() {
		this(null);
	}

	Commands(final ToDoAfterDestinationReached toDoAfterDestinationReached) {
		this.toDoAfterDestinationReached = toDoAfterDestinationReached;
	}

	public ToDoAfterDestinationReached getToDoAfterDestinationReached() {
		return toDoAfterDestinationReached;
	}
}
