package com.gadarts.isometric.systems.camera;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.gadarts.isometric.systems.SystemEventsSubscriber;

public interface CameraSystemEventsSubscriber extends SystemEventsSubscriber {
	default void onCameraSystemReady(final CameraSystem cameraSystem) {

	}

	default void onCameraMove(final OrthographicCamera camera) {

	}
}
