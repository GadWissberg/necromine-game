package com.gadarts.isometric.utils;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pools;
import com.gadarts.isometric.components.*;
import com.gadarts.isometric.components.character.CharacterAnimations;
import com.gadarts.isometric.components.character.CharacterComponent;
import com.gadarts.isometric.components.character.CharacterSpriteData;
import com.gadarts.isometric.components.character.SpriteType;
import com.gadarts.isometric.components.model.GameModelInstance;
import com.gadarts.isometric.components.player.*;
import com.gadarts.isometric.utils.assets.Assets;
import lombok.AccessLevel;
import lombok.Setter;

public final class EntityBuilder {
	private static final EntityBuilder instance = new EntityBuilder();
	private final static Vector2 auxVector = new Vector2();
	@Setter(AccessLevel.PRIVATE)
	private PooledEngine engine;
	private Entity currentEntity;

	private EntityBuilder() {
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
		ModelInstanceComponent component = engine.createComponent(ModelInstanceComponent.class);
		component.init(modelInstance, visible);
		currentEntity.add(component);
		return instance;
	}

	public Entity finishAndAddToEngine() {
		engine.addEntity(currentEntity);
		return finish();
	}

	public Entity finish() {
		Entity result = currentEntity;
		instance.reset();
		return result;
	}

	private void reset() {
		engine = null;
		currentEntity = null;
	}

	public EntityBuilder addCursorComponent() {
		CursorComponent cursorComponent = engine.createComponent(CursorComponent.class);
		currentEntity.add(cursorComponent);
		return instance;
	}

	public EntityBuilder addPlayerComponent(final Weapon selectedWeapon, final CharacterAnimations general) {
		PlayerComponent playerComponent = engine.createComponent(PlayerComponent.class);
		playerComponent.init(selectedWeapon, general);
		currentEntity.add(playerComponent);
		return instance;
	}

	public EntityBuilder addCharacterComponent(final CharacterSpriteData characterSpriteData,
											   final Entity target,
											   final Assets.Sounds painSound,
											   final Assets.Sounds deathSound) {
		CharacterComponent charComponent = engine.createComponent(CharacterComponent.class);
		charComponent.init(characterSpriteData, painSound, deathSound);
		charComponent.setTarget(target);
		currentEntity.add(charComponent);
		return instance;
	}

	public EntityBuilder addCharacterDecalComponent(final CharacterAnimations animations,
													final SpriteType spriteType,
													final CharacterComponent.Direction direction,
													final Vector3 position) {
		CharacterDecalComponent characterDecalComponent = engine.createComponent(CharacterDecalComponent.class);
		characterDecalComponent.init(animations, spriteType, direction, position);
		currentEntity.add(characterDecalComponent);
		return instance;
	}

	public EntityBuilder addSimpleDecalComponent(final Vector3 position, final Texture texture, final boolean visible) {
		SimpleDecalComponent simpleDecalComponent = engine.createComponent(SimpleDecalComponent.class);
		simpleDecalComponent.init(texture, visible);
		Decal decal = simpleDecalComponent.getDecal();
		decal.setPosition(position);
		decal.setScale(CharacterDecalComponent.BILLBOARD_SCALE);
		currentEntity.add(simpleDecalComponent);
		return instance;
	}

	public EntityBuilder addSimpleDecalComponent(final Vector3 position,
												 final TextureRegion textureRegion,
												 final Vector3 rotationAroundAxis) {
		SimpleDecalComponent simpleDecalComponent = engine.createComponent(SimpleDecalComponent.class);
		simpleDecalComponent.init(textureRegion, true);
		Decal decal = simpleDecalComponent.getDecal();
		decal.setPosition(position);
		decal.setScale(CharacterDecalComponent.BILLBOARD_SCALE);
		rotateSimpleDecal(decal, rotationAroundAxis);
		currentEntity.add(simpleDecalComponent);
		return instance;
	}

	private void rotateSimpleDecal(final Decal decal, final Vector3 rotationAroundAxis) {
		if (!rotationAroundAxis.isZero()) {
			decal.setRotation(rotationAroundAxis.y, rotationAroundAxis.x, rotationAroundAxis.z);
		}
	}

	public EntityBuilder addEnemyComponent(final Assets.Sounds attackSound) {
		EnemyComponent component = engine.createComponent(EnemyComponent.class);
		component.init(attackSound);
		currentEntity.add(component);
		return instance;
	}

	public EntityBuilder addAnimationComponent() {
		AnimationComponent animComponent = engine.createComponent(AnimationComponent.class);
		currentEntity.add(animComponent);
		return instance;
	}

	public EntityBuilder addFloorComponent() {
		FloorComponent floorComponent = engine.createComponent(FloorComponent.class);
		currentEntity.add(floorComponent);
		return instance;
	}

	public EntityBuilder addWallComponent(final int topLeftX,
										  final int topLeftY,
										  final int bottomRightX,
										  final int bottomRightY) {
		WallComponent wallComponent = engine.createComponent(WallComponent.class);
		wallComponent.init(topLeftX, topLeftY, bottomRightX, bottomRightY);
		currentEntity.add(wallComponent);
		return instance;
	}

	public EntityBuilder addObstacleComponent(final int x, final int y, final boolean blockPath) {
		ObstacleComponent obstacleComponent = engine.createComponent(ObstacleComponent.class);
		obstacleComponent.init(x, y, blockPath);
		currentEntity.add(obstacleComponent);
		return instance;
	}

	public EntityBuilder addPickUpComponentAsWeapon(final WeaponsDefinitions definition,
													final Texture displayImage,
													final TextureAtlas.AtlasRegion bulletRegion) {
		Weapon weapon = (Weapon) addPickUpComponent(Weapon.class, definition, displayImage);
		weapon.setBulletTextureRegion(bulletRegion);
		return instance;
	}

	private Item addPickUpComponent(final Class<? extends Item> type,
									final ItemDefinition definition,
									final Texture displayImage) {
		Item pickup = Pools.obtain(type);
		pickup.init(definition, 0, 0, displayImage);
		PickUpComponent pickupComponent = engine.createComponent(PickUpComponent.class);
		pickupComponent.setItem(pickup);
		currentEntity.add(pickupComponent);
		return pickup;
	}

	public EntityBuilder addPickUpComponent(final ItemDefinition definition,
											final Texture displayImage) {
		addPickUpComponent(Item.class, definition, displayImage);
		return instance;
	}

	public EntityBuilder addBulletComponent(final Vector3 initialPosition, final Vector2 direction, final Entity owner) {
		BulletComponent bulletComponent = engine.createComponent(BulletComponent.class);
		bulletComponent.init(auxVector.set(initialPosition.x, initialPosition.z), direction, owner);
		currentEntity.add(bulletComponent);
		return instance;
	}

	public EntityBuilder addCollisionComponent() {
		CollisionComponent collisionComponent = engine.createComponent(CollisionComponent.class);
		currentEntity.add(collisionComponent);
		return instance;
	}

	public EntityBuilder addLightComponent(final float x, final float y, final float z, final float radius) {
		LightComponent lightComponent = engine.createComponent(LightComponent.class);
		lightComponent.init(x, y, z, radius);
		currentEntity.add(lightComponent);
		return instance;
	}
}
