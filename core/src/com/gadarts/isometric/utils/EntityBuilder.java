package com.gadarts.isometric.utils;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.isometric.components.*;
import lombok.AccessLevel;
import lombok.Setter;

public final class EntityBuilder {
	public static final float BILLBOARD_SCALE = 0.015f;
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

	public EntityBuilder addModelInstanceComponent(final ModelInstance modelInstance) {
		ModelInstanceComponent component = engine.createComponent(ModelInstanceComponent.class);
		component.init(modelInstance);
		currentEntity.add(component);
		return instance;
	}

	public Entity finishAndAddToEngine() {
		engine.addEntity(currentEntity);
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

	public EntityBuilder addDecalComponent(final CharacterAnimations animations,
										   final SpriteType spriteType,
										   final CharacterComponent.Direction direction,
										   final Vector3 position) {
		DecalComponent decalComponent = engine.createComponent(DecalComponent.class);
		decalComponent.init(animations, spriteType, direction);
		Decal decal = decalComponent.getDecal();
		decal.setPosition(position);
		decal.setScale(BILLBOARD_SCALE);
		currentEntity.add(decalComponent);
		return instance;
	}

	public EntityBuilder addEnemyComponent() {
		currentEntity.add(engine.createComponent(EnemyComponent.class));
		return instance;
	}

	public EntityBuilder addAnimationComponent() {
		AnimationComponent animComponent = engine.createComponent(AnimationComponent.class);
		currentEntity.add(animComponent);
		return instance;
	}
}