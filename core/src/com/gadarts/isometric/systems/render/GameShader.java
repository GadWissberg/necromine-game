package com.gadarts.isometric.systems.render;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;

public class GameShader extends DefaultShader {
	public static final String UNIFORM_LIGHTS = "u_lights[0]";
	private final float[] lights = {1.5f, 2f, 2.5f, 2.5f, 2f, 4.5f};
	private int lightsLocation;

	public GameShader(final Renderable renderable, final Config shaderConfig) {
		super(renderable, shaderConfig);
	}

	@Override
	public void init() {
		super.init();
		lightsLocation = program.getUniformLocation(UNIFORM_LIGHTS);
		if (program.getLog().length() != 0)
			System.out.println(program.getLog());
	}

	@Override
	public void render(final Renderable renderable) {
		super.render(renderable);
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
