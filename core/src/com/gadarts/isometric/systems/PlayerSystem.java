package com.gadarts.isometric.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.PlayerComponent;
import com.gadarts.isometric.utils.MapGraph;

import java.util.ArrayList;
import java.util.List;

public class PlayerSystem extends GameEntitySystem implements
		InputSystemEventsSubscriber,
		EventsNotifier<PlayerSystemEventsSubscriber>,
		CharacterSystemEventsSubscriber {

	private static final Vector3 auxVector3_1 = new Vector3();
	private final List<PlayerSystemEventsSubscriber> subscribers = new ArrayList<>();
	private final MapGraph map;
	private CameraSystem cameraSystem;
	private Entity player;
	private TurnsSystem turnsSystem;
	private CharacterSystem characterSystem;
	private HudSystem hudSystem;

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
	}

	@Override
	public void mouseMoved(final int screenX, final int screenY) {

	}

	@Override
	public void touchDown(final int screenX, final int screenY, final int button) {
		if (cameraSystem.isRotateCamera()) return;
		if (turnsSystem.getCurrentTurn() == Turns.PLAYER) {
			if (button == Input.Buttons.LEFT && !characterSystem.isProcessingCommand()) {
				Vector3 dest = hudSystem.getCursorModelInstance().transform.getTranslation(auxVector3_1);
				CharacterSystem.auxCommand.init(Commands.GO_TO, map.getNode((int) dest.x, (int) dest.z), player);
				characterSystem.applyCommand(CharacterSystem.auxCommand, player);
			}
		}
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
}
