package com.gadarts.isometric.systems.hud;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.gadarts.isometric.systems.GameSystem;

public interface HudSystem extends GameSystem {
	Stage getStage();

	ModelInstance getCursorModelInstance();

	boolean hasOpenWindows();
}
