package com.gadarts.isometric.systems.camera;

import com.badlogic.gdx.graphics.OrthographicCamera;

/**
 * Handles camera logic.
 */
public interface CameraSystem {

    OrthographicCamera getCamera();

    boolean isCameraRotating();
}
