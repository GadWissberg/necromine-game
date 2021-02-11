package com.gadarts.isometric.systems.render.shaders.main;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.gadarts.isometric.systems.render.DrawFlags;
import com.gadarts.necromine.assets.Assets;
import com.gadarts.necromine.assets.GameAssetsManager;

public class MainShaderProvider extends DefaultShaderProvider {
	private final DefaultShader.Config mainShaderConfig;
	private final int[][] fowMap;
	private final DrawFlags drawFlags;

	public MainShaderProvider(final GameAssetsManager assetsManager, final int[][] fowMap, final DrawFlags drawFlags) {
		this.fowMap = fowMap;
		this.drawFlags = drawFlags;
		mainShaderConfig = new DefaultShader.Config();
		mainShaderConfig.vertexShader = assetsManager.getShader(Assets.Shaders.VERTEX);
		mainShaderConfig.fragmentShader = assetsManager.getShader(Assets.Shaders.FRAGMENT);
	}

	@Override
	protected Shader createShader(final Renderable renderable) {
		return new MainShader(renderable, mainShaderConfig, fowMap, drawFlags);
	}
}
