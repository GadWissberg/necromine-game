package com.gadarts.isometric.systems.character;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.TimeUtils;
import com.gadarts.isometric.components.AnimationComponent;
import com.gadarts.isometric.components.CharacterDecalComponent;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.character.*;
import com.gadarts.isometric.components.character.CharacterComponent.Direction;
import com.gadarts.isometric.components.player.Weapon;
import com.gadarts.isometric.components.player.WeaponsDefinitions;
import com.gadarts.isometric.systems.GameEntitySystem;
import com.gadarts.isometric.systems.bullets.BulletsSystemEventsSubscriber;
import com.gadarts.isometric.systems.pickup.PickUpSystem;
import com.gadarts.isometric.systems.pickup.PickupSystemEventsSubscriber;
import com.gadarts.isometric.systems.render.RenderSystem;
import com.gadarts.isometric.systems.render.RenderSystemEventsSubscriber;
import com.gadarts.isometric.utils.EntityBuilder;
import com.gadarts.isometric.utils.Utils;
import com.gadarts.isometric.utils.assets.Assets;
import com.gadarts.isometric.utils.map.MapGraphNode;
import com.gadarts.isometric.utils.map.MapGraphPath;

import static com.gadarts.isometric.components.character.CharacterMotivation.TO_PICK_UP;
import static com.gadarts.isometric.components.character.SpriteType.PAIN;

/**
 * Handles characters behaviour.
 */
public class CharacterSystemImpl extends GameEntitySystem<CharacterSystemEventsSubscriber>
		implements RenderSystemEventsSubscriber,
		CharacterSystem,
		PickupSystemEventsSubscriber,
		BulletsSystemEventsSubscriber {

	private static final Vector3 auxVector3_1 = new Vector3();
	private static final Vector3 auxVector3_2 = new Vector3();
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
		currentCommand.setStarted(false);
		if (ComponentsMapper.character.get(character).getCharacterSpriteData().getSpriteType() != PAIN) {
			beginProcessingCommand(command, character);
		}
	}

	private void beginProcessingCommand(final CharacterCommand command, final Entity character) {
		currentCommand.setStarted(true);
		graphData.getCurrentPath().clear();
		if (command.getType().isRequiresMovement()) {
			graphData.getCurrentPath().nodes.addAll(command.getPath().nodes);
		}
		if (graphData.getCurrentPath().nodes.size > 1) {
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
		graphData.getCurrentPath().clear();
		CharacterComponent characterComponent = ComponentsMapper.character.get(character);
		characterComponent.setMotivation(null);
		characterComponent.getCharacterSpriteData().setSpriteType(SpriteType.IDLE);
		currentCommand = null;
		for (CharacterSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onCommandDone(character);
		}
	}

	private void handlePickup(final Entity character) {
		CharacterMotivation mode = ComponentsMapper.character.get(character).getMotivationData().getMotivation();
		if (mode == TO_PICK_UP && currentCommand.getAdditionalData() != null) {
			Entity itemPickedUp = (Entity) currentCommand.getAdditionalData();
			for (CharacterSystemEventsSubscriber subscriber : subscribers) {
				subscriber.onItemPickedUp(itemPickedUp);
			}
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
			SpriteType spriteType = characterComponent.getCharacterSpriteData().getSpriteType();
			if (spriteType == SpriteType.ATTACK || spriteType == SpriteType.PICKUP) {
				handleModeWithNonLoopingAnimation(character);
			} else {
				handleRotation(character, characterComponent);
			}
		}
		for (Entity character : characters) {
			handlePain(character);
		}
	}

	private void handlePain(final Entity character) {
		CharacterComponent characterComponent = ComponentsMapper.character.get(character);
		long lastDamage = characterComponent.getHealthData().getLastDamage();
		CharacterSpriteData spriteData = characterComponent.getCharacterSpriteData();
		if (spriteData.getSpriteType() == PAIN && TimeUtils.timeSinceMillis(lastDamage) > CHARACTER_PAIN_DURATION) {
			characterComponent.setMotivation(null);
			spriteData.setSpriteType(SpriteType.IDLE);
			if (currentCommand != null && !currentCommand.isStarted()) {
				applyCommand(currentCommand, character);
			}
		}
	}

	private void handleRotation(final Entity character, final CharacterComponent charComponent) {
		if (charComponent.getCharacterSpriteData().getSpriteType() == PAIN) return;
		CharacterRotationData rotationData = charComponent.getRotationData();
		if (rotationData.isRotating() && TimeUtils.timeSinceMillis(rotationData.getLastRotation()) > ROT_INTERVAL) {
			rotationData.setLastRotation(TimeUtils.millis());
			Direction directionToDest;
			CharacterSpriteData characterSpriteData = charComponent.getCharacterSpriteData();
			CharacterMotivationData motivationData = charComponent.getMotivationData();
			CharacterMotivation motivation = motivationData.getMotivation();
			if (motivation == CharacterMotivation.TO_ATTACK) {
				directionToDest = calculateDirectionToTarget(character);
			} else if (motivation == TO_PICK_UP) {
				directionToDest = characterSpriteData.getFacingDirection();
			} else {
				directionToDest = calculateDirectionToDestination(character);
			}
			if (characterSpriteData.getFacingDirection() != directionToDest) {
				rotate(charComponent, directionToDest);
			} else {
				rotationData.setRotating(false);
				SpriteType spriteType;
				if (motivation == CharacterMotivation.TO_ATTACK) {
					spriteType = SpriteType.ATTACK;
				} else if (motivation == CharacterMotivation.TO_PICK_UP) {
					spriteType = SpriteType.PICKUP;
				} else {
					spriteType = SpriteType.RUN;
				}
				ComponentsMapper.animation.get(character).resetStateTime();
				characterSpriteData.setSpriteType(spriteType);
			}
		}
	}

	private void rotate(final CharacterComponent charComponent, final Direction directionToDest) {
		CharacterSpriteData characterSpriteData = charComponent.getCharacterSpriteData();
		Vector2 currentDirVector = characterSpriteData.getFacingDirection().getDirection(auxVector2_1);
		int side;
		float diff = directionToDest.getDirection(auxVector2_2).angleDeg() - currentDirVector.angleDeg();
		if (auxVector2_3.set(1, 0).setAngleDeg(diff).angleDeg() > 180) {
			side = -1;
		} else {
			side = 1;
		}
		characterSpriteData.setFacingDirection(Direction.findDirection(currentDirVector.rotateDeg(45f * side)));
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
		Vector2 directionToDest = destPos.sub(pos.x, pos.z).nor();
		return Direction.findDirection(directionToDest);
	}

	private void handleModeWithNonLoopingAnimation(final Entity character) {
		AnimationComponent animationComponent = ComponentsMapper.animation.get(character);
		Animation<TextureAtlas.AtlasRegion> animation = animationComponent.getAnimation();
		if (animation.isAnimationFinished(animationComponent.getStateTime())) {
			SpriteType spriteType = ComponentsMapper.character.get(character).getCharacterSpriteData().getSpriteType();
			if (spriteType.isAddReverse()) {
				if (animationComponent.isDoingReverse()) {
					commandDone(character);
					animationComponent.setDoingReverse(false);
					animation.setPlayMode(Animation.PlayMode.NORMAL);
				} else {
					animationComponent.setDoingReverse(true);
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
		handleDeath(character);
	}

	private void handleDeath(final Entity character) {
		CharacterComponent characterComponent = ComponentsMapper.character.get(character);
		CharacterHealthData healthData = characterComponent.getHealthData();
		if (healthData.getHp() <= 0) {
			characterComponent.getCharacterSpriteData().setSpriteType(SpriteType.DIE);
			if (ComponentsMapper.animation.has(character)) {
				ComponentsMapper.animation.get(character).resetStateTime();
			}
			characterComponent.setMotivation(null);
			soundPlayer.playSound(characterComponent.getDeathSound());
		} else {
			soundPlayer.playSound(characterComponent.getPainSound());
			applyTargetToDisplayPain(character);
			for (CharacterSystemEventsSubscriber subscriber : subscribers) {
				subscriber.onCharacterGotDamage(character);
			}
			if (healthData.getHp() > 0) {
				characterComponent.setMotivation(null);
			}
		}
	}

	private void applyTargetToDisplayPain(final Entity character) {
		CharacterComponent characterComponent = ComponentsMapper.character.get(character);
		characterComponent.getCharacterSpriteData().setSpriteType(PAIN);
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
		CharacterSpriteData characterSpriteData = characterComponent.getCharacterSpriteData();
		if (characterSpriteData.getSpriteType() == SpriteType.RUN) {
			applyRunning(character, newFrame, characterComponent);
		} else if (characterSpriteData.getSpriteType() == SpriteType.ATTACK) {
			if (newFrame.index == characterSpriteData.getHitFrameIndex()) {
				Entity target = characterComponent.getTarget();
				if (ComponentsMapper.player.has(character)) {
					Weapon weapon = ComponentsMapper.player.get(character).getStorage().getSelectedWeapon();
					WeaponsDefinitions definition = (WeaponsDefinitions) weapon.getDefinition();
					if (definition.isMelee()) {
						applyDamageToCharacter(target);
					} else {
						CharacterDecalComponent characterDecalComponent = ComponentsMapper.characterDecal.get(character);
						Decal decal = characterDecalComponent.getDecal();
						Vector3 position = auxVector3_2.set(decal.getPosition());
						CharacterDecalComponent targetDecalComponent = ComponentsMapper.characterDecal.get(target);
						Vector3 targetPosition = targetDecalComponent.getDecal().getPosition();
						Vector2 direction = auxVector2_1.set(targetPosition.x, targetPosition.z).sub(position.x, position.z);
						EntityBuilder.beginBuildingEntity((PooledEngine) getEngine())
								.addBulletComponent(position, direction, character)
								.addSimpleDecalComponent(position, weapon.getBulletTextureRegion(), auxVector3_1.set(-90, 360f - direction.angleDeg(), 0))
								.finishAndAddToEngine();
					}
				} else {
					applyDamageToCharacter(target);
				}
			}
		} else if (characterSpriteData.getSpriteType() == SpriteType.PICKUP) {
			if (newFrame.index == 1 && ComponentsMapper.animation.get(character).isDoingReverse()) {
				handlePickup(character);
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
		ComponentsMapper.character.get(entity).getDestinationNode().getCenterPosition(auxVector2_2);
		CharacterDecalComponent characterDecalComponent = ComponentsMapper.characterDecal.get(entity);
		Decal decal = characterDecalComponent.getDecal();
		Decal shadowDecal = characterDecalComponent.getShadowDecal();
		auxVector2_1.set(decal.getX(), decal.getZ());
		Vector2 velocity = auxVector2_2.sub(auxVector2_1).nor().scl(CHARACTER_STEP_SIZE);
		decal.translate(auxVector3_1.set(velocity.x, 0, velocity.y));
		shadowDecal.translate(auxVector3_1.set(velocity.x, 0, velocity.y));
	}


	@Override
	public void onPickUpSystemReady(final PickUpSystem pickUpSystem) {
		addSystem(PickUpSystem.class, pickUpSystem);
	}

	@Override
	public void onBulletCollision(final Entity bullet, final Entity collidable) {
		if (ComponentsMapper.character.has(collidable)) {
			applyDamageToCharacter(collidable);
		}
	}
}
