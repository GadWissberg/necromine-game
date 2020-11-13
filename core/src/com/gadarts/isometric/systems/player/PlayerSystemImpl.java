package com.gadarts.isometric.systems.player;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.player.PlayerComponent;
import com.gadarts.isometric.systems.GameEntitySystem;
import com.gadarts.isometric.systems.camera.CameraSystem;
import com.gadarts.isometric.systems.camera.CameraSystemEventsSubscriber;
import com.gadarts.isometric.systems.character.CharacterCommand;
import com.gadarts.isometric.systems.character.CharacterSystem;
import com.gadarts.isometric.systems.character.CharacterSystemEventsSubscriber;
import com.gadarts.isometric.systems.character.Commands;
import com.gadarts.isometric.systems.hud.HudSystem;
import com.gadarts.isometric.systems.hud.HudSystemEventsSubscriber;
import com.gadarts.isometric.systems.input.InputSystem;
import com.gadarts.isometric.systems.input.InputSystemEventsSubscriber;
import com.gadarts.isometric.systems.turns.TurnsSystem;
import com.gadarts.isometric.systems.turns.TurnsSystemEventsSubscriber;
import com.gadarts.isometric.utils.map.MapGraphNode;
import com.gadarts.isometric.utils.map.MapGraphPath;

import java.util.List;

public class PlayerSystemImpl extends GameEntitySystem<PlayerSystemEventsSubscriber> implements
		PlayerSystem,
		CameraSystemEventsSubscriber,
		TurnsSystemEventsSubscriber,
		HudSystemEventsSubscriber,
		InputSystemEventsSubscriber,
		CharacterSystemEventsSubscriber {
	private static final CharacterCommand auxCommand = new CharacterCommand();
	private Entity player;
	private CharacterSystem characterSystem;


	@Override
	public void dispose() {

	}

	@Override
	public void addedToEngine(final Engine engine) {
		super.addedToEngine(engine);
		player = getEngine().getEntitiesFor(Family.all(PlayerComponent.class).get()).first();
	}

	@Override
	public void mouseMoved(final int screenX, final int screenY) {

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
	public void inputSystemReady(final InputSystem inputSystem) {

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

	@Override
	public void onCharacterSystemReady(final CharacterSystem characterSystem) {
		this.characterSystem = characterSystem;
	}

	@Override
	public void onCharacterGotDamage(final Entity target) {

	}

	@Override
	public void onItemPickedUp(final Entity itemPickedUp) {
		ComponentsMapper.player.get(player).getStorage().addItem(ComponentsMapper.pickup.get(itemPickedUp).getItem());
	}


	@Override
	public void onCameraSystemReady(final CameraSystem cameraSystem) {

	}

	@Override
	public void onHudSystemReady(final HudSystem hudSystem) {

	}

	@Override
	public void onPathCreated(final boolean pathToEnemy) {

	}

	@Override
	public void onEnemyTurn() {

	}

	@Override
	public void onPlayerTurn() {

	}

	@Override
	public void onTurnsSystemReady(final TurnsSystem turnsSystem) {
	}

	@Override
	public Entity getPlayer() {
		return player;
	}

	@Override
	public void activateAttackMode(final Entity enemyAtNode, final List<MapGraphNode> availableNodes) {
		ComponentsMapper.character.get(player).setTarget(enemyAtNode);
		for (PlayerSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onAttackModeActivated(availableNodes);
		}
	}

	@Override
	public void applyGoToCommand(final MapGraphPath path) {
		MapGraphNode playerNode = map.getNode(ComponentsMapper.characterDecal.get(player).getDecal().getPosition());
		if (path.getCount() > 0 && !playerNode.equals(path.get(path.getCount() - 1))) {
			characterSystem.applyCommand(auxCommand.init(Commands.GO_TO, path, player), player);
		}
	}

	@Override
	public void deactivateAttackMode() {
		for (PlayerSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onAttackModeDeactivated();
		}
	}

	@Override
	public void applyGoToMeleeCommand(final MapGraphPath path) {
		characterSystem.applyCommand(auxCommand.init(Commands.GO_TO_MELEE, path, player), player);
	}

	@Override
	public void applyGoToPickupCommand(final MapGraphPath path, final Entity itemToPickup) {
		characterSystem.applyCommand(auxCommand.init(Commands.GO_TO_PICKUP, path, player, itemToPickup), player);
	}

	@Override
	public void activate() {
		for (PlayerSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onPlayerSystemReady(this);
		}
	}
}
