package com.gadarts.isometric.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.gadarts.isometric.IsometricGame;

public class DesktopLauncher {


	public static void main(final String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = IsometricGame.RESOLUTION_WIDTH;
		config.height = IsometricGame.RESOLUTION_HEIGHT;
		new LwjglApplication(new IsometricGame(), config);
	}

}
