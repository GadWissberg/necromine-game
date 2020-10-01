package com.gadarts.isometric.systems;

import com.badlogic.ashley.core.Entity;
import com.gadarts.isometric.utils.MapGraphNode;
import lombok.Getter;

@Getter
public class CharacterCommand {
	private Commands type;
	private MapGraphNode destination;
	private Entity character;

	public MapGraphNode getDestination() {
		return destination;
	}

	public void init(final Commands type, final MapGraphNode destination, final Entity character) {
		this.type = type;
		this.destination = destination;
		this.character = character;
	}
}
