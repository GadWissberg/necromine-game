package com.gadarts.isometric.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.gadarts.isometric.IsometricGame;
import com.gadarts.isometric.components.*;
import com.gadarts.isometric.systems.camera.CameraSystem;
import com.gadarts.isometric.systems.camera.CameraSystemEventsSubscriber;
import com.gadarts.isometric.systems.hud.HudSystem;
import com.gadarts.isometric.systems.hud.HudSystemEventsSubscriber;
import com.gadarts.isometric.systems.input.InputSystemEventsSubscriber;
import com.gadarts.isometric.systems.player.PlayerSystemEventsSubscriber;
import com.gadarts.isometric.utils.EntityBuilder;
import com.gadarts.isometric.utils.map.MapGraph;
import com.gadarts.isometric.utils.map.MapGraphNode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class HudSystemImpl extends GameEntitySystem<HudSystemEventsSubscriber> implements
		HudSystem,
		InputSystemEventsSubscriber,
		PlayerSystemEventsSubscriber,
		CameraSystemEventsSubscriber {
	public static final Color CURSOR_REGULAR = Color.YELLOW;
	private static final Vector3 auxVector3_1 = new Vector3();
	private static final Vector3 auxVector3_2 = new Vector3();
	private static final Vector3 auxVector3_3 = new Vector3();
	private static final Vector3 auxVector3_4 = new Vector3();
	private static final Vector3 auxVector3_5 = new Vector3();
	private static final Color CURSOR_ATTACK = Color.RED;
	private final MapGraph map;
	private final List<Entity> attackNodesEntities = new ArrayList<>();
	@Getter
	private ModelInstance cursorModelInstance;
	private ImmutableArray<Entity> enemiesEntities;
	private Model attackNodeModel;
	private CameraSystem cameraSystem;
	private Stage stage;
	private ImmutableArray<Entity> pickupEntities;

	public HudSystemImpl(final MapGraph map) {
		this.map = map;
	}

	@Override
	public void dispose() {
		attackNodeModel.dispose();
	}

	@Override
	public void addedToEngine(final Engine engine) {
		super.addedToEngine(engine);
		stage = new Stage(new FitViewport(IsometricGame.RESOLUTION_WIDTH, IsometricGame.RESOLUTION_HEIGHT));
		Entity cursorEntity = engine.getEntitiesFor(Family.all(CursorComponent.class).get()).first();
		cursorModelInstance = ComponentsMapper.modelInstance.get(cursorEntity).getModelInstance();
		enemiesEntities = engine.getEntitiesFor(Family.all(EnemyComponent.class).get());
		pickupEntities = engine.getEntitiesFor(Family.all(PickUpComponent.class).get());
		attackNodeModel = createAttackNodeModel();
		createAttackNodesEntities();
	}

	private void createAttackNodesEntities() {
		IntStream.range(0, 8).forEach(i ->
				attackNodesEntities.add(EntityBuilder.beginBuildingEntity((PooledEngine) getEngine())
						.addModelInstanceComponent(new ModelInstance(attackNodeModel), false)
						.finishAndAddToEngine()
				));
	}

	private Model createAttackNodeModel() {
		ModelBuilder builder = new ModelBuilder();
		builder.begin();
		Material material = new Material(ColorAttribute.createDiffuse(HudSystemImpl.CURSOR_ATTACK));
		MeshPartBuilder meshPartBuilder = builder.part(
				"attack_node_1",
				GL20.GL_LINES,
				VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
				material);

		meshPartBuilder.rect(
				auxVector3_1.set(1, 0, 1),
				auxVector3_2.set(1, 0, 0),
				auxVector3_3.set(0, 0, 0),
				auxVector3_4.set(0, 0, 1),
				auxVector3_5.set(0, -1, 0));

		return builder.end();
	}

	@Override
	public void mouseMoved(final int screenX, final int screenY) {
		MapGraphNode newNode = map.getRayNode(screenX, screenY, cameraSystem.getCamera());
		MapGraphNode oldNode = map.getNode(cursorModelInstance.transform.getTranslation(auxVector3_2));
		if (!newNode.equals(oldNode)) {
			cursorModelInstance.transform.setTranslation(newNode.getX(), 0, newNode.getY());
			colorizeCursor(newNode);
		}
		highlightPickupsUnderMouse(screenX, screenY);
	}

	private void highlightPickupsUnderMouse(final int screenX, final int screenY) {
		Ray ray = cameraSystem.getCamera().getPickRay(screenX, screenY);
		for (Entity pickup : pickupEntities) {
			handlePickupHighlight(ray, pickup);
		}
	}

	private void handlePickupHighlight(final Ray ray, final Entity pickup) {
		ModelInstanceComponent mic = ComponentsMapper.modelInstance.get(pickup);
		Vector3 center = mic.getModelInstance().transform.getTranslation(auxVector3_1);
		ColorAttribute attr = (ColorAttribute) mic.getModelInstance().materials.get(0).get(ColorAttribute.Emissive);
		if (Intersector.intersectRayBoundsFast(ray, center, auxVector3_2.set(0.5f, 0.5f, 0.5f))) {
			attr.color.set(1, 1, 1, 1);
		} else {
			attr.color.set(0, 0, 0, 0);
		}
	}

	private void colorizeCursor(final MapGraphNode newNode) {
		if (map.getEnemyFromNode(enemiesEntities, newNode) != null) {
			setCursorColor(CURSOR_ATTACK);
		} else {
			setCursorColor(CURSOR_REGULAR);
		}
	}

	private void setCursorColor(final Color color) {
		Material material = cursorModelInstance.materials.get(0);
		ColorAttribute colorAttribute = (ColorAttribute) material.get(ColorAttribute.Diffuse);
		colorAttribute.color.set(color);
	}


	@Override
	public void touchDown(final int screenX, final int screenY, final int button) {

	}

	@Override
	public void update(final float deltaTime) {
		super.update(deltaTime);
		stage.act();
	}

	private void displayAttackNodes(final List<MapGraphNode> availableNodes) {
		for (int i = 0; i < availableNodes.size(); i++) {
			Entity attackNodeEntity = attackNodesEntities.get(i);
			ModelInstanceComponent modelInstanceComp = ComponentsMapper.modelInstance.get(attackNodeEntity);
			modelInstanceComp.setVisible(true);
			MapGraphNode availableNode = availableNodes.get(i);
			ModelInstance modelInstance = modelInstanceComp.getModelInstance();
			modelInstance.transform.setTranslation(availableNode.getX(), 0, availableNode.getY());
		}
	}


	@Override
	public void touchUp(final int screenX, final int screenY, final int button) {

	}

	@Override
	public void touchDragged(final int screenX, final int screenY) {

	}

	@Override
	public void onPlayerFinishedTurn() {

	}

	@Override
	public void onAttackModeActivated(final List<MapGraphNode> availableNodes) {
		hideAttackNodes();
		displayAttackNodes(availableNodes);
	}

	private void hideAttackNodes() {
		for (Entity attackNodeEntity : attackNodesEntities) {
			ModelInstanceComponent modelInstanceComp = ComponentsMapper.modelInstance.get(attackNodeEntity);
			modelInstanceComp.setVisible(false);
		}
	}

	@Override
	public void onAttackModeDeactivated() {
		hideAttackNodes();
	}

	@Override
	public void init() {
		for (HudSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onHudSystemReady(this);
		}
	}

	@Override
	public void onCameraSystemReady(final CameraSystem cameraSystem) {
		this.cameraSystem = cameraSystem;
	}

	@Override
	public Stage getStage() {
		return stage;
	}
}
