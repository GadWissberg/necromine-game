package com.gadarts.isometric.systems.character.actions;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.character.CharacterComponent;
import com.gadarts.isometric.components.character.CharacterMotivation;
import com.gadarts.isometric.systems.character.commands.ToDoAfterDestinationReached;
import com.gadarts.isometric.utils.SoundPlayer;
import com.gadarts.isometric.utils.map.MapGraph;
import com.gadarts.isometric.utils.map.MapGraphNode;

import java.util.ArrayList;
import java.util.List;

public class MeleeAction implements ToDoAfterDestinationReached {
	private static final List<MapGraphNode> auxNodesList = new ArrayList<>();

	@Override
	public void run(final Entity character,
					final MapGraph map,
					final SoundPlayer soundPlayer,
					final Object additionalData) {
		CharacterComponent characterComponent = ComponentsMapper.character.get(character);
		Entity target = characterComponent.getTarget();
		if (target != null) {
			Vector2 targetPosition = ComponentsMapper.characterDecal.get(target).getNodePosition(auxVector2);
			MapGraphNode targetNode = map.getNode(targetPosition);
			MapGraphNode myNode = map.getNode(ComponentsMapper.characterDecal.get(character).getNodePosition(auxVector2));
			List<MapGraphNode> nearbyNodes = map.getNodesAround(myNode, auxNodesList);
			characterComponent.getRotationData().setRotating(true);
			boolean nearTarget = false;
			for (MapGraphNode nearbyNode : nearbyNodes) {
				nearTarget = nearbyNode.equals(targetNode);
				if (nearTarget) {
					characterComponent.setMotivation(CharacterMotivation.TO_ATTACK);
					break;
				}
			}
			if (!nearTarget) {
				characterComponent.setMotivation(CharacterMotivation.END_MY_TURN);
			}
		}
	}
}
