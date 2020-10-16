package com.gadarts.isometric.systems.character;

import com.badlogic.ashley.core.Entity;
import com.gadarts.isometric.utils.SoundPlayer;
import com.gadarts.isometric.utils.map.MapGraph;

public interface ToDoAfterDestinationReached {
	void run(Entity character, MapGraph map, SoundPlayer soundPlayer);
}
