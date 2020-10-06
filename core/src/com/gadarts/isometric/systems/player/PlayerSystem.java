package com.gadarts.isometric.systems.player;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.EnemyComponent;
import com.gadarts.isometric.components.PlayerComponent;
import com.gadarts.isometric.systems.CameraSystem;
import com.gadarts.isometric.systems.EventsNotifier;
import com.gadarts.isometric.systems.GameEntitySystem;
import com.gadarts.isometric.systems.HudSystem;
import com.gadarts.isometric.systems.character.CharacterCommand;
import com.gadarts.isometric.systems.character.CharacterSystem;
import com.gadarts.isometric.systems.character.CharacterSystemEventsSubscriber;
import com.gadarts.isometric.systems.character.Commands;
import com.gadarts.isometric.systems.input.InputSystemEventsSubscriber;
import com.gadarts.isometric.systems.turns.Turns;
import com.gadarts.isometric.systems.turns.TurnsSystem;
import com.gadarts.isometric.utils.map.MapGraph;
import com.gadarts.isometric.utils.map.MapGraphNode;

import java.util.ArrayList;
import java.util.List;

public class PlayerSystem extends GameEntitySystem implements
		InputSystemEventsSubscriber,
		EventsNotifier<PlayerSystemEventsSubscriber>,
		CharacterSystemEventsSubscriber {
	private static final Vector3 auxVector3_1 = new Vector3();
	private static final CharacterCommand auxCommand = new CharacterCommand();
	private final List<PlayerSystemEventsSubscriber> subscribers = new ArrayList<>();
	private final MapGraph map;
	private MapGraphNode selectedAttackNode;
	private CameraSystem cameraSystem;
	private Entity player;
	private TurnsSystem turnsSystem;
	private CharacterSystem characterSystem;
	private HudSystem hudSystem;
	private ImmutableArray<Entity> enemiesEntities;

	public PlayerSystem(final MapGraph map) {
		this.map = map;
	}

	@Override
	public void dispose() {

	}

	@Override
	public void addedToEngine(final Engine engine) {
		super.addedToEngine(engine);
		cameraSystem = getEngine().getSystem(CameraSystem.class);
		player = getEngine().getEntitiesFor(Family.all(PlayerComponent.class).get()).first();
		turnsSystem = engine.getSystem(TurnsSystem.class);
		characterSystem = engine.getSystem(CharacterSystem.class);
		hudSystem = engine.getSystem(HudSystem.class);
		enemiesEntities = engine.getEntitiesFor(Family.all(EnemyComponent.class).get());
	}

	@Override
	public void mouseMoved(final int screenX, final int screenY) {

	}

	@Override
	public void touchDown(final int screenX, final int screenY, final int button) {
		if (cameraSystem.isRotateCamera()) return;
		if (turnsSystem.getCurrentTurn() == Turns.PLAYER) {
			if (button == Input.Buttons.LEFT && !characterSystem.isProcessingCommand()) {
				MapGraphNode node = map.getRayNode(screenX, screenY, cameraSystem.getCamera());
				if (selectedAttackNode == null) {
					Entity enemyAtNode = map.getEnemyFromNode(enemiesEntities, node);
					if (enemyAtNode != null) {
						List<MapGraphNode> availableNodes = map.getAvailableNodesAroundNode(enemiesEntities, node);
						if (availableNodes.size() > 0) {
							selectedAttackNode = node;
							ComponentsMapper.character.get(player).setTarget(enemyAtNode);
							for (PlayerSystemEventsSubscriber subscriber : subscribers) {
								subscriber.onAttackModeActivated(availableNodes);
							}
						}
					} else {
						MapGraphNode selectedNode = getCursorNode();
						MapGraphNode playerNode = map.getNode(ComponentsMapper.decal.get(player).getDecal().getPosition());
						if (!playerNode.equals(selectedNode)) {
							characterSystem.applyCommand(auxCommand.init(Commands.GO_TO, selectedNode, player), player);
						}
					}
				} else {
					List<MapGraphNode> availableNodes = map.getAvailableNodesAroundNode(enemiesEntities, selectedAttackNode);
					MapGraphNode selectedNode = getCursorNode();
					boolean result = false;
					for (MapGraphNode availableNode : availableNodes) {
						if (availableNode.equals(selectedNode)) {
							result = true;
							break;
						}
					}
					if (result) {
						characterSystem.applyCommand(auxCommand.init(Commands.GO_TO_MELEE, selectedNode, player), player);
					}
					selectedAttackNode = null;
					for (PlayerSystemEventsSubscriber subscriber : subscribers) {
						subscriber.onAttackModeDeactivated();
					}
				}
			}
		}
	}

	private MapGraphNode getCursorNode() {
		Vector3 dest = hudSystem.getCursorModelInstance().transform.getTranslation(auxVector3_1);
		MapGraphNode selectedNode = map.getNode((int) dest.x, (int) dest.z);
		return selectedNode;
	}


	@Override
	public void touchUp(final int screenX, final int screenY, final int button) {

	}

	@Override
	public void touchDragged(final int screenX, final int screenY) {

	}

	@Override
	public void subscribeForEvents(final PlayerSystemEventsSubscriber sub) {
		if (subscribers.contains(sub)) return;
		subscribers.add(sub);
	}

	@Override
	public void onDestinationReached(final Entity character) {

	}

	@Override
	public void onCommandDone(final Entity character) {
		if (ComponentsMapper.player.has(character)) {
			for (PlayerSystemEventsSubscriber subscriber : subscribers) {
				subscriber.onPlayerFinishedTurn();
			}
		}
	}

	@Override
	public void onNewCommandSet(final CharacterCommand command) {

	}
}
