package com.gadarts.isometric.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.EnemyComponent;
import com.gadarts.isometric.components.ModelInstanceComponent;
import com.gadarts.isometric.components.PickUpComponent;
import com.gadarts.isometric.systems.camera.CameraSystem;
import com.gadarts.isometric.systems.camera.CameraSystemEventsSubscriber;
import com.gadarts.isometric.systems.hud.HudSystem;
import com.gadarts.isometric.systems.hud.HudSystemEventsSubscriber;
import com.gadarts.isometric.systems.input.InputSystemEventsSubscriber;
import com.gadarts.isometric.systems.pickup.PickUpSystem;
import com.gadarts.isometric.systems.pickup.PickupSystemEventsSubscriber;
import com.gadarts.isometric.utils.map.MapGraphNode;

public class PickUpSystemImpl extends GameEntitySystem<PickupSystemEventsSubscriber>
		implements PickUpSystem,
		InputSystemEventsSubscriber,
		HudSystemEventsSubscriber,
		CameraSystemEventsSubscriber {

	private static final float PICK_UP_ROTATION = 10;
	private static final Vector3 auxVector3_1 = new Vector3();
	private static final Vector3 auxVector3_2 = new Vector3();

	private final float[] hsvArray = new float[3];
	private ImmutableArray<Entity> pickupsEntities;
	private Entity currentHighLightedPickup;
	private Entity itemToPickup;
	private ImmutableArray<Entity> enemiesEntities;

	@Override
	public void addedToEngine(final Engine engine) {
		super.addedToEngine(engine);
		pickupsEntities = engine.getEntitiesFor(Family.all(PickUpComponent.class).get());
		enemiesEntities = engine.getEntitiesFor(Family.all(EnemyComponent.class).get());
	}

	@Override
	public void update(final float deltaTime) {
		super.update(deltaTime);
		for (Entity pickup : pickupsEntities) {
			rotatePickup(deltaTime, pickup);
			PickUpComponent pickUpComponent = ComponentsMapper.pickup.get(pickup);
			flickerPickup(pickup, pickUpComponent);
		}
	}

	private void flickerPickup(final Entity pickup, final PickUpComponent pickUpComponent) {
		float flickerValue = pickUpComponent.getFlicker();
		Color color = ComponentsMapper.modelInstance.get(pickup).getColorAttribute().color;
		color.toHsv(hsvArray);
		float value = hsvArray[2];
		if (flickerValue > 0) {
			fadeOut(pickup, value < 1, Math.min(value + flickerValue, 1), value >= 1);
		} else {
			fadeOut(pickup, value > 0, Math.max(value + flickerValue, 0), value <= 0);
		}
	}

	private void fadeOut(final Entity pickup,
						 final boolean insideRange,
						 final float newValue,
						 final boolean reachedBound) {
		Color color = ComponentsMapper.modelInstance.get(pickup).getColorAttribute().color;
		if (insideRange) {
			hsvArray[2] = newValue;
			color.fromHsv(hsvArray);
		} else if (reachedBound) {
			PickUpComponent pickUpComponent = ComponentsMapper.pickup.get(pickup);
			pickUpComponent.setFlicker(-pickUpComponent.getFlicker());
		}
	}

	private void rotatePickup(final float deltaTime, final Entity pickup) {
		ModelInstanceComponent modelInstanceComponent = ComponentsMapper.modelInstance.get(pickup);
		ModelInstance modelInstance = modelInstanceComponent.getModelInstance();
		modelInstance.transform.rotate(Vector3.Y, deltaTime * PICK_UP_ROTATION);
	}

	@Override
	public void dispose() {

	}

	@Override
	public void onItemPickedUp(final Entity pickup) {
		PooledEngine engine = (PooledEngine) getEngine();
		engine.removeEntity(pickup);
	}

	@Override
	public Entity getCurrentHighLightedPickup() {
		return currentHighLightedPickup;
	}

	@Override
	public Entity getItemToPickup() {
		return itemToPickup;
	}

	@Override
	public void activate() {
		subscribers.forEach(sub -> sub.onPickUpSystemReady(PickUpSystemImpl.this));
	}

	private boolean handlePickupHighlight(final Ray ray, final Entity pickup, final MapGraphNode currentNode) {
		ModelInstance mic = ComponentsMapper.modelInstance.get(pickup).getModelInstance();
		Vector3 cen = mic.transform.getTranslation(auxVector3_1);
		ColorAttribute attr = (ColorAttribute) mic.materials.get(0).get(ColorAttribute.Emissive);
		boolean rayCheck = Intersector.intersectRayBoundsFast(ray, cen, auxVector3_2.set(0.5f, 0.5f, 0.5f));
		if (rayCheck && map.getEnemyFromNode(enemiesEntities, currentNode) == null) {
			attr.color.set(1, 1, 1, 1);
			return true;
		} else {
			attr.color.set(0, 0, 0, 0);
			return false;
		}
	}

	private boolean highlightPickupUnderMouse(final int screenX, final int screenY, final MapGraphNode currentNode) {
		Ray ray = getSystem(CameraSystem.class).getCamera().getPickRay(screenX, screenY);
		boolean result = false;
		for (Entity pickup : pickupsEntities) {
			result = handlePickupHighlight(ray, pickup, currentNode);
			if (result) {
				currentHighLightedPickup = pickup;
				break;
			}
		}
		return result;
	}

	@Override
	public void mouseMoved(final int screenX, final int screenY) {
		MapGraphNode newNode = map.getRayNode(screenX, screenY, getSystem(CameraSystem.class).getCamera());
		boolean foundPickup = highlightPickupUnderMouse(screenX, screenY, newNode);
		currentHighLightedPickup = foundPickup ? currentHighLightedPickup : null;
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

	@Override
	public void onHudSystemReady(final HudSystem hudSystem) {

	}

	@Override
	public void onPathCreated(final boolean pathToEnemy) {
		if (!pathToEnemy && getCurrentHighLightedPickup() != null) {
			itemToPickup = currentHighLightedPickup;
		}
	}

	@Override
	public void onCameraSystemReady(final CameraSystem cameraSystem) {
		addSystem(CameraSystem.class, cameraSystem);
	}
}
