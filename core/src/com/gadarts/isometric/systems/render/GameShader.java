package com.gadarts.isometric.systems.render;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;

public class GameShader extends DefaultShader {
	public static final String UNIFORM_TEST = "u_test_light";
	private int testLocation;

	public GameShader(final Renderable renderable, final Config shaderConfig) {
		super(renderable, shaderConfig);
	}

	@Override
	public void init() {
		super.init();
		testLocation = program.getUniformLocation(UNIFORM_TEST);
		if (program.getLog().length() != 0)
			System.out.println(program.getLog());
	}

	@Override
	public void begin(final Camera camera, final RenderContext context) {
		super.begin(camera, context);
		program.setUniformf(testLocation, 1f, 2f, 2f);
	}

	@Override
	public boolean canRender(final Renderable renderable) {
		return super.canRender(renderable);
	}
}
