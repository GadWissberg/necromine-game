package com.gadarts.isometric.systems.render;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.LightComponent;
import com.gadarts.isometric.components.ModelInstanceComponent;
import com.gadarts.isometric.components.model.GameModelInstance;
import com.gadarts.isometric.utils.DefaultGameSettings;

import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class LightsRenderer {
	private static final float DECAL_DARKEST_COLOR = 0.2f;
	private static final float DECAL_LIGHT_OFFSET = 1.5f;
	private static final Vector3 auxVector3_1 = new Vector3();
	private static final Vector3 auxVector3_2 = new Vector3();
	private final ImmutableArray<Entity> lightsEntities;

	public LightsRenderer(final ImmutableArray<Entity> lightsEntities) {
		this.lightsEntities = lightsEntities;
	}

	private float applyLightOnDecal(final Decal decal, float minDistance, final Entity light) {
		float distance = ComponentsMapper.light.get(light).getPosition(auxVector3_1).dst(decal.getPosition());
		float maxLightDistanceForDecal = ComponentsMapper.light.get(light).getRadius() * 2;
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

	void applyLightsOnDecal(final Decal decal) {
		float minDistance = Float.MAX_VALUE;
		for (Entity light : lightsEntities) {
			minDistance = applyLightOnDecal(decal, minDistance, light);
		}
		if (minDistance == Float.MAX_VALUE) {
			decal.setColor(DECAL_DARKEST_COLOR, DECAL_DARKEST_COLOR, DECAL_DARKEST_COLOR, 1f);
		}
	}

	public void applyLightsOnModel(final ModelInstanceComponent mic) {
		mic.getModelInstance().getNearbyLights().clear();
		if (!DefaultGameSettings.DISABLE_LIGHTS) {
			if (mic.isAffectedByLight()) {
				for (Entity light : lightsEntities) {
					addLightIfClose(mic.getModelInstance(), mic.getModelInstance().getNearbyLights(), light);
				}
				mic.getModelInstance().userData = mic.getModelInstance().getNearbyLights();
			} else {
				mic.getModelInstance().userData = null;
			}
		}
	}

	private void addLightIfClose(final GameModelInstance modelInstance,
								 final List<Entity> nearbyLights,
								 final Entity light) {
		LightComponent lightComponent = ComponentsMapper.light.get(light);
		Vector3 lightPosition = lightComponent.getPosition(auxVector3_1);
		float distance = lightPosition.dst(modelInstance.transform.getTranslation(auxVector3_2));
		if (distance <= LightComponent.LIGHT_RADIUS) {
			nearbyLights.add(light);
		}
	}
}
