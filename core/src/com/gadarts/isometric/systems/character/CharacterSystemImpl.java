package com.gadarts.isometric.systems.character;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.pfa.Heuristic;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.TimeUtils;
import com.gadarts.isometric.components.AnimationComponent;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.character.CharacterComponent;
import com.gadarts.isometric.components.character.CharacterComponent.Direction;
import com.gadarts.isometric.components.character.SpriteType;
import com.gadarts.isometric.systems.GameEntitySystem;
import com.gadarts.isometric.systems.render.RenderSystemEventsSubscriber;
import com.gadarts.isometric.utils.Utils;
import com.gadarts.isometric.utils.map.GameHeuristic;
import com.gadarts.isometric.utils.map.MapGraph;
import com.gadarts.isometric.utils.map.MapGraphNode;
import com.gadarts.isometric.utils.map.MapGraphPath;

/**
 * Handles characters behaviour.
 */
public class CharacterSystemImpl extends GameEntitySystem<CharacterSystemEventsSubscriber>
		implements RenderSystemEventsSubscriber, CharacterSystem {

	private static final Vector3 auxVector3_1 = new Vector3();
	private static final Vector2 auxVector2_1 = new Vector2();
	private static final Vector2 auxVector2_2 = new Vector2();
	private static final Vector2 auxVector2_3 = new Vector2();
	private static final float CHARACTER_STEP_SIZE = 0.3f;
	private static final int ROT_INTERVAL = 125;

	private final MapGraphPath currentPath = new MapGraphPath();
	private final MapGraph map;
	private final IndexedAStarPathFinder<MapGraphNode> pathFinder;
	private final Heuristic<MapGraphNode> heuristic;
	private CharacterCommand currentCommand;

	public CharacterSystemImpl(final MapGraph map) {
		this.map = map;
		this.pathFinder = new IndexedAStarPathFinder<>(map);
		this.heuristic = new GameHeuristic();
	}

	/**
	 * Applies a given command on the given character.
	 *
	 * @param command
	 * @param character
	 */
	@SuppressWarnings("JavaDoc")
	public void applyCommand(final CharacterCommand command, final Entity character) {
		boolean foundPath = calculatePathForCommand(command, character);
		currentCommand = command;
		if (currentPath.nodes.size > 1) {
			if (foundPath) {
				for (CharacterSystemEventsSubscriber subscriber : subscribers) {
					subscriber.onNewCommandSet(command);
				}
				initDestinationNode(ComponentsMapper.character.get(character), currentPath.get(1));
			}
		} else {
			destinationReached(character);
		}
	}

	private boolean calculatePathForCommand(final CharacterCommand command, final Entity character) {
		currentPath.clear();
		MapGraphNode sourceNode = map.getNode(ComponentsMapper.decal.get(character).getCellPosition(auxVector3_1));
		MapGraphNode destNode = command.getDestination();
		return pathFinder.searchNodePath(sourceNode, destNode, heuristic, currentPath);
	}

	public void destinationReached(final Entity character) {
		if (currentCommand.getType().getToDoAfterDestinationReached() != null) {
			executeActionsAfterDestinationReached(character);
		} else {
			commandDone(character);
		}
	}

	private void commandDone(final Entity character) {
		ComponentsMapper.character.get(character).setAttacking(false);
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
									 final MapGraphNode destNode) {
		characterComponent.setRotating(true);
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
				handleAttack(character);
			} else if (currentCommand.getType() == Commands.GO_TO || currentCommand.getType() == Commands.GO_TO_MELEE) {
				handleRotation(character, characterComponent);
			}
		}
	}

	private void handleRotation(final Entity character, final CharacterComponent charComponent) {
		if (charComponent.isRotating() && TimeUtils.timeSinceMillis(charComponent.getLastRotation()) > ROT_INTERVAL) {
			charComponent.setLastRotation(TimeUtils.millis());
			Direction directionToDest;
			if (!charComponent.isAttacking()) {
				directionToDest = calculateDirectionToDestination(character);
			} else {
				directionToDest = calculateDirectionToTarget(character);
			}
			if (charComponent.getDirection() != directionToDest) {
				rotate(charComponent, directionToDest);
			} else {
				charComponent.setRotating(false);
				charComponent.setSpriteType(charComponent.isAttacking() ? SpriteType.ATTACK : SpriteType.RUN);
			}
		}
	}

	private void rotate(final CharacterComponent charComponent, final Direction directionToDest) {
		Vector2 currentDirVector = charComponent.getDirection().getDirection(auxVector2_1);
		int side;
		float diff = directionToDest.getDirection(auxVector2_2).angle() - currentDirVector.angle();
		if (auxVector2_3.set(1, 0).setAngle(diff).angle() > 180) {
			side = -1;
		} else {
			side = 1;
		}
		charComponent.setDirection(Direction.findDirection(currentDirVector.rotate(45f * side)));
	}

	private Direction calculateDirectionToDestination(final Entity character) {
		Vector3 pos = auxVector3_1.set(ComponentsMapper.decal.get(character).getDecal().getPosition());
		CharacterComponent characterComponent = ComponentsMapper.character.get(character);
		MapGraphNode destinationNode = characterComponent.getDestinationNode();
		Vector2 destPos = destinationNode.getCenterPosition(auxVector2_2);
		Vector2 directionToDest = destPos.sub(map.getNode(pos).getCenterPosition(auxVector2_1)).nor();
		return Direction.findDirection(directionToDest);
	}

	private Direction calculateDirectionToTarget(final Entity character) {
		Vector3 pos = auxVector3_1.set(ComponentsMapper.decal.get(character).getDecal().getPosition());
		CharacterComponent characterComponent = ComponentsMapper.character.get(character);
		Entity target = characterComponent.getTarget();
		MapGraphNode targetNode = map.getNode(ComponentsMapper.decal.get(target).getDecal().getPosition());
		Vector2 destPos = targetNode.getCenterPosition(auxVector2_2);
		Vector2 directionToDest = destPos.sub(map.getNode(pos).getCenterPosition(auxVector2_1)).nor();
		return Direction.findDirection(directionToDest);
	}

	private void handleAttack(final Entity character) {
		AnimationComponent animationComponent = ComponentsMapper.animation.get(character);
		if (animationComponent.getAnimation().isAnimationFinished(animationComponent.getStateTime())) {
			commandDone(character);
		}
	}

	@Override
	public void dispose() {

	}


	/**
	 * @return Whether a command is being processed.
	 */
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
			takeStep(character);
		}
	}

	private void reachedNodeOfPath(final Entity character,
								   final CharacterComponent characterComponent,
								   final MapGraphNode oldDest) {
		MapGraphNode newDest = currentPath.getNextOf(oldDest);
		if (newDest != null) {
			initDestinationNode(characterComponent, newDest);
			takeStep(character);
		} else {
			destinationReached(character);
		}
	}

	private void takeStep(final Entity entity) {
		MapGraphNode oldDest = ComponentsMapper.character.get(entity).getDestinationNode();
		oldDest.getCenterPosition(auxVector2_2);
		Decal decal = ComponentsMapper.decal.get(entity).getDecal();
		auxVector2_1.set(decal.getX(), decal.getZ());
		Vector2 velocity = auxVector2_2.sub(auxVector2_1).nor().scl(CHARACTER_STEP_SIZE);
		decal.translate(auxVector3_1.set(velocity.x, 0, velocity.y));
	}

	@Override
	public void init() {
		for (CharacterSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onCharacterSystemReady(this);
		}
	}
}
