package com.gadarts.isometric.systems.render.shaders.main;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.gadarts.isometric.systems.render.DrawFlags;
import com.gadarts.isometric.utils.map.MapGraph;
import com.gadarts.necromine.assets.Assets;
import com.gadarts.necromine.assets.GameAssetsManager;

public class MainShaderProvider extends DefaultShaderProvider {
	private final DefaultShader.Config mainShaderConfig;
	private final MapGraph map;
	private final DrawFlags drawFlags;

	public MainShaderProvider(final GameAssetsManager assetsManager, final MapGraph map, final DrawFlags drawFlags) {
		this.map = map;
		this.drawFlags = drawFlags;
		mainShaderConfig = new DefaultShader.Config();
		mainShaderConfig.vertexShader = assetsManager.getShader(Assets.Shaders.VERTEX);
		mainShaderConfig.fragmentShader = assetsManager.getShader(Assets.Shaders.FRAGMENT);
	}

	@Override
	protected Shader createShader(final Renderable renderable) {
		return new MainShader(renderable, mainShaderConfig, map, drawFlags);
	}
}
