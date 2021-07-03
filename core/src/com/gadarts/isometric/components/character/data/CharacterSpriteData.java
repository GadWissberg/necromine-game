package com.gadarts.isometric.components.character.data;

import com.badlogic.gdx.utils.Pool;
import com.gadarts.necromine.model.characters.Direction;
import com.gadarts.necromine.model.characters.SpriteType;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CharacterSpriteData implements Pool.Poolable {
	private Direction facingDirection;
	private int frameIndexNotAffectedByLight;
	private SpriteType spriteType;
	private int meleeHitFrameIndex;
	private int primaryAttackHitFrameIndex;

	@Override
	public void reset( ) {
		frameIndexNotAffectedByLight = -1;
	}

	public void init(final Direction direction,
					 final SpriteType spriteType,
					 final int hitFrameIndex,
					 final int primaryAttackHitFrameIndex) {
		this.facingDirection = direction;
		this.spriteType = spriteType;
		this.meleeHitFrameIndex = hitFrameIndex;
		this.primaryAttackHitFrameIndex = primaryAttackHitFrameIndex;
	}
}
