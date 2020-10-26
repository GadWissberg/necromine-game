package com.gadarts.isometric.systems.character;

import com.badlogic.gdx.math.Vector3;
import com.gadarts.isometric.systems.character.actions.MeleeAction;
import com.gadarts.isometric.systems.character.actions.PickUpAction;

public enum Commands {
	GO_TO,
	GO_TO_MELEE(new MeleeAction()),
	GO_TO_PICKUP(new PickUpAction());

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
