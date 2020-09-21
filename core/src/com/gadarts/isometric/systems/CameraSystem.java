package com.gadarts.isometric.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import lombok.Getter;

public class CameraSystem extends GameEntitySystem {
	private static final float FOV = 67f;
	private static final float NEAR = 0.1f;
	private static final float FAR = 200f;

	@Getter
	private final OrthographicCamera camera;

	public CameraSystem() {
		camera = createCamera();
		camera.direction.rotate(Vector3.Y,45);
		camera.direction.rotate(Vector3.X,-45);
		camera.position.set(4,3,4);
	}

	@Override
	public void update(final float deltaTime) {
		super.update(deltaTime);
		camera.update();
	}

	private OrthographicCamera createCamera() {
		OrthographicCamera cam = new OrthographicCamera(5, 5);
		cam.near = NEAR;
		cam.far = FAR;
		cam.update();
		return cam;
	}

	@Override
	public void dispose() {
	}
}
