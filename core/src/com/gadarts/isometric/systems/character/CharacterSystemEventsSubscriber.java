package com.gadarts.isometric.systems.character;

import com.badlogic.ashley.core.Entity;
import com.gadarts.isometric.systems.SystemEventsSubscriber;
import com.gadarts.isometric.utils.map.MapGraphNode;

public interface CharacterSystemEventsSubscriber extends SystemEventsSubscriber {
	void onDestinationReached(Entity character);

	void onCommandDone(Entity character);

	void onNewCommandSet(CharacterCommand command);

	void onCharacterSystemReady(CharacterSystem characterSystem);

	void onCharacterGotDamage(Entity target);

	void onItemPickedUp(Entity itemPickedUp);

	void onCharacterDies(Entity character);

	void onCharacterNodeChanged(Entity entity, MapGraphNode oldNode, MapGraphNode newNode);
}
