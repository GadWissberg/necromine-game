package com.gadarts.isometric.systems.character;

import com.badlogic.ashley.core.Entity;
import com.gadarts.isometric.systems.GameSystem;
import com.gadarts.isometric.systems.character.commands.CharacterCommand;
import com.gadarts.isometric.utils.map.MapGraphNode;
import com.gadarts.isometric.utils.map.MapGraphPath;

public interface CharacterSystem extends GameSystem {
	boolean isProcessingCommand();

	void applyCommand(CharacterCommand command, Entity character);

	boolean calculatePath(MapGraphNode sourceNode, MapGraphNode destinationNode, MapGraphPath outputPath);

	boolean calculatePathToCharacter(MapGraphNode sourceNode, Entity character, MapGraphPath outputPath);
}
