package com.gadarts.isometric;

import com.badlogic.gdx.Game;

public class IsometricGame extends Game {
	public static final int RESOLUTION_WIDTH = 800;
	public static final int RESOLUTION_HEIGHT = 600;

	@Override
	public void create() {
		setScreen(new BattleScreen());
	}

}
