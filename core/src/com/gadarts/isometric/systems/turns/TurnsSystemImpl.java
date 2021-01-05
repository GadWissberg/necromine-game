package com.gadarts.isometric.systems.turns;

import com.badlogic.ashley.core.Entity;
import com.gadarts.isometric.systems.GameEntitySystem;
import com.gadarts.isometric.systems.enemy.EnemySystemEventsSubscriber;
import com.gadarts.isometric.systems.player.PlayerSystem;
import com.gadarts.isometric.systems.player.PlayerSystemEventsSubscriber;
import com.gadarts.isometric.utils.DefaultGameSettings;
import com.gadarts.isometric.utils.map.MapGraphNode;
import lombok.Getter;

import java.util.List;

@Getter
public class TurnsSystemImpl extends GameEntitySystem<TurnsSystemEventsSubscriber> implements
		TurnsSystem,
		PlayerSystemEventsSubscriber,
		EnemySystemEventsSubscriber {

	private Turns currentTurn = Turns.PLAYER;
	private long currentTurnId;

	@Override
	public long getCurrentTurnId() {
		return currentTurnId;
	}

	@Override
	public void dispose() {

	}

	@Override
	public void onPlayerFinishedTurn() {
		if (currentTurn == Turns.PLAYER) {
			currentTurnId++;
			if (!DefaultGameSettings.PARALYZED_ENEMIES) {
				currentTurn = Turns.ENEMY;
				for (TurnsSystemEventsSubscriber subscriber : subscribers) {
					subscriber.onEnemyTurn(currentTurnId);
				}
			}
		}
	}

	@Override
	public void onPathCreated(final boolean pathToEnemy) {

	}

	@Override
	public void onEnemySelectedWithRangeWeapon(final MapGraphNode node) {

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
	public void onEnemyFinishedTurn() {
		if (currentTurn == Turns.ENEMY) {
			currentTurnId++;
			currentTurn = Turns.PLAYER;
			for (TurnsSystemEventsSubscriber subscriber : subscribers) {
				subscriber.onPlayerTurn(currentTurnId);
			}
		}
	}

	@Override
	public void onEnemyAwaken(final Entity enemy) {

	}


	@Override
	public void activate() {
		for (TurnsSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onTurnsSystemReady(this);
		}
	}
}
