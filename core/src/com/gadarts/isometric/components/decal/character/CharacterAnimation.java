package com.gadarts.isometric.components.decal.character;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.gadarts.necromine.model.characters.Direction;
import lombok.Getter;

@Getter
public class CharacterAnimation extends Animation<TextureAtlas.AtlasRegion> {

	private final Direction direction;

	public CharacterAnimation(final float animationDuration,
							  final Array<TextureAtlas.AtlasRegion> regions,
							  final PlayMode playMode,
							  final Direction dir) {
		super(animationDuration, regions, playMode);
		this.direction = dir;
	}
}
