package com.gadarts.isometric.components;

import com.badlogic.gdx.graphics.g3d.decals.Decal;
import lombok.Getter;

@Getter
public class DecalComponent implements GameComponent {

	private Decal decal;
	private CharacterAnimations animations;
	private SpriteType type;
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
		this.type = type;
		decal = Decal.newDecal(animations.get(type, direction).getKeyFrames()[0], true);//Optimize this - it creates an object each time.
	}


	public void initializeSprite(final SpriteType type, final CharacterComponent.Direction direction) {
		this.type = type;
		this.direction = direction;
		decal.setTextureRegion(animations.get(type, direction).getKeyFrames()[0]);
	}
}
