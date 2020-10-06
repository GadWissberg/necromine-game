package com.gadarts.isometric.utils.map;

import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;

public class GamePathFinder extends IndexedAStarPathFinder<MapGraphNode> {
	private final MapGraph map;

	public GamePathFinder(final MapGraph graph) {
		super(graph);
		this.map = graph;
	}

	public boolean searchNodePathBeforeCommand(final MapGraphNode source,
											   final MapGraphNode destination,
											   final GameHeuristic heuristic,
											   final MapGraphPath output) {
		MapGraphNode oldDest = map.getCurrentDestination();
		map.setCurrentDestination(destination);
		boolean result = searchNodePath(source, destination, heuristic, output);
		map.setCurrentDestination(oldDest);
		return result;
	}
}
