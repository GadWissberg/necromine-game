package com.gadarts.isometric.systems.character;

import com.badlogic.ashley.core.Entity;
import com.gadarts.isometric.systems.SystemEventsSubscriber;

public interface CharacterSystemEventsSubscriber extends SystemEventsSubscriber {
	void onDestinationReached(Entity character);

	void onCommandDone(Entity character);

	void onNewCommandSet(CharacterCommand command);

	void onCharacterSystemReady(CharacterSystem characterSystem);
}
