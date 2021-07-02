package com.gadarts.isometric.systems.character;

import com.badlogic.ashley.core.Entity;
import com.gadarts.isometric.systems.SystemEventsSubscriber;
import com.gadarts.isometric.systems.character.commands.CharacterCommand;
import com.gadarts.isometric.utils.map.MapGraphNode;

public interface CharacterSystemEventsSubscriber extends SystemEventsSubscriber {
	default void onDestinationReached(final Entity character) {

	}

	default void onCharacterCommandDone(final Entity character, CharacterCommand lastCommand) {

	}

	default void onNewCharacterCommandSet(final CharacterCommand command) {

	}

	default void onCharacterSystemReady(final CharacterSystem characterSystem) {

	}

	default void onCharacterGotDamage(final Entity target) {

	}

	default void onItemPickedUp(final Entity itemPickedUp) {

	}

	default void onCharacterDies(final Entity character) {

	}

	default void onCharacterNodeChanged(final Entity entity, final MapGraphNode oldNode, final MapGraphNode newNode) {

	}
}
