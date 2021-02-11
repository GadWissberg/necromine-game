package com.gadarts.isometric.systems.camera;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.collision.Ray;
import com.gadarts.isometric.NecromineGame;
import com.gadarts.isometric.systems.GameEntitySystem;
import com.gadarts.isometric.systems.hud.HudSystem;
import com.gadarts.isometric.systems.hud.HudSystemEventsSubscriber;
import com.gadarts.isometric.systems.input.InputSystem;
import com.gadarts.isometric.systems.input.InputSystemEventsSubscriber;
import com.gadarts.isometric.utils.DefaultGameSettings;
import lombok.Getter;

import static com.gadarts.isometric.utils.map.MapGraph.MAP_SIZE;

public class CameraSystemImpl extends GameEntitySystem<CameraSystemEventsSubscriber>
		implements CameraSystem,
		InputSystemEventsSubscriber,
		HudSystemEventsSubscriber {

	public static final int VIEWPORT_WIDTH = NecromineGame.RESOLUTION_WIDTH / 75;
	public static final int VIEWPORT_HEIGHT = NecromineGame.RESOLUTION_HEIGHT / 75;
	public static final float SCROLL_SCALE = 0.12f;
	public static final int CAMERA_HEIGHT = 6;
	public static final float FAR = 100f;
	private static final float NEAR = 0.01f;
	private static final Plane groundPlane = new Plane(new Vector3(0, 1, 0), 0);
	private static final Vector3 auxVector3_1 = new Vector3();
	private static final Vector3 auxVector3_2 = new Vector3();
	private static final float SCROLL_OFFSET = 50;

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
		if (!DefaultGameSettings.DEBUG_INPUT && !rotateCamera && !getSystem(HudSystem.class).hasOpenWindows()) {
			handleHorizontalScroll();
			handleVerticalScroll();
		}
		camera.update();
	}

	private void handleHorizontalScroll() {
		if (lastMousePosition.x >= NecromineGame.RESOLUTION_WIDTH - SCROLL_OFFSET) {
			float diff = NecromineGame.RESOLUTION_WIDTH - SCROLL_OFFSET - lastMousePosition.x;
			horizontalTranslateCamera(SCROLL_SCALE, diff);
		} else if (lastMousePosition.x <= SCROLL_OFFSET) {
			float diff = SCROLL_OFFSET - lastMousePosition.x;
			horizontalTranslateCamera(-SCROLL_SCALE, diff);
		}
	}

	private void horizontalTranslateCamera(final float scrollScaleHorizontal, final float diff) {
		float abs = Math.abs(diff);
		float cameraHorizontalInterpolationAlpha = MathUtils.norm(0, SCROLL_OFFSET, abs);
		Vector3 direction = auxVector3_1.set(camera.direction).crs(camera.up).nor().scl(scrollScaleHorizontal);
		setCameraTranslationClamped(direction, cameraHorizontalInterpolationAlpha);
	}

	private void setCameraTranslationClamped(final Vector3 direction, final float alpha) {
		Vector3 newPosition = auxVector3_2.set(camera.position).add(direction.x, 0, direction.z);
		float cameraX = camera.position.x;
		float cameraZ = camera.position.z;
		auxVector3_1.x = clampTranslation(cameraX, newPosition.x);
		auxVector3_1.y = camera.position.y;
		auxVector3_1.z = clampTranslation(cameraZ, newPosition.z);
		camera.position.interpolate(auxVector3_1, alpha, Interpolation.smooth2);
		for (CameraSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onCameraMove(camera);
		}
	}

	private float clampTranslation(final float cameraFrag, final float newPositionFrag) {
		boolean canMoveWhenCameraOnLeftEdge = cameraFrag < 0 && newPositionFrag >= cameraFrag;
		boolean canMoveWhenCameraOnRightEdge = cameraFrag > MAP_SIZE && newPositionFrag <= cameraFrag;
		boolean isInSideMap = cameraFrag >= 0 && cameraFrag <= MAP_SIZE;
		if (canMoveWhenCameraOnLeftEdge || canMoveWhenCameraOnRightEdge || isInSideMap) {
			return newPositionFrag;
		} else {
			return cameraFrag;
		}
	}

	private void handleVerticalScroll() {
		if (lastMousePosition.y >= NecromineGame.RESOLUTION_HEIGHT - SCROLL_OFFSET) {
			float diff = NecromineGame.RESOLUTION_HEIGHT - SCROLL_OFFSET - lastMousePosition.y;
			verticalTranslateCamera(-SCROLL_SCALE, diff);
		} else if (lastMousePosition.y <= SCROLL_OFFSET) {
			float diff = SCROLL_OFFSET - lastMousePosition.y;
			verticalTranslateCamera(SCROLL_SCALE, diff);
		}
	}

	private void verticalTranslateCamera(final float v, final float diff) {
		float abs = Math.abs(diff);
		float cameraVerticalInterpolationAlpha = MathUtils.norm(0, SCROLL_OFFSET, abs);
		Vector3 direction = auxVector3_1.set(camera.direction).nor().scl(v);
		setCameraTranslationClamped(direction, cameraVerticalInterpolationAlpha);
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
	public void inputSystemReady(final InputSystem inputSystem) {

	}


	@Override
	public boolean isCameraRotating() {
		return rotateCamera;
	}


	@Override
	public void activate() {
		camera = createCamera();
		camera.position.set(4, CAMERA_HEIGHT, 4);
		camera.direction.rotate(Vector3.X, -45);
		camera.direction.rotate(Vector3.Y, 45);
		for (CameraSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onCameraSystemReady(this);
		}
	}

	@Override
	public void onHudSystemReady(final HudSystem hudSystem) {
		addSystem(HudSystem.class, hudSystem);
	}


}
