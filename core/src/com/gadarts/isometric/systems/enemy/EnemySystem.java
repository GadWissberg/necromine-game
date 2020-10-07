package com.gadarts.isometric.systems.enemy;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.EnemyComponent;
import com.gadarts.isometric.components.PlayerComponent;
import com.gadarts.isometric.systems.GameEntitySystem;
import com.gadarts.isometric.systems.character.CharacterCommand;
import com.gadarts.isometric.systems.character.CharacterSystem;
import com.gadarts.isometric.systems.character.CharacterSystemEventsSubscriber;
import com.gadarts.isometric.systems.character.Commands;
import com.gadarts.isometric.systems.turns.TurnsSystem;
import com.gadarts.isometric.systems.turns.TurnsSystemEventsSubscriber;
import com.gadarts.isometric.utils.map.*;

public class EnemySystem extends GameEntitySystem<EnemySystemEventsSubscriber> implements
		TurnsSystemEventsSubscriber,
		CharacterSystemEventsSubscriber {

	private static final Vector3 auxVector3_1 = new Vector3();
	private static final Vector3 auxVector3_2 = new Vector3();
	private static final MapGraphPath auxPath = new MapGraphPath();
	private final static CharacterCommand auxCommand = new CharacterCommand();
	private final GamePathFinder pathFinder;
	private final GameHeuristic heuristic;
	private final MapGraph map;
	private ImmutableArray<Entity> enemies;
	private CharacterSystem characterSystem;
	private Entity player;


	public EnemySystem(final MapGraph map) {
		this.map = map;
		this.pathFinder = new GamePathFinder(map);
		this.heuristic = new GameHeuristic();
	}

	@Override
	public void dispose() {

	}

	@Override
	public void addedToEngine(final Engine engine) {
		super.addedToEngine(engine);
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
			boolean pathFound = pathFinder.searchNodePathBeforeCommand(enemyNode, playerNode, heuristic, auxPath);
			if (pathFound) {
				auxCommand.init(Commands.GO_TO_MELEE, auxPath.get(auxPath.nodes.size - 2), enemy);
				characterSystem.applyCommand(auxCommand, enemy);
			}
			break;
		}
	}

	@Override
	public void onPlayerTurn() {

	}

	@Override
	public void onTurnsSystemReady(final TurnsSystem turnsSystem) {

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
	public void onNewCommandSet(final CharacterCommand command) {

	}

	@Override
	public void onCharacterSystemReady(final CharacterSystem characterSystem) {
		this.characterSystem = characterSystem;
	}

	@Override
	public void init() {

	}
}
