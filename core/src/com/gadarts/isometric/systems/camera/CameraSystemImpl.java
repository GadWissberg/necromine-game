package com.gadarts.isometric.systems.camera;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.player.PlayerComponent;
import com.gadarts.isometric.systems.GameEntitySystem;
import com.gadarts.isometric.systems.hud.UserInterfaceSystem;
import com.gadarts.isometric.systems.hud.UserInterfaceSystemEventsSubscriber;
import com.gadarts.isometric.systems.input.InputSystemEventsSubscriber;
import com.gadarts.isometric.systems.player.PlayerSystem;
import com.gadarts.isometric.systems.player.PlayerSystemEventsSubscriber;
import com.gadarts.isometric.systems.render.RenderSystemEventsSubscriber;
import com.gadarts.necromine.model.GeneralUtils;

import static com.gadarts.isometric.NecronemesGame.*;
import static com.gadarts.isometric.utils.DefaultGameSettings.DEBUG_INPUT;
import static com.gadarts.isometric.utils.DefaultGameSettings.FULL_SCREEN;


public class CameraSystemImpl extends GameEntitySystem<CameraSystemEventsSubscriber>
		implements CameraSystem,
		PlayerSystemEventsSubscriber,
		InputSystemEventsSubscriber,
		UserInterfaceSystemEventsSubscriber,
		RenderSystemEventsSubscriber {

	public static final int CAMERA_HEIGHT = 15;
	public static final float FAR = 100f;
	private static final float NEAR = 0.01f;
	private static final Vector3 auxVector3_1 = new Vector3();
	private static final Vector3 auxVector3_2 = new Vector3();
	private static final Vector3 auxVector3_3 = new Vector3();
	private static final Vector2 auxVector2_1 = new Vector2();
	private static final float START_OFFSET = 7;
	private static final float MENU_CAMERA_ROTATION = 0.1F;
	private static final float EXTRA_LEVEL_PADDING = 16;
	private static final float[] MENU_CAMERA_POSITION = {14, CAMERA_HEIGHT, 20};
	private static final float INITIAL_CAMERA_ANGLE_AROUND_Y = 80;
	private final Vector2 lastRightPressMousePosition = new Vector2();
	private final Vector2 lastMousePosition = new Vector2();
	private boolean rotateCamera;
	public static OrthographicCamera camera;

	public CameraSystemImpl( ) {
	}

	@Override
	public void update(final float deltaTime) {
		super.update(deltaTime);
		UserInterfaceSystem userInterfaceSystem = getSystem(UserInterfaceSystem.class);
		Entity player = getSystem(PlayerSystem.class).getPlayer();
		if (!DEBUG_INPUT && !rotateCamera && !userInterfaceSystem.hasOpenWindows() && userInterfaceSystem.isMenuClosed()) {
			handleCameraFollow(player);
		}
		handleMenuRotation(player);
		camera.update();
	}

	private void handleMenuRotation(final Entity player) {
		if (ComponentsMapper.player.get(player).isDisabled()) {
			Decal decal = ComponentsMapper.characterDecal.get(player).getDecal();
			camera.rotateAround(decal.getPosition(), Vector3.Y, MENU_CAMERA_ROTATION);
		}
	}

	private void handleCameraFollow(final Entity player) {
		Vector3 playerPos = ComponentsMapper.characterDecal.get(player).getDecal().getPosition();
		Vector3 rotationPoint = GeneralUtils.defineRotationPoint(auxVector3_1, camera);
		Vector3 diff = auxVector3_2.set(playerPos).sub(rotationPoint);
		Vector3 cameraPosDest = auxVector3_3.set(camera.position).add(diff.x, 0, diff.z);
		camera.position.interpolate(cameraPosDest, 0.1F, Interpolation.bounce);
	}

	private void clampCameraPosition(final Vector3 pos) {
		pos.x = MathUtils.clamp(pos.x, -EXTRA_LEVEL_PADDING, services.getMapService().getMap().getWidth() + EXTRA_LEVEL_PADDING);
		pos.z = MathUtils.clamp(pos.z, -EXTRA_LEVEL_PADDING, services.getMapService().getMap().getDepth() + EXTRA_LEVEL_PADDING);
	}

	private void createAndInitCamera( ) {
		int viewportWidth = (FULL_SCREEN ? FULL_SCREEN_RESOLUTION_WIDTH : WINDOWED_RESOLUTION_WIDTH) / 75;
		int viewportHeight = (FULL_SCREEN ? FULL_SCREEN_RESOLUTION_HEIGHT : WINDOWED_RESOLUTION_HEIGHT) / 75;
		OrthographicCamera cam = new OrthographicCamera(viewportWidth, viewportHeight);
		cam.near = NEAR;
		cam.far = FAR;
		cam.update();
		camera = cam;
		initCamera();
	}

	private void initCamera( ) {
		Entity player = getEngine().getEntitiesFor(Family.all(PlayerComponent.class).get()).get(0);
		Vector2 nodePosition = ComponentsMapper.characterDecal.get(player).getNodePosition(auxVector2_1);
		camera.position.set(nodePosition.x + START_OFFSET, CAMERA_HEIGHT, nodePosition.y + START_OFFSET);
		camera.direction.rotate(Vector3.X, -45);
		camera.direction.rotate(Vector3.Y, INITIAL_CAMERA_ANGLE_AROUND_Y);
		camera.update();
	}

	@Override
	public void dispose( ) {
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
		if (rotateCamera && getSystem(UserInterfaceSystem.class).isMenuClosed()) {
			Entity player = getSystem(PlayerSystem.class).getPlayer();
			Vector3 rotationPoint = ComponentsMapper.characterDecal.get(player).getDecal().getPosition();
			camera.rotateAround(rotationPoint, Vector3.Y, (lastRightPressMousePosition.x - screenX) / 2f);
			clampCameraPosition(camera.position);
			lastRightPressMousePosition.set(screenX, screenY);
		}
	}


	@Override
	public Camera getCamera( ) {
		return camera;
	}

	@Override
	public boolean isCameraRotating( ) {
		return rotateCamera;
	}

	@Override
	public Vector3 getRotationPoint(final Vector3 output) {
		return output.set(0, 0, 0);
	}


	@Override
	public void activate( ) {
		createAndInitCamera();
		for (CameraSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onCameraSystemReady(this);
		}
	}

	@Override
	public void onHudSystemReady(final UserInterfaceSystem userInterfaceSystem) {
		addSystem(UserInterfaceSystem.class, userInterfaceSystem);
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
		camera.viewportWidth = (fullScreen ? FULL_SCREEN_RESOLUTION_WIDTH : WINDOWED_RESOLUTION_WIDTH) / 75F;
		camera.viewportHeight = (fullScreen ? FULL_SCREEN_RESOLUTION_HEIGHT : WINDOWED_RESOLUTION_HEIGHT) / 75F;
	}
}
