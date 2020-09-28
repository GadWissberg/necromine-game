package com.gadarts.isometric.utils;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.utils.Array;

public class MapGraph implements IndexedGraph<MapGraphNode> {
	static final int MAP_SIZE = 20;
	private final Array<MapGraphNode> nodes;

	public MapGraph() {
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
				if (x > 0 && y > 0) addConnection(n, -1, -1);
				if (y > 0) addConnection(n, 0, -1);
				if (y > 0 && x < MAP_SIZE - 1) addConnection(n, 1, -1);
				if (x < MAP_SIZE - 1) addConnection(n, 1, 0);
				if (x < MAP_SIZE - 1 && y < MAP_SIZE - 1) addConnection(n, 1, 1);
				if (y < MAP_SIZE - 1) addConnection(n, 0, 1);
			}
		}
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
		return fromNode.getConnections();
	}
}
