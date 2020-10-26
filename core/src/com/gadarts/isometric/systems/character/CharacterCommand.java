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

	public CharacterCommand init(final Commands type, final MapGraphPath path, final Entity character) {
		this.type = type;
		this.path = path;
		this.character = character;
		return this;
	}
}
