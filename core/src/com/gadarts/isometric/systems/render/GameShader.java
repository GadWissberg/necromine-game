package com.gadarts.isometric.systems.render;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.LightComponent;

import java.util.List;

public class GameShader extends DefaultShader {
    public static final String UNIFORM_LIGHTS = "u_lights[0]";
    public static final String UNIFORM_LIGHTS_DATA = "u_lights_data[0]";
    public static final String UNIFORM_NUMBER_OF_LIGHTS = "u_number_of_lights";
    public static final int MAX_LIGHTS = 8;
    private final static Vector3 auxVector = new Vector3();
    private static final int LIGHT_NUMBER_OF_EXTRA_ATTRIBUTES = 2;
    private final float[] lightsPositions = new float[MAX_LIGHTS * 3];
    private final float[] lightsExtraData = new float[MAX_LIGHTS * LIGHT_NUMBER_OF_EXTRA_ATTRIBUTES];
    private int lightsLocation;
    private int lightsExtraDataLocation;
    private int numberOfLightsLocation;

    public GameShader(final Renderable renderable, final Config shaderConfig) {
        super(renderable, shaderConfig);
    }

    @Override
    public void init() {
        super.init();
        lightsLocation = program.getUniformLocation(UNIFORM_LIGHTS);
        lightsExtraDataLocation = program.getUniformLocation(UNIFORM_LIGHTS_DATA);
        numberOfLightsLocation = program.getUniformLocation(UNIFORM_NUMBER_OF_LIGHTS);
        if (program.getLog().length() != 0) {
            Gdx.app.log("GLSL", program.getLog());
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
            program.setUniform3fv(lightsLocation, lightsPositions, 0, size * 3);
            program.setUniform2fv(lightsExtraDataLocation, lightsExtraData, 0, size * LIGHT_NUMBER_OF_EXTRA_ATTRIBUTES);
        }
    }

    private void insertToLightsArray(final List<Entity> nearbyLights, final int i) {
        insertLightPositionToArray(nearbyLights, i);
        LightComponent lightComponent = ComponentsMapper.light.get(nearbyLights.get(i));
        int extraDataIndex = i * 3;
        lightsExtraData[extraDataIndex] = lightComponent.getRadius();
        lightsPositions[extraDataIndex + 1] = lightComponent.getIntensity();
    }

    private void insertLightPositionToArray(final List<Entity> nearbyLights, final int i) {
        LightComponent lightComponent = ComponentsMapper.light.get(nearbyLights.get(i));
        Vector3 position = lightComponent.getPosition(auxVector);
        int positionIndex = i * 3;
        lightsPositions[positionIndex] = position.x;
        lightsPositions[positionIndex + 1] = position.y;
        lightsPositions[positionIndex + 2] = position.z;
    }

    @Override
    public boolean canRender(final Renderable renderable) {
        return super.canRender(renderable);
    }
}
