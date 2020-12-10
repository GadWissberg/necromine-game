package com.gadarts.isometric.systems.render;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.utils.Disposable;
import com.gadarts.isometric.systems.render.shaders.main.MainShaderProvider;
import lombok.Getter;

@Getter
public class RenderBatches implements Disposable {
	private static final int DECALS_POOL_SIZE = 200;

	private final ModelBatch modelBatch;
	private final DecalBatch decalBatch;
	private final MainShaderProvider shaderProvider;
	private final ModelBatch shadowBatch;

	public RenderBatches(final Camera camera) {
		shaderProvider = new MainShaderProvider();
		this.modelBatch = new ModelBatch(shaderProvider);
		this.decalBatch = new DecalBatch(DECALS_POOL_SIZE, new CameraGroupStrategy(camera));
		this.shadowBatch = new ModelBatch(new DepthShaderProvider());
	}

	@Override
	public void dispose() {
		modelBatch.dispose();
		decalBatch.dispose();
	}

}