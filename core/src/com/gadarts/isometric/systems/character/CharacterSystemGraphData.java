package com.gadarts.isometric.systems.character;

import com.gadarts.isometric.utils.map.GameHeuristic;
import com.gadarts.isometric.utils.map.GamePathFinder;
import com.gadarts.isometric.utils.map.MapGraph;
import com.gadarts.isometric.utils.map.MapGraphPath;
import lombok.Getter;

@Getter
public class CharacterSystemGraphData {
	private final MapGraphPath currentPath;
	private final GamePathFinder pathFinder;
	private final GameHeuristic heuristic;

	public CharacterSystemGraphData(final MapGraph map) {
		this.pathFinder = new GamePathFinder(map);
		this.heuristic = new GameHeuristic();
		this.currentPath = new MapGraphPath();
	}
}
