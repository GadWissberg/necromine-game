package com.gadarts.isometric.components.character;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;
import com.gadarts.isometric.components.GameComponent;
import com.gadarts.isometric.utils.assets.Assets;
import com.gadarts.isometric.utils.map.MapGraphNode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CharacterComponent implements GameComponent {

	private static final Vector2 auxVector = new Vector2();

	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private MapGraphNode destinationNode;

	private CharacterMode mode = CharacterMode.IDLE;
	private Entity target;
	private CharacterRotationData rotationData = new CharacterRotationData();
	private int hp;
	private long lastDamage;
	private Object modeAdditionalData;
	private Assets.Sounds attackSound;
	private CharacterSpriteData characterSpriteData;

	public MapGraphNode getDestinationNode() {
		return destinationNode;
	}

	public void setDestinationNode(final MapGraphNode newValue) {
		this.destinationNode = newValue;
	}


	public void setMode(final CharacterMode mode) {
		setMode(mode, null);
	}

	public void setMode(final CharacterMode mode, final Object additionalData) {
		this.mode = mode;
		this.modeAdditionalData = additionalData;
	}

	@Override
	public void reset() {
		destinationNode = null;
		mode = CharacterMode.IDLE;
		target = null;
		rotationData.reset();
	}

	public void init(final CharacterSpriteData characterSpriteData) {
		this.characterSpriteData = characterSpriteData;
		this.hp = 1;
	}

	public void dealDamage(final int damagePoints) {
		hp -= damagePoints;
		mode = CharacterMode.PAIN;
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

		private static final float DIR_ANGLE_SIZE = 45;
		private final Vector2 directionVector;
		private final float bottomBound;
		private final float upperBound;

		Direction(final int x, final int z) {
			directionVector = new Vector2(x, z).nor();
			float angleDeg = directionVector.angleDeg();
			bottomBound = CharacterComponent.auxVector.set(1, 0).setAngleDeg(angleDeg - DIR_ANGLE_SIZE / 2).angleDeg();
			upperBound = angleDeg + DIR_ANGLE_SIZE / 2;
		}

		public static Direction findDirection(final Vector2 direction) {
			Direction[] values = values();
			Direction result = SOUTH;
			float angleDeg = direction.angleDeg();
			for (Direction dir : values) {
				boolean isBottomLessThanUpper = dir.bottomBound < dir.upperBound;
				boolean firstOpt = isBottomLessThanUpper && angleDeg >= dir.bottomBound && angleDeg < dir.upperBound;
				boolean secondOpt = !isBottomLessThanUpper && (angleDeg >= dir.bottomBound || angleDeg < dir.upperBound);
				if (firstOpt || secondOpt) {
					result = dir;
					break;
				}
			}
			return result;
		}

		public Vector2 getDirection(final Vector2 output) {
			return output.set(directionVector);
		}
	}
}
