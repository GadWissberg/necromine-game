package com.gadarts.isometric.systems.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;
import com.gadarts.isometric.systems.render.shaders.main.MainShaderProvider;
import com.gadarts.isometric.systems.render.shaders.shadow.ShadowShader;
import lombok.Getter;

@Getter
public class RenderBatches implements Disposable {
	private static final int DECALS_POOL_SIZE = 200;

	private final ModelBatch modelBatch;
	private final DecalBatch decalBatch;
	private final MainShaderProvider shaderProvider;
	private ModelBatch modelBatchDepthMap;
	private ShaderProgram shaderProgramDepthMap;

	public RenderBatches(final Camera camera) {
		shaderProvider = new MainShaderProvider();
		this.modelBatch = new ModelBatch(shaderProvider);
		this.decalBatch = new DecalBatch(DECALS_POOL_SIZE, new CameraGroupStrategy(camera));
	}

	public ShaderProgram setupShader() {
		ShaderProgram.pedantic = false;
		final ShaderProgram shaderProgram = new ShaderProgram(
				Gdx.files.internal("shaders/shadow_vertex.glsl").readString(),
				Gdx.files.internal("shaders/shadow_fragment.glsl").readString()
		);
		if (shaderProgram.getLog().length() != 0) {
			Gdx.app.log("shaders", shaderProgram.getLog());
		}
		return shaderProgram;
	}

	@Override
	public void dispose() {
		modelBatch.dispose();
		decalBatch.dispose();
	}

	public void initializeModelBatchDepthMap() {
		shaderProgramDepthMap = setupShader();
		modelBatchDepthMap = new ModelBatch(new DefaultShaderProvider() {
			@Override
			protected Shader createShader(final Renderable renderable) {
				return new ShadowShader(renderable, shaderProgramDepthMap);
			}
		});
	}
}