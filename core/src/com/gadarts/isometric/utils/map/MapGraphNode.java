package com.gadarts.isometric.utils.map;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import lombok.Getter;
import lombok.Setter;

@Getter
public class MapGraphNode {
	public static final int BLOCK_DIAGONAL = 2;
	private final Array<Connection<MapGraphNode>> connections;
	private int x;
	private int y;
	private int type;

	@Setter
	private Entity entity;

	public MapGraphNode(final int x, final int y, final int type, final int connections) {
		this.x = x;
		this.y = y;
		this.type = type;
		this.connections = new Array<>(connections);
	}

	@Override
	public String toString() {
		return "MapGraphNode{" +
				"x=" + x +
				", y=" + y +
				'}';
	}

	public Vector2 getCenterPosition(final Vector2 output) {
		return output.set(x + 0.5f, y + 0.5f);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		MapGraphNode that = (MapGraphNode) o;

		if (x != that.x) return false;
		if (y != that.y) return false;
		if (type != that.type) return false;
		return connections.equals(that.connections);
	}

	@Override
	public int hashCode() {
		int result = x;
		result = 31 * result + y;
		result = 31 * result + type;
		result = 31 * result + connections.hashCode();
		return result;
	}

	public int getIndex() {
		return x * MapGraph.MAP_SIZE + y;
	}

	public MapGraphNode set(final MapGraphNode newValue) {
		this.x = newValue.x;
		this.y = newValue.y;
		this.type = newValue.type;
		this.connections.clear();
		connections.addAll(newValue.connections);
		return this;
	}

	public boolean isConnectedNeighbour(final MapGraphNode selectedAttackNode) {
		boolean result = false;
		for (Connection<MapGraphNode> connection : connections) {
			if (connection.getToNode().equals(selectedAttackNode)) {
				result = true;
				break;
			}
		}
		return result;
	}
}
