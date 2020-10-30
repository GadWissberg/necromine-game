package com.gadarts.isometric.systems.camera;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.gadarts.isometric.systems.GameSystem;

/**
 * Handles camera logic.
 */
public interface CameraSystem extends GameSystem {

	/**
	 * @return The orthographic camera object.
	 */
	OrthographicCamera getCamera();

	/**
	 * @return Whether the camera is in the middle of the rotation process.
	 */
	boolean isCameraRotating();
}
