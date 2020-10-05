package com.gadarts.isometric.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.CursorComponent;
import com.gadarts.isometric.systems.input.InputSystemEventsSubscriber;
import com.gadarts.isometric.utils.Utils;
import lombok.Getter;

public class HudSystem extends GameEntitySystem implements InputSystemEventsSubscriber {
	private static final Vector3 auxVector = new Vector3();

	@Getter
	private ModelInstance cursorModelInstance;
	private OrthographicCamera camera;

	@Override
	public void dispose() {

	}

	@Override
	public void addedToEngine(final Engine engine) {
		super.addedToEngine(engine);
		Entity cursorEntity = engine.getEntitiesFor(Family.all(CursorComponent.class).get()).first();
		cursorModelInstance = ComponentsMapper.modelInstance.get(cursorEntity).getModelInstance();
		camera = engine.getSystem(CameraSystem.class).getCamera();
	}

	@Override
	public void mouseMoved(final int screenX, final int screenY) {
		Vector3 position = Utils.calculateGridPositionFromMouse(camera, screenX, screenY, auxVector);
		position.set(Math.max(position.x, 0), Math.max(position.y, 0), Math.max(position.z, 0));
		cursorModelInstance.transform.setTranslation(position);
	}

	@Override
	public void touchDown(final int screenX, final int screenY, final int button) {

	}

	@Override
	public void touchUp(final int screenX, final int screenY, final int button) {

	}

	@Override
	public void touchDragged(final int screenX, final int screenY) {

	}
}
