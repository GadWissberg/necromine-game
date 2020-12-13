package com.gadarts.isometric.systems.render;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.gadarts.isometric.components.CharacterDecalComponent;
import com.gadarts.isometric.components.LightComponent;
import com.gadarts.isometric.components.ModelInstanceComponent;
import com.gadarts.isometric.components.SimpleDecalComponent;
import lombok.Getter;

@Getter
public class RenderSystemRelatedEntities {
	private final ImmutableArray<Entity> modelInstanceEntities;
	private final ImmutableArray<Entity> characterDecalsEntities;
	private final ImmutableArray<Entity> simpleDecalsEntities;
	private final ImmutableArray<Entity> lightsEntities;

	public RenderSystemRelatedEntities(final Engine engine) {
		modelInstanceEntities = engine.getEntitiesFor(Family.all(ModelInstanceComponent.class).get());
		characterDecalsEntities = engine.getEntitiesFor(Family.all(CharacterDecalComponent.class).get());
		simpleDecalsEntities = engine.getEntitiesFor(Family.all(SimpleDecalComponent.class).get());
		lightsEntities = engine.getEntitiesFor(Family.all(LightComponent.class).get());
	}
}
