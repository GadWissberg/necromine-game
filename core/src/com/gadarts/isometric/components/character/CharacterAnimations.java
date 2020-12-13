package com.gadarts.isometric.components.character;

import com.gadarts.isometric.components.CharacterAnimation;
import com.gadarts.isometric.components.character.CharacterComponent.Direction;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CharacterAnimations {
	final HashMap<SpriteType, Map<Direction, CharacterAnimation>> animations = new HashMap<>();

	public void put(final SpriteType type, final Direction dir, final CharacterAnimation animation) {
		if (!animations.containsKey(type)) {
			animations.put(type, new HashMap<>());
		}
		animations.get(type).put(dir, animation);
	}

	public void clear() {
		Set<Map.Entry<SpriteType, Map<Direction, CharacterAnimation>>> entrySet = animations.entrySet();
		for (Map.Entry<SpriteType, Map<Direction, CharacterAnimation>> entry : entrySet) {
			entry.getValue().clear();
		}
	}

	public CharacterAnimation get(final SpriteType type, final Direction direction) {
		return animations.get(type).get(direction);
	}

	public boolean contains(final SpriteType spriteType) {
		return animations.containsKey(spriteType);
	}
}
