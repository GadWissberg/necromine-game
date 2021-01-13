package com.gadarts.isometric.systems.turns;

import com.gadarts.isometric.systems.GameEntitySystem;
import com.gadarts.isometric.systems.enemy.EnemySystemEventsSubscriber;
import com.gadarts.isometric.systems.player.PlayerSystemEventsSubscriber;
import com.gadarts.isometric.utils.DefaultGameSettings;
import lombok.Getter;

@Getter
public class TurnsSystemImpl extends GameEntitySystem<TurnsSystemEventsSubscriber> implements
		TurnsSystem,
		PlayerSystemEventsSubscriber,
		EnemySystemEventsSubscriber {

	private Turns currentTurn = Turns.PLAYER;
	private long currentTurnId;
	private boolean enemyTurnDone;
	private boolean playerTurnDone;

	@Override
	public long getCurrentTurnId() {
		return currentTurnId;
	}

	@Override
	public void dispose() {

	}

	@Override
	public void onPlayerFinishedTurn() {
		playerTurnDone = true;
	}

	@Override
	public void update(final float deltaTime) {
		super.update(deltaTime);
		if (currentTurn == Turns.PLAYER && playerTurnDone) {
			invokePlayerTurnDone();
		} else if (currentTurn == Turns.ENEMY && enemyTurnDone) {
			invokeEnemyTurnDone();
		}
	}

	private void invokeEnemyTurnDone() {
		resetTurnFlags();
		currentTurnId++;
		currentTurn = Turns.PLAYER;
		for (TurnsSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onPlayerTurn(currentTurnId);
		}
	}

	private void invokePlayerTurnDone() {
		resetTurnFlags();
		currentTurnId++;
		if (!DefaultGameSettings.PARALYZED_ENEMIES) {
			currentTurn = Turns.ENEMY;
			for (TurnsSystemEventsSubscriber subscriber : subscribers) {
				subscriber.onEnemyTurn(currentTurnId);
			}
		}
	}

	private void resetTurnFlags() {
		playerTurnDone = false;
		enemyTurnDone = false;
	}


	@Override
	public void onEnemyFinishedTurn() {
		enemyTurnDone = true;
	}


	@Override
	public void activate() {
		for (TurnsSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onTurnsSystemReady(this);
		}
	}
}
