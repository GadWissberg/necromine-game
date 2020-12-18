package com.gadarts.isometric.systems.render.shaders.main;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.LightComponent;

import java.util.List;

public class MainShader extends DefaultShader {
	public static final String UNIFORM_LIGHTS_POSITIONS = "u_lights_positions[0]";
	public static final String UNIFORM_LIGHTS_EXTRA_DATA = "u_lights_extra_data[0]";
	public static final String UNIFORM_NUMBER_OF_LIGHTS = "u_number_of_lights";
	public static final int MAX_LIGHTS = 8;
	public static final int LIGHT_EXTRA_DATA_SIZE = 2;
	private static final Vector3 auxVector = new Vector3();
	private final float[] lightsPositions = new float[MAX_LIGHTS * 3];
	private final float[] lightsExtraData = new float[MAX_LIGHTS * LIGHT_EXTRA_DATA_SIZE];
	private int lightsPositionsLocation;
	private int lightsExtraDataLocation;
	private int numberOfLightsLocation;

	public MainShader(final Renderable renderable, final Config shaderConfig) {
		super(renderable, shaderConfig);
	}

	@Override
	public void init() {
		super.init();
		lightsPositionsLocation = program.getUniformLocation(UNIFORM_LIGHTS_POSITIONS);
		lightsExtraDataLocation = program.getUniformLocation(UNIFORM_LIGHTS_EXTRA_DATA);
		numberOfLightsLocation = program.getUniformLocation(UNIFORM_NUMBER_OF_LIGHTS);
		if (program.getLog().length() != 0) {
			System.out.println(program.getLog());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void render(final Renderable renderable) {
		super.render(renderable);
		if (renderable.userData != null) {
			List<Entity> nearbyLights = (List<Entity>) renderable.userData;
			int size = nearbyLights.size();
			program.setUniformi(numberOfLightsLocation, size);
			for (int i = 0; i < size; i++) {
				insertToLightsArray(nearbyLights, i);
			}
			program.setUniform3fv(lightsPositionsLocation, lightsPositions, 0, size * 3);
			program.setUniform2fv(lightsExtraDataLocation, lightsExtraData, 0, size * LIGHT_EXTRA_DATA_SIZE);
		} else {
			program.setUniformi(numberOfLightsLocation, -1);
		}
	}

	private void insertToLightsArray(final List<Entity> nearbyLights, final int i) {
		insertLightPositionToArray(nearbyLights, i);
		LightComponent lightComponent = ComponentsMapper.light.get(nearbyLights.get(i));
		int extraDataIndex = i * LIGHT_EXTRA_DATA_SIZE;
		lightsExtraData[extraDataIndex] = lightComponent.getIntensity();
		lightsExtraData[extraDataIndex + 1] = lightComponent.getRadius();
	}

	private void insertLightPositionToArray(final List<Entity> nearbyLights, final int i) {
		LightComponent lightComponent = ComponentsMapper.light.get(nearbyLights.get(i));
		Vector3 position = lightComponent.getPosition(auxVector);
		int positionIndex = i * 3;
		lightsPositions[positionIndex] = position.x;
		lightsPositions[positionIndex + 1] = position.y;
		lightsPositions[positionIndex + 2] = position.z;
		lightsPositions[positionIndex + 3] = lightComponent.getIntensity();
	}

}
