package com.gadarts.isometric.components;

import com.badlogic.ashley.core.ComponentMapper;
import com.gadarts.isometric.components.character.CharacterComponent;
import com.gadarts.isometric.components.decal.CharacterDecalComponent;
import com.gadarts.isometric.components.decal.SimpleDecalComponent;
import com.gadarts.isometric.components.enemy.EnemyComponent;
import com.gadarts.isometric.components.player.PlayerComponent;

/**
 * Easy to access component mappers.
 */
public class ComponentsMapper {
	public static final ComponentMapper<ModelInstanceComponent> modelInstance = ComponentMapper.getFor(ModelInstanceComponent.class);
	public static final ComponentMapper<CursorComponent> cursor = ComponentMapper.getFor(CursorComponent.class);
	public static final ComponentMapper<PickUpComponent> pickup = ComponentMapper.getFor(PickUpComponent.class);
	public static final ComponentMapper<ObstacleComponent> obstacle = ComponentMapper.getFor(ObstacleComponent.class);
	public static final ComponentMapper<WallComponent> wall = ComponentMapper.getFor(WallComponent.class);
	public static final ComponentMapper<EnemyComponent> enemy = ComponentMapper.getFor(EnemyComponent.class);
	public static final ComponentMapper<PlayerComponent> player = ComponentMapper.getFor(PlayerComponent.class);
	public static final ComponentMapper<FloorComponent> floor = ComponentMapper.getFor(FloorComponent.class);
	public static final ComponentMapper<CharacterDecalComponent> characterDecal = ComponentMapper.getFor(CharacterDecalComponent.class);
	public static final ComponentMapper<SimpleDecalComponent> simpleDecal = ComponentMapper.getFor(SimpleDecalComponent.class);
	public static final ComponentMapper<AnimationComponent> animation = ComponentMapper.getFor(AnimationComponent.class);
	public static final ComponentMapper<CharacterComponent> character = ComponentMapper.getFor(CharacterComponent.class);
	public static final ComponentMapper<BulletComponent> bullet = ComponentMapper.getFor(BulletComponent.class);
	public static final ComponentMapper<LightComponent> light = ComponentMapper.getFor(LightComponent.class);
	public static final ComponentMapper<ParticleComponent> particle = ComponentMapper.getFor(ParticleComponent.class);
	public static final ComponentMapper<ParticleEffectParentComponent> particlesParent = ComponentMapper.getFor(ParticleEffectParentComponent.class);
	public static final ComponentMapper<FlowerSkillIconComponent> flowerSkillIcon = ComponentMapper.getFor(FlowerSkillIconComponent.class);
}
