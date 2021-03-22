package com.gadarts.isometric.systems.camera;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.gadarts.isometric.NecromineGame;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.player.PlayerComponent;
import com.gadarts.isometric.systems.GameEntitySystem;
import com.gadarts.isometric.systems.hud.HudSystem;
import com.gadarts.isometric.systems.hud.HudSystemEventsSubscriber;
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
    public static final int CAMERA_HEIGHT = 10;
    public static final float FAR = 100f;
    private static final float NEAR = 0.01f;
    private static final Plane groundPlane = new Plane(new Vector3(0, 1, 0), 0);
    private static final Vector3 auxVector3_1 = new Vector3();
    private static final Vector3 auxVector3_2 = new Vector3();
    private static final Vector3 auxVector3_3 = new Vector3();
    private static final float SCROLL_OFFSET = 100;
    private static final Vector2 auxVector2_1 = new Vector2();
    private static final float START_OFFSET = 7;
    private final Vector2 lastRightPressMousePosition = new Vector2();
    private final Vector2 lastMousePosition = new Vector2();
    private final Vector3 rotationPoint = new Vector3();
    private boolean rotateCamera;
    @Getter
    private OrthographicCamera camera;

    @Override
    public void update(final float deltaTime) {
        super.update(deltaTime);
        HudSystem hudSystem = getSystem(HudSystem.class);
        if (!DefaultGameSettings.DEBUG_INPUT && !rotateCamera && !hudSystem.hasOpenWindows() && !hudSystem.isMenuOpen()) {
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
        Vector3 originalPosition = auxVector3_3.set(camera.position);
        Vector3 newPosition = auxVector3_2.set(camera.position).add(direction.x, 0, direction.z);
        auxVector3_1.x = clampTranslation(camera.position.x, newPosition.x);
        auxVector3_1.y = camera.position.y;
        auxVector3_1.z = clampTranslation(camera.position.z, newPosition.z);
        camera.position.interpolate(auxVector3_1, alpha, Interpolation.smooth2);
        cameraMoved(auxVector3_1.set(camera.position).sub(originalPosition));
    }

    private void cameraMoved(final Vector3 delta) {
        for (CameraSystemEventsSubscriber subscriber : subscribers) {
            subscriber.onCameraMove(camera);
        }
        rotationPoint.add(delta);
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

    private void createAndInitCamera() {
        OrthographicCamera cam = new OrthographicCamera(VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
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
        Ray ray = camera.getPickRay(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f);
        Intersector.intersectRayPlane(ray, groundPlane, rotationPoint);
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
        if (rotateCamera && !getSystem(HudSystem.class).isMenuOpen()) {
            camera.rotateAround(rotationPoint, Vector3.Y, (lastRightPressMousePosition.x - screenX) / 2f);
            lastRightPressMousePosition.set(screenX, screenY);
        }
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
    public void onHudSystemReady(final HudSystem hudSystem) {
        addSystem(HudSystem.class, hudSystem);
    }


}
