package com.gadarts.isometric.components;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.gadarts.isometric.utils.Assets;

import java.util.HashMap;

public class CharacterAnimations {
	HashMap<Assets.CharacterDirRegions, Animation<TextureAtlas.AtlasRegion>> animations = new HashMap<>();

	public void put(final Assets.CharacterDirRegions dir, final Animation<TextureAtlas.AtlasRegion> animation) {
		animations.put(dir, animation);
	}

	public void clear() {
		animations.clear();
	}

	public Animation<TextureAtlas.AtlasRegion> get(Assets.CharacterDirRegions region) {
		return animations.get(region);
	}
}
