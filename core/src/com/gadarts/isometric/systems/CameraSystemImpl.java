package com.gadarts.isometric.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.gadarts.isometric.IsometricGame;
import com.gadarts.isometric.systems.camera.CameraSystem;
import com.gadarts.isometric.systems.camera.CameraSystemEventsSubscriber;
import com.gadarts.isometric.systems.input.InputSystemEventsSubscriber;
import lombok.Getter;

public class CameraSystemImpl extends GameEntitySystem<CameraSystemEventsSubscriber>
		implements InputSystemEventsSubscriber, CameraSystem {

	public static final int VIEWPORT_WIDTH = IsometricGame.RESOLUTION_WIDTH / 75;
	public static final int VIEWPORT_HEIGHT = IsometricGame.RESOLUTION_HEIGHT / 75;
	private static final float NEAR = 0.1f;
	private static final float FAR = 100f;
	private static final Plane groundPlane = new Plane(new Vector3(0, 1, 0), 0);
	private final Vector2 lastMousePosition = new Vector2();
	private final Vector3 rotationPoint = new Vector3();
	@Getter
	private OrthographicCamera camera;
	@Getter
	private boolean rotateCamera;

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

	@Override
	public void mouseMoved(final int screenX, final int screenY) {
	}

	@Override
	public void touchDown(final int screenX, final int screenY, final int button) {
		if (button == Input.Buttons.RIGHT) {
			Ray ray = camera.getPickRay(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f);
			Intersector.intersectRayPlane(ray, groundPlane, rotationPoint);
			rotateCamera = true;
			lastMousePosition.set(screenX, screenY);
		}
	}

	@Override
	public void touchUp(final int screenX, final int screenY, final int button) {
		if (button == Input.Buttons.RIGHT) {
			rotateCamera = false;
		}
	}

	@Override
	public void touchDragged(final int screenX, final int screenY) {
		if (rotateCamera) {
			camera.rotateAround(rotationPoint, Vector3.Y, (lastMousePosition.x - screenX) / 2f);
			lastMousePosition.set(screenX, screenY);
		}
	}

	@Override
	public void init() {
		camera = createCamera();
		camera.position.set(4, 3, 4);
		camera.direction.rotate(Vector3.X, -45);
		camera.direction.rotate(Vector3.Y, 45);
		for (CameraSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onCameraSystemReady(this);
		}
	}

	@Override
	public boolean isCameraRotating() {
		return rotateCamera;
	}
}
