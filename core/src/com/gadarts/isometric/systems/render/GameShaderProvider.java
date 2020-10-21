package com.gadarts.isometric.systems.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;

public class GameShaderProvider extends DefaultShaderProvider {
	private final DefaultShader.Config mainShaderConfig;

	public GameShaderProvider() {
		mainShaderConfig = new DefaultShader.Config();
		mainShaderConfig.vertexShader = Gdx.files.internal("shaders/vertex.glsl").readString();
		mainShaderConfig.fragmentShader = Gdx.files.internal("shaders/fragment.glsl").readString();
	}

	@Override
	protected Shader createShader(final Renderable renderable) {
		return new GameShader(renderable, mainShaderConfig);
	}
}
