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
	private int col;
	private int row;
	private int type;

	@Setter
	private float height;

	@Setter
	private Entity entity;

	public MapGraphNode(final int col, final int row, final int type, final int connections) {
		this.col = col;
		this.row = row;
		this.type = type;
		this.connections = new Array<>(connections);
	}

	@Override
	public String toString() {
		return "MapGraphNode{" +
				"x=" + col +
				", y=" + row +
				'}';
	}

	public Vector2 getCenterPosition(final Vector2 output) {
		return output.set(col + 0.5f, row + 0.5f);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		MapGraphNode that = (MapGraphNode) o;

		if (col != that.col) return false;
		if (row != that.row) return false;
		if (type != that.type) return false;
		return connections.equals(that.connections);
	}

	@Override
	public int hashCode() {
		int result = col;
		result = 31 * result + row;
		result = 31 * result + type;
		result = 31 * result + connections.hashCode();
		return result;
	}

	public int getIndex() {
		return col * MapGraph.MAP_SIZE + row;
	}

	public MapGraphNode set(final MapGraphNode newValue) {
		this.col = newValue.col;
		this.row = newValue.row;
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
