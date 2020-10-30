package com.gadarts.isometric.systems.camera;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.gadarts.isometric.systems.GameSystem;

/**
 * Handles camera logic.
 */
public interface CameraSystem extends GameSystem {

    OrthographicCamera getCamera();

    boolean isCameraRotating();
}
