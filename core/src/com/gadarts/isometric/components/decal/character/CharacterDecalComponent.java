package com.gadarts.isometric.components.decal.character;

import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.isometric.components.GameComponent;
import com.gadarts.isometric.components.character.CharacterAnimations;
import com.gadarts.necromine.model.characters.Direction;
import com.gadarts.necromine.model.characters.SpriteType;
import lombok.Getter;

import static com.gadarts.necromine.model.characters.CharacterTypes.BILLBOARD_SCALE;
import static com.gadarts.necromine.model.characters.CharacterTypes.BILLBOARD_Y;

@Getter
public class CharacterDecalComponent implements GameComponent {
	public static final float SHADOW_OPACITY = 0.6f;
	public static final float SHADOW_OFFSET_Z = 0.4f;
	public static final float SHADOW_OFFSET_Y = BILLBOARD_Y - 0.1f;
	private static final Vector3 auxVector3 = new Vector3();
	private static final Vector2 auxVector2 = new Vector2();
	private Decal decal;
	private Decal shadowDecal;
	private CharacterAnimations animations;
	private SpriteType spriteType;
	private Direction direction;

	@Override
	public void reset( ) {
		animations.clear();
	}

	public void init(final CharacterAnimations animations,
					 final SpriteType type,
					 final Direction direction,
					 final Vector3 position) {
		this.animations = animations;
		this.direction = direction;
		this.spriteType = type;
		createDecals(animations, type, direction, position);
	}

	private void createDecals(final CharacterAnimations animations,
							  final SpriteType type,
							  final Direction direction,
							  final Vector3 position) {
		createCharacterDecal(animations, type, direction, position);
		createShadowDecal(animations, type, direction, position);
	}

	private void createShadowDecal(final CharacterAnimations animations,
								   final SpriteType type,
								   final Direction direction,
								   final Vector3 position) {
		shadowDecal = Decal.newDecal(animations.get(type, direction).getKeyFrames()[0], true);//Optimize this - it creates an object each time.
		shadowDecal.setPosition(auxVector3.set(position).set(
				auxVector3.x,
				auxVector3.y - SHADOW_OFFSET_Y,
				auxVector3.z - SHADOW_OFFSET_Z
		));
		shadowDecal.rotateX(-90);
		shadowDecal.setScale(BILLBOARD_SCALE);
		shadowDecal.setColor(0, 0, 0, SHADOW_OPACITY);
	}

	private void createCharacterDecal(final CharacterAnimations animations,
									  final SpriteType type,
									  final Direction direction,
									  final Vector3 position) {
		decal = Decal.newDecal(animations.get(type, direction).getKeyFrames()[0], true);//Optimize this - it creates an object each time.
		decal.setScale(BILLBOARD_SCALE);
		decal.setPosition(position);
	}


	public void initializeSprite(final SpriteType type, final Direction direction) {
		this.spriteType = type;
		this.direction = direction;
	}

	public Vector2 getNodePosition(final Vector2 output) {
		Vector3 position = decal.getPosition();
		Vector2 decalPosition = auxVector2.set(position.x, position.z);
		return output.set(decalPosition.set(MathUtils.floor(auxVector2.x), MathUtils.floor(auxVector2.y)));
	}

	public Vector3 getNodePosition(final Vector3 output) {
		Vector3 position = decal.getPosition();
		Vector3 decalPosition = auxVector3.set(position.x, 0, position.z);
		return output.set(decalPosition.set(MathUtils.floor(auxVector3.x), 0, MathUtils.floor(auxVector3.z)));
	}
}
