package com.gadarts.isometric.systems.character.actions;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.character.CharacterComponent;
import com.gadarts.isometric.components.character.CharacterMode;
import com.gadarts.isometric.systems.character.ToDoAfterDestinationReached;
import com.gadarts.isometric.utils.SoundPlayer;
import com.gadarts.isometric.utils.map.MapGraph;

public class PickUpAction implements ToDoAfterDestinationReached {

	@Override
	public void run(final Entity character,
					final MapGraph map,
					final SoundPlayer soundPlayer,
					final Object itemToPickup) {
		Vector3 charPos = ComponentsMapper.characterDecal.get(character).getCellPosition(auxVector);
		Entity pickup = map.getPickupFromNode(map.getNode(charPos));
		if (pickup != null) {
			CharacterComponent characterComponent = ComponentsMapper.character.get(character);
			characterComponent.getRotationData().setRotating(true);
			characterComponent.setMode(CharacterMode.PICKING_UP, itemToPickup);
		}
	}
}
