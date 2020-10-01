package com.gadarts.isometric.systems;

import com.badlogic.ashley.core.Entity;
import com.gadarts.isometric.utils.MapGraph;

public interface ToDoAfterDestinationReached {
	void run(Entity character, MapGraph map);
}
