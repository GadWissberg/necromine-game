package com.gadarts.isometric.systems;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.isometric.IsometricGame;
import lombok.Getter;

public class CameraSystem extends GameEntitySystem {
	private static final float NEAR = 0.1f;
	public static final int VIEWPORT_WIDTH = IsometricGame.RESOLUTION_WIDTH / 100;
	public static final int VIEWPORT_HEIGHT = IsometricGame.RESOLUTION_HEIGHT / 100;
	private static final float FAR = 100f;

	@Getter
	private final OrthographicCamera camera;

	public CameraSystem() {
		camera = createCamera();
		camera.position.set(4, 3, 4);
		camera.direction.rotate(Vector3.X, -45);
		camera.direction.rotate(Vector3.Y, 45);
	}

	@Override
	public void update(final float deltaTime) {
		super.update(deltaTime);
		camera.update();
	}

	private OrthographicCamera createCamera() {
		OrthographicCamera cam = new OrthographicCamera(VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
		cam.near = NEAR;
		cam.far = FAR;
		cam.update();
		return cam;
	}

	@Override
	public void dispose() {
	}
}
