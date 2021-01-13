package com.gadarts.isometric.systems.character.commands;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.gadarts.isometric.utils.SoundPlayer;
import com.gadarts.isometric.utils.map.MapGraph;

public interface ToDoAfterDestinationReached {
    Vector2 auxVector2 = new Vector2();

    void run(Entity character, MapGraph map, SoundPlayer soundPlayer, Object additionalData);
}
