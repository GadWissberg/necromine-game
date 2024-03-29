package com.gadarts.isometric.systems.render;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.TimeUtils;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.LightComponent;
import com.gadarts.isometric.components.ModelInstanceComponent;
import com.gadarts.isometric.components.character.data.CharacterSpriteData;
import com.gadarts.isometric.components.model.GameModelInstance;
import com.gadarts.isometric.utils.DefaultGameSettings;

import java.util.List;

import static com.gadarts.necromine.model.characters.SpriteType.ATTACK;
import static com.gadarts.necromine.model.characters.SpriteType.ATTACK_PRIMARY;
import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Responsible to gather nearby lights to entity and apply them on it.
 */
public class LightsHandler {
	public static final float FLICKER_RANDOM_MIN = 0.95F;
	public static final float FLICKER_RANDOM_MAX = 1.05F;

	private static final float DECAL_DARKEST_COLOR = 0.2f;
	private static final float DECAL_LIGHT_OFFSET = 1.5f;
	private static final Vector3 auxVector3_1 = new Vector3();
	private static final Vector3 auxVector3_2 = new Vector3();
	private static final int FLICKER_MAX_INTERVAL = 150;

	private final ImmutableArray<Entity> lightsEntities;

	public LightsHandler(final ImmutableArray<Entity> lightsEntities) {
		this.lightsEntities = lightsEntities;
	}

	private float applyLightOnDecal(final Decal decal, float minDistance, final Entity light) {
		float distance = ComponentsMapper.light.get(light).getPosition(auxVector3_1).dst(decal.getPosition());
		float maxLightDistanceForDecal = ComponentsMapper.light.get(light).getRadius();
		if (distance <= maxLightDistanceForDecal) {
			minDistance = calculateDecalColorAffectedByLight(decal, minDistance, distance, maxLightDistanceForDecal);
		}
		return minDistance;
	}

	private float calculateDecalColorAffectedByLight(final Decal d,
													 float minDistance,
													 final float distance,
													 final float maxLightDistanceForDecal) {
		float newC = convertDistanceToColorValueForDecal(maxLightDistanceForDecal, distance);
		Color c = d.getColor();
		if (minDistance == Float.MAX_VALUE) {
			d.setColor(min(newC, 1f), min(newC, 1f), min(newC, 1f), 1f);
		} else {
			d.setColor(min(max(c.r, newC), 1f), min(max(c.g, newC), 1f), min(max(c.b, newC), 1f), 1f);
		}
		minDistance = min(minDistance, distance);
		return minDistance;
	}

	private float convertDistanceToColorValueForDecal(final float maxLightDistanceForDecal, final float distance) {
		return MathUtils.map(
				0,
				(maxLightDistanceForDecal - DECAL_LIGHT_OFFSET),
				DECAL_DARKEST_COLOR,
				1f,
				maxLightDistanceForDecal - distance
		);
	}

	void setDecalColorAccordingToLights(final Entity entity, final WorldEnvironment environment) {
		Decal decal = ComponentsMapper.characterDecal.get(entity).getDecal();
		if (shouldApplyLightsOnDecal(entity, ComponentsMapper.character.get(entity).getCharacterSpriteData())) {
			float minDistance = Float.MAX_VALUE;
			minDistance = applyLightsOnDecal(decal, minDistance);
			if (minDistance == Float.MAX_VALUE) {
				decal.setColor(DECAL_DARKEST_COLOR, DECAL_DARKEST_COLOR, DECAL_DARKEST_COLOR, 1f);
			}
			Color color = decal.getColor().add(environment.getAmbientColor());
			decal.setColor(color);
		} else {
			decal.setColor(Color.WHITE);
		}
	}

	private float applyLightsOnDecal(final Decal decal, float minDistance) {
		for (Entity light : lightsEntities) {
			minDistance = applyLightOnDecal(decal, minDistance, light);
		}
		return minDistance;
	}

	private boolean shouldApplyLightsOnDecal(final Entity entity,
											 final CharacterSpriteData spriteData) {
		Decal decal = ComponentsMapper.characterDecal.get(entity).getDecal();
		TextureAtlas.AtlasRegion textureRegion = (TextureAtlas.AtlasRegion) decal.getTextureRegion();
		if (ComponentsMapper.enemy.has(entity)) {
			return spriteData.getSpriteType() != ATTACK_PRIMARY;
		} else {
			return spriteData.getSpriteType() != ATTACK
					|| textureRegion.index != spriteData.getMeleeHitFrameIndex()
					|| ComponentsMapper.player.get(entity).getStorage().getSelectedWeapon().isMelee();
		}
	}

	/**
	 * Finds nearby lights to the given model instance and caches them in its list.
	 *
	 * @param mic The model-instance to apply the lights on.
	 */
	public void applyLightsOnModel(final ModelInstanceComponent mic) {
		List<Entity> nearbyLights = mic.getModelInstance().getAdditionalRenderData().getNearbyLights();
		nearbyLights.clear();
		if (!DefaultGameSettings.DISABLE_LIGHTS) {
			if (mic.getModelInstance().getAdditionalRenderData().isAffectedByLight()) {
				for (Entity light : lightsEntities) {
					addLightIfClose(mic.getModelInstance(), nearbyLights, light);
				}
			}
		}
	}

	private void addLightIfClose(final GameModelInstance modelInstance,
								 final List<Entity> nearbyLights,
								 final Entity light) {
		LightComponent lightComponent = ComponentsMapper.light.get(light);
		Vector3 lightPosition = lightComponent.getPosition(auxVector3_1);
		Vector3 modelPosition = modelInstance.transform.getTranslation(auxVector3_2);
		float distance = lightPosition.dst(modelPosition);
		if (distance <= LightComponent.LIGHT_MAX_RADIUS) {
			nearbyLights.add(light);
		}
	}

	public void updateLights( ) {
		for (Entity light : lightsEntities) {
			updateLight(light);
		}
	}

	private void updateLight(final Entity light) {
		LightComponent lc = ComponentsMapper.light.get(light);
		long now = TimeUtils.millis();
		if (lc.isFlicker() && now >= lc.getNextFlicker()) {
			lc.setIntensity(MathUtils.random(FLICKER_RANDOM_MIN, FLICKER_RANDOM_MAX) * lc.getOriginalIntensity());
			lc.setRadius(MathUtils.random(FLICKER_RANDOM_MIN, FLICKER_RANDOM_MAX) * lc.getOriginalRadius());
			lc.setNextFlicker(now + MathUtils.random(FLICKER_MAX_INTERVAL));
		}
		if (ComponentsMapper.simpleDecal.has(light)) {
			lc.setPosition(ComponentsMapper.simpleDecal.get(lc.getParent()).getDecal().getPosition());
		}
	}
}
