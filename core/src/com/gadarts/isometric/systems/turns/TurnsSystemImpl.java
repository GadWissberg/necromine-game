package com.gadarts.isometric.systems.turns;

import com.badlogic.ashley.core.Entity;
import com.gadarts.isometric.systems.GameEntitySystem;
import com.gadarts.isometric.systems.enemy.EnemySystemEventsSubscriber;
import com.gadarts.isometric.systems.player.PlayerSystem;
import com.gadarts.isometric.systems.player.PlayerSystemEventsSubscriber;
import com.gadarts.isometric.utils.map.MapGraphNode;
import lombok.Getter;

import java.util.List;

@Getter
public class TurnsSystemImpl extends GameEntitySystem<TurnsSystemEventsSubscriber> implements
		TurnsSystem,
		PlayerSystemEventsSubscriber,
		EnemySystemEventsSubscriber {

	private Turns currentTurn = Turns.PLAYER;

	@Override
	public void dispose() {

	}

	@Override
	public void onPlayerFinishedTurn() {
		if (currentTurn == Turns.PLAYER) {
			currentTurn = Turns.ENEMY;
			for (TurnsSystemEventsSubscriber subscriber : subscribers) {
				subscriber.onEnemyTurn();
			}
		}
	}

	@Override
	public void onPlayerSystemReady(final PlayerSystem playerSystem) {

	}

	@Override
	public void onAttackModeActivated(final List<MapGraphNode> availableNodes) {

	}

	@Override
	public void onAttackModeDeactivated() {

	}

	@Override
	public void onEnemyFinishedTurn(final Entity character) {
		if (currentTurn == Turns.ENEMY) {
			currentTurn = Turns.PLAYER;
			for (TurnsSystemEventsSubscriber subscriber : subscribers) {
				subscriber.onPlayerTurn();
			}
		}
	}

	@Override
	public void init() {
		for (TurnsSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onTurnsSystemReady(this);
		}
	}

}
