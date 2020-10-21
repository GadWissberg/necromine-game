package com.gadarts.isometric.systems.render;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.utils.Disposable;
import lombok.Getter;

@Getter
public class RenderBatches implements Disposable {
	private static final int DECALS_POOL_SIZE = 200;

	private final ModelBatch modelBatch;
	private final DecalBatch decalBatch;
	private final ShaderProvider shaderProvider;

	public RenderBatches(final Camera camera) {
		shaderProvider = new GameShaderProvider();
		this.modelBatch = new ModelBatch(shaderProvider);
		this.decalBatch = new DecalBatch(DECALS_POOL_SIZE, new CameraGroupStrategy(camera));
	}

	@Override
	public void dispose() {
		modelBatch.dispose();
		decalBatch.dispose();
	}
}