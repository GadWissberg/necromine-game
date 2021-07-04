package com.gadarts.isometric.utils;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pools;
import com.gadarts.isometric.components.*;
import com.gadarts.isometric.components.character.CharacterAnimations;
import com.gadarts.isometric.components.character.CharacterComponent;
import com.gadarts.isometric.components.character.CharacterSkillsParameters;
import com.gadarts.isometric.components.character.data.CharacterSoundData;
import com.gadarts.isometric.components.character.data.CharacterSpriteData;
import com.gadarts.isometric.components.decal.CharacterDecalComponent;
import com.gadarts.isometric.components.decal.SimpleDecalComponent;
import com.gadarts.isometric.components.enemy.EnemyComponent;
import com.gadarts.isometric.components.model.GameModelInstance;
import com.gadarts.isometric.components.player.Item;
import com.gadarts.isometric.components.player.PlayerComponent;
import com.gadarts.isometric.components.player.Weapon;
import com.gadarts.necromine.model.EnvironmentDefinitions;
import com.gadarts.necromine.model.MapNodeData;
import com.gadarts.necromine.model.characters.Direction;
import com.gadarts.necromine.model.characters.Enemies;
import com.gadarts.necromine.model.characters.SpriteType;
import com.gadarts.necromine.model.pickups.ItemDefinition;
import com.gadarts.necromine.model.pickups.WeaponsDefinitions;
import lombok.AccessLevel;
import lombok.Setter;

import java.util.Optional;

import static com.gadarts.necromine.model.characters.CharacterTypes.BILLBOARD_SCALE;

@SuppressWarnings("UnusedReturnValue")
public final class EntityBuilder {
	public static final String MSG_FAIL_CALL_BEGIN_BUILDING_ENTITY_FIRST = "Call beginBuildingEntity() first!";
	private static final EntityBuilder instance = new EntityBuilder();
	private final static Vector2 auxVector = new Vector2();
	private Entity currentEntity;

	@Setter(AccessLevel.PRIVATE)
	private PooledEngine engine;


	private EntityBuilder( ) {
	}

	public static EntityBuilder beginBuildingEntity(final PooledEngine engine) {
		instance.init(engine);
		return instance;
	}

	private void init(final PooledEngine engine) {
		this.engine = engine;
		this.currentEntity = engine.createEntity();
	}

	public EntityBuilder addModelInstanceComponent(final GameModelInstance modelInstance, final boolean visible) {
		return addModelInstanceComponent(modelInstance, visible, true);
	}

	public EntityBuilder addModelInstanceComponent(final GameModelInstance modelInstance,
												   final boolean visible,
												   final boolean castShadow) {
		if (engine == null) throw new RuntimeException(MSG_FAIL_CALL_BEGIN_BUILDING_ENTITY_FIRST);
		ModelInstanceComponent component = engine.createComponent(ModelInstanceComponent.class);
		component.init(modelInstance, visible, castShadow);
		currentEntity.add(component);
		component.getModelInstance().userData = currentEntity;
		return instance;
	}

	public EntityBuilder addWallComponent(final MapNodeData parentNode) {
		if (engine == null) throw new RuntimeException(MSG_FAIL_CALL_BEGIN_BUILDING_ENTITY_FIRST);
		WallComponent component = engine.createComponent(WallComponent.class);
		component.init(parentNode);
		currentEntity.add(component);
		return instance;
	}

	public Entity finishAndAddToEngine( ) {
		if (engine == null) throw new RuntimeException(MSG_FAIL_CALL_BEGIN_BUILDING_ENTITY_FIRST);
		engine.addEntity(currentEntity);
		return finish();
	}

	public Entity finish( ) {
		if (engine == null) throw new RuntimeException(MSG_FAIL_CALL_BEGIN_BUILDING_ENTITY_FIRST);
		Entity result = currentEntity;
		instance.reset();
		return result;
	}

	private void reset( ) {
		engine = null;
		currentEntity = null;
	}

	public EntityBuilder addCursorComponent( ) {
		if (engine == null) throw new RuntimeException(MSG_FAIL_CALL_BEGIN_BUILDING_ENTITY_FIRST);
		CursorComponent cursorComponent = engine.createComponent(CursorComponent.class);
		currentEntity.add(cursorComponent);
		return instance;
	}

	public EntityBuilder addPlayerComponent(final Weapon selectedWeapon, final CharacterAnimations general) {
		if (engine == null) throw new RuntimeException(MSG_FAIL_CALL_BEGIN_BUILDING_ENTITY_FIRST);
		PlayerComponent playerComponent = engine.createComponent(PlayerComponent.class);
		playerComponent.init(selectedWeapon, general);
		currentEntity.add(playerComponent);
		return instance;
	}

