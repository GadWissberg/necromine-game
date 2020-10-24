package com.gadarts.isometric.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.gadarts.isometric.IsometricGame;
import com.gadarts.isometric.components.*;
import com.gadarts.isometric.systems.camera.CameraSystem;
import com.gadarts.isometric.systems.camera.CameraSystemEventsSubscriber;
import com.gadarts.isometric.systems.character.CharacterCommand;
import com.gadarts.isometric.systems.character.CharacterSystem;
import com.gadarts.isometric.systems.character.CharacterSystemEventsSubscriber;
import com.gadarts.isometric.systems.hud.HudSystem;
import com.gadarts.isometric.systems.hud.HudSystemEventsSubscriber;
import com.gadarts.isometric.systems.input.InputSystemEventsSubscriber;
import com.gadarts.isometric.systems.player.PlayerSystem;
import com.gadarts.isometric.systems.player.PlayerSystemEventsSubscriber;
import com.gadarts.isometric.systems.turns.Turns;
import com.gadarts.isometric.systems.turns.TurnsSystem;
import com.gadarts.isometric.systems.turns.TurnsSystemEventsSubscriber;
import com.gadarts.isometric.utils.EntityBuilder;
import com.gadarts.isometric.utils.assets.Assets;
import com.gadarts.isometric.utils.assets.GameAssetsManager;
import com.gadarts.isometric.utils.map.MapGraph;
import com.gadarts.isometric.utils.map.MapGraphNode;
import com.gadarts.isometric.utils.map.MapGraphPath;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class HudSystemImpl extends GameEntitySystem<HudSystemEventsSubscriber> implements
		HudSystem,
		InputSystemEventsSubscriber,
		PlayerSystemEventsSubscriber,
		CameraSystemEventsSubscriber,
		TurnsSystemEventsSubscriber,
		CharacterSystemEventsSubscriber {

	public static final Color CURSOR_REGULAR = Color.YELLOW;
	public static final int ATTACK_NODES_POOL_SIZE = 8;
	public static final int ARROWS_POOL_SIZE = 20;
	private static final Vector3 auxVector3_1 = new Vector3();
	private static final Vector3 auxVector3_2 = new Vector3();
	private static final Vector3 auxVector3_3 = new Vector3();
	private static final Vector3 auxVector3_4 = new Vector3();
	private static final Vector3 auxVector3_5 = new Vector3();
	private static final Color CURSOR_ATTACK = Color.RED;
	private final static Vector2 auxVector2 = new Vector2();
	private final MapGraph map;
	private final List<Entity> attackNodesEntities = new ArrayList<>();
	private final GameAssetsManager assetManager;
	private final MapGraphPath plannedPath = new MapGraphPath();
	private final List<Entity> arrowsEntities = new ArrayList<>();
	@Getter
	private ModelInstance cursorModelInstance;
	private ImmutableArray<Entity> enemiesEntities;
	private Model attackNodeModel;
	private CameraSystem cameraSystem;
	private Stage stage;
	private ImmutableArray<Entity> pickupEntities;
	private TurnsSystem turnsSystem;
	private PlayerSystem playerSystem;
	private CharacterSystem characterSystem;
	private MapGraphNode selectedAttackNode;

	public HudSystemImpl(final MapGraph map, final GameAssetsManager assetManager) {
		this.map = map;
		this.assetManager = assetManager;
	}

	@Override
	public void dispose() {
		attackNodeModel.dispose();
	}

	@Override
	public void addedToEngine(final Engine engine) {
		super.addedToEngine(engine);
		createArrowsEntities();
		stage = new Stage(new FitViewport(IsometricGame.RESOLUTION_WIDTH, IsometricGame.RESOLUTION_HEIGHT));
		Entity cursorEntity = engine.getEntitiesFor(Family.all(CursorComponent.class).get()).first();
		cursorModelInstance = ComponentsMapper.modelInstance.get(cursorEntity).getModelInstance();
		enemiesEntities = engine.getEntitiesFor(Family.all(EnemyComponent.class).get());
		pickupEntities = engine.getEntitiesFor(Family.all(PickUpComponent.class).get());
		createAttackNodesForFutureUse();
	}

	private void createAttackNodesForFutureUse() {
		attackNodeModel = createAttackNodeModelTest();
		createAttackNodesEntities();
	}

	private void createArrowsEntities() {
		Texture texture = assetManager.getTexture(Assets.Textures.PATH_ARROW);
		IntStream.range(0, ARROWS_POOL_SIZE).forEach(i -> {
			Entity entity = EntityBuilder.beginBuildingEntity((PooledEngine) getEngine())
					.addSimpleDecalComponent(auxVector3_1.setZero(), texture, false)
					.finishAndAddToEngine();
			arrowsEntities.add(entity);
		});
	}

	private void createAttackNodesEntities() {
		IntStream.range(0, ATTACK_NODES_POOL_SIZE).forEach(i ->
				attackNodesEntities.add(EntityBuilder.beginBuildingEntity((PooledEngine) getEngine())
						.addModelInstanceComponent(new ModelInstance(attackNodeModel), false)
						.finishAndAddToEngine()
				));
	}

	private Model createAttackNodeModelTest() {
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

	private MapGraphNode getCursorNode() {
		Vector3 dest = getCursorModelInstance().transform.getTranslation(auxVector3_1);
		return map.getNode((int) dest.x, (int) dest.z);
	}

	@Override
	public void touchDown(final int screenX, final int screenY, final int button) {
		if (cameraSystem.isCameraRotating()) return;
		Entity player = playerSystem.getPlayer();
		if (turnsSystem.getCurrentTurn() == Turns.PLAYER && ComponentsMapper.character.get(player).getHp() > 0) {
			if (button == Input.Buttons.LEFT && !characterSystem.isProcessingCommand()) {
				applyPlayerTurn(screenX, screenY);
			}
		}
	}

	private void applyPlayerTurn(final int screenX, final int screenY) {
		MapGraphNode cursorNode = getCursorNode();
		arrowsEntities.forEach(arrow -> ComponentsMapper.simpleDecal.get(arrow).setVisible(false));
		if (plannedPath.getCount() > 0 && plannedPath.get(plannedPath.getCount() - 1).equals(cursorNode)) {
			applyPlayerCommand(screenX, screenY, cursorNode);
		} else {
			Entity enemyAtNode = map.getEnemyFromNode(enemiesEntities, cursorNode);
			if (calculatePathAccordingToSelection(cursorNode, enemyAtNode)) {
				displayPathPlan(cursorNode, enemyAtNode);
			}
		}
	}

	private boolean calculatePathAccordingToSelection(final MapGraphNode cursorNode, final Entity enemyAtNode) {
		CharacterDecalComponent characterDecalComponent = ComponentsMapper.characterDecal.get(playerSystem.getPlayer());
		MapGraphNode playerNode = map.getNode(characterDecalComponent.getCellPosition(auxVector3_1));
		return (enemyAtNode != null && characterSystem.calculatePathToCharacter(playerNode, enemyAtNode, plannedPath))
				|| characterSystem.calculatePath(playerNode, cursorNode, plannedPath);
	}

	private void displayPathPlan(final MapGraphNode cursorNode, final Entity enemyFromNode) {
		if (enemyFromNode != null) {
			enemySelected(cursorNode, enemyFromNode);
		}
		IntStream.range(0, plannedPath.getCount()).forEach(i -> {
			if (i < arrowsEntities.size() && i < plannedPath.getCount() - 1) {
				MapGraphNode n = plannedPath.get(i);
				MapGraphNode next = plannedPath.get(i + 1);
				Vector2 dirVector = auxVector2.set(next.getX(), next.getY()).sub(n.getX(), n.getY()).nor().scl(0.5f);
				transformArrowDecal(n, dirVector, ComponentsMapper.simpleDecal.get(arrowsEntities.get(i)).getDecal());
				ComponentsMapper.simpleDecal.get(arrowsEntities.get(i)).setVisible(true);
			}
		});
	}

	private void transformArrowDecal(final MapGraphNode currentNode, final Vector2 directionVector, final Decal decal) {
		decal.getRotation().idt();
		decal.rotateX(90);
		decal.rotateZ(directionVector.angle());
		Vector3 pos = auxVector3_1.set(currentNode.getX() + 0.5f, 0.1f, currentNode.getY() + 0.5f);
		decal.setPosition(pos.add(directionVector.x, 0, directionVector.y));
	}

	private void applyPlayerCommand(final int screenX, final int screenY, final MapGraphNode cursorNode) {
		CharacterDecalComponent characterDecalComponent = ComponentsMapper.characterDecal.get(playerSystem.getPlayer());
		MapGraphNode playerNode = map.getNode(characterDecalComponent.getCellPosition(auxVector3_1));
		MapGraphNode node = map.getRayNode(screenX, screenY, cameraSystem.getCamera());
		if (selectedAttackNode == null) {
			applyCommandWhenNoAttackNodeSelected(cursorNode, node);
		} else {
			applyPlayerAttackCommand(cursorNode, playerNode);
		}
	}

	private void applyPlayerAttackCommand(final MapGraphNode cursorNode, final MapGraphNode playerNode) {
		List<MapGraphNode> availableNodes = map.getAvailableNodesAroundNode(enemiesEntities, selectedAttackNode);
		boolean result = false;
		result = isNodeInAvailableNodes(cursorNode, availableNodes, result);
		result |= cursorNode.equals(selectedAttackNode) && playerNode.isConnectedNeighbour(selectedAttackNode);
		if (result) {
			playerSystem.applyGoToMeleeCommand(cursorNode);
		}
		selectedAttackNode = null;
		playerSystem.deactivateAttackMode();
	}

	private boolean isNodeInAvailableNodes(final MapGraphNode cursorNode, final List<MapGraphNode> availableNodes, boolean result) {
		for (MapGraphNode availableNode : availableNodes) {
			if (availableNode.equals(cursorNode)) {
				result = true;
				break;
			}
		}
		return result;
	}

	private void applyCommandWhenNoAttackNodeSelected(final MapGraphNode cursorNode, final MapGraphNode node) {
		Entity enemyAtNode = map.getEnemyFromNode(enemiesEntities, node);
		if (enemyAtNode != null) {
			enemySelected(node, enemyAtNode);
		} else {
			MapGraphNode selectedNode = cursorNode;
			playerSystem.applyGoToCommand(selectedNode);
		}
	}

	private void enemySelected(final MapGraphNode node, final Entity enemyAtNode) {
		List<MapGraphNode> availableNodes = map.getAvailableNodesAroundNode(enemiesEntities, node);
		if (availableNodes.size() > 0) {
			selectedAttackNode = node;
			playerSystem.activateAttackMode(enemyAtNode, availableNodes);
		}
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
	public void onPlayerSystemReady(final PlayerSystem playerSystem) {
		this.playerSystem = playerSystem;
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

	@Override
	public void onEnemyTurn() {

	}

	@Override
	public void onPlayerTurn() {

	}

	@Override
	public void onTurnsSystemReady(final TurnsSystem turnsSystem) {
		this.turnsSystem = turnsSystem;
	}

	@Override
	public void onDestinationReached(final Entity character) {

	}

	@Override
	public void onCommandDone(final Entity character) {

	}

	@Override
	public void onNewCommandSet(final CharacterCommand command) {

	}

	@Override
	public void onCharacterSystemReady(final CharacterSystem characterSystem) {
		this.characterSystem = characterSystem;
	}

	@Override
	public void onCharacterGotDamage(final Entity target) {

	}
}
