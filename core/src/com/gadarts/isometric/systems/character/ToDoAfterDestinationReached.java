package com.gadarts.isometric.systems.character;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.isometric.systems.pickup.PickUpSystem;
import com.gadarts.isometric.utils.SoundPlayer;
import com.gadarts.isometric.utils.map.MapGraph;

public interface ToDoAfterDestinationReached {
    Vector3 auxVector = new Vector3();

    boolean run(Entity character, MapGraph map, SoundPlayer soundPlayer, PickUpSystem pickUpSystem);
}
