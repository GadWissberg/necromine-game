package com.gadarts.isometric.systems.input;

import com.badlogic.gdx.InputProcessor;
import com.gadarts.isometric.systems.GameSystem;

public interface InputSystem extends GameSystem {
	void addInputProcessor(InputProcessor inputProcessor);
}
