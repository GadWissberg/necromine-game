package com.gadarts.isometric.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.isometric.components.CharacterComponent;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.SpriteType;
import com.gadarts.isometric.utils.MapGraph;
import com.gadarts.isometric.utils.MapGraphNode;
import com.gadarts.isometric.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class MeleeAction implements ToDoAfterDestinationReached {
	private final static Vector3 auxVector = new Vector3();
	private final static List<MapGraphNode> auxNodesList = new ArrayList<>();

	@Override
	public void run(final Entity character, final MapGraph map) {
		CharacterComponent characterComponent = ComponentsMapper.character.get(character);
		Entity target = characterComponent.getTarget();
		if (target != null) {
			Vector3 targetPosition = ComponentsMapper.decal.get(target).getCellPosition(auxVector);
			MapGraphNode targetNode = map.getNode(targetPosition);
			MapGraphNode myNode = map.getNode(ComponentsMapper.decal.get(character).getCellPosition(auxVector));
			List<MapGraphNode> nearbyNodes = map.getNodesAround(myNode, auxNodesList);
			for (MapGraphNode nearbyNode : nearbyNodes) {
				if (nearbyNode.equals(targetNode)) {
					CharacterComponent.Direction direction = Utils.getDirectionBetweenNodes(myNode, targetNode);
					characterComponent.setDirection(direction);
					characterComponent.setSpriteType(SpriteType.ATTACK);
				}
			}
		}
	}
}