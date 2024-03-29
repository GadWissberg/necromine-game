package com.gadarts.isometric;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.gadarts.isometric.utils.DefaultGameSettings;

public class NecronemesGame extends Game {
	public static final int WINDOWED_RESOLUTION_WIDTH = 800;
	public static final int WINDOWED_RESOLUTION_HEIGHT = 600;
	public static final int FULL_SCREEN_RESOLUTION_WIDTH = 1920;
	public static final int FULL_SCREEN_RESOLUTION_HEIGHT = 1080;
	public static final String TITLE = "necronemes";
	private static String versionName;
	private static int versionNumber;

	public NecronemesGame(final String versionName, final int versionNumber) {
		NecronemesGame.versionName = versionName;
		NecronemesGame.versionNumber = versionNumber;
	}

	public static String getVersionName( ) {
		return versionName;
	}

	public static int getVersionNumber( ) {
		return versionNumber;
	}

	@Override
	public void create() {
		Gdx.app.setLogLevel(DefaultGameSettings.LOG_LEVEL);
		setScreen(new BattleScreen());
	}

}
