package com.gadarts.isometric.systems.hud;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.gadarts.isometric.NecromineGame;
import com.gadarts.isometric.components.CharacterDecalComponent;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.CursorComponent;
import com.gadarts.isometric.components.EnemyComponent;
import com.gadarts.isometric.components.player.PlayerComponent;
import com.gadarts.isometric.components.player.Weapon;
import com.gadarts.isometric.systems.GameEntitySystem;
import com.gadarts.isometric.systems.camera.CameraSystem;
import com.gadarts.isometric.systems.camera.CameraSystemEventsSubscriber;
import com.gadarts.isometric.systems.character.CharacterCommand;
import com.gadarts.isometric.systems.character.CharacterSystem;
import com.gadarts.isometric.systems.character.CharacterSystemEventsSubscriber;
import com.gadarts.isometric.systems.input.InputSystem;
import com.gadarts.isometric.systems.input.InputSystemEventsSubscriber;
import com.gadarts.isometric.systems.pickup.PickUpSystem;
import com.gadarts.isometric.systems.pickup.PickupSystemEventsSubscriber;
import com.gadarts.isometric.systems.player.PlayerSystem;
import com.gadarts.isometric.systems.player.PlayerSystemEventsSubscriber;
import com.gadarts.isometric.systems.turns.Turns;
import com.gadarts.isometric.systems.turns.TurnsSystem;
import com.gadarts.isometric.systems.turns.TurnsSystemEventsSubscriber;
import com.gadarts.isometric.utils.DefaultGameSettings;
import com.gadarts.isometric.utils.SoundPlayer;
import com.gadarts.isometric.utils.assets.Assets;
import com.gadarts.isometric.utils.assets.GameAssetsManager;
import com.gadarts.isometric.utils.map.MapGraph;
import com.gadarts.isometric.utils.map.MapGraphNode;
import com.gadarts.isometric.utils.map.MapGraphPath;

import java.util.List;

import static com.badlogic.gdx.Application.LOG_DEBUG;

