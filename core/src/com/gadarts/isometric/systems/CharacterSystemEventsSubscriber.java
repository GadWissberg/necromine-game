package com.gadarts.isometric.systems;

import com.badlogic.ashley.core.Entity;

public interface CharacterSystemEventsSubscriber {
	void onCommandFinished(Entity character);
}
