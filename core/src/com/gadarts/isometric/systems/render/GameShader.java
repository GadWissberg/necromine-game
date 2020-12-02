package com.gadarts.isometric.systems.render;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.isometric.components.ComponentsMapper;

import java.util.List;

public class GameShader extends DefaultShader {
    public static final String UNIFORM_LIGHTS = "u_lights[0]";
    public static final String UNIFORM_NUMBER_OF_LIGHTS = "u_number_of_lights";
    private final static Vector3 auxVector = new Vector3();
    private final float[] lights = new float[8];
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
            Gdx.app.log("GLSL", program.getLog());
        }
    }

    @Override
    public void render(final Renderable renderable) {
        super.render(renderable);
        if (renderable.userData != null) {
            List<Entity> nearbyLights = (List<Entity>) renderable.userData;
            int size = nearbyLights.size();
            program.setUniformf(numberOfLightsLocation, size);
            for (int i = 0; i < size; i++) {
                insertToLightsArray(nearbyLights, i);
            }
            program.setUniform3fv(lightsLocation, lights, 0, size * 3);
        }
    }

    private void insertToLightsArray(List<Entity> nearbyLights, int i) {
        Vector3 position = ComponentsMapper.light.get(nearbyLights.get(i)).getPosition(auxVector);
        lights[i * 3] = position.x;
        lights[i * 3 + 1] = position.y;
        lights[i * 3 + 2] = position.z;
    }

    @Override
    public void begin(final Camera camera, final RenderContext context) {
        super.begin(camera, context);
        program.setUniform3fv(lightsLocation, lights, 0, lights.length);
    }

    @Override
    public boolean canRender(final Renderable renderable) {
        return super.canRender(renderable);
    }
}
