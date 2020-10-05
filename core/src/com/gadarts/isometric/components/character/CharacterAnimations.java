package com.gadarts.isometric.components.character;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.gadarts.isometric.components.character.CharacterComponent.Direction;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CharacterAnimations {
	HashMap<SpriteType, Map<Direction, Animation<TextureAtlas.AtlasRegion>>> animations = new HashMap<>();

	public void put(final SpriteType type, final Direction dir, final Animation<TextureAtlas.AtlasRegion> animation) {
		if (!animations.containsKey(type)) {
			animations.put(type, new HashMap<>());
		}
		animations.get(type).put(dir, animation);
	}

	public void clear() {
		Set<Map.Entry<SpriteType, Map<Direction, Animation<TextureAtlas.AtlasRegion>>>> entrySet = animations.entrySet();
		for (Map.Entry<SpriteType, Map<Direction, Animation<TextureAtlas.AtlasRegion>>> entry : entrySet) {
			entry.getValue().clear();
		}
	}

	public Animation<TextureAtlas.AtlasRegion> get(final SpriteType type, final Direction direction) {
		return animations.get(type).get(direction);
	}

}
