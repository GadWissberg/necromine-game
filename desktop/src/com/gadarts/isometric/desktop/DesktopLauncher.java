package com.gadarts.isometric.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.gadarts.isometric.NecronemesGame;
import com.gadarts.isometric.utils.DefaultGameSettings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.gadarts.isometric.NecronemesGame.*;

public class DesktopLauncher {


	public static void main(final String[] arg) {
		LwjglApplicationConfiguration config = createGameConfig();
		String versionName = "0.0";
		int versionNumber = 0;
		try {
			InputStream res = DesktopLauncher.class.getClassLoader().getResourceAsStream("version.txt");
			BufferedReader versionFile = new BufferedReader(new InputStreamReader(Objects.requireNonNull(res)));
			String line;
			List<String> lines = new ArrayList<>();
			while ((line = versionFile.readLine()) != null) {
				lines.add(line);
			}
			versionName = lines.get(0);
			versionNumber = Integer.parseInt(lines.get(1));
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		new LwjglApplication(new NecronemesGame(versionName, versionNumber), config);
	}

	private static LwjglApplicationConfiguration createGameConfig() {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = DefaultGameSettings.FULL_SCREEN ? FULL_SCREEN_RESOLUTION_WIDTH : WINDOWED_RESOLUTION_WIDTH;
		config.height = DefaultGameSettings.FULL_SCREEN ? FULL_SCREEN_RESOLUTION_HEIGHT : WINDOWED_RESOLUTION_HEIGHT;
		config.resizable = false;
		config.samples = 3;
		config.fullscreen = DefaultGameSettings.FULL_SCREEN;
		return config;
	}

}
