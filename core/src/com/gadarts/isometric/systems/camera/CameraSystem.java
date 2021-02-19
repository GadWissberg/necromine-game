package com.gadarts.isometric.systems.camera;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.isometric.systems.GameSystem;

/**
 * Handles camera logic.
 */
public interface CameraSystem extends GameSystem {

	/**
	 * @return The orthographic camera object.
	 */
	Camera getCamera();

	/**
	 * @return Whether the camera is in the middle of the rotation process.
	 */
	boolean isCameraRotating();

	/**
	 * @return Returns the last point the camera rotated around.
	 */
	Vector3 getRotationPoint(Vector3 output);
}
