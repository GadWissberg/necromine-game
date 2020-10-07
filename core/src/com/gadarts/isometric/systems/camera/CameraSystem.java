package com.gadarts.isometric.systems.camera;

import com.badlogic.gdx.graphics.OrthographicCamera;

public interface CameraSystem {
	OrthographicCamera getCamera();

	boolean isCameraRotating();
}
