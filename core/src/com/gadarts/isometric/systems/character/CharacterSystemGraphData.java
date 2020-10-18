package com.gadarts.isometric.systems.character;

import com.badlogic.gdx.ai.pfa.Heuristic;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.gadarts.isometric.utils.map.GameHeuristic;
import com.gadarts.isometric.utils.map.MapGraph;
import com.gadarts.isometric.utils.map.MapGraphNode;
import com.gadarts.isometric.utils.map.MapGraphPath;
import lombok.Getter;

@Getter
public class CharacterSystemGraphData {
	private final MapGraphPath currentPath;
	private final IndexedAStarPathFinder<MapGraphNode> pathFinder;
	private final Heuristic<MapGraphNode> heuristic;

	public CharacterSystemGraphData(final MapGraph map) {
		this.pathFinder = new IndexedAStarPathFinder<>(map);
		this.heuristic = new GameHeuristic();
		this.currentPath = new MapGraphPath();
	}
}
