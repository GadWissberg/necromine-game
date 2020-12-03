package com.gadarts.isometric.systems.render;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.LightComponent;

import java.util.List;

public class GameShader extends DefaultShader {
	public static final String UNIFORM_LIGHTS = "u_lights[0]";
	public static final String UNIFORM_NUMBER_OF_LIGHTS = "u_number_of_lights";
	public static final int MAX_LIGHTS = 8;
	public static final int LIGHT_NUMBER_OF_ATTRIBUTES = 4;
	private static final Vector3 auxVector = new Vector3();
	private final float[] lights = new float[MAX_LIGHTS * LIGHT_NUMBER_OF_ATTRIBUTES];
	private int lightsLocation;
	private int numberOfLightsLocation;

	public GameShader(final Renderable renderable, final Config shaderConfig) {
		super(renderable, shaderConfig);
	}

	@Override
	public void init() {
		super.init();
		lightsLocation = program.getUniformLocation(UNIFORM_LIGHTS);
		numberOfLightsLocation = program.getUniformLocation(UNIFORM_NUMBER_OF_LIGHTS);
		if (program.getLog().length() != 0) {
			System.out.println(program.getLog());
		}
	}

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
			program.setUniform4fv(lightsLocation, lights, 0, size * LIGHT_NUMBER_OF_ATTRIBUTES);
		}
	}

	private void insertToLightsArray(final List<Entity> nearbyLights, final int i) {
		insertLightPositionToArray(nearbyLights, i);
	}

	private void insertLightPositionToArray(final List<Entity> nearbyLights, final int i) {
		LightComponent lightComponent = ComponentsMapper.light.get(nearbyLights.get(i));
		Vector3 position = lightComponent.getPosition(auxVector);
		int positionIndex = i * LIGHT_NUMBER_OF_ATTRIBUTES;
		lights[positionIndex] = position.x;
		lights[positionIndex + 1] = position.y;
		lights[positionIndex + 2] = position.z;
		lights[positionIndex + 3] = lightComponent.getIntensity();
	}

	@Override
	public boolean canRender(final Renderable renderable) {
		return super.canRender(renderable);
	}
}
