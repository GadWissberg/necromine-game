package com.gadarts.isometric.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.pfa.Heuristic;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.isometric.components.CharacterComponent;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.SpriteType;
import com.gadarts.isometric.utils.*;

import java.util.ArrayList;
import java.util.List;

public class CharacterSystem extends GameEntitySystem implements EventsNotifier<CharacterSystemEventsSubscriber> {
	static final CharacterCommand auxCommand = new CharacterCommand();
	private static final Vector3 auxVector3_1 = new Vector3();
	private static final Vector2 auxVector2_1 = new Vector2();
	private static final Vector2 auxVector2_2 = new Vector2();

	private final MapGraphPath currentPath = new MapGraphPath();
	private final MapGraph map;
	private final IndexedAStarPathFinder<MapGraphNode> pathFinder;
	private final Heuristic<MapGraphNode> heuristic;
	private final List<CharacterSystemEventsSubscriber> subscribers = new ArrayList<>();
	private CharacterCommand currentCommand;

	public CharacterSystem(final MapGraph map) {
		this.map = map;
		pathFinder = new IndexedAStarPathFinder<>(map);
		this.heuristic = new GameHeuristic();
	}

	void applyCommand(final CharacterCommand command, final Entity character) {
		currentCommand = command;
		Vector3 destination = command.getDestination(auxVector3_1);
		Decal decal = ComponentsMapper.decal.get(character).getDecal();
		MapGraphNode source = map.getNode(MathUtils.floor(decal.getX()), MathUtils.floor(decal.getZ()));
		MapGraphNode dest = map.getNode(MathUtils.floor(destination.x), MathUtils.floor(destination.z));
		if (pathFinder.searchNodePath(source, dest, heuristic, currentPath)) {
			CharacterComponent characterComponent = ComponentsMapper.character.get(character);
			characterComponent.setSpriteType(SpriteType.RUN);
			initDestinationNode(characterComponent, currentPath.get(1), currentPath.get(0));
		} else {
			finishCommand(character);
		}
	}

	public void finishCommand(final Entity character) {
		currentCommand = null;
		currentPath.clear();
		ComponentsMapper.character.get(character).setSpriteType(SpriteType.IDLE);
		for (CharacterSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onCommandFinished(character);
		}
	}

	private void initDestinationNode(final CharacterComponent characterComponent, final MapGraphNode destNode, final MapGraphNode srcNode) {
		Vector2 direction = destNode.getRealPosition(auxVector2_2).sub(srcNode.getRealPosition(auxVector2_1)).nor();
		CharacterComponent.Direction newDirection = CharacterComponent.Direction.findDirection(direction);
		characterComponent.setDirection(newDirection);
		characterComponent.setDestinationNode(destNode);
	}

	@Override
	public void update(final float deltaTime) {
		super.update(deltaTime);
		if (currentCommand != null) {
			Entity character = currentCommand.getCharacter();
			CharacterComponent characterComponent = ComponentsMapper.character.get(character);
			MapGraphNode oldDest = characterComponent.getDestinationNode();
			Decal decal = ComponentsMapper.decal.get(character).getDecal();
			if (auxVector2_1.set(decal.getX(), decal.getZ()).dst2(oldDest.getRealPosition(auxVector2_2)) < Utils.EPSILON) {
				MapGraphNode newDest = currentPath.getNextOf(oldDest);
				if (newDest != null) {
					initDestinationNode(characterComponent, newDest, oldDest);
					characterComponent.setDestinationNode(newDest);
				} else {
					finishCommand(character);
				}
			} else {
				auxVector2_1.set(decal.getX(), decal.getZ());
				auxVector2_2.set(oldDest.getX() + 0.5f, oldDest.getY() + 0.5f);
				Vector2 velocity = auxVector2_2.sub(auxVector2_1).nor().scl(deltaTime);
				decal.translate(auxVector3_1.set(velocity.x, 0, velocity.y));
			}
		}
	}

	@Override
	public void dispose() {

	}


	@Override
	public void subscribeForEvents(final CharacterSystemEventsSubscriber sub) {
		if (subscribers.contains(sub)) return;
		subscribers.add(sub);

	}

	public boolean isProcessingCommand() {
		return currentCommand != null;
	}
}
