package com.gadarts.isometric.utils.map;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.gadarts.isometric.components.*;
import com.gadarts.isometric.components.model.GameModelInstance;
import com.gadarts.isometric.systems.character.CharacterSystem;
import com.gadarts.isometric.systems.character.CharacterSystemEventsSubscriber;
import com.gadarts.isometric.systems.character.commands.CharacterCommand;
import com.gadarts.isometric.utils.Utils;
import com.gadarts.necromine.model.EnvironmentDefinitions;
import com.gadarts.necromine.model.MapNodesTypes;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class MapGraph implements IndexedGraph<MapGraphNode>, CharacterSystemEventsSubscriber {
	public static final int MAP_SIZE = 20;
	private static final Array<Connection<MapGraphNode>> auxConnectionsList = new Array<>();
	private static final Vector3 auxVector3 = new Vector3();
	private static final Vector2 auxVector2 = new Vector2();
	private static final List<MapGraphNode> auxNodesList_1 = new ArrayList<>();
	private static final List<MapGraphNode> auxNodesList_2 = new ArrayList<>();
	private static final float PASSABLE_MAX_HEIGHT_DIFF = 0.3f;

	private final Array<MapGraphNode> nodes;
	private final ImmutableArray<Entity> characterEntities;
	private final ImmutableArray<Entity> pickupEntities;
	private final ImmutableArray<Entity> obstacleEntities;

	@Getter
	private final int[][] fowMap;

	@Setter(AccessLevel.PACKAGE)
	@Getter(AccessLevel.PACKAGE)
	MapGraphNode currentDestination;

	public MapGraph(final ImmutableArray<Entity> characterEntities,
					final ImmutableArray<Entity> wallEntities,
					final ImmutableArray<Entity> obstacleEntities,
					final PooledEngine engine) {
		this.pickupEntities = engine.getEntitiesFor(Family.all(PickUpComponent.class).get());
		this.obstacleEntities = engine.getEntitiesFor(Family.all(ObstacleComponent.class).get());
		this.characterEntities = characterEntities;
		this.nodes = new Array<>(MAP_SIZE * MAP_SIZE);
		this.fowMap = new int[MAP_SIZE][MAP_SIZE];
		int[][] map = new int[MAP_SIZE][MAP_SIZE];
		wallEntities.forEach(wall -> {
			WallComponent wallComponent = ComponentsMapper.wall.get(wall);
			int topLeftX = wallComponent.getTopLeftX();
			int topLeftY = wallComponent.getTopLeftY();
			int bottomRightX = wallComponent.getBottomRightX();
			int bottomRightY = wallComponent.getBottomRightY();
			if (topLeftX >= 0 && topLeftY >= 0 && bottomRightX >= 0 && bottomRightY >= 0) {
				for (int x = topLeftX; x <= bottomRightX; x++) {
					for (int y = topLeftY; y <= bottomRightY; y++) {
						map[y][x] = MapNodesTypes.OBSTACLE_KEY_DIAGONAL_FORBIDDEN.ordinal();
					}
				}
			}
		});
		obstacleEntities.forEach(obstacle -> {
			ObstacleComponent obstacleComponent = ComponentsMapper.obstacle.get(obstacle);
			EnvironmentDefinitions definition = obstacleComponent.getDefinition();
			MapNodesTypes nodeType = definition.getNodeType();
			int current = map[obstacleComponent.getY()][obstacleComponent.getX()];
			map[obstacleComponent.getY()][obstacleComponent.getX()] = Math.max(nodeType.ordinal(), current);
		});
		for (int x = 0; x < MAP_SIZE; x++) {
			for (int y = 0; y < MAP_SIZE; y++) {
				nodes.add(new MapGraphNode(x, y, map[y][x], 8));
			}
		}
		ImmutableArray<Entity> floorEntities = engine.getEntitiesFor(Family.all(FloorComponent.class).get());
		floorEntities.forEach(entity -> {
			GameModelInstance modelInstance = ComponentsMapper.modelInstance.get(entity).getModelInstance();
			Vector3 pos = modelInstance.transform.getTranslation(auxVector3);
			getNode(pos).setEntity(entity);
		});
	}

	void applyConnections() {
		for (int x = 0; x < MAP_SIZE; x++) {
			int idx = x * MAP_SIZE;
			for (int y = 0; y < MAP_SIZE; y++) {
				MapGraphNode n = nodes.get(idx + y);
				if (x > 0) addConnection(n, -1, 0);
				if (x > 0 && y < MAP_SIZE - 1) addConnection(n, -1, 1);
				if (x > 0 && y > 0) addConnection(n, -1, -1);
				if (y > 0) addConnection(n, 0, -1);
				if (y > 0 && x < MAP_SIZE - 1) addConnection(n, 1, -1);
				if (x < MAP_SIZE - 1) addConnection(n, 1, 0);
				if (x < MAP_SIZE - 1 && y < MAP_SIZE - 1) addConnection(n, 1, 1);
				if (y < MAP_SIZE - 1) addConnection(n, 0, 1);
			}
		}
	}

	public List<MapGraphNode> getAvailableNodesAroundNode(final ImmutableArray<Entity> enemiesEntities,
														  final MapGraphNode node) {
		auxNodesList_1.clear();
		auxNodesList_2.clear();
		List<MapGraphNode> nodesAround = getNodesAround(node, auxNodesList_1);
		List<MapGraphNode> availableNodes = auxNodesList_2;
		for (MapGraphNode nearbyNode : nodesAround) {
			boolean isRevealed = fowMap[nearbyNode.getRow()][nearbyNode.getCol()] != 0;
			if (nearbyNode.getType() == 0 && getAliveEnemyFromNode(enemiesEntities, nearbyNode) == null && isRevealed) {
				availableNodes.add(nearbyNode);
			}
		}
		return availableNodes;
	}

	public Entity getAliveEnemyFromNode(final ImmutableArray<Entity> enemiesEntities, final MapGraphNode node) {
		Entity result = null;
		for (Entity enemy : enemiesEntities) {
			MapGraphNode enemyNode = getNode(ComponentsMapper.characterDecal.get(enemy).getDecal().getPosition());
			if (ComponentsMapper.character.get(enemy).getHealthData().getHp() > 0 && enemyNode.equals(node)) {
				result = enemy;
				break;
			}
		}
		return result;
	}

	public MapGraphNode getRayNode(final int screenX, final int screenY, final Camera camera) {
		Vector3 output = Utils.calculateGridPositionFromMouse(camera, screenX, screenY, auxVector3);
		output.set(Math.max(output.x, 0), Math.max(output.y, 0), Math.max(output.z, 0));
		return getNode(output);
	}

	public MapGraphNode getNode(final int col, final int row) {
		return nodes.get(Math.min(col, MAP_SIZE - 1) * MAP_SIZE + Math.min(row, MAP_SIZE - 1));
	}

	private void addConnection(final MapGraphNode source, final int xOffset, final int yOffset) {
		MapGraphNode target = getNode(source.getCol() + xOffset, source.getRow() + yOffset);
		float heightDiff = Math.abs(source.getHeight() - target.getHeight());
		int ordinal = MapNodesTypes.PASSABLE_NODE.ordinal();
		if (target.getType() == ordinal && heightDiff <= PASSABLE_MAX_HEIGHT_DIFF && isDiagonalPossible(source, target)) {
			source.getConnections().add(new MapGraphConnection<>(source, target));
		}
	}

	private boolean isDiagonalPossible(final MapGraphNode source, final MapGraphNode target) {
		if (source.getCol() != target.getCol() && source.getRow() != target.getRow()) {
			if (source.getCol() < target.getCol()) {
				if (isDiagonalBlockedWithEastOrWest(source, source.getCol() + 1)) {
					return false;
				}
			} else {
				if (isDiagonalBlockedWithEastOrWest(source, source.getCol() - 1)) {
					return false;
				}
			}
			return !isDiagonalBlockedWithNorthAndSouth(target, source.getCol(), source.getRow(), source.getHeight());
		}
		return true;
	}

	private boolean isDiagonalBlockedWithEastOrWest(final MapGraphNode source, final int col) {
		float east = getNode(col, source.getRow()).getHeight();
		return Math.abs(source.getHeight() - east) > PASSABLE_MAX_HEIGHT_DIFF;
	}

	private boolean isDiagonalBlockedWithNorthAndSouth(final MapGraphNode target,
													   final int srcX,
													   final int srcY,
													   final float srcHeight) {
		if (srcY < target.getRow()) {
			float bottom = getNode(srcX, srcY + 1).getHeight();
			return Math.abs(srcHeight - bottom) > PASSABLE_MAX_HEIGHT_DIFF;
		} else {
			float top = getNode(srcX, srcY - 1).getHeight();
			return Math.abs(srcHeight - top) > PASSABLE_MAX_HEIGHT_DIFF;
		}
	}

	@Override
	public int getIndex(final MapGraphNode node) {
		return node.getIndex();
	}

	@Override
	public int getNodeCount() {
		return nodes.size;
	}

	@Override
	public Array<Connection<MapGraphNode>> getConnections(final MapGraphNode fromNode) {
		auxConnectionsList.clear();
		Array<Connection<MapGraphNode>> connections = fromNode.getConnections();
		for (Connection<MapGraphNode> connection : connections) {
			boolean available;
			available = checkIfNodeIsAvailable(connection);
			if (available && checkIfConnectionPassable(connection)) {
				auxConnectionsList.add(connection);
			}
		}
		return auxConnectionsList;
	}

	private boolean checkIfConnectionPassable(final Connection<MapGraphNode> con) {
		MapGraphNode fromNode = con.getFromNode();
		MapGraphNode toNode = con.getToNode();
		boolean result = fromNode.getType() == 0 && toNode.getType() == 0;
		result &= Math.abs(fromNode.getCol() - toNode.getCol()) < 2 && Math.abs(fromNode.getRow() - toNode.getRow()) < 2;
		if ((fromNode.getCol() != toNode.getCol()) && (fromNode.getRow() != toNode.getRow())) {
			result &= getNode(fromNode.getCol(), toNode.getRow()).getType() != MapGraphNode.BLOCK_DIAGONAL;
			result &= getNode(toNode.getCol(), fromNode.getRow()).getType() != MapGraphNode.BLOCK_DIAGONAL;
		}
		return result;
	}

	private boolean checkIfNodeIsAvailable(final Connection<MapGraphNode> connection) {
		for (Entity character : characterEntities) {
			MapGraphNode node = getNode(ComponentsMapper.characterDecal.get(character).getNodePosition(auxVector2));
			if (currentDestination == node || ComponentsMapper.character.get(character).getHealthData().getHp() <= 0) {
				continue;
			}
			if (node.equals(connection.getToNode())) {
				return false;
			}
		}
		return true;
	}

	public List<MapGraphNode> getNodesAround(final MapGraphNode node, final List<MapGraphNode> output) {
		output.clear();
		getThreeBehind(node, output);
		getThreeInFront(node, output);
		if (node.getCol() > 0) {
			output.add(getNode(node.getCol() - 1, node.getRow()));
		}
		if (node.getCol() < MAP_SIZE - 1) {
			output.add(getNode(node.getCol() + 1, node.getRow()));
		}
		return output;
	}

	private void getThreeBehind(final MapGraphNode node, final List<MapGraphNode> output) {
		int x = node.getCol();
		int y = node.getRow();
		if (y > 0) {
			if (x > 0) {
				output.add(getNode(x - 1, y - 1));
			}
			output.add(getNode(x, y - 1));
			if (x < MAP_SIZE - 1) {
				output.add(getNode(x + 1, y - 1));
			}
		}
	}

	private void getThreeInFront(final MapGraphNode node, final List<MapGraphNode> output) {
		int x = node.getCol();
		int y = node.getRow();
		if (y < MAP_SIZE - 1) {
			if (x > 0) {
				output.add(getNode(x - 1, y + 1));
			}
			output.add(getNode(x, y + 1));
			if (x < MAP_SIZE - 1) {
				output.add(getNode(x + 1, y + 1));
			}
		}
	}

	public MapGraphNode getNode(final Vector3 position) {
		return getNode((int) position.x, (int) position.z);
	}

	public MapGraphNode getNode(final Vector2 position) {
		return getNode((int) position.x, (int) position.y);
	}

	@Override
	public void onDestinationReached(final Entity character) {

	}

	@Override
	public void onCharacterCommandDone(final Entity character) {
		currentDestination = null;
	}

	@Override
	public void onNewCharacterCommandSet(final CharacterCommand command) {
		MapGraphPath path = command.getPath();
		currentDestination = path.get(path.getCount() - 1);
	}

	@Override
	public void onCharacterSystemReady(final CharacterSystem characterSystem) {

	}

	@Override
	public void onCharacterGotDamage(final Entity target) {

	}

	@Override
	public void onItemPickedUp(final Entity itemPickedUp) {

	}

	@Override
	public void onCharacterDies(final Entity character) {

	}

	@Override
	public void onCharacterNodeChanged(final Entity entity, final MapGraphNode oldNode, final MapGraphNode newNode) {

	}

	public Entity getPickupFromNode(final MapGraphNode node) {
		Entity result = null;
		for (Entity pickup : pickupEntities) {
			ModelInstance modelInstance = ComponentsMapper.modelInstance.get(pickup).getModelInstance();
			MapGraphNode pickupNode = getNode(modelInstance.transform.getTranslation(auxVector3));
			if (pickupNode.equals(node)) {
				result = pickup;
				break;
			}
		}
		return result;
	}

	public Entity getObstacleFromNode(final MapGraphNode node) {
		Entity result = null;
		for (Entity obstacle : obstacleEntities) {
			ModelInstance modelInstance = ComponentsMapper.modelInstance.get(obstacle).getModelInstance();
			MapGraphNode pickupNode = getNode(modelInstance.transform.getTranslation(auxVector3));
			if (pickupNode.equals(node)) {
				result = obstacle;
				break;
			}
		}
		return result;
	}


}
