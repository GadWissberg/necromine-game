package com.gadarts.isometric.systems.hud;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.enemy.EnemyComponent;
import com.gadarts.isometric.components.player.PlayerComponent;
import com.gadarts.isometric.services.GameServices;
import com.gadarts.isometric.systems.GameEntitySystem;
import com.gadarts.isometric.systems.camera.CameraSystem;
import com.gadarts.isometric.systems.camera.CameraSystemEventsSubscriber;
import com.gadarts.isometric.systems.character.CharacterSystem;
import com.gadarts.isometric.systems.character.CharacterSystemEventsSubscriber;
import com.gadarts.isometric.systems.hud.console.ConsoleCommandParameter;
import com.gadarts.isometric.systems.hud.console.ConsoleCommandResult;
import com.gadarts.isometric.systems.hud.console.ConsoleCommands;
import com.gadarts.isometric.systems.hud.console.ConsoleEventsSubscriber;
import com.gadarts.isometric.systems.input.InputSystem;
import com.gadarts.isometric.systems.input.InputSystemEventsSubscriber;
import com.gadarts.isometric.systems.pickup.PickUpSystem;
import com.gadarts.isometric.systems.pickup.PickupSystemEventsSubscriber;
import com.gadarts.isometric.systems.player.PlayerSystem;
import com.gadarts.isometric.systems.player.PlayerSystemEventsSubscriber;
import com.gadarts.isometric.systems.render.DrawFlags;
import com.gadarts.isometric.systems.render.RenderSystem;
import com.gadarts.isometric.systems.render.RenderSystemEventsSubscriber;
import com.gadarts.isometric.systems.turns.Turns;
import com.gadarts.isometric.systems.turns.TurnsSystem;
import com.gadarts.isometric.systems.turns.TurnsSystemEventsSubscriber;
import com.gadarts.isometric.utils.map.MapGraph;
import com.gadarts.isometric.utils.map.MapGraphNode;

import java.util.List;

import static com.gadarts.isometric.NecronemesGame.*;

