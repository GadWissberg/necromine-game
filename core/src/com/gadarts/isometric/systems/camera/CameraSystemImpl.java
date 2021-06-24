package com.gadarts.isometric.systems.camera;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.collision.Ray;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.player.PlayerComponent;
import com.gadarts.isometric.systems.GameEntitySystem;
import com.gadarts.isometric.systems.hud.InterfaceSystem;
import com.gadarts.isometric.systems.hud.InterfaceSystemEventsSubscriber;
import com.gadarts.isometric.systems.input.InputSystemEventsSubscriber;
import com.gadarts.isometric.systems.player.PlayerSystem;
import com.gadarts.isometric.systems.player.PlayerSystemEventsSubscriber;
import com.gadarts.isometric.systems.render.RenderSystemEventsSubscriber;
import lombok.Getter;

import static com.gadarts.isometric.NecromineGame.*;
import static com.gadarts.isometric.utils.DefaultGameSettings.DEBUG_INPUT;
import static com.gadarts.isometric.utils.DefaultGameSettings.FULL_SCREEN;


public class CameraSystemImpl extends GameEntitySystem<CameraSystemEventsSubscriber>
		implements CameraSystem,
		PlayerSystemEventsSubscriber,
		InputSystemEventsSubscriber,
		InterfaceSystemEventsSubscriber,
		RenderSystemEventsSubscriber {

	public static final int CAMERA_HEIGHT = 15;
	public static final float FAR = 100f;
	public static final float CAMERA_ACCELERATION_SCALE = 0.24f;
	private static final float NEAR = 0.01f;
	private static final Plane groundPlane = new Plane(new Vector3(0, 1, 0), 0);
	private static final Vector3 auxVector3_1 = new Vector3();
	private static final Vector3 auxVector3_2 = new Vector3();
	private static final Vector3 auxVector3_3 = new Vector3();
	private static final float SCROLL_OFFSET = 100;
	private static final Vector2 auxVector2_1 = new Vector2();
	private static final float START_OFFSET = 7;
	private static final float MENU_CAMERA_ROTATION = 0.1F;
	private static final float CAMERA_MAX_SPEED = 32F;
	private static final float CAMERA_MIN_SPEED = 0.01F;
	private static final float CAMERA_DECELERATION_SCALE = 0.9F;
	private static final Vector3 rotationPoint = new Vector3();
	private static final float EXTRA_LEVEL_PADDING = 16;
	private static final float[] MENU_CAMERA_POSITION = {14, CAMERA_HEIGHT, 20};
	private final Vector2 lastRightPressMousePosition = new Vector2();
	private final Vector2 lastMousePosition = new Vector2();
	private final Vector3 cameraSpeed = new Vector3();
	private boolean rotateCamera;
	@Getter
	private OrthographicCamera camera;

	public CameraSystemImpl() {
	}

	@Override
	public void update(final float deltaTime) {
		super.update(deltaTime);
		InterfaceSystem interfaceSystem = getSystem(InterfaceSystem.class);
		if (!DEBUG_INPUT && !rotateCamera && !interfaceSystem.hasOpenWindows() && interfaceSystem.isMenuClosed()) {
			handleScrolling(deltaTime);
		}
		if (ComponentsMapper.player.get(getSystem(PlayerSystem.class).getPlayer()).isDisabled()) {
			camera.rotateAround(rotationPoint, Vector3.Y, MENU_CAMERA_ROTATION);
		}
		camera.update();
	}


	private void handleScrolling(final float deltaTime) {
		boolean moved = handleHorizontalScroll();
		moved |= handleVerticalScroll();
		if (!moved) {
			decelerateCamera();
		}
		if (!cameraSpeed.isZero()) {
			handleCameraTranslation(deltaTime);
		}
	}

	private void handleCameraTranslation(final float deltaTime) {
		Vector3 newPosition = auxVector3_3.set(camera.position).add(auxVector3_1.set(cameraSpeed).scl(deltaTime));
		clampCameraPosition(newPosition);
		auxVector3_2.set(camera.position);
		camera.position.set(newPosition);
		auxVector3_2.sub(newPosition);
		cameraMoved();
	}

	private void clampCameraPosition(final Vector3 pos) {
		pos.x = MathUtils.clamp(pos.x, -EXTRA_LEVEL_PADDING, services.getMapService().getMap().getWidth() + EXTRA_LEVEL_PADDING);
		pos.z = MathUtils.clamp(pos.z, -EXTRA_LEVEL_PADDING, services.getMapService().getMap().getDepth() + EXTRA_LEVEL_PADDING);
	}

	private boolean handleHorizontalScroll() {
		if (lastMousePosition.x >= Gdx.graphics.getWidth() - SCROLL_OFFSET) {
			horizontalTranslateCamera(CAMERA_ACCELERATION_SCALE);
			return true;
		} else if (lastMousePosition.x <= SCROLL_OFFSET) {
			horizontalTranslateCamera(-CAMERA_ACCELERATION_SCALE);
			return true;
		}
		return false;
	}

	private void decelerateCamera() {
		float speedSize = cameraSpeed.len2();
		if (speedSize < CAMERA_MIN_SPEED) {
			cameraSpeed.setZero();
		} else {
			cameraSpeed.scl(CAMERA_DECELERATION_SCALE);
		}
	}

	private void horizontalTranslateCamera(final float scrollScaleHorizontal) {
		Vector3 direction = auxVector3_1.set(camera.direction).crs(camera.up).nor().scl(scrollScaleHorizontal);
		float speedSize = cameraSpeed.add(direction).len2();
		if (speedSize > CAMERA_MAX_SPEED) {
			cameraSpeed.setLength2(CAMERA_MAX_SPEED);
		}
	}

	private void verticalTranslateCamera(final float scrollScaleVertical) {
		Vector3 direction = auxVector3_1.set(camera.direction.x, 0, camera.direction.z).nor().scl(scrollScaleVertical);
		float speedSize = cameraSpeed.add(direction).len2();
		if (speedSize > CAMERA_MAX_SPEED) {
			cameraSpeed.setLength2(CAMERA_MAX_SPEED);
		}
	}

	@SuppressWarnings("SameParameterValue")
	private void cameraMoved() {
		for (CameraSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onCameraMove(camera);
		}
	}

	private boolean handleVerticalScroll() {
		if (lastMousePosition.y >= Gdx.graphics.getHeight() - SCROLL_OFFSET) {
			verticalTranslateCamera(-CAMERA_ACCELERATION_SCALE);
			return true;
		} else if (lastMousePosition.y <= SCROLL_OFFSET) {
			verticalTranslateCamera(CAMERA_ACCELERATION_SCALE);
			return true;
		}
		return false;
	}

	private void createAndInitCamera() {
		int viewportWidth = (FULL_SCREEN ? FULL_SCREEN_RESOLUTION_WIDTH : WINDOWED_RESOLUTION_WIDTH) / 75;
		int viewportHeight = (FULL_SCREEN ? FULL_SCREEN_RESOLUTION_HEIGHT : WINDOWED_RESOLUTION_HEIGHT) / 75;
		OrthographicCamera cam = new OrthographicCamera(viewportWidth, viewportHeight);
		cam.near = NEAR;
		cam.far = FAR;
		cam.update();
		camera = cam;
		initCamera();
	}

	private void initCamera() {
		Entity player = getEngine().getEntitiesFor(Family.all(PlayerComponent.class).get()).get(0);
		Vector2 nodePosition = ComponentsMapper.characterDecal.get(player).getNodePosition(auxVector2_1);
		camera.position.set(nodePosition.x + START_OFFSET, CAMERA_HEIGHT, nodePosition.y + START_OFFSET);
		camera.direction.rotate(Vector3.X, -45);
		camera.direction.rotate(Vector3.Y, 45);
		camera.update();
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
		if (rotateCamera && getSystem(InterfaceSystem.class).isMenuClosed()) {
			defineRotationPoint(rotationPoint);
			camera.rotateAround(rotationPoint, Vector3.Y, (lastRightPressMousePosition.x - screenX) / 2f);
			clampCameraPosition(camera.position);
			lastRightPressMousePosition.set(screenX, screenY);
		}
	}

	private void defineRotationPoint(final Vector3 positionVector) {
		Ray ray = camera.getPickRay(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f);
		Intersector.intersectRayPlane(ray, groundPlane, positionVector);
	}


	@Override
	public boolean isCameraRotating() {
		return rotateCamera;
	}

	@Override
	public Vector3 getRotationPoint(final Vector3 output) {
		return output.set(rotationPoint);
	}


	@Override
	public void activate() {
		createAndInitCamera();
		for (CameraSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onCameraSystemReady(this);
		}
	}

	@Override
	public void onHudSystemReady(final InterfaceSystem interfaceSystem) {
		addSystem(InterfaceSystem.class, interfaceSystem);
	}

	@Override
	public void onPlayerSystemReady(final PlayerSystem playerSystem, final Entity player) {
		addSystem(PlayerSystem.class, playerSystem);
		if (ComponentsMapper.player.get(getSystem(PlayerSystem.class).getPlayer()).isDisabled()) {
			camera.position.set(MENU_CAMERA_POSITION);
		}
	}

	@Override
	public void onFullScreenToggle(final boolean fullScreen) {
		camera.viewportWidth = (fullScreen ? FULL_SCREEN_RESOLUTION_WIDTH : WINDOWED_RESOLUTION_WIDTH) / 75;
		camera.viewportHeight = (fullScreen ? FULL_SCREEN_RESOLUTION_HEIGHT : WINDOWED_RESOLUTION_HEIGHT) / 75;
	}
}
