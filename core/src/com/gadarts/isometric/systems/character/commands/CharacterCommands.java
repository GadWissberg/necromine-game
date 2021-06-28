package com.gadarts.isometric.systems.character.commands;

import com.gadarts.isometric.systems.character.actions.MeleeAction;
import com.gadarts.isometric.systems.character.actions.PickUpAction;
import com.gadarts.isometric.systems.character.actions.PrimaryAttackAction;
import lombok.Getter;

public enum CharacterCommands {
	GO_TO,
	GO_TO_MELEE(new MeleeAction()),
	GO_TO_PICKUP(new PickUpAction()),
	ATTACK_PRIMARY(new PrimaryAttackAction(), false);

	private final ToDoAfterDestinationReached toDoAfterDestinationReached;

	@Getter
	private final boolean requiresMovement;

	CharacterCommands( ) {
		this(null);
	}

	CharacterCommands(final ToDoAfterDestinationReached toDoAfterDestinationReached) {
		this(toDoAfterDestinationReached, true);
	}

	CharacterCommands(final ToDoAfterDestinationReached toDoAfterDestinationReached, final boolean requiresMovement) {
		this.toDoAfterDestinationReached = toDoAfterDestinationReached;
		this.requiresMovement = requiresMovement;
	}

	public ToDoAfterDestinationReached getToDoAfterDestinationReached() {
		return toDoAfterDestinationReached;
	}
}
