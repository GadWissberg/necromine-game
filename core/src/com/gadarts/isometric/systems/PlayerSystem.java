package com.gadarts.isometric.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ai.pfa.Heuristic;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.isometric.components.CharacterComponent;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.PlayerComponent;
import com.gadarts.isometric.utils.*;

public class PlayerSystem extends GameEntitySystem implements InputSystemEventsSubscriber {
	private static final PlayerCommand auxCommand = new PlayerCommand();
	private static final Vector3 auxVector3_1 = new Vector3();
	private static final Vector2 auxVector2_1 = new Vector2();
	private static final Vector2 auxVector2_2 = new Vector2();
	private static final MapGraphPath currentPath = new MapGraphPath();
	private final static MapGraphNode auxNode = new MapGraphNode(0, 0, 0, 0);
	private final MapGraph map;
	private final IndexedAStarPathFinder<MapGraphNode> pathFinder;
	private CameraSystem cameraSystem;
	private PlayerCommand currentCommand;
	private Entity player;
	private Heuristic<MapGraphNode> heuristic;
	private float EPSILON = 0.01f;

	public PlayerSystem(final MapGraph map) {
		this.map = map;
		pathFinder = new IndexedAStarPathFinder<>(map);
		this.heuristic = new GameHeuristic();
	}

	@Override
	public void dispose() {

	}

	@Override
	public void addedToEngine(final Engine engine) {
		super.addedToEngine(engine);
		cameraSystem = getEngine().getSystem(CameraSystem.class);
		player = getEngine().getEntitiesFor(Family.all(PlayerComponent.class).get()).first();
	}

	@Override
	public void mouseMoved(final int screenX, final int screenY) {

	}

	@Override
	public void touchDown(final int screenX, final int screenY, final int button) {
		if (cameraSystem.isRotateCamera()) return;
		if (button == Input.Buttons.LEFT && currentCommand == null) {
			Vector3 dest = Utils.calculateGridPositionFromMouse(cameraSystem.getCamera(), screenX, screenY, auxVector3_1);
			auxCommand.init(Commands.GO_TO, dest);
			applyCommand(auxCommand);
		}
	}

	private void applyCommand(final PlayerCommand command) {
		currentCommand = command;
		Vector3 destination = command.getDestination(auxVector3_1);
		Decal decal = ComponentsMapper.decal.get(player).getDecal();
		MapGraphNode source = map.getNode(MathUtils.floor(decal.getX()), MathUtils.floor(decal.getZ()));
		MapGraphNode dest = map.getNode(MathUtils.floor(destination.x), MathUtils.floor(destination.z));
		if (pathFinder.searchNodePath(source, dest, heuristic, currentPath)) {
			ComponentsMapper.character.get(player).setDestinationNode(currentPath.get(1));
		} else {
			finishCommand();
		}
	}

	private void finishCommand() {
		currentCommand = null;
		currentPath.clear();
	}

	@Override
	public void update(final float deltaTime) {
		super.update(deltaTime);
		if (currentCommand != null) {
			CharacterComponent characterComponent = ComponentsMapper.character.get(player);
			MapGraphNode destinationNode = characterComponent.getDestinationNode();
			Decal decal = ComponentsMapper.decal.get(player).getDecal();
			if (auxVector2_1.set(decal.getX(), decal.getZ()).dst2(destinationNode.getRealPosition(auxVector2_2)) < EPSILON) {
				Gdx.app.log("!", "Reached:" + destinationNode);
				MapGraphNode newDest = currentPath.getNextOf(destinationNode);
				if (newDest != null) {
					characterComponent.setDestinationNode(newDest);
				} else {
					finishCommand();
				}
			} else {
				auxVector2_1.set(decal.getX(), decal.getZ());
				auxVector2_2.set(destinationNode.getX() + 0.5f, destinationNode.getY() + 0.5f);
				Vector2 velocity = auxVector2_2.sub(auxVector2_1).nor().scl(deltaTime);
				decal.translate(auxVector3_1.set(velocity.x, 0, velocity.y));
			}
		}
	}

	@Override
	public void touchUp(final int screenX, final int screenY, final int button) {

	}

	@Override
	public void touchDragged(final int screenX, final int screenY) {

	}
}
