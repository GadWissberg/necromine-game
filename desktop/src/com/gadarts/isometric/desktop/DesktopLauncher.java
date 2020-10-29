package com.gadarts.isometric.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.gadarts.isometric.IsometricGame;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class DesktopLauncher {


    public static void main(final String[] arg) {
        LwjglApplicationConfiguration config = createGameConfig();
        String versionName = "0.0";
        int versionNumber = 0;
        try {
            String path = "core" + File.separator + "assets" + File.separator + "version.txt";
            List<String> lines = Files.readAllLines(Paths.get(path));
            versionName = lines.get(0);
            versionNumber = Integer.parseInt(lines.get(1));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        new LwjglApplication(new IsometricGame(versionName, versionNumber), config);
    }

    private static LwjglApplicationConfiguration createGameConfig() {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = IsometricGame.RESOLUTION_WIDTH;
        config.height = IsometricGame.RESOLUTION_HEIGHT;
        config.resizable = false;
        config.samples = 3;
        return config;
    }

}
