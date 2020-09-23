package com.gadarts.isometric.components;

import com.badlogic.ashley.core.ComponentMapper;

public class ComponentsMapper {
	public static final ComponentMapper<ModelInstanceComponent> modelInstance = ComponentMapper.getFor(ModelInstanceComponent.class);
	public static final ComponentMapper<CursorComponent> cursor = ComponentMapper.getFor(CursorComponent.class);
	public static final ComponentMapper<DecalComponent> decal = ComponentMapper.getFor(DecalComponent.class);
	public static final ComponentMapper<AnimationComponent> animation = ComponentMapper.getFor(AnimationComponent.class);
	public static final ComponentMapper<CharacterComponent> character = ComponentMapper.getFor(CharacterComponent.class);
}
