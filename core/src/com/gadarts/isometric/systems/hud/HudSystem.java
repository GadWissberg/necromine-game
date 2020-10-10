package com.gadarts.isometric.systems.hud;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.scenes.scene2d.Stage;

public interface HudSystem {
	Stage getStage();

	ModelInstance getCursorModelInstance();
}
