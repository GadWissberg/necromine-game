package com.gadarts.isometric.systems.render;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.utils.Disposable;
import com.gadarts.isometric.systems.render.shaders.main.MainShaderProvider;
import com.gadarts.isometric.utils.map.MapGraph;
import com.gadarts.necromine.assets.GameAssetsManager;
import lombok.Getter;

@Getter
public class RenderBatches implements Disposable {
	private static final int DECALS_POOL_SIZE = 200;

	private final ModelBatch modelBatch;
	private final DecalBatch decalBatch;
	private final MainShaderProvider shaderProvider;
	private final ModelBatch shadowBatch;

	public RenderBatches(final Camera camera,
						 final GameAssetsManager assetsManager,
						 final MapGraph mapGraph,
						 final DrawFlags drawFlags) {
		shaderProvider = new MainShaderProvider(assetsManager, mapGraph, drawFlags);
		this.modelBatch = new ModelBatch(shaderProvider);
		GameCameraGroupStrategy groupStrategy = new GameCameraGroupStrategy(camera, assetsManager);
		this.decalBatch = new DecalBatch(DECALS_POOL_SIZE, groupStrategy);
		this.shadowBatch = new ModelBatch(new DepthShaderProvider());
	}

	@Override
	public void dispose() {
		modelBatch.dispose();
		decalBatch.dispose();
	}

}