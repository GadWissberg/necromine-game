package com.gadarts.isometric.components;

import com.badlogic.ashley.core.ComponentMapper;

public class ComponentsMapper {
	public static final ComponentMapper<ModelInstanceComponent> modelInstance = ComponentMapper.getFor(ModelInstanceComponent.class);
	public static final ComponentMapper<CursorComponent> cursor = ComponentMapper.getFor(CursorComponent.class);
}