	public EntityBuilder addCharacterComponent(final CharacterSpriteData characterSpriteData,
											   final CharacterSoundData characterSoundData,
											   final CharacterSkillsParameters skills) {
		if (engine == null) throw new RuntimeException(MSG_FAIL_CALL_BEGIN_BUILDING_ENTITY_FIRST);
		CharacterComponent charComponent = engine.createComponent(CharacterComponent.class);
		charComponent.init(characterSpriteData, characterSoundData, skills);
		currentEntity.add(charComponent);
		return instance;
	}

	public EntityBuilder addCharacterDecalComponent(final CharacterAnimations animations,
													final SpriteType spriteType,
													final Direction direction,
													final Vector3 position) {
		if (engine == null) throw new RuntimeException(MSG_FAIL_CALL_BEGIN_BUILDING_ENTITY_FIRST);
		CharacterDecalComponent characterDecalComponent = engine.createComponent(CharacterDecalComponent.class);
		characterDecalComponent.init(animations, spriteType, direction, position);
		currentEntity.add(characterDecalComponent);
		return instance;
	}

	public EntityBuilder addSimpleDecalComponent(final Vector3 position, final Texture texture, final boolean visible) {
		return addSimpleDecalComponent(position, texture, visible, false);
	}

	public EntityBuilder addSimpleDecalComponent(final Vector3 position,
												 final Texture texture,
												 final boolean visible,
												 final boolean billboard) {
		if (engine == null) throw new RuntimeException(MSG_FAIL_CALL_BEGIN_BUILDING_ENTITY_FIRST);
		SimpleDecalComponent simpleDecalComponent = engine.createComponent(SimpleDecalComponent.class);
		simpleDecalComponent.init(texture, visible, billboard);
		Decal decal = simpleDecalComponent.getDecal();
		decal.setPosition(position);
		decal.setScale(BILLBOARD_SCALE);
		currentEntity.add(simpleDecalComponent);
		return instance;
	}


	public EntityBuilder addSimpleDecalComponent(final Vector3 position,
												 final TextureRegion textureRegion,
												 final boolean billboard,
												 final boolean animatedByAnimationComponent) {
		return addSimpleDecalComponent(position, textureRegion, Vector3.Zero, billboard, animatedByAnimationComponent);
	}

	public EntityBuilder addSimpleDecalComponent(final Vector3 position,
												 final TextureRegion textureRegion,
												 final Vector3 rotationAroundAxis) {
		return addSimpleDecalComponent(
				position,
				textureRegion,
				rotationAroundAxis,
				false,
				true);
	}

	public EntityBuilder addSimpleDecalComponent(final Vector3 position,
												 final TextureRegion textureRegion,
												 final Vector3 rotationAroundAxis,
												 final boolean billboard,
												 final boolean animatedByAnimationComponent) {
		if (engine == null) throw new RuntimeException(MSG_FAIL_CALL_BEGIN_BUILDING_ENTITY_FIRST);
		SimpleDecalComponent simpleDecalComponent = engine.createComponent(SimpleDecalComponent.class);
		simpleDecalComponent.init(textureRegion, true, billboard, animatedByAnimationComponent);
		Decal decal = simpleDecalComponent.getDecal();
		initializeSimpleDecal(position, rotationAroundAxis, decal);
		currentEntity.add(simpleDecalComponent);
		return instance;
	}

	private void initializeSimpleDecal(final Vector3 position, final Vector3 rotationAroundAxis, final Decal decal) {
		decal.setPosition(position);
		decal.setScale(BILLBOARD_SCALE);
		rotateSimpleDecal(decal, rotationAroundAxis);
	}

	private void rotateSimpleDecal(final Decal decal, final Vector3 rotationAroundAxis) {
		if (!rotationAroundAxis.isZero()) {
			decal.setRotation(rotationAroundAxis.y, rotationAroundAxis.x, rotationAroundAxis.z);
		}
	}

	public EntityBuilder addEnemyComponent(final Enemies enemyDefinition,
										   final int skill,
										   final Animation<TextureAtlas.AtlasRegion> bulletRegions) {
		if (engine == null) throw new RuntimeException(MSG_FAIL_CALL_BEGIN_BUILDING_ENTITY_FIRST);
		EnemyComponent component = engine.createComponent(EnemyComponent.class);
		component.init(enemyDefinition, skill, bulletRegions);
		currentEntity.add(component);
		return instance;
	}

	public EntityBuilder addAnimationComponent( ) {
		if (engine == null) throw new RuntimeException(MSG_FAIL_CALL_BEGIN_BUILDING_ENTITY_FIRST);
		AnimationComponent animComponent = engine.createComponent(AnimationComponent.class);
		currentEntity.add(animComponent);
		return instance;
	}

	public EntityBuilder addAnimationComponent(final float frameDuration,
											   final Animation<TextureAtlas.AtlasRegion> animation) {
		if (engine == null) throw new RuntimeException(MSG_FAIL_CALL_BEGIN_BUILDING_ENTITY_FIRST);
		AnimationComponent animComponent = engine.createComponent(AnimationComponent.class);
		currentEntity.add(animComponent);
		Optional.ofNullable(animation).ifPresent(a -> animComponent.init(frameDuration, animation));
		return instance;
	}

	public EntityBuilder addFloorComponent( ) {
		if (engine == null) throw new RuntimeException(MSG_FAIL_CALL_BEGIN_BUILDING_ENTITY_FIRST);
		FloorComponent floorComponent = engine.createComponent(FloorComponent.class);
		currentEntity.add(floorComponent);
		return instance;
	}

	public EntityBuilder addObstacleWallComponent(final Vector2 topLeft,
												  final Vector2 bottomRight,
												  final EnvironmentDefinitions type) {
		if (engine == null) throw new RuntimeException(MSG_FAIL_CALL_BEGIN_BUILDING_ENTITY_FIRST);
		ObstacleComponent obstacleComponent = engine.createComponent(ObstacleComponent.class);
		obstacleComponent.init(topLeft, bottomRight, type);
		currentEntity.add(obstacleComponent);
		return instance;
	}

	public EntityBuilder addPickUpComponentAsWeapon(final WeaponsDefinitions definition,
													final Texture displayImage,
													final TextureAtlas.AtlasRegion bulletRegion) {
		if (engine == null) throw new RuntimeException(MSG_FAIL_CALL_BEGIN_BUILDING_ENTITY_FIRST);
		Weapon weapon = (Weapon) addPickUpComponent(Weapon.class, definition, displayImage);
		weapon.setBulletTextureRegion(bulletRegion);
		return instance;
	}

	private Item addPickUpComponent(final Class<? extends Item> type,
									final ItemDefinition definition,
									final Texture displayImage) {
		if (engine == null) throw new RuntimeException(MSG_FAIL_CALL_BEGIN_BUILDING_ENTITY_FIRST);
		Item pickup = Pools.obtain(type);
		pickup.init(definition, 0, 0, displayImage);
		PickUpComponent pickupComponent = engine.createComponent(PickUpComponent.class);
		pickupComponent.setItem(pickup);
		currentEntity.add(pickupComponent);
		return pickup;
	}

	@SuppressWarnings("unused")
	public EntityBuilder addPickUpComponent(final ItemDefinition definition,
											final Texture displayImage) {
		if (engine == null) throw new RuntimeException(MSG_FAIL_CALL_BEGIN_BUILDING_ENTITY_FIRST);
		addPickUpComponent(Item.class, definition, displayImage);
		return instance;
	}

	public EntityBuilder addBulletComponent(final Vector3 initialPosition, final Vector2 direction, final Entity owner) {
		if (engine == null) throw new RuntimeException(MSG_FAIL_CALL_BEGIN_BUILDING_ENTITY_FIRST);
		BulletComponent bulletComponent = engine.createComponent(BulletComponent.class);
		bulletComponent.init(auxVector.set(initialPosition.x, initialPosition.z), direction, owner);
		currentEntity.add(bulletComponent);
		return instance;
	}

	public EntityBuilder addCollisionComponent( ) {
		if (engine == null) throw new RuntimeException(MSG_FAIL_CALL_BEGIN_BUILDING_ENTITY_FIRST);
		CollisionComponent collisionComponent = engine.createComponent(CollisionComponent.class);
		currentEntity.add(collisionComponent);
		return instance;
	}

	public EntityBuilder addLightComponent(final Vector3 position, final float intensity, final float radius) {
		return addLightComponent(position, intensity, radius, false);
	}

	public EntityBuilder addLightComponent(final Vector3 position,
										   final float intensity,
										   final float radius,
										   final Color color) {
		return addLightComponent(position, intensity, radius, false, color);
	}

	public EntityBuilder addLightComponent(final Vector3 position,
										   final float intensity,
										   final float radius,
										   final boolean flicker) {
		return addLightComponent(position, intensity, radius, flicker, Color.WHITE);
	}

	public EntityBuilder addLightComponent(final Vector3 position,
										   final float intensity,
										   final float radius,
										   final boolean flicker,
										   final Color color) {
		if (engine == null) throw new RuntimeException(MSG_FAIL_CALL_BEGIN_BUILDING_ENTITY_FIRST);
		LightComponent lightComponent = engine.createComponent(LightComponent.class);
		lightComponent.init(position, intensity, radius, flicker, currentEntity);
		lightComponent.setColor(color);
		currentEntity.add(lightComponent);
		return instance;
	}
}
