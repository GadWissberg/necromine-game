package com.gadarts.isometric.systems.character;

import com.gadarts.isometric.systems.character.actions.MeleeAction;
import com.gadarts.isometric.systems.character.actions.PickUpAction;
import com.gadarts.isometric.systems.character.actions.ShootAction;
import lombok.Getter;

public enum Commands {
	GO_TO,
	GO_TO_MELEE(new MeleeAction()),
	GO_TO_PICKUP(new PickUpAction()),
	SHOOT(new ShootAction(), false);

	private final ToDoAfterDestinationReached toDoAfterDestinationReached;

	@Getter
	private final boolean requiresMovement;

	Commands() {
		this(null);
	}

	Commands(final ToDoAfterDestinationReached toDoAfterDestinationReached) {
		this(toDoAfterDestinationReached, true);
	}

	Commands(final ToDoAfterDestinationReached toDoAfterDestinationReached, final boolean requiresMovement) {
		this.toDoAfterDestinationReached = toDoAfterDestinationReached;
		this.requiresMovement = requiresMovement;
	}

	public ToDoAfterDestinationReached getToDoAfterDestinationReached() {
		return toDoAfterDestinationReached;
	}
}
