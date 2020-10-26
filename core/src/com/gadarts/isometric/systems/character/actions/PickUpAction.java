package com.gadarts.isometric.systems.character.actions;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.systems.character.ToDoAfterDestinationReached;
import com.gadarts.isometric.systems.pickup.PickUpSystem;
import com.gadarts.isometric.utils.SoundPlayer;
import com.gadarts.isometric.utils.map.MapGraph;

public class PickUpAction implements ToDoAfterDestinationReached {

    @Override
    public boolean run(final Entity character,
                       final MapGraph map,
                       final SoundPlayer soundPlayer,
                       final PickUpSystem pickUpSystem) {
        Vector3 charPos = ComponentsMapper.characterDecal.get(character).getCellPosition(auxVector);
        Entity pickup = map.getPickupFromNode(map.getNode(charPos));
        if (pickup != null) {
            pickUpSystem.onItemPickedUp(pickup);
        }
        return true;
    }
}
