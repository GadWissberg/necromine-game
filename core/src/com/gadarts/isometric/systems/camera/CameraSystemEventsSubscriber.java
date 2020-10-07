package com.gadarts.isometric.systems.camera;

import com.gadarts.isometric.systems.SystemEventsSubscriber;

public interface CameraSystemEventsSubscriber extends SystemEventsSubscriber {
	void onCameraSystemReady(CameraSystem cameraSystem);

}
