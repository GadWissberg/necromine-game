package com.gadarts.isometric.systems.character;

import com.badlogic.ashley.core.Entity;
import com.gadarts.isometric.utils.map.MapGraphNode;
import lombok.Getter;
import lombok.Setter;

@Getter
public class CharacterCommand {
	private Commands type;

	@Setter
	private MapGraphNode destination;
	private Entity character;

	public MapGraphNode getDestination() {
		return destination;
	}

	public CharacterCommand init(final Commands type, final MapGraphNode destination, final Entity character) {
		this.type = type;
		this.destination = destination;
		this.character = character;
		return this;
	}
}
