package com.gadarts.isometric.systems.character;

import com.badlogic.ashley.core.Entity;
import com.gadarts.isometric.utils.map.MapGraphPath;
import lombok.Getter;
import lombok.Setter;

@Getter
public class CharacterCommand {
	private Commands type;

	@Setter
	@Getter
	private MapGraphPath path;

	private Entity character;
	private Object additionalData;

	public CharacterCommand init(final Commands type,
								 final MapGraphPath path,
								 final Entity character) {
		return init(type, path, character, null);
	}

	public CharacterCommand init(final Commands type,
								 final MapGraphPath path,
								 final Entity character,
								 final Object additionalData) {
		this.type = type;
		this.path = path;
		this.character = character;
		this.additionalData = additionalData;
		return this;
	}
}
