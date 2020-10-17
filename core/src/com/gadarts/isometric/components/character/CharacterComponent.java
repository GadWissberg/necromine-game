package com.gadarts.isometric.components.character;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;
import com.gadarts.isometric.components.GameComponent;
import com.gadarts.isometric.utils.Utils;
import com.gadarts.isometric.utils.map.MapGraphNode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CharacterComponent implements GameComponent {

	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private MapGraphNode destinationNode;

	private boolean attacking;
	private boolean inPain;
	private Entity target;
	private CharacterRotationData rotationData = new CharacterRotationData();
	private SpriteType spriteType;
	private Direction facingDirection;
	private int hp;
	private long lastDamage;

	public MapGraphNode getDestinationNode() {
		return destinationNode;
	}

	public void setDestinationNode(final MapGraphNode newValue) {
		this.destinationNode = newValue;
	}

	@Override
	public void reset() {
		destinationNode = null;
		attacking = false;
		target = null;
		rotationData.reset();
	}

	public void init(final Direction direction, final SpriteType type) {
		this.facingDirection = direction;
		this.spriteType = type;
		this.hp = 3;
	}

	public void dealDamage(final int damagePoints) {
		hp -= damagePoints;
		inPain = true;
		lastDamage = TimeUtils.millis();
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

		private final Vector2 directionVector;

		Direction(final int x, final int z) {
			directionVector = new Vector2(x, z).nor();
		}

		public static Direction findDirection(final Vector2 direction) {
			Direction[] values = values();
			Direction result = SOUTH;
			for (Direction dir : values) {
				if (dir.directionVector.epsilonEquals(direction, Utils.EPSILON)) {
					result = dir;
				}
			}
			return result;
		}

		public Vector2 getDirection(final Vector2 output) {
			return output.set(directionVector);
		}
	}
}
