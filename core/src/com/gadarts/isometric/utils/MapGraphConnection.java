package com.gadarts.isometric.utils;

import com.badlogic.gdx.ai.pfa.Connection;

public class MapGraphConnection<T> implements Connection<T> {
	private final MapGraph mapGraph;
	private final T source;
	private final T dest;

	public MapGraphConnection(final MapGraph mapGraph, final T source, final T target) {
		this.mapGraph = mapGraph;
		this.source = source;
		this.dest = target;
	}

	@Override
	public float getCost() {
		return 1;
	}

	@Override
	public T getFromNode() {
		return source;
	}

	@Override
	public T getToNode() {
		return dest;
	}

}
