package com.gadarts.isometric.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.EnemyComponent;
import com.gadarts.isometric.components.PlayerComponent;
import com.gadarts.isometric.utils.GameHeuristic;
import com.gadarts.isometric.utils.MapGraph;
import com.gadarts.isometric.utils.MapGraphNode;
import com.gadarts.isometric.utils.MapGraphPath;

import java.util.ArrayList;
import java.util.List;

public class EnemySystem extends GameEntitySystem implements
		TurnsSystemEventsSubscriber,
		CharacterSystemEventsSubscriber,
		EventsNotifier<EnemySystemEventsSubscriber> {

	private static final Vector3 auxVector3_1 = new Vector3();
	private static final Vector3 auxVector3_2 = new Vector3();
	private static final MapGraphPath auxPath = new MapGraphPath();
	private final List<EnemySystemEventsSubscriber> subscribers = new ArrayList<>();
	private final IndexedAStarPathFinder<MapGraphNode> pathFinder;
	private final GameHeuristic heuristic;
	private final MapGraph map;
	private ImmutableArray<Entity> enemies;
	private CharacterSystem characterSystem;
	private Entity player;


	public EnemySystem(final MapGraph map) {
		this.map = map;
		this.pathFinder = new IndexedAStarPathFinder<>(map);
		this.heuristic = new GameHeuristic();
	}

	@Override
	public void dispose() {

	}

	@Override
	public void addedToEngine(final Engine engine) {
		super.addedToEngine(engine);
		characterSystem = engine.getSystem(CharacterSystem.class);
		enemies = engine.getEntitiesFor(Family.all(EnemyComponent.class).get());
		player = engine.getEntitiesFor(Family.all(PlayerComponent.class).get()).first();
	}


	@Override
	public void onEnemyTurn() {
		for (Entity enemy : enemies) {
			Vector3 enemyPosition = ComponentsMapper.decal.get(enemy).getCellPosition(auxVector3_2);
			Vector3 playerPosition = ComponentsMapper.decal.get(player).getCellPosition(auxVector3_1);
			MapGraphNode enemyNode = map.getNode((int) enemyPosition.x, (int) enemyPosition.z);
			MapGraphNode playerNode = map.getNode((int) playerPosition.x, (int) playerPosition.z);
			auxPath.clear();
			boolean pathFound = pathFinder.searchNodePath(enemyNode, playerNode, heuristic, auxPath);
			if (pathFound) {
				CharacterSystem.auxCommand.init(Commands.GO_TO_MELEE, auxPath.get(auxPath.nodes.size - 2), enemy);
				characterSystem.applyCommand(CharacterSystem.auxCommand, enemy);
			}
			break;
		}
	}

	@Override
	public void onPlayerTurn() {

	}

	@Override
	public void onDestinationReached(final Entity character) {
	}

	@Override
	public void onCommandDone(final Entity character) {
		if (ComponentsMapper.enemy.has(character)) {
			for (EnemySystemEventsSubscriber subscriber : subscribers) {
				subscriber.onEnemyFinishedTurn(character);
			}
		}
	}

	@Override
	public void subscribeForEvents(final EnemySystemEventsSubscriber sub) {
		if (subscribers.contains(sub)) return;
		subscribers.add(sub);
	}
}
