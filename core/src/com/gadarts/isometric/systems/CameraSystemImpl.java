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
		implements InputSystemEventsSubscriber,
		CameraSystem {

	public static final int VIEWPORT_WIDTH = IsometricGame.RESOLUTION_WIDTH / 75;
	public static final int VIEWPORT_HEIGHT = IsometricGame.RESOLUTION_HEIGHT / 75;
	public static final float SCROLL_SCALE_HORIZONTAL = 0.04f;
	public static final float SCROLL_SCALE_VERTICAL = 0.09f;
	public static final int CAMERA_HEIGHT = 6;
	private static final float FAR = 100f;
	private static final float NEAR = 0.01f;
	private static final Plane groundPlane = new Plane(new Vector3(0, 1, 0), 0);
	private static final Vector3 auxVector3 = new Vector3();
	private static final float SCROLL_OFFSET = 100;

	private final Vector2 lastRightPressMousePosition = new Vector2();
	private final Vector2 lastMousePosition = new Vector2();
	private final Vector3 rotationPoint = new Vector3();

	@Getter
	private OrthographicCamera camera;

	@Getter
	private boolean rotateCamera;

	@Override
	public void update(final float deltaTime) {
		super.update(deltaTime);
		handleHorizontalScroll();
		handleVerticalScroll();
		camera.update();
	}

	private void handleHorizontalScroll() {
		if (lastMousePosition.x >= IsometricGame.RESOLUTION_WIDTH - SCROLL_OFFSET) {
			Vector3 direction = auxVector3.set(camera.direction).crs(camera.up).nor().scl(SCROLL_SCALE_HORIZONTAL);
			camera.translate(direction.x, 0, direction.z);
		} else if (lastMousePosition.x <= SCROLL_OFFSET) {
			Vector3 direction = auxVector3.set(camera.direction).crs(camera.up).nor().scl(-SCROLL_SCALE_HORIZONTAL);
			camera.translate(direction.x, 0, direction.z);
		}
	}

	private void handleVerticalScroll() {
		if (lastMousePosition.y >= IsometricGame.RESOLUTION_HEIGHT - SCROLL_OFFSET) {
			Vector3 direction = auxVector3.set(camera.direction).nor().scl(-SCROLL_SCALE_VERTICAL);
			camera.translate(direction.x, 0, direction.z);
		} else if (lastMousePosition.y <= SCROLL_OFFSET) {
			Vector3 direction = auxVector3.set(camera.direction).nor().scl(SCROLL_SCALE_VERTICAL);
			camera.translate(direction.x, 0, direction.z);
		}
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
		lastMousePosition.set(screenX, screenY);
	}

	@Override
	public void touchDown(final int screenX, final int screenY, final int button) {
		if (button == Input.Buttons.RIGHT) {
			Ray ray = camera.getPickRay(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f);
			Intersector.intersectRayPlane(ray, groundPlane, rotationPoint);
			rotateCamera = true;
			lastRightPressMousePosition.set(screenX, screenY);
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
			camera.rotateAround(rotationPoint, Vector3.Y, (lastRightPressMousePosition.x - screenX) / 2f);
			lastRightPressMousePosition.set(screenX, screenY);
		}
	}

	@Override
	public void init() {
		camera = createCamera();
		camera.position.set(4, CAMERA_HEIGHT, 4);
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
