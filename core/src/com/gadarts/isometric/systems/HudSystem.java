package com.gadarts.isometric.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.CursorComponent;

public class HudSystem extends GameEntitySystem implements InputSystemEventsSubscriber {
	private static final Vector3 auxVector = new Vector3();
	private static final Plane floorPlane = new Plane(new Vector3(0, 1, 0), 0);
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
		Ray ray = camera.getPickRay(screenX, screenY);
		Intersector.intersectRayPlane(ray, floorPlane, auxVector);
		auxVector.x = MathUtils.round(auxVector.x);
		auxVector.y = 0;
		auxVector.z = MathUtils.round(auxVector.z);
		cursorModelInstance.transform.setTranslation(auxVector);
	}
}
