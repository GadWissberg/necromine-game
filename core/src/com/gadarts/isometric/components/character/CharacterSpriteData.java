package com.gadarts.isometric.components.character;

import com.badlogic.gdx.utils.Pool;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CharacterSpriteData implements Pool.Poolable {
	private CharacterComponent.Direction facingDirection;
	private SpriteType spriteType;
	private int hitFrameIndex;

	@Override
	public void reset() {

	}

	public void init(final CharacterComponent.Direction direction, final SpriteType spriteType, final int hitFrameIndex) {
		this.facingDirection = direction;
		this.spriteType = spriteType;
		this.hitFrameIndex = hitFrameIndex;
	}
}
