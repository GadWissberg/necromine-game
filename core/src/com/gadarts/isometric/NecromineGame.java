package com.gadarts.isometric;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.gadarts.isometric.utils.DefaultGameSettings;

public class NecromineGame extends Game {
	public static final int RESOLUTION_WIDTH = DefaultGameSettings.FULL_SCREEN ? 1920 : 800;
	public static final int RESOLUTION_HEIGHT = DefaultGameSettings.FULL_SCREEN ? 1080 : 600;
	public static final String TITLE = "necromine";
	private static String versionName;
	private static int versionNumber;

	public NecromineGame(final String versionName, final int versionNumber) {
		NecromineGame.versionName = versionName;
		NecromineGame.versionNumber = versionNumber;
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
