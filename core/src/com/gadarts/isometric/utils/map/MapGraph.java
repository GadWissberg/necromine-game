package com.gadarts.isometric.utils.map;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.systems.character.CharacterCommand;
import com.gadarts.isometric.systems.character.CharacterSystem;
import com.gadarts.isometric.systems.character.CharacterSystemEventsSubscriber;
import com.gadarts.isometric.utils.Utils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class MapGraph implements IndexedGraph<MapGraphNode>, CharacterSystemEventsSubscriber {
    static final int MAP_SIZE = 20;
    private static final Array<Connection<MapGraphNode>> auxConnectionsList = new Array<>();
    private static final Vector3 auxVector = new Vector3();
    private static final List<MapGraphNode> auxNodesList_1 = new ArrayList<>();
    private static final List<MapGraphNode> auxNodesList_2 = new ArrayList<>();

    private final Array<MapGraphNode> nodes;
    private final ImmutableArray<Entity> characterEntities;

    @Setter(AccessLevel.PACKAGE)
    @Getter(AccessLevel.PACKAGE)
    MapGraphNode currentDestination;

    public MapGraph(final ImmutableArray<Entity> characterEntities) {
        this.characterEntities = characterEntities;
        this.nodes = new Array<>(MAP_SIZE * MAP_SIZE);
        int[][] map = new int[MAP_SIZE][MAP_SIZE];
        for (int x = 0; x < MAP_SIZE; x++) {
            for (int y = 0; y < MAP_SIZE; y++) {
                nodes.add(new MapGraphNode(x, y, map[x][y], 8));
            }
        }
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
            if (nearbyNode.getType() == 0 && getEnemyFromNode(enemiesEntities, nearbyNode) == null) {
                availableNodes.add(nearbyNode);
            }
        }
        return availableNodes;
    }

    public Entity getEnemyFromNode(final ImmutableArray<Entity> enemiesEntities, final MapGraphNode node) {
        Entity result = null;
        for (Entity enemy : enemiesEntities) {
            MapGraphNode enemyNode = getNode(ComponentsMapper.decal.get(enemy).getDecal().getPosition());
            if (enemyNode.equals(node)) {
                result = enemy;
                break;
            }
        }
        return result;
    }

    public MapGraphNode getRayNode(final int screenX, final int screenY, final OrthographicCamera camera) {
        Vector3 output = Utils.calculateGridPositionFromMouse(camera, screenX, screenY, auxVector);
        output.set(Math.max(output.x, 0), Math.max(output.y, 0), Math.max(output.z, 0));
        return getNode(output);
    }

    public MapGraphNode getNode(final int x, final int y) {
        return nodes.get(x * MAP_SIZE + y);
    }

    private void addConnection(final MapGraphNode n, final int xOffset, final int yOffset) {
        MapGraphNode target = getNode(n.getX() + xOffset, n.getY() + yOffset);
        if (target.getType() == 0) {
            n.getConnections().add(new MapGraphConnection<>(n, target));
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
            if (available) {
                auxConnectionsList.add(connection);
            }
        }
        return auxConnectionsList;
    }

    private boolean checkIfNodeIsAvailable(final Connection<MapGraphNode> connection) {
        for (Entity character : characterEntities) {
            MapGraphNode node = getNode(ComponentsMapper.decal.get(character).getCellPosition(auxVector));
            if (currentDestination == node) {
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
        if (node.getX() > 0) {
            output.add(getNode(node.getX() - 1, node.getY()));
        }
        if (node.getX() < MAP_SIZE - 1) {
            output.add(getNode(node.getX() + 1, node.getY()));
        }
        return output;
    }

    private void getThreeBehind(final MapGraphNode node, final List<MapGraphNode> output) {
        int x = node.getX();
        int y = node.getY();
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
        int x = node.getX();
        int y = node.getY();
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

    @Override
    public void onDestinationReached(final Entity character) {

    }

    @Override
    public void onCommandDone(final Entity character) {
        currentDestination = null;
    }

    @Override
    public void onNewCommandSet(final CharacterCommand command) {
        currentDestination = command.getDestination();
    }

    @Override
    public void onCharacterSystemReady(final CharacterSystem characterSystem) {

    }

    @Override
    public void onCharacterGotDamage(final Entity target) {

    }
}