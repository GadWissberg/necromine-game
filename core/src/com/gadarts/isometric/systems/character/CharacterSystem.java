package com.gadarts.isometric.systems.character;

import com.badlogic.ashley.core.Entity;
import com.gadarts.isometric.systems.GameSystem;
import com.gadarts.isometric.systems.character.commands.CharacterCommand;
import com.gadarts.isometric.utils.map.MapGraphNode;
import com.gadarts.isometric.utils.map.MapGraphPath;

/**
 * Handles characters behaviour.
 */
public interface CharacterSystem extends GameSystem {
	/**
	 * @return Whether a command is being processed.
	 */
	boolean isProcessingCommand( );

	/**
	 * Applies the given command.
	 *
	 * @param command   The given command to apply.
	 * @param character The character to apply the command on.
	 */
	void applyCommand(CharacterCommand command, Entity character);

	/**
	 * Calculates a path.
	 *
	 * @param sourceNode      The node the path starts from.
	 * @param destinationNode The node the path ends at.
	 * @param outputPath      The object that contains the result path.
	 * @return Whether it had successfully calculated a path.
	 */
	boolean calculatePath(MapGraphNode sourceNode, MapGraphNode destinationNode, MapGraphPath outputPath);

	/**
	 * Calculates a path to a given character.
	 *
	 * @param sourceNode  The node the path starts from.
	 * @param character   The character the path ends at.
	 * @param outputPath  The object that contains the result path.
	 * @param enemyBlocks Whether other enemies can be considered as obstacles.
	 * @return Whether it had successfully calculated a path.
	 */
	boolean calculatePathToCharacter(MapGraphNode sourceNode,
									 Entity character,
									 MapGraphPath outputPath,
									 boolean enemyBlocks);
}
