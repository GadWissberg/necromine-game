package com.gadarts.isometric.systems.render;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;

public class GameShader extends DefaultShader {
	private int highlightLocation;

	public GameShader(final Renderable renderable, final Config shaderConfig) {
		super(renderable, shaderConfig);
	}

	@Override
	public void init() {
		super.init();
		highlightLocation = program.getUniformLocation("u_highlight");
		if (Gdx.app.getLogLevel() == Application.LOG_DEBUG && program.getLog().length() != 0) {
			System.out.println(program.getLog());
		}
	}

}
