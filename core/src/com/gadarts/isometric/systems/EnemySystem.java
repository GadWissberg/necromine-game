package com.gadarts.isometric.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.EnemyComponent;
import com.gadarts.isometric.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class EnemySystem extends GameEntitySystem implements
		TurnsSystemEventsSubscriber,
		CharacterSystemEventsSubscriber,
		EventsNotifier<EnemySystemEventsSubscriber> {
	private static final Vector3 auxVector3_1 = new Vector3();
	private static final Vector3 auxVector3_2 = new Vector3();
	private final List<EnemySystemEventsSubscriber> subscribers = new ArrayList<>();
	private ImmutableArray<Entity> enemies;
	private CharacterSystem characterSystem;

	@Override
	public void dispose() {

	}

	@Override
	public void addedToEngine(final Engine engine) {
		super.addedToEngine(engine);
		characterSystem = engine.getSystem(CharacterSystem.class);
		enemies = engine.getEntitiesFor(Family.all(EnemyComponent.class).get());
	}


	@Override
	public void onEnemyTurn() {
		for (Entity enemy : enemies) {
			int randomX = MathUtils.random(3);
			int randomZ = MathUtils.random(3);
			Vector3 destination = Utils.alignPositionToGrid(auxVector3_1.set(randomX, 0, randomZ));
			Vector3 enemyPosition = auxVector3_2.set(ComponentsMapper.decal.get(enemy).getDecal().getPosition());
			if (destination.equals(Utils.alignPositionToGrid(enemyPosition))) {
				onCommandFinished(enemy);
			} else {
				CharacterSystem.auxCommand.init(Commands.GO_TO, destination, enemy);
				characterSystem.applyCommand(CharacterSystem.auxCommand, enemy);
			}
			break;
		}
	}

	@Override
	public void onPlayerTurn() {

	}

	@Override
	public void onCommandFinished(final Entity character) {
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
