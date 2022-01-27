package com.gadarts.isometric.utils.map;

import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.gadarts.isometric.systems.character.CalculatePathOptions;

public class GamePathFinder extends IndexedAStarPathFinder<MapGraphNode> {
	private static final CalculatePathOptions calculatePathOptions = new CalculatePathOptions();
	private final MapGraph map;

	public GamePathFinder(final MapGraph graph) {
		super(graph);
		this.map = graph;
	}

	public boolean searchNodePathBeforeCommand(final MapGraphNode source,
											   final MapGraphNode destination,
											   final GameHeuristic heuristic,
											   final MapGraphPath output,
											   final CalculatePathOptions options) {
		MapGraphNode oldDest = map.getCurrentDestination();
		map.setIncludeEnemiesInGetConnections(options.isAvoidCharactersInCalculations());
		map.setCurrentDestination(destination);
		map.setMaxConnectionCostInSearch(options.getMaxCostInclusive());
		boolean result = searchNodePath(source, destination, heuristic, output);
		map.setMaxConnectionCostInSearch(MapGraphConnectionCosts.CLEAN);
		map.setCurrentDestination(oldDest);
		map.setIncludeEnemiesInGetConnections(true);
		return result;
	}
}
