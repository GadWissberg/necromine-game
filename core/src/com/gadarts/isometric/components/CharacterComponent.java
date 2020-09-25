package com.gadarts.isometric.components;

import com.badlogic.gdx.math.Vector2;
import com.gadarts.isometric.systems.EventsNotifier;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CharacterComponent implements GameComponent, EventsNotifier<CharacterComponentEventsSubscriber> {
	@Getter(AccessLevel.NONE)
	private final List<CharacterComponentEventsSubscriber> subscribers = new ArrayList<>();
	private Direction direction;

	@Override
	public void reset() {
		subscribers.clear();
	}

	public void init(final Direction direction) {
		this.direction = direction;
	}

	@Override
	public void subscribeForEvents(final CharacterComponentEventsSubscriber sub) {
		if (subscribers.contains(sub)) return;
		subscribers.add(sub);
	}

	public enum Direction {
		SOUTH(0, 1),
		SOUTH_EAST(1, 1),
		EAST(1, 0),
		NORTH_EAST(1, -1),
		NORTH(0, -1),
		NORTH_WEST(-1, -1),
		WEST(-1, 0),
		SOUTH_WEST(-1, 1);

		private final Vector2 direction;

		Direction(final int x, final int z) {
			direction = new Vector2(x, z);
		}

		public Vector2 getDirection(final Vector2 output) {
			return output.set(direction);
		}
	}
}
