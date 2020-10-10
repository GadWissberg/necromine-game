package com.gadarts.isometric;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.gadarts.isometric.utils.DefaultGameSettings;

public class IsometricGame extends Game {
	public static final int RESOLUTION_WIDTH = 800;
	public static final int RESOLUTION_HEIGHT = 600;

	@Override
	public void create() {
		Gdx.app.setLogLevel(DefaultGameSettings.LOG_LEVEL);
		setScreen(new BattleScreen());
	}

}
