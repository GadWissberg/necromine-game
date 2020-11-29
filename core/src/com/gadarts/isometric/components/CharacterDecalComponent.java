package com.gadarts.isometric.components;

import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.isometric.components.character.CharacterAnimations;
import com.gadarts.isometric.components.character.CharacterComponent;
import com.gadarts.isometric.components.character.SpriteType;
import lombok.Getter;

@Getter
public class CharacterDecalComponent implements GameComponent {
	public static final float BILLBOARD_SCALE = 0.015f;
	public static final float BILLBOARD_Y = 0.7f;
	private static final Vector3 auxVector = new Vector3();
	public static final float SHADOW_OPACITY = 0.6f;
	public static final float SHADOW_OFFSET_Z = 0.4f;
	private static final float SHADOW_OFFSET_Y = BILLBOARD_Y - 0.1f;
	private Decal decal;
	private Decal shadowDecal;
	private CharacterAnimations animations;
	private SpriteType spriteType;
	private CharacterComponent.Direction direction;

	@Override
	public void reset() {
		animations.clear();
	}

	public void init(final CharacterAnimations animations,
					 final SpriteType type,
					 final CharacterComponent.Direction direction,
					 final Vector3 position) {
		this.animations = animations;
		this.direction = direction;
		this.spriteType = type;
		createDecals(animations, type, direction, position);
	}

	private void createDecals(final CharacterAnimations animations,
							  final SpriteType type,
							  final CharacterComponent.Direction direction,
							  final Vector3 position) {
		createCharacterDecal(animations, type, direction, position);
		createShadowDecal(animations, type, direction, position);
	}

	private void createShadowDecal(final CharacterAnimations animations,
								   final SpriteType type,
								   final CharacterComponent.Direction direction,
								   final Vector3 position) {
		shadowDecal = Decal.newDecal(animations.get(type, direction).getKeyFrames()[0], true);//Optimize this - it creates an object each time.
		shadowDecal.setPosition(auxVector.set(position).set(
				auxVector.x,
				auxVector.y - SHADOW_OFFSET_Y,
				auxVector.z - SHADOW_OFFSET_Z
		));
		shadowDecal.rotateX(-90);
		shadowDecal.setScale(BILLBOARD_SCALE);
		shadowDecal.setColor(0, 0, 0, SHADOW_OPACITY);
	}

	private void createCharacterDecal(final CharacterAnimations animations,
									  final SpriteType type,
									  final CharacterComponent.Direction direction,
									  final Vector3 position) {
		decal = Decal.newDecal(animations.get(type, direction).getKeyFrames()[0], true);//Optimize this - it creates an object each time.
		decal.setScale(BILLBOARD_SCALE);
		decal.setPosition(position);
	}


	public void initializeSprite(final SpriteType type, final CharacterComponent.Direction direction) {
		this.spriteType = type;
		this.direction = direction;
	}

	public Vector3 getCellPosition(final Vector3 output) {
		Vector3 decalPosition = auxVector.set(decal.getPosition());
		return output.set(decalPosition.set(MathUtils.floor(auxVector.x), auxVector.y, MathUtils.floor(auxVector.z)));
	}
}
