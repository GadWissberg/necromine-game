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
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.FloorComponent;
import com.gadarts.isometric.components.ObstacleComponent;
import com.gadarts.isometric.components.model.GameModelInstance;
import com.gadarts.isometric.systems.character.CharacterSystemEventsSubscriber;
import com.gadarts.isometric.systems.character.commands.CharacterCommand;
import com.gadarts.isometric.utils.Utils;
import com.gadarts.necromine.model.Coords;
import com.gadarts.necromine.model.map.MapNodesTypes;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class MapGraph implements IndexedGraph<MapGraphNode>, CharacterSystemEventsSubscriber {
	private static final Array<Connection<MapGraphNode>> auxConnectionsList = new Array<>();
	private static final Vector3 auxVector3 = new Vector3();
	private static final Vector2 auxVector2 = new Vector2();
	private static final List<MapGraphNode> auxNodesList_1 = new ArrayList<>();
	private static final List<MapGraphNode> auxNodesList_2 = new ArrayList<>();
	private static final float PASSABLE_MAX_HEIGHT_DIFF = 0.3f;

	@Setter
	private boolean includeEnemiesInGetConnections = true;
	@Getter
	private final Array<MapGraphNode> nodes;

	@Getter
	private final int[][] fowMap;

	@Getter
	private final float ambient;

	private final MapGraphRelatedEntities relatedEntities;

	private final Dimension mapSize;

	@Setter(AccessLevel.PACKAGE)
	@Getter(AccessLevel.PACKAGE)
	MapGraphNode currentDestination;

	@Setter
	private MapGraphConnectionCosts maxConnectionCostInSearch;


	public MapGraph(final float ambient, final Dimension mapSize, final PooledEngine engine) {
		this.mapSize = mapSize;
		this.ambient = ambient;
		this.relatedEntities = new MapGraphRelatedEntities(engine);
		this.nodes = new Array<>(mapSize.width * mapSize.height);
		this.fowMap = new int[mapSize.height][mapSize.width];
		IntStream.range(0, mapSize.height).forEach(row -> IntStream.range(0, mapSize.width).forEach(col -> fowMap[row][col] = 1));
		for (int row = 0; row < mapSize.height; row++) {
			for (int col = 0; col < mapSize.width; col++) {
				nodes.add(new MapGraphNode(col, row, MapNodesTypes.values()[MapNodesTypes.PASSABLE_NODE.ordinal()], 8));
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
		for (int row = 0; row < mapSize.height; row++) {
			int rows = row * mapSize.width;
			for (int col = 0; col < mapSize.width; col++) {
				MapGraphNode n = nodes.get(rows + col);
				if (col > 0) addConnection(n, -1, 0);
				if (col > 0 && row < mapSize.height - 1) addConnection(n, -1, 1);
				if (col > 0 && row > 0) addConnection(n, -1, -1);
				if (row > 0) addConnection(n, 0, -1);
				if (row > 0 && col < mapSize.width - 1) addConnection(n, 1, -1);
				if (col < mapSize.width - 1) addConnection(n, 1, 0);
				if (col < mapSize.width - 1 && row < mapSize.height - 1) addConnection(n, 1, 1);
				if (row < mapSize.height - 1) addConnection(n, 0, 1);
			}
		}
	}

	public List<MapGraphNode> getAvailableNodesAroundNode(final MapGraphNode node) {
		auxNodesList_1.clear();
		auxNodesList_2.clear();
		List<MapGraphNode> nodesAround = getNodesAround(node, auxNodesList_1);
		List<MapGraphNode> availableNodes = auxNodesList_2;
		for (MapGraphNode nearbyNode : nodesAround) {
			boolean isRevealed = fowMap[nearbyNode.getRow()][nearbyNode.getCol()] != 0;
			if (nearbyNode.getType() == MapNodesTypes.PASSABLE_NODE && getAliveEnemyFromNode(nearbyNode) == null && isRevealed) {
				availableNodes.add(nearbyNode);
			}
		}
		return availableNodes;
	}

	public Entity getAliveEnemyFromNode(final MapGraphNode node) {
		Entity result = null;
		for (Entity enemy : relatedEntities.getEnemiesEntities()) {
			MapGraphNode enemyNode = getNode(ComponentsMapper.characterDecal.get(enemy).getDecal().getPosition());
			if (ComponentsMapper.character.get(enemy).getSkills().getHealthData().getHp() > 0 && enemyNode.equals(node)) {
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
		int index = Math.max(Math.min(row, mapSize.height) * mapSize.width + Math.min(col, mapSize.width), 0);
		MapGraphNode result = null;
		if (index < getWidth() * getDepth()) {
			result = nodes.get(index);
		}
		return result;
	}

	private void addConnection(final MapGraphNode source, final int xOffset, final int yOffset) {
		MapGraphNode target = getNode(source.getCol() + xOffset, source.getRow() + yOffset);
		if (target.getType() == MapNodesTypes.PASSABLE_NODE && isDiagonalPossible(source, target)) {
			MapGraphConnection connection;
			if (Math.abs(source.getHeight() - target.getHeight()) <= PASSABLE_MAX_HEIGHT_DIFF) {
				connection = new MapGraphConnection(source, target, MapGraphConnectionCosts.CLEAN);
			} else {
				connection = new MapGraphConnection(source, target, MapGraphConnectionCosts.HEIGHT_DIFF);
			}
			source.getConnections().add(connection);
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
		return node.getIndex(mapSize);
	}

	@Override
	public int getNodeCount() {
		return nodes.size;
	}

	@Override
	public Array<Connection<MapGraphNode>> getConnections(final MapGraphNode fromNode) {
		auxConnectionsList.clear();
		Array<MapGraphConnection> connections = fromNode.getConnections();
		for (Connection<MapGraphNode> connection : connections) {
			checkIfConnectionIsAvailable(connection);
		}
		return auxConnectionsList;
	}

	private void checkIfConnectionIsAvailable(final Connection<MapGraphNode> connection) {
		boolean available = true;
		if (includeEnemiesInGetConnections) {
			available = checkIfNodeIsAvailable(connection.getToNode());
		}
		boolean validCost = connection.getCost() <= maxConnectionCostInSearch.getCostValue();
		if (available && validCost && checkIfConnectionPassable(connection)) {
			auxConnectionsList.add(connection);
		}
	}

	private boolean checkIfConnectionPassable(final Connection<MapGraphNode> con) {
		MapGraphNode fromNode = con.getFromNode();
		MapGraphNode toNode = con.getToNode();
		boolean result = fromNode.getType() == MapNodesTypes.PASSABLE_NODE && toNode.getType() == MapNodesTypes.PASSABLE_NODE;
		result &= Math.abs(fromNode.getCol() - toNode.getCol()) < 2 && Math.abs(fromNode.getRow() - toNode.getRow()) < 2;
		if ((fromNode.getCol() != toNode.getCol()) && (fromNode.getRow() != toNode.getRow())) {
			result &= getNode(fromNode.getCol(), toNode.getRow()).getType() != MapNodesTypes.OBSTACLE_KEY_DIAGONAL_FORBIDDEN;
			result &= getNode(toNode.getCol(), fromNode.getRow()).getType() != MapNodesTypes.OBSTACLE_KEY_DIAGONAL_FORBIDDEN;
		}
		return result;
	}

	public boolean checkIfNodeIsAvailable(final MapGraphNode destinationNode) {
		for (Entity c : relatedEntities.getCharacterEntities()) {
			MapGraphNode node = getNode(ComponentsMapper.characterDecal.get(c).getNodePosition(auxVector2));
			if (currentDestination == node || ComponentsMapper.character.get(c).getSkills().getHealthData().getHp() <= 0) {
				continue;
			}
			if (node.equals(destinationNode)) {
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
		if (node.getCol() < mapSize.width - 1) {
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
			if (x < mapSize.width - 1) {
				output.add(getNode(x + 1, y - 1));
			}
		}
	}

	private void getThreeInFront(final MapGraphNode node, final List<MapGraphNode> output) {
		int x = node.getCol();
		int y = node.getRow();
		if (y < mapSize.height - 1) {
			if (x > 0) {
				output.add(getNode(x - 1, y + 1));
			}
			output.add(getNode(x, y + 1));
			if (x < mapSize.width - 1) {
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
	public void onCharacterCommandDone(final Entity character, final CharacterCommand executedCommand) {
		currentDestination = null;
	}

	@Override
	public void onNewCharacterCommandSet(final CharacterCommand command) {
		MapGraphPath path = command.getPath();
		currentDestination = path.get(path.getCount() - 1);
	}

	public Entity getPickupFromNode(final MapGraphNode node) {
		Entity result = null;
		for (Entity pickup : relatedEntities.getPickupEntities()) {
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
		for (Entity obstacle : relatedEntities.getObstaclesEntities()) {
			ModelInstance modelInstance = ComponentsMapper.modelInstance.get(obstacle).getModelInstance();
			MapGraphNode pickupNode = getNode(modelInstance.transform.getTranslation(auxVector3));
			if (pickupNode.equals(node)) {
				result = obstacle;
				break;
			}
		}
		return result;
	}


	public MapGraphNode getNode(final Coords coord) {
		return getNode(coord.getCol(), coord.getRow());
	}

	public void init() {
		relatedEntities.getObstaclesEntities().forEach(wall -> {
			ObstacleComponent obstacleWallComponent = ComponentsMapper.obstacle.get(wall);
			int topLeftX = obstacleWallComponent.getTopLeftX();
			int topLeftY = obstacleWallComponent.getTopLeftY();
			int bottomRightX = obstacleWallComponent.getBottomRightX();
			int bottomRightY = obstacleWallComponent.getBottomRightY();
			if (topLeftX >= 0 && topLeftY >= 0 && bottomRightX >= 0 && bottomRightY >= 0) {
				for (int x = topLeftX; x <= bottomRightX; x++) {
					for (int z = topLeftY; z <= bottomRightY; z++) {
						ObstacleComponent obstacleComponent = ComponentsMapper.obstacle.get(wall);
						getNode(x, z).setType(obstacleComponent.getType().getNodeType());
					}
				}
			}
		});
		applyConnections();
	}

	public int getWidth() {
		return mapSize.width;
	}

	public int getDepth() {
		return mapSize.height;
	}

	public Entity getAliveCharacterFromNode(final MapGraphNode dest) {
		ImmutableArray<Entity> characterEntities = relatedEntities.getCharacterEntities();
		Entity result = null;
		for (Entity entity : characterEntities) {
			if (getNode(ComponentsMapper.characterDecal.get(entity).getNodePosition(auxVector2)).equals(dest)) {
				if (ComponentsMapper.character.get(entity).getSkills().getHealthData().getHp() > 0) {
					result = entity;
					break;
				}
			}
		}
		return result;
	}

	public MapGraphConnection findConnection(MapGraphNode node1, MapGraphNode node2) {
		if (node1 == null || node2 == null) return null;
		MapGraphConnection result = findConnectionBetweenTwoNodes(node1, node2);
		if (result == null) {
			result = findConnectionBetweenTwoNodes(node2, node1);
		}
		return result;
	}

	private MapGraphConnection findConnectionBetweenTwoNodes(MapGraphNode src, MapGraphNode dst) {
		Array<MapGraphConnection> connections = src.getConnections();
		for (MapGraphConnection connection : connections) {
			if (connection.getToNode() == dst) {
				return connection;
			}
		}
		return null;
	}
}
