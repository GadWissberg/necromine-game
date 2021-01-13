package com.gadarts.isometric.systems.character.commands;

import com.badlogic.ashley.core.Entity;
import com.gadarts.isometric.utils.map.MapGraphPath;
import lombok.Getter;
import lombok.Setter;

@Getter
public class CharacterCommand {
	private CharacterCommands type;
	private Entity character;
	private Object additionalData;

	@Setter
	private boolean started;

	@Setter
	private MapGraphPath path;

	public CharacterCommand init(final CharacterCommands type,
								 final MapGraphPath path,
								 final Entity character) {
		return init(type, path, character, null);
	}

	public CharacterCommand init(final CharacterCommands type,
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
