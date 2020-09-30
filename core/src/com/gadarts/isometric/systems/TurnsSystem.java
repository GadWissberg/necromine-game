package com.gadarts.isometric.systems;

import com.badlogic.ashley.core.Entity;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class TurnsSystem extends GameEntitySystem implements
		PlayerSystemEventsSubscriber,
		EventsNotifier<TurnsSystemEventsSubscriber>,
		EnemySystemEventsSubscriber {

	private final List<TurnsSystemEventsSubscriber> subscribers = new ArrayList<>();
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
	public void subscribeForEvents(final TurnsSystemEventsSubscriber sub) {
		if (subscribers.contains(sub)) return;
		subscribers.add(sub);
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
}
