package com.gadarts.isometric.systems.pickup;

import com.badlogic.ashley.core.Entity;
import com.gadarts.isometric.systems.turns.GameSystem;

public interface PickUpSystem extends GameSystem {
    void onItemPickedUp(Entity pickup);
}