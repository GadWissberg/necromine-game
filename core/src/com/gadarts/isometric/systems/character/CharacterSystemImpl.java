package com.gadarts.isometric.systems.character;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.TimeUtils;
import com.gadarts.isometric.components.AnimationComponent;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.character.CharacterComponent;
import com.gadarts.isometric.components.character.CharacterMotivation;
import com.gadarts.isometric.components.character.data.*;
import com.gadarts.isometric.components.decal.CharacterDecalComponent;
import com.gadarts.isometric.components.player.PlayerComponent;
import com.gadarts.isometric.components.player.Weapon;
import com.gadarts.isometric.systems.GameEntitySystem;
import com.gadarts.isometric.systems.bullets.BulletsSystemEventsSubscriber;
import com.gadarts.isometric.systems.character.commands.CharacterCommand;
import com.gadarts.isometric.systems.character.commands.CommandsHandler;
import com.gadarts.isometric.systems.pickup.PickUpSystem;
import com.gadarts.isometric.systems.pickup.PickupSystemEventsSubscriber;
import com.gadarts.isometric.systems.render.RenderSystemEventsSubscriber;
import com.gadarts.isometric.utils.EntityBuilder;
import com.gadarts.isometric.utils.SoundPlayer;
import com.gadarts.isometric.utils.Utils;
import com.gadarts.isometric.utils.map.MapGraph;
import com.gadarts.isometric.utils.map.MapGraphNode;
import com.gadarts.isometric.utils.map.MapGraphPath;
import com.gadarts.necromine.assets.Assets;
import com.gadarts.necromine.model.characters.CharacterTypes;
import com.gadarts.necromine.model.characters.Direction;
import com.gadarts.necromine.model.characters.SpriteType;
import com.gadarts.necromine.model.characters.attributes.Strength;
import com.gadarts.necromine.model.pickups.WeaponsDefinitions;

import static com.gadarts.isometric.components.character.CharacterMotivation.*;
import static com.gadarts.necromine.model.characters.SpriteType.*;

/**
 * Responsible for all character-related logic (whether player or enemy).
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
	private static final float CHARACTER_STEP_SIZE = 0.22f;
	private static final int ROT_INTERVAL = 125;
	private static final long CHARACTER_PAIN_DURATION = 1000;

	private CommandsHandler commandsHandler;
	private CharacterSystemGraphData graphData;
	private ImmutableArray<Entity> characters;
	private ParticleEffect bloodSplatterEffect;

	@Override
	public void addedToEngine(final Engine engine) {
		super.addedToEngine(engine);
		characters = engine.getEntitiesFor(Family.all(CharacterComponent.class).get());
	}

	@Override
	public void activate( ) {
		this.graphData = new CharacterSystemGraphData(services.getMapService().getMap());
		bloodSplatterEffect = services.getAssetManager().getParticleEffect(Assets.Particles.BLOOD_SPLATTER);
		commandsHandler = new CommandsHandler(graphData, subscribers, services.getSoundPlayer(), services.getMapService().getMap());
		for (CharacterSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onCharacterSystemReady(this);
		}
	}

	private boolean isPathHasUnrevealedNodes(final MapGraphPath plannedPath) {
		boolean result = false;
		for (MapGraphNode node : plannedPath.nodes) {
			if (services.getMapService().getMap().getFowMap()[node.getRow()][node.getCol()] == 0) {
				result = true;
				break;
			}
		}
		return result;
	}

	@Override
	public boolean calculatePath(final MapGraphNode sourceNode,
								 final MapGraphNode destinationNode,
								 final MapGraphPath outputPath) {
		outputPath.clear();
		boolean success = graphData.getPathFinder().searchNodePath(
				sourceNode,
				destinationNode,
				graphData.getHeuristic(),
				outputPath
		);
		return success && !isPathHasUnrevealedNodes(outputPath);
	}

	@Override
	public boolean calculatePathToCharacter(final MapGraphNode sourceNode,
											final Entity character,
											final MapGraphPath outputPath) {
		outputPath.clear();
		Vector2 cellPosition = ComponentsMapper.characterDecal.get(character).getNodePosition(auxVector2_1);
		return graphData.getPathFinder().searchNodePathBeforeCommand(
				sourceNode,
				services.getMapService().getMap().getNode((int) cellPosition.x, (int) cellPosition.y),
				graphData.getHeuristic(),
				outputPath
		);
	}


	private void handlePickup(final Entity character) {
		CharacterMotivation mode = ComponentsMapper.character.get(character).getMotivationData().getMotivation();
		CharacterCommand currentCommand = commandsHandler.getCurrentCommand();
		if (mode == TO_PICK_UP && currentCommand.getAdditionalData() != null) {
			Entity itemPickedUp = (Entity) currentCommand.getAdditionalData();
			for (CharacterSystemEventsSubscriber subscriber : subscribers) {
				subscriber.onItemPickedUp(itemPickedUp);
			}
		}
	}


	@Override
	public void update(final float deltaTime) {
		super.update(deltaTime);
		CharacterCommand currentCommand = commandsHandler.getCurrentCommand();
		if (currentCommand != null) {
			handleCurrentCommand(currentCommand);
		}
		for (Entity character : characters) {
			handlePain(character);
		}
	}

	private void handleCurrentCommand(final CharacterCommand currentCommand) {
		CharacterComponent characterComponent = ComponentsMapper.character.get(currentCommand.getCharacter());
		SpriteType spriteType = characterComponent.getCharacterSpriteData().getSpriteType();
		if (spriteType == ATTACK || spriteType == PICKUP || spriteType == ATTACK_PRIMARY) {
			handleModeWithNonLoopingAnimation(currentCommand.getCharacter());
		} else if (characterComponent.getMotivationData().getMotivation() == END_MY_TURN) {
			commandsHandler.commandDone(currentCommand.getCharacter());
		} else {
			handleRotation(currentCommand.getCharacter(), characterComponent);
		}
	}

	private void handlePain(final Entity character) {
		CharacterComponent characterComponent = ComponentsMapper.character.get(character);
		long lastDamage = characterComponent.getSkills().getHealthData().getLastDamage();
		CharacterSpriteData spriteData = characterComponent.getCharacterSpriteData();
		if (spriteData.getSpriteType() == PAIN && TimeUtils.timeSinceMillis(lastDamage) > CHARACTER_PAIN_DURATION) {
			characterComponent.setMotivation(null);
			spriteData.setSpriteType(IDLE);
			if (commandsHandler.getCurrentCommand() != null && !commandsHandler.getCurrentCommand().isStarted()) {
				applyCommand(commandsHandler.getCurrentCommand(), character);
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
					Integer motivationAdditionalData = (Integer) motivationData.getMotivationAdditionalData();
					if (motivationAdditionalData != null && motivationAdditionalData == USE_PRIMARY) {
						spriteType = ATTACK_PRIMARY;
					} else {
						spriteType = ATTACK;
					}
				} else if (motivation == CharacterMotivation.TO_PICK_UP) {
					spriteType = PICKUP;
				} else {
					spriteType = RUN;
				}
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
		Vector3 characterPos = auxVector3_1.set(ComponentsMapper.characterDecal.get(character).getDecal().getPosition());
		CharacterComponent characterComponent = ComponentsMapper.character.get(character);
		MapGraphNode destinationNode = characterComponent.getDestinationNode();
		Vector2 destPos = destinationNode.getCenterPosition(auxVector2_2);
		Vector2 directionToDest = destPos.sub(characterPos.x, characterPos.z).nor();
		return Direction.findDirection(directionToDest);
	}

	private Direction calculateDirectionToTarget(final Entity character) {
		Vector3 pos = auxVector3_1.set(ComponentsMapper.characterDecal.get(character).getDecal().getPosition());
		CharacterComponent characterComponent = ComponentsMapper.character.get(character);
		Entity target = characterComponent.getTarget();
		MapGraphNode targetNode = services.getMapService().getMap().getNode(ComponentsMapper.characterDecal.get(target).getDecal().getPosition());
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
					commandsHandler.commandDone(character);
					animationComponent.setDoingReverse(false);
					animation.setPlayMode(Animation.PlayMode.NORMAL);
				} else {
					animationComponent.setDoingReverse(true);
					animation.setPlayMode(Animation.PlayMode.REVERSED);
					animation.setFrameDuration(spriteType.getAnimationDuration());
					animationComponent.resetStateTime();
				}
			} else {
				commandsHandler.commandDone(character);
			}
		}
	}

	private void applyMeleeDamageToCharacter(final Entity attacker, final Entity attacked) {
		Strength strength = ComponentsMapper.character.get(attacker).getSkills().getStrength();
		applyDamageToCharacter(attacked, MathUtils.random(strength.getMinDamage(), strength.getMaxDamage()));
	}

	private void applyDamageToCharacter(final Entity attacked, final int damage) {
		CharacterComponent characterComponent = ComponentsMapper.character.get(attacked);
		characterComponent.dealDamage(damage);
		handleDeath(attacked);
		addSplatterEffect(attacked);
	}

	private void addSplatterEffect(final Entity attacked) {
		Vector3 pos = ComponentsMapper.characterDecal.get(attacked).getNodePosition(auxVector3_1);
		float height = calculateSplatterEffectHeight(attacked, pos);
		EntityBuilder.beginBuildingEntity((PooledEngine) getEngine())
				.addParticleComponent(bloodSplatterEffect, auxVector3_1.set(pos.x + 0.5F, height, pos.z + 0.5F))
				.finishAndAddToEngine();
	}

	private float calculateSplatterEffectHeight(final Entity attacked, final Vector3 pos) {
		float height = pos.y;
		if (ComponentsMapper.enemy.has(attacked)) {
			height += ComponentsMapper.enemy.get(attacked).getEnemyDefinition().getHeight() / 2F;
		} else if (ComponentsMapper.player.has(attacked)) {
			height += PlayerComponent.PLAYER_HEIGHT;
		}
		return height;
	}

	private void handleDeath(final Entity character) {
		CharacterComponent characterComponent = ComponentsMapper.character.get(character);
		CharacterHealthData healthData = characterComponent.getSkills().getHealthData();
		CharacterSoundData soundData = characterComponent.getSoundData();
		SoundPlayer soundPlayer = services.getSoundPlayer();
		if (healthData.getHp() <= 0) {
			CharacterSpriteData charSpriteData = characterComponent.getCharacterSpriteData();
			charSpriteData.setSpriteType(charSpriteData.isSingleDeathAnimation() ? LIGHT_DEATH_1 : randomLightDeath());
			if (ComponentsMapper.animation.has(character)) {
				ComponentsMapper.animation.get(character).resetStateTime();
			}
			characterComponent.setMotivation(null);
			soundPlayer.playSound(soundData.getDeathSound());
			for (CharacterSystemEventsSubscriber subscriber : subscribers) {
				subscriber.onCharacterDies(character);
			}
		} else {
			soundPlayer.playSound(soundData.getPainSound());
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
	public void dispose( ) {

	}


	@Override
	public boolean isProcessingCommand( ) {
		return commandsHandler.getCurrentCommand() != null;
	}

	@Override
	public void applyCommand(final CharacterCommand command, final Entity character) {
		commandsHandler.applyCommand(command, character);
	}

	@Override
	public void onFrameChanged(final Entity character, final float deltaTime, final TextureAtlas.AtlasRegion newFrame) {
		CharacterComponent characterComponent = ComponentsMapper.character.get(character);
		CharacterSpriteData characterSpriteData = characterComponent.getCharacterSpriteData();
		if (characterSpriteData.getSpriteType() == RUN) {
			applyRunning(character, newFrame, characterComponent);
		} else if (characterSpriteData.getSpriteType() == ATTACK) {
			if (newFrame.index == characterSpriteData.getMeleeHitFrameIndex()) {
				Entity target = characterComponent.getTarget();
				if (ComponentsMapper.player.has(character)) {
					Weapon weapon = ComponentsMapper.player.get(character).getStorage().getSelectedWeapon();
					WeaponsDefinitions definition = (WeaponsDefinitions) weapon.getDefinition();
					if (definition.isMelee()) {
						applyMeleeDamageToCharacter(character, target);
					}
				} else {
					applyMeleeDamageToCharacter(character, target);
				}
			}
		} else if (characterSpriteData.getSpriteType() == ATTACK_PRIMARY) {
			if (newFrame.index == characterSpriteData.getPrimaryAttackHitFrameIndex()) {
				Entity target = characterComponent.getTarget();
				CharacterDecalComponent characterDecalComponent = ComponentsMapper.characterDecal.get(character);
				Decal decal = characterDecalComponent.getDecal();
				CharacterDecalComponent targetDecalComponent = ComponentsMapper.characterDecal.get(target);
				MapGraph map = services.getMapService().getMap();
				MapGraphNode targetNode = map.getNode(targetDecalComponent.getDecal().getPosition());
				MapGraphNode positionNode = map.getNode(decal.getPosition());
				Vector2 targetNodeCenterPosition = targetNode.getCenterPosition(auxVector2_1);
				Vector2 positionNodeCenterPosition = positionNode.getCenterPosition(auxVector2_2);
				Vector3 direction = auxVector3_1.set(targetNodeCenterPosition.x, 0, targetNodeCenterPosition.y).sub(positionNodeCenterPosition.x, 0, positionNodeCenterPosition.y);
				for (CharacterSystemEventsSubscriber subscriber : subscribers) {
					subscriber.onCharacterEngagesPrimaryAttack(character, direction, auxVector3_2.set(positionNodeCenterPosition.x, 0, positionNodeCenterPosition.y));
				}
			}
		} else if (characterSpriteData.getSpriteType() == PICKUP) {
			if (newFrame.index == 1 && ComponentsMapper.animation.get(character).isDoingReverse()) {
				handlePickup(character);
			}
		}
	}

	private void applyRunning(final Entity character,
							  final TextureAtlas.AtlasRegion newFrame,
							  final CharacterComponent characterComponent) {
		playStepSoundWhenNeeded(newFrame, ComponentsMapper.character.get(character));
		MapGraphNode oldDest = characterComponent.getDestinationNode();
		Decal decal = ComponentsMapper.characterDecal.get(character).getDecal();
		if (auxVector2_1.set(decal.getX(), decal.getZ()).dst2(oldDest.getCenterPosition(auxVector2_2)) < Utils.EPSILON) {
			reachedNodeOfPath(character, oldDest);
		} else {
			takeStep(character);
		}
	}

	private void playStepSoundWhenNeeded(final TextureAtlas.AtlasRegion newFrame,
										 final CharacterComponent characterComponent) {
		if (newFrame.index == 0 || newFrame.index == 5) {
			services.getSoundPlayer().playSound(characterComponent.getSoundData().getStepSound());
		}
	}


	private void reachedNodeOfPath(final Entity character,
								   final MapGraphNode oldDest) {
		CharacterComponent characterComponent = ComponentsMapper.character.get(character);
		MapGraphNode newDest = graphData.getCurrentPath().getNextOf(oldDest);
		if (newDest != null) {
			commandsHandler.initDestinationNode(characterComponent, newDest);
			takeStep(character);
		} else {
			commandsHandler.destinationReached(character);
		}
	}

	private void takeStep(final Entity entity) {
		CharacterDecalComponent characterDecalComponent = ComponentsMapper.characterDecal.get(entity);
		MapGraph map = services.getMapService().getMap();
		MapGraphNode oldNode = map.getNode(characterDecalComponent.getNodePosition(auxVector2_3));
		translateCharacter(entity, characterDecalComponent);
		MapGraphNode newNode = map.getNode(characterDecalComponent.getNodePosition(auxVector2_3));
		if (oldNode != newNode) {
			enteredNewNode(entity, oldNode, newNode);
		}
	}

	private void enteredNewNode(final Entity entity, final MapGraphNode oldNode, final MapGraphNode newNode) {
		fixHeightPositionOfDecals(entity, newNode);
		for (CharacterSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onCharacterNodeChanged(entity, oldNode, newNode);
		}
	}

	private void fixHeightPositionOfDecals(final Entity entity, final MapGraphNode newNode) {
		CharacterDecalComponent characterDecalComponent = ComponentsMapper.characterDecal.get(entity);
		Decal decal = characterDecalComponent.getDecal();
		Vector3 position = decal.getPosition();
		float newNodeHeight = newNode.getHeight();
		decal.setPosition(position.x, newNodeHeight + CharacterTypes.BILLBOARD_Y, position.z);
		Decal shadowDecal = characterDecalComponent.getShadowDecal();
		Vector3 shadowPos = shadowDecal.getPosition();
		shadowDecal.setPosition(shadowPos.x, position.y - CharacterDecalComponent.SHADOW_OFFSET_Y, shadowPos.z);
	}

	private void translateCharacter(final Entity entity, final CharacterDecalComponent characterDecalComponent) {
		ComponentsMapper.character.get(entity).getDestinationNode().getCenterPosition(auxVector2_2);
		Decal decal = characterDecalComponent.getDecal();
		Vector2 velocity = auxVector2_2.sub(auxVector2_1.set(decal.getX(), decal.getZ())).nor().scl(CHARACTER_STEP_SIZE);
		decal.translate(auxVector3_1.set(velocity.x, 0, velocity.y));
		characterDecalComponent.getShadowDecal().translate(auxVector3_1.set(velocity.x, 0, velocity.y));
	}


	@Override
	public void onPickUpSystemReady(final PickUpSystem pickUpSystem) {
		addSystem(PickUpSystem.class, pickUpSystem);
	}

	@Override
	public void onProjectileCollisionWithAnotherEntity(final Entity bullet, final Entity collidable) {
		if (ComponentsMapper.character.has(collidable)) {
			applyDamageToCharacter(collidable, ComponentsMapper.bullet.get(bullet).getDamage());
		}
	}

	@Override
	public void onHitScanCollisionWithAnotherEntity(final WeaponsDefinitions definition, final Entity collidable) {
		if (ComponentsMapper.character.has(collidable)) {
			applyDamageToCharacter(collidable, definition.getDamage());
		}
	}

}