public class HudSystemImpl extends GameEntitySystem<HudSystemEventsSubscriber> implements
		HudSystem,
		InputSystemEventsSubscriber,
		PlayerSystemEventsSubscriber,
		CameraSystemEventsSubscriber,
		TurnsSystemEventsSubscriber,
		CharacterSystemEventsSubscriber,
		PickupSystemEventsSubscriber {

	public static final Color CURSOR_REGULAR = Color.YELLOW;
	static final Color CURSOR_ATTACK = Color.RED;
	private static final Vector3 auxVector3_1 = new Vector3();
	private static final Vector3 auxVector3_2 = new Vector3();
	private static final float BUTTON_PADDING = 40;
	private final AttackNodesHandler attackNodesHandler = new AttackNodesHandler();
	private PathPlanHandler pathPlanHandler;
	private ImmutableArray<Entity> enemiesEntities;
	private ModelInstance cursorModelInstance;
	private GameStage stage;
	private Entity player;


	@Override
	public void dispose() {
		attackNodesHandler.dispose();
	}

	@Override
	public void init(final MapGraph map, final SoundPlayer soundPlayer, final GameAssetsManager assetManager) {
		super.init(map, soundPlayer, assetManager);
		createStageAndAddHud();
	}

	@Override
	public void addedToEngine(final Engine engine) {
		super.addedToEngine(engine);
		Entity cursorEntity = engine.getEntitiesFor(Family.all(CursorComponent.class).get()).first();
		cursorModelInstance = ComponentsMapper.modelInstance.get(cursorEntity).getModelInstance();
		enemiesEntities = engine.getEntitiesFor(Family.all(EnemyComponent.class).get());
		attackNodesHandler.init(getEngine());
		player = engine.getEntitiesFor(Family.all(PlayerComponent.class).get()).first();
	}

	private void createStageAndAddHud() {
		Entity player = getEngine().getEntitiesFor(Family.all(PlayerComponent.class).get()).first();
		FitViewport fitViewport = new FitViewport(NecromineGame.RESOLUTION_WIDTH, NecromineGame.RESOLUTION_HEIGHT);
		stage = new GameStage(fitViewport, ComponentsMapper.player.get(player));
		Table table = new Table();
		stage.setDebugAll(Gdx.app.getLogLevel() == LOG_DEBUG && DefaultGameSettings.DISPLAY_HUD_OUTLINES);
		table.setFillParent(true);
		addStorageButton(table);
		stage.addActor(table);
	}

	private void addStorageButton(final Table table) {
		Button.ButtonStyle buttonStyle = new Button.ButtonStyle();
		buttonStyle.up = new TextureRegionDrawable(assetsManager.getTexture(Assets.UiTextures.BUTTON_STORAGE));
		buttonStyle.down = new TextureRegionDrawable(assetsManager.getTexture(Assets.UiTextures.BUTTON_STORAGE_DOWN));
		buttonStyle.over = new TextureRegionDrawable(assetsManager.getTexture(Assets.UiTextures.BUTTON_STORAGE_HOVER));
		Button button = new Button(buttonStyle);
		button.addListener(new ClickListener() {
			@Override
			public void clicked(final InputEvent event, final float x, final float y) {
				super.clicked(event, x, y);
				stage.openStorageWindow(assetsManager);
			}
		});
		table.add(button).expand().left().bottom().pad(BUTTON_PADDING);
	}


	@Override
	public void mouseMoved(final int screenX, final int screenY) {
		MapGraphNode newNode = map.getRayNode(screenX, screenY, getSystem(CameraSystem.class).getCamera());
		MapGraphNode oldNode = map.getNode(cursorModelInstance.transform.getTranslation(auxVector3_2));
		if (!newNode.equals(oldNode)) {
			cursorModelInstance.transform.setTranslation(newNode.getX(), 0, newNode.getY());
			colorizeCursor(newNode);
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
		if (getSystem(CameraSystem.class).isCameraRotating()) return;
		Entity player = getSystem(PlayerSystem.class).getPlayer();
		Turns currentTurn = getSystem(TurnsSystem.class).getCurrentTurn();
		if (currentTurn == Turns.PLAYER && ComponentsMapper.character.get(player).getHp() > 0) {
			if (button == Input.Buttons.LEFT && !getSystem(CharacterSystem.class).isProcessingCommand()) {
				applyPlayerTurn();
			}
		}
	}


	private void applyPlayerTurn() {
		MapGraphNode cursorNode = getCursorNode();
		int pathSize = pathPlanHandler.getPath().getCount();
		if (!pathPlanHandler.getPath().nodes.isEmpty() && pathPlanHandler.getPath().get(pathSize - 1).equals(cursorNode)) {
			applyPlayerCommandAccordingToPlan(cursorNode);
		} else {
			planPath(cursorNode);
		}
	}

	private void planPath(final MapGraphNode cursorNode) {
		Entity enemyAtNode = map.getEnemyFromNode(enemiesEntities, cursorNode);
		if (calculatePathAccordingToSelection(cursorNode, enemyAtNode)) {
			MapGraphNode selectedAttackNode = attackNodesHandler.getSelectedAttackNode();
			if (getSystem(PickUpSystem.class).getCurrentHighLightedPickup() != null || (selectedAttackNode != null && !isNodeInAvailableNodes(cursorNode, map.getAvailableNodesAroundNode(enemiesEntities, selectedAttackNode)))) {
				attackNodesHandler.reset();
			}
			pathHasCreated(cursorNode, enemyAtNode);
		}
	}

	private void pathHasCreated(final MapGraphNode cursorNode, final Entity enemyAtNode) {
		if (enemyAtNode != null) {
			enemySelected(cursorNode, enemyAtNode);
		}
		for (HudSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onPathCreated(enemyAtNode != null);
		}
		pathPlanHandler.displayPathPlan();
	}

	private boolean calculatePathAccordingToSelection(final MapGraphNode cursorNode, final Entity enemyAtNode) {
		PlayerSystem system = getSystem(PlayerSystem.class);
		CharacterDecalComponent characterDecalComponent = ComponentsMapper.characterDecal.get(system.getPlayer());
		MapGraphNode playerNode = map.getNode(characterDecalComponent.getCellPosition(auxVector3_1));
		CharacterSystem characterSystem = getSystem(CharacterSystem.class);
		MapGraphPath plannedPath = pathPlanHandler.getPath();
		return (enemyAtNode != null && characterSystem.calculatePathToCharacter(playerNode, enemyAtNode, plannedPath))
				|| getSystem(PickUpSystem.class).getCurrentHighLightedPickup() != null && characterSystem.calculatePath(playerNode, cursorNode, plannedPath)
				|| characterSystem.calculatePath(playerNode, cursorNode, plannedPath);
	}


	private void applyPlayerCommandAccordingToPlan(final MapGraphNode cursorNode) {
		pathPlanHandler.hideAllArrows();
		PlayerSystem playerSystem = getSystem(PlayerSystem.class);
		CharacterDecalComponent charDecalComp = ComponentsMapper.characterDecal.get(playerSystem.getPlayer());
		MapGraphNode playerNode = map.getNode(charDecalComp.getCellPosition(auxVector3_1));
		if (attackNodesHandler.getSelectedAttackNode() == null) {
			applyCommandWhenNoAttackNodeSelected();
		} else {
			applyPlayerAttackCommand(cursorNode, playerNode);
		}
	}

	private void applyPlayerAttackCommand(final MapGraphNode targetNode, final MapGraphNode playerNode) {
		MapGraphNode attackNode = attackNodesHandler.getSelectedAttackNode();
		boolean result = targetNode.equals(attackNode);
		result |= isNodeInAvailableNodes(targetNode, map.getAvailableNodesAroundNode(enemiesEntities, attackNode));
		result |= targetNode.equals(attackNode) && playerNode.isConnectedNeighbour(attackNode);
		if (result) {
			if (map.getEnemyFromNode(enemiesEntities, targetNode) != null) {
				Array<MapGraphNode> nodes = pathPlanHandler.getPath().nodes;
				nodes.removeIndex(nodes.size - 1);
			}
			getSystem(PlayerSystem.class).applyGoToMeleeCommand(pathPlanHandler.getPath());
		}
		attackNodesHandler.setSelectedAttackNode(null);
		getSystem(PlayerSystem.class).deactivateAttackMode();
	}

	private boolean isNodeInAvailableNodes(final MapGraphNode node, final List<MapGraphNode> availableNodes) {
		boolean result = false;
		for (MapGraphNode availableNode : availableNodes) {
			if (availableNode.equals(node)) {
				result = true;
				break;
			}
		}
		return result;
	}

	private void applyCommandWhenNoAttackNodeSelected() {
		MapGraphPath plannedPath = pathPlanHandler.getPath();
		Entity itemToPickup = getSystem(PickUpSystem.class).getItemToPickup();
		if (itemToPickup != null) {
			getSystem(PlayerSystem.class).applyGoToPickupCommand(plannedPath, itemToPickup);
		} else {
			getSystem(PlayerSystem.class).applyGoToCommand(plannedPath);
		}
	}

	private void enemySelected(final MapGraphNode node, final Entity enemyAtNode) {
		Weapon selectedWeapon = ComponentsMapper.player.get(player).getStorage().getSelectedWeapon();
		if (selectedWeapon.isMelee()) {
			List<MapGraphNode> availableNodes = map.getAvailableNodesAroundNode(enemiesEntities, node);
			attackNodesHandler.setSelectedAttackNode(node);
			getSystem(PlayerSystem.class).activateAttackMode(enemyAtNode, availableNodes);
		} else {
			pathPlanHandler.resetPlan();
			for (HudSystemEventsSubscriber subscriber : subscribers) {
				subscriber.onEnemySelectedWithRangeWeapon(node);
			}
		}
	}

	@Override
	public void update(final float deltaTime) {
		super.update(deltaTime);
		stage.act();
	}


	@Override
	public void touchUp(final int screenX, final int screenY, final int button) {

	}

	@Override
	public void touchDragged(final int screenX, final int screenY) {

	}

	@Override
	public void inputSystemReady(final InputSystem inputSystem) {
		inputSystem.addInputProcessor(stage);
	}

	@Override
	public void onPlayerFinishedTurn() {

	}

	@Override
	public void onPlayerSystemReady(final PlayerSystem playerSystem) {
		addSystem(PlayerSystem.class, playerSystem);
	}

	@Override
	public void onAttackModeActivated(final List<MapGraphNode> availableNodes) {
		attackNodesHandler.onAttackModeActivated(availableNodes);
	}


	@Override
	public void onAttackModeDeactivated() {
		attackNodesHandler.onAttackModeDeactivated();
	}


	@Override
	public void onCameraSystemReady(final CameraSystem cameraSystem) {
		addSystem(CameraSystem.class, cameraSystem);
	}

	@Override
	public Stage getStage() {
		return stage;
	}

	@Override
	public ModelInstance getCursorModelInstance() {
		return cursorModelInstance;
	}

	@Override
	public boolean hasOpenWindows() {
		return stage.hasOpenWindows();
	}

	@Override
	public void onEnemyTurn() {

	}

	@Override
	public void onPlayerTurn() {

	}

	@Override
	public void onTurnsSystemReady(final TurnsSystem turnsSystem) {
		super.addSystem(TurnsSystem.class, turnsSystem);
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
		addSystem(CharacterSystem.class, characterSystem);
	}

	@Override
	public void onCharacterGotDamage(final Entity target) {

	}

	@Override
	public void onItemPickedUp(final Entity itemPickedUp) {

	}

	@Override
	public void activate() {
		pathPlanHandler = new PathPlanHandler(assetsManager);
		pathPlanHandler.init((PooledEngine) getEngine());
		for (HudSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onHudSystemReady(this);
		}
	}

	@Override
	public void onPickUpSystemReady(final PickUpSystem pickUpSystem) {
		addSystem(PickUpSystem.class, pickUpSystem);
	}

}