public class InterfaceSystemImpl extends GameEntitySystem<InterfaceSystemEventsSubscriber> implements InterfaceSystem,
		InputSystemEventsSubscriber,
		PlayerSystemEventsSubscriber,
		CameraSystemEventsSubscriber,
		RenderSystemEventsSubscriber,
		TurnsSystemEventsSubscriber,
		CharacterSystemEventsSubscriber,
		PickupSystemEventsSubscriber,
		ConsoleEventsSubscriber {

	private static final Vector3 auxVector3_2 = new Vector3();

	private final InterfaceSystemHandlers interfaceSystemHandlers = new InterfaceSystemHandlers();
	private ImmutableArray<Entity> enemiesEntities;

	@Override
	public void toggleMenu(final boolean active) {
		interfaceSystemHandlers.getMenuHandler().toggleMenu(active, interfaceSystemHandlers.getHudHandler().getStage());
	}

	@Override
	public void applyMenuOptions(final MenuOptionDefinition[] options) {
		interfaceSystemHandlers.getMenuHandler().applyMenuOptions(options, services, getSystem(PlayerSystem.class).getPlayer(), this);
	}

	@Override
	public void dispose( ) {
		interfaceSystemHandlers.dispose();
	}

	@Override
	public void init(final GameServices services) {
		super.init(services);
		Entity player = getEngine().getEntitiesFor(Family.all(PlayerComponent.class).get()).first();
		interfaceSystemHandlers.getCursorHandler().init(getEngine());
		interfaceSystemHandlers.getHudHandler().init(services, player);
	}


	@Override
	public void onPlayerStatusChanged(final boolean disabled) {
		ComponentsMapper.cursor.get(interfaceSystemHandlers.getCursorHandler().getCursor()).setDisabled(disabled);
	}


	@Override
	public void onRenderSystemReady(final RenderSystem renderSystem) {
		DrawFlags drawFlags = renderSystem.getDrawFlags();
		interfaceSystemHandlers.initializeToolTipHandler(interfaceSystemHandlers.getHudHandler().getStage(), drawFlags);
	}

	@Override
	public void addedToEngine(final Engine engine) {
		super.addedToEngine(engine);
		enemiesEntities = engine.getEntitiesFor(Family.all(EnemyComponent.class).get());
		interfaceSystemHandlers.initializeAttackNodesHandler(getEngine());
	}


	@Override
	public void mouseMoved(final int screenX, final int screenY) {
		if (interfaceSystemHandlers.getMenuHandler().getMenuTable().isVisible()) return;
		MapGraph map = services.getMapService().getMap();
		MapGraphNode newNode = map.getRayNode(screenX, screenY, getSystem(CameraSystem.class).getCamera());
		ModelInstance cursorModelInstance = interfaceSystemHandlers.getCursorHandler().getCursorModelInstance();
		MapGraphNode oldNode = map.getNode(cursorModelInstance.transform.getTranslation(auxVector3_2));
		if (newNode != null && !newNode.equals(oldNode)) {
			interfaceSystemHandlers.onMouseEnteredNewNode(newNode, services, enemiesEntities);
		}
	}


	@Override
	public void touchDown(final int screenX, final int screenY, final int button) {
		if (getSystem(CameraSystem.class).isCameraRotating()) return;
		Entity player = getSystem(PlayerSystem.class).getPlayer();
		Turns currentTurn = getSystem(TurnsSystem.class).getCurrentTurn();
		int hp = ComponentsMapper.character.get(player).getSkills().getHealthData().getHp();
		if (currentTurn == Turns.PLAYER && hp > 0) {
			if (button == Input.Buttons.LEFT && !getSystem(CharacterSystem.class).isProcessingCommand()) {
				interfaceSystemHandlers.onUserSelectedNodeToApplyTurn(services, subscribers);
			}
		}
	}


	@Override
	public void update(final float deltaTime) {
		super.update(deltaTime);
		interfaceSystemHandlers.update(deltaTime, services, enemiesEntities);
	}


	@Override
	public void inputSystemReady(final InputSystem inputSystem) {
		inputSystem.addInputProcessor(interfaceSystemHandlers.getHudHandler().getStage());
	}

	@Override
	public void keyDown(final int keycode) {
		boolean playerDisabled = ComponentsMapper.player.get(getSystem(PlayerSystem.class).getPlayer()).isDisabled();
		if (keycode == Input.Keys.ESCAPE && !playerDisabled) {
			toggleMenu(isMenuClosed());
		}
	}


	@Override
	public void onPlayerSystemReady(final PlayerSystem playerSystem, final Entity player) {
		addSystem(PlayerSystem.class, playerSystem);
		GameStage stage = interfaceSystemHandlers.getHudHandler().getStage();
		Table table = interfaceSystemHandlers.getHudHandler().addTable();
		interfaceSystemHandlers.getMenuHandler().addMenuTable(stage, player, table, services, this);
	}

	@Override
	public void onAttackModeActivated(final List<MapGraphNode> availableNodes) {
		interfaceSystemHandlers.getAttackNodesHandler().onAttackModeActivated(availableNodes);
	}


	@Override
	public void onAttackModeDeactivated( ) {
		interfaceSystemHandlers.getAttackNodesHandler().onAttackModeDeactivated();
	}


	@Override
	public void onCameraSystemReady(final CameraSystem cameraSystem) {
		addSystem(CameraSystem.class, cameraSystem);
	}

	@Override
	public Stage getStage( ) {
		return interfaceSystemHandlers.getHudHandler().getStage();
	}

	@Override
	public boolean hasOpenWindows( ) {
		return interfaceSystemHandlers.getHudHandler().getStage().hasOpenWindows();
	}

	@Override
	public boolean isMenuClosed( ) {
		return !interfaceSystemHandlers.getMenuHandler().getMenuTable().isVisible();
	}


	@Override
	public void onEnemyTurn(final long currentTurnId) {
		interfaceSystemHandlers.getHudHandler().onEnemyTurn();
	}

	@Override
	public void onPlayerTurn(final long currentTurnId) {
		interfaceSystemHandlers.getHudHandler().onPlayerTurn();
	}

	@Override
	public void onTurnsSystemReady(final TurnsSystem turnsSystem) {
		super.addSystem(TurnsSystem.class, turnsSystem);
	}


	@Override
	public void onCharacterSystemReady(final CharacterSystem characterSystem) {
		addSystem(CharacterSystem.class, characterSystem);
	}


	@Override
	public void activate( ) {
		for (InterfaceSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onHudSystemReady(this);
		}
	}

	@Override
	public void onPickUpSystemReady(final PickUpSystem pickUpSystem) {
		addSystem(PickUpSystem.class, pickUpSystem);
	}

	@Override
	public void onConsoleActivated( ) {

	}

	@Override
	public boolean onCommandRun(final ConsoleCommands command, final ConsoleCommandResult consoleCommandResult) {
		boolean result;
		result = interfaceSystemHandlers.getHudHandler().onCommandRun(command, consoleCommandResult);
		return result;
	}

	@Override
	public boolean onCommandRun(final ConsoleCommands command,
								final ConsoleCommandResult consoleCommandResult,
								final ConsoleCommandParameter parameter) {
		return false;
	}

	@Override
	public void onConsoleDeactivated( ) {

	}

	@Override
	public void onFullScreenToggle(final boolean fullScreen) {
		Viewport viewport = interfaceSystemHandlers.getHudHandler().getStage().getViewport();
		viewport.setScreenWidth((fullScreen ? FULL_SCREEN_RESOLUTION_WIDTH : WINDOWED_RESOLUTION_WIDTH));
		viewport.setScreenHeight((fullScreen ? FULL_SCREEN_RESOLUTION_HEIGHT : WINDOWED_RESOLUTION_HEIGHT));
	}
}
