package com.gadarts.isometric.components.character;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.gadarts.isometric.components.GameComponent;
import com.gadarts.isometric.systems.EventsNotifier;
import com.gadarts.isometric.utils.Utils;
import com.gadarts.isometric.utils.map.MapGraphNode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CharacterComponent implements GameComponent, EventsNotifier<CharacterComponentEventsSubscriber> {

	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private final List<CharacterComponentEventsSubscriber> subscribers = new ArrayList<>();

	private boolean attacking;
	private Entity target;

	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private MapGraphNode destinationNode;

	private boolean rotating;
	private long lastRotation;
	private SpriteType spriteType;
	private Direction direction;

	public MapGraphNode getDestinationNode() {
		return destinationNode;
	}

	public void setDestinationNode(final MapGraphNode newValue) {
		this.destinationNode = newValue;
	}

	@Override
	public void reset() {
		subscribers.clear();
	}

	public void init(final Direction direction, final SpriteType type) {
		this.direction = direction;
		this.spriteType = type;
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
			direction = new Vector2(x, z).nor();
		}

		public static Direction findDirection(final Vector2 direction) {
			Direction[] values = values();
			Direction result = null;
			for (Direction dir : values) {
				if (dir.direction.epsilonEquals(direction, Utils.EPSILON)) {
					result = dir;
				}
			}
			return result;
		}

		public Vector2 getDirection(final Vector2 output) {
			return output.set(direction);
		}
	}
}
