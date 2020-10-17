package com.gadarts.isometric.components;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.gadarts.isometric.components.character.CharacterComponent;
import lombok.Getter;

@Getter
public class CharacterAnimation extends Animation<TextureAtlas.AtlasRegion> {

	private final CharacterComponent.Direction direction;

	public CharacterAnimation(final float animationDuration,
							  final Array<TextureAtlas.AtlasRegion> regions,
							  final PlayMode playMode,
							  final CharacterComponent.Direction dir) {
		super(animationDuration, regions, playMode);
		this.direction = dir;
	}
}
