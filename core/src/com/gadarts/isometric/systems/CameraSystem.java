package com.gadarts.isometric.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import lombok.Getter;

public class CameraSystem extends GameEntitySystem {
	private static final float FOV = 67f;
	private static final float NEAR = 0.1f;
	private static final float FAR = 200f;

	@Getter
	private final PerspectiveCamera camera;

	public CameraSystem() {
		camera = createCamera();
	}

	@Override
	public void update(final float deltaTime) {
		super.update(deltaTime);
		camera.update();
	}

	private PerspectiveCamera createCamera() {
		PerspectiveCamera cam = new PerspectiveCamera(FOV, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.near = NEAR;
		cam.far = FAR;
		cam.update();
		return cam;
	}

	@Override
	public void dispose() {
	}
}
