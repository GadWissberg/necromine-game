package com.gadarts.isometric.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.pfa.Heuristic;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.isometric.components.AnimationComponent;
import com.gadarts.isometric.components.CharacterComponent;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.SpriteType;
import com.gadarts.isometric.utils.*;

import java.util.ArrayList;
import java.util.List;

public class CharacterSystem extends GameEntitySystem implements
		EventsNotifier<CharacterSystemEventsSubscriber>,
		RenderSystemEventsSubscriber {

	static final CharacterCommand auxCommand = new CharacterCommand();
	private static final Vector3 auxVector3_1 = new Vector3();
	private static final Vector2 auxVector2_1 = new Vector2();
	private static final Vector2 auxVector2_2 = new Vector2();
	private static final float CHARACTER_SPEED = 0.2f;

	private final MapGraphPath currentPath = new MapGraphPath();
	private final MapGraph map;
	private final IndexedAStarPathFinder<MapGraphNode> pathFinder;
	private final Heuristic<MapGraphNode> heuristic;
	private final List<CharacterSystemEventsSubscriber> subscribers = new ArrayList<>();
	private CharacterCommand currentCommand;

	public CharacterSystem(final MapGraph map) {
		this.map = map;
		this.pathFinder = new IndexedAStarPathFinder<>(map);
		this.heuristic = new GameHeuristic();
	}

	void applyCommand(final CharacterCommand command, final Entity character) {
		currentCommand = command;
		MapGraphNode sourceNode = map.getNode(ComponentsMapper.decal.get(character).getCellPosition(auxVector3_1));
		MapGraphNode destNode = command.getDestination();
		boolean foundPath = pathFinder.searchNodePath(sourceNode, destNode, heuristic, currentPath);
		CharacterComponent characterComponent = ComponentsMapper.character.get(character);
		if (foundPath && currentPath.nodes.size > 1) {
			characterComponent.setSpriteType(SpriteType.RUN);
			initDestinationNode(characterComponent, currentPath.get(1), currentPath.get(0));
		} else {
			currentCommand = null;
		}
	}

	public void destinationReached(final Entity character) {
		if (currentCommand.getType().getToDoAfterDestinationReached() != null) {
			executeActionsAfterDestinationReached(character);
		} else {
			commandDone(character);
		}
	}

	private void commandDone(final Entity character) {
		ComponentsMapper.character.get(character).setSpriteType(SpriteType.IDLE);
		currentCommand = null;
		currentPath.clear();
		for (CharacterSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onCommandDone(character);
		}
	}

	private void executeActionsAfterDestinationReached(final Entity character) {
		for (CharacterSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onDestinationReached(character);
		}
		currentCommand.getType().getToDoAfterDestinationReached().run(character, map);
	}

	private void initDestinationNode(final CharacterComponent characterComponent,
									 final MapGraphNode destNode,
									 final MapGraphNode srcNode) {
		Vector2 direction = destNode.getCenterPosition(auxVector2_2).sub(srcNode.getCenterPosition(auxVector2_1)).nor();
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
			SpriteType spriteType = characterComponent.getSpriteType();
			if (spriteType == SpriteType.ATTACK) {
				AnimationComponent animationComponent = ComponentsMapper.animation.get(character);
				if (animationComponent.getAnimation().isAnimationFinished(animationComponent.getStateTime())) {
					commandDone(character);
				}
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

	@Override
	public void onRunFrameChanged(final Entity character, final float deltaTime) {
		CharacterComponent characterComponent = ComponentsMapper.character.get(character);
		MapGraphNode oldDest = characterComponent.getDestinationNode();
		Decal decal = ComponentsMapper.decal.get(character).getDecal();
		if (auxVector2_1.set(decal.getX(), decal.getZ()).dst2(oldDest.getCenterPosition(auxVector2_2)) < Utils.EPSILON) {
			reachedNodeOfPath(character, characterComponent, oldDest);
		} else {
			takeStep(character, deltaTime);
		}
	}

	private void reachedNodeOfPath(final Entity character,
								   final CharacterComponent characterComponent,
								   final MapGraphNode oldDest) {
		MapGraphNode newDest = currentPath.getNextOf(oldDest);
		if (newDest != null) {
			initDestinationNode(characterComponent, newDest, oldDest);
		} else {
			destinationReached(character);
		}
	}

	private void takeStep(final Entity entity, final float deltaTime) {
		MapGraphNode oldDest = ComponentsMapper.character.get(entity).getDestinationNode();
		oldDest.getCenterPosition(auxVector2_2);
		Decal decal = ComponentsMapper.decal.get(entity).getDecal();
		auxVector2_1.set(decal.getX(), decal.getZ());
		Vector2 velocity = auxVector2_2.sub(auxVector2_1).nor().scl(CHARACTER_SPEED);
		decal.translate(auxVector3_1.set(velocity.x, 0, velocity.y));
	}
}
