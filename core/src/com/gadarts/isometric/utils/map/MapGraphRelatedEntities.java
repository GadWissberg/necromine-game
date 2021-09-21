package com.gadarts.isometric.utils.map;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.utils.ImmutableArray;
import com.gadarts.isometric.components.ObstacleComponent;
import com.gadarts.isometric.components.PickUpComponent;
import com.gadarts.isometric.components.character.CharacterComponent;
import com.gadarts.isometric.components.enemy.EnemyComponent;
import lombok.Getter;

@Getter
public class MapGraphRelatedEntities {
	private final ImmutableArray<Entity> characterEntities;
	private final ImmutableArray<Entity> pickupEntities;
	private final ImmutableArray<Entity> enemiesEntities;
	private final ImmutableArray<Entity> obstaclesEntities;

	public MapGraphRelatedEntities(PooledEngine engine) {
		this.pickupEntities = engine.getEntitiesFor(Family.all(PickUpComponent.class).get());
		this.characterEntities = engine.getEntitiesFor(Family.all(CharacterComponent.class).get());
		this.enemiesEntities = engine.getEntitiesFor(Family.all(EnemyComponent.class).get());
		this.obstaclesEntities = engine.getEntitiesFor(Family.all(ObstacleComponent.class).get());
	}
}
