package com.gadarts.isometric;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.gadarts.isometric.utils.DefaultGameSettings;

public class IsometricGame extends Game {
	public static final int RESOLUTION_WIDTH = 800;
	public static final int RESOLUTION_HEIGHT = 600;
	private static String versionName;
	private static int versionNumber;

	public IsometricGame(final String versionName, final int versionNumber) {
		IsometricGame.versionName = versionName;
		IsometricGame.versionNumber = versionNumber;
	}

	public static String getVersionName() {
		return versionName;
	}

	public static int getVersionNumber() {
		return versionNumber;
	}

	@Override
	public void create() {
		Gdx.app.setLogLevel(DefaultGameSettings.LOG_LEVEL);
		setScreen(new BattleScreen());
	}

}
