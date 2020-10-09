package com.gadarts.isometric.systems.character;

import com.badlogic.ashley.core.Entity;

public interface CharacterSystem {
	boolean isProcessingCommand();

	void applyCommand(CharacterCommand command, Entity character);

}
