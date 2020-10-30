package com.gadarts.isometric.utils;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.isometric.components.*;
import com.gadarts.isometric.components.character.CharacterAnimations;
import com.gadarts.isometric.components.character.CharacterComponent;
import com.gadarts.isometric.components.character.SpriteType;
import lombok.AccessLevel;
import lombok.Setter;

public final class EntityBuilder {
	private static final float BILLBOARD_SCALE = 0.015f;
	private static final EntityBuilder instance = new EntityBuilder();

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

	public EntityBuilder addModelInstanceComponent(final ModelInstance modelInstance, final boolean visible) {
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

	public EntityBuilder addPlayerComponent() {
		currentEntity.add(engine.createComponent(PlayerComponent.class));
		return instance;
	}

	public EntityBuilder addCharacterComponent(final CharacterComponent.Direction direction,
											   final SpriteType spriteType,
											   final Entity target) {
		CharacterComponent charComponent = engine.createComponent(CharacterComponent.class);
		charComponent.init(direction, spriteType);
		charComponent.setTarget(target);
		currentEntity.add(charComponent);
		return instance;
	}

	public EntityBuilder addCharacterDecalComponent(final CharacterAnimations animations,
													final SpriteType spriteType,
													final CharacterComponent.Direction direction,
													final Vector3 position) {
		CharacterDecalComponent characterDecalComponent = engine.createComponent(CharacterDecalComponent.class);
		characterDecalComponent.init(animations, spriteType, direction);
		Decal decal = characterDecalComponent.getDecal();
		decal.setPosition(position);
		decal.setScale(BILLBOARD_SCALE);
		currentEntity.add(characterDecalComponent);
		return instance;
	}

	public EntityBuilder addSimpleDecalComponent(final Vector3 position, final Texture texture, final boolean visible) {
		SimpleDecalComponent simpleDecalComponent = engine.createComponent(SimpleDecalComponent.class);
		simpleDecalComponent.init(texture, visible);
		Decal decal = simpleDecalComponent.getDecal();
		decal.setPosition(position);
		decal.setScale(BILLBOARD_SCALE);
		currentEntity.add(simpleDecalComponent);
		return instance;
	}

	public EntityBuilder addEnemyComponent() {
		EnemyComponent component = engine.createComponent(EnemyComponent.class);
		component.init();
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

	public EntityBuilder addObstacleComponent(final int x, final int y) {
		ObstacleComponent obstacleComponent = engine.createComponent(ObstacleComponent.class);
		obstacleComponent.init(x, y);
		currentEntity.add(obstacleComponent);
		return instance;
	}

	public EntityBuilder addPickUpComponent() {
		PickUpComponent pickupComponent = engine.createComponent(PickUpComponent.class);
		currentEntity.add(pickupComponent);
		return instance;
	}
}
