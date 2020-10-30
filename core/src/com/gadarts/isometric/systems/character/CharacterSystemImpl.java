package com.gadarts.isometric.systems.character;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.TimeUtils;
import com.gadarts.isometric.components.AnimationComponent;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.character.CharacterComponent;
import com.gadarts.isometric.components.character.CharacterComponent.Direction;
import com.gadarts.isometric.components.character.CharacterMode;
import com.gadarts.isometric.components.character.CharacterRotationData;
import com.gadarts.isometric.components.character.SpriteType;
import com.gadarts.isometric.systems.GameEntitySystem;
import com.gadarts.isometric.systems.pickup.PickUpSystem;
import com.gadarts.isometric.systems.pickup.PickupSystemEventsSubscriber;
import com.gadarts.isometric.systems.render.RenderSystem;
import com.gadarts.isometric.systems.render.RenderSystemEventsSubscriber;
import com.gadarts.isometric.utils.Utils;
import com.gadarts.isometric.utils.assets.Assets;
import com.gadarts.isometric.utils.map.MapGraphNode;
import com.gadarts.isometric.utils.map.MapGraphPath;

import static com.gadarts.isometric.components.character.CharacterMode.*;

/**
 * Handles characters behaviour.
 */
public class CharacterSystemImpl extends GameEntitySystem<CharacterSystemEventsSubscriber>
		implements RenderSystemEventsSubscriber,
		CharacterSystem,
		PickupSystemEventsSubscriber {

	private static final Vector3 auxVector3_1 = new Vector3();
	private static final Vector2 auxVector2_1 = new Vector2();
	private static final Vector2 auxVector2_2 = new Vector2();
	private static final Vector2 auxVector2_3 = new Vector2();
	private static final float CHARACTER_STEP_SIZE = 0.3f;
	private static final int ROT_INTERVAL = 125;
	private static final long CHARACTER_PAIN_DURATION = 1000;

	private CharacterSystemGraphData graphData;
	private CharacterCommand currentCommand;
	private ImmutableArray<Entity> characters;

	@Override
	public void addedToEngine(final Engine engine) {
		super.addedToEngine(engine);
		characters = engine.getEntitiesFor(Family.all(CharacterComponent.class).get());
	}

	@Override
	public void activate() {
		this.graphData = new CharacterSystemGraphData(map);
		for (CharacterSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onCharacterSystemReady(this);
		}
	}

	/**
	 * Applies a given command on the given character.
	 *
	 * @param command
	 * @param character
	 */
	@SuppressWarnings("JavaDoc")
	public void applyCommand(final CharacterCommand command, final Entity character) {
		currentCommand = command;
		MapGraphPath currentPath = graphData.getCurrentPath();
		currentPath.clear();
		currentPath.nodes.addAll(command.getPath().nodes);
		if (currentPath.nodes.size > 1) {
			commandSet(command, character);
		} else {
			destinationReached(character);
		}
	}

	@Override
	public boolean calculatePath(final MapGraphNode sourceNode,
								 final MapGraphNode destinationNode,
								 final GraphPath<MapGraphNode> outputPath) {
		outputPath.clear();
		return graphData.getPathFinder().searchNodePath(
				sourceNode,
				destinationNode,
				graphData.getHeuristic(),
				outputPath
		);
	}

	@Override
	public boolean calculatePathToCharacter(final MapGraphNode sourceNode,
											final Entity character,
											final MapGraphPath outputPath) {
		outputPath.clear();
		return graphData.getPathFinder().searchNodePathBeforeCommand(
				sourceNode,
				map.getNode(ComponentsMapper.characterDecal.get(character).getCellPosition(auxVector3_1)),
				graphData.getHeuristic(),
				outputPath
		);
	}

	private void commandSet(final CharacterCommand command, final Entity character) {
		for (CharacterSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onNewCommandSet(command);
		}
		initDestinationNode(ComponentsMapper.character.get(character), graphData.getCurrentPath().get(1));
	}

	public void destinationReached(final Entity character) {
		if (currentCommand.getType().getToDoAfterDestinationReached() != null) {
			executeActionsAfterDestinationReached(character);
		} else {
			commandDone(character);
		}
	}

	private void commandDone(final Entity character) {
		handlePickupOnCommandDone(character);
		graphData.getCurrentPath().clear();
		CharacterComponent characterComponent = ComponentsMapper.character.get(character);
		characterComponent.setMode(IDLE);
		characterComponent.setSpriteType(SpriteType.IDLE);
		currentCommand = null;
		for (CharacterSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onCommandDone(character);
		}
	}

	private void handlePickupOnCommandDone(final Entity character) {
		CharacterMode mode = ComponentsMapper.character.get(character).getMode();
		if (mode == PICKING_UP && currentCommand.getAdditionalData() != null) {
			getSystem(PickUpSystem.class).onItemPickedUp((Entity) currentCommand.getAdditionalData());
		}
	}

	private void executeActionsAfterDestinationReached(final Entity character) {
		for (CharacterSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onDestinationReached(character);
		}
		currentCommand.getType()
				.getToDoAfterDestinationReached()
				.run(character, map, soundPlayer, currentCommand.getAdditionalData());
	}

	private void initDestinationNode(final CharacterComponent characterComponent,
									 final MapGraphNode destNode) {
		characterComponent.getRotationData().setRotating(true);
		characterComponent.setDestinationNode(destNode);
	}

	@Override
	public void update(final float deltaTime) {
		super.update(deltaTime);
		if (currentCommand != null) {
			Entity character = currentCommand.getCharacter();
			CharacterComponent characterComponent = ComponentsMapper.character.get(character);
			SpriteType spriteType = characterComponent.getSpriteType();
			if (spriteType == SpriteType.ATTACK || spriteType == SpriteType.PICKUP) {
				handleModeWithNonLoopingAnimation(character);
			} else {
				Commands type = currentCommand.getType();
				if (type == Commands.GO_TO || type == Commands.GO_TO_MELEE || type == Commands.GO_TO_PICKUP) {
					handleRotation(character, characterComponent);
				}
			}
		}
		for (Entity character : characters) {
			handlePain(character);
		}
	}

	private void handlePain(final Entity character) {
		CharacterComponent characterComponent = ComponentsMapper.character.get(character);
		long lastDamage = characterComponent.getLastDamage();
		if (characterComponent.getMode() == PAIN && TimeUtils.timeSinceMillis(lastDamage) > CHARACTER_PAIN_DURATION) {
			characterComponent.setMode(IDLE);
			characterComponent.setSpriteType(SpriteType.IDLE);
		}
	}

	private void handleRotation(final Entity character, final CharacterComponent charComponent) {
		if (charComponent.getMode() == CharacterMode.PAIN) return;
		CharacterRotationData rotationData = charComponent.getRotationData();
		if (rotationData.isRotating() && TimeUtils.timeSinceMillis(rotationData.getLastRotation()) > ROT_INTERVAL) {
			rotationData.setLastRotation(TimeUtils.millis());
			Direction directionToDest;
			if (charComponent.getMode() == CharacterMode.ATTACKING) {
				directionToDest = calculateDirectionToTarget(character);
			} else if (charComponent.getMode() == PICKING_UP) {
				directionToDest = charComponent.getFacingDirection();
			} else {
				directionToDest = calculateDirectionToDestination(character);
			}
			if (charComponent.getFacingDirection() != directionToDest) {
				rotate(charComponent, directionToDest);
			} else {
				rotationData.setRotating(false);
				SpriteType spriteType;
				if (charComponent.getMode() == CharacterMode.ATTACKING) {
					spriteType = SpriteType.ATTACK;
				} else if (charComponent.getMode() == CharacterMode.PICKING_UP) {
					spriteType = SpriteType.PICKUP;
				} else {
					spriteType = SpriteType.RUN;
				}
				charComponent.setSpriteType(spriteType);
			}
		}
	}

	private void rotate(final CharacterComponent charComponent, final Direction directionToDest) {
		Vector2 currentDirVector = charComponent.getFacingDirection().getDirection(auxVector2_1);
		int side;
		float diff = directionToDest.getDirection(auxVector2_2).angle() - currentDirVector.angle();
		if (auxVector2_3.set(1, 0).setAngle(diff).angle() > 180) {
			side = -1;
		} else {
			side = 1;
		}
		charComponent.setFacingDirection(Direction.findDirection(currentDirVector.rotate(45f * side)));
	}

	private Direction calculateDirectionToDestination(final Entity character) {
		Vector3 pos = auxVector3_1.set(ComponentsMapper.characterDecal.get(character).getDecal().getPosition());
		CharacterComponent characterComponent = ComponentsMapper.character.get(character);
		MapGraphNode destinationNode = characterComponent.getDestinationNode();
		Vector2 destPos = destinationNode.getCenterPosition(auxVector2_2);
		Vector2 directionToDest = destPos.sub(map.getNode(pos).getCenterPosition(auxVector2_1)).nor();
		return Direction.findDirection(directionToDest);
	}

	private Direction calculateDirectionToTarget(final Entity character) {
		Vector3 pos = auxVector3_1.set(ComponentsMapper.characterDecal.get(character).getDecal().getPosition());
		CharacterComponent characterComponent = ComponentsMapper.character.get(character);
		Entity target = characterComponent.getTarget();
		MapGraphNode targetNode = map.getNode(ComponentsMapper.characterDecal.get(target).getDecal().getPosition());
		Vector2 destPos = targetNode.getCenterPosition(auxVector2_2);
		Vector2 directionToDest = destPos.sub(map.getNode(pos).getCenterPosition(auxVector2_1)).nor();
		return Direction.findDirection(directionToDest);
	}

	private void handleModeWithNonLoopingAnimation(final Entity character) {
		AnimationComponent animationComponent = ComponentsMapper.animation.get(character);
		Animation<TextureAtlas.AtlasRegion> animation = animationComponent.getAnimation();
		if (animation.isAnimationFinished(animationComponent.getStateTime())) {
			SpriteType spriteType = ComponentsMapper.character.get(character).getSpriteType();
			if (spriteType.isAddReverse()) {
				if (animationComponent.isDoReverse()) {
					commandDone(character);
					animationComponent.setDoReverse(false);
				} else {
					animationComponent.setDoReverse(true);
					animation.setPlayMode(Animation.PlayMode.REVERSED);
					animation.setFrameDuration(spriteType.getAnimationDuration());
					animationComponent.resetStateTime();
				}
			} else {
				commandDone(character);
			}
		}
	}

	private void applyDamageToCharacter(final Entity character) {
		CharacterComponent characterComponent = ComponentsMapper.character.get(character);
		characterComponent.dealDamage(1);
		if (characterComponent.getHp() <= 0) {
			characterComponent.setSpriteType(SpriteType.DIE);
			characterComponent.setMode(CharacterMode.DEAD);
			soundPlayer.playSound(Assets.Sounds.ENEMY_DIE);
		} else {
			applyTargetToDisplayPain(character);
			for (CharacterSystemEventsSubscriber subscriber : subscribers) {
				subscriber.onCharacterGotDamage(character);
			}
			if (characterComponent.getHp() > 0) {
				characterComponent.setMode(CharacterMode.PAIN);
			}
		}
	}

	private void applyTargetToDisplayPain(final Entity character) {
		CharacterComponent characterComponent = ComponentsMapper.character.get(character);
		characterComponent.setSpriteType(SpriteType.PAIN);
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
	public void onFrameChanged(final Entity character, final float deltaTime, final TextureAtlas.AtlasRegion newFrame) {
		CharacterComponent characterComponent = ComponentsMapper.character.get(character);
		if (characterComponent.getSpriteType() == SpriteType.RUN) {
			applyRunning(character, newFrame, characterComponent);
		} else if (characterComponent.getSpriteType() == SpriteType.ATTACK) {
			if (newFrame.index == 1) {
				soundPlayer.playSound(Assets.Sounds.ATTACK_CLAW);
				applyDamageToCharacter(characterComponent.getTarget());
			}
		}
	}

	private void applyRunning(final Entity character,
							  final TextureAtlas.AtlasRegion newFrame,
							  final CharacterComponent characterComponent) {
		if (newFrame.index % 2 == 0) {
			soundPlayer.playRandomSound(Assets.Sounds.STEP_1, Assets.Sounds.STEP_2, Assets.Sounds.STEP_3);
		}
		MapGraphNode oldDest = characterComponent.getDestinationNode();
		Decal decal = ComponentsMapper.characterDecal.get(character).getDecal();
		if (auxVector2_1.set(decal.getX(), decal.getZ()).dst2(oldDest.getCenterPosition(auxVector2_2)) < Utils.EPSILON) {
			reachedNodeOfPath(character, oldDest);
		} else {
			takeStep(character);
		}
	}

	@Override
	public void onRenderSystemReady(final RenderSystem renderSystem) {

	}

	private void reachedNodeOfPath(final Entity character,
								   final MapGraphNode oldDest) {
		CharacterComponent characterComponent = ComponentsMapper.character.get(character);
		MapGraphNode newDest = graphData.getCurrentPath().getNextOf(oldDest);
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
		Decal decal = ComponentsMapper.characterDecal.get(entity).getDecal();
		auxVector2_1.set(decal.getX(), decal.getZ());
		Vector2 velocity = auxVector2_2.sub(auxVector2_1).nor().scl(CHARACTER_STEP_SIZE);
		decal.translate(auxVector3_1.set(velocity.x, 0, velocity.y));
	}


	@Override
	public void onPickUpSystemReady(final PickUpSystem pickUpSystem) {
		addSystem(PickUpSystem.class, pickUpSystem);
	}
}
