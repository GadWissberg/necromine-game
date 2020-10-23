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

	private static final Vector3 auxVector = new Vector3();
	private Decal decal;
	private CharacterAnimations animations;
	private SpriteType spriteType;
	private CharacterComponent.Direction direction;

	@Override
	public void reset() {
		animations.clear();
	}

	public void init(final CharacterAnimations animations,
					 final SpriteType type,
					 final CharacterComponent.Direction direction) {
		this.animations = animations;
		this.direction = direction;
		this.spriteType = type;
		decal = Decal.newDecal(animations.get(type, direction).getKeyFrames()[0], true);//Optimize this - it creates an object each time.
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
