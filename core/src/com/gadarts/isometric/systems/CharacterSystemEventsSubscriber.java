package com.gadarts.isometric.systems;

import com.badlogic.ashley.core.Entity;

public interface CharacterSystemEventsSubscriber {
	void onDestinationReached(Entity character);

	void onCommandDone(Entity character);

	void onNewCommandSet(CharacterCommand command);
}
