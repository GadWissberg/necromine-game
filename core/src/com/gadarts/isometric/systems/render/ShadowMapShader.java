package com.gadarts.isometric.systems.render;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.ShadowLightComponent;

public class ShadowMapShader extends BaseShader {
	private static final Vector3 auxVector = new Vector3();
	private final ImmutableArray<Entity> lights;
	public Renderable renderable;

	public ShadowMapShader(final Renderable renderable,
						   final ShaderProgram shaderProgramModelBorder,
						   final ImmutableArray<Entity> lights) {
		this.lights = lights;
		this.renderable = renderable;
		this.program = shaderProgramModelBorder;
		register(DefaultShader.Inputs.worldTrans, DefaultShader.Setters.worldTrans);
		register(DefaultShader.Inputs.projViewTrans, DefaultShader.Setters.projViewTrans);
		register(DefaultShader.Inputs.normalMatrix, DefaultShader.Setters.normalMatrix);

	}

	@Override
	public void end( ) {
		super.end();
	}

	@Override
	public void begin(final Camera camera, final RenderContext context) {
		super.begin(camera, context);
		context.setDepthTest(GL20.GL_LEQUAL);
		context.setCullFace(GL20.GL_BACK);

	}

	@Override
	public void render(final Renderable renderable) {
//		if (!renderable.material.has(BlendingAttribute.Type)) {
//			context.setBlending(false, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
//		} else {
//			context.setBlending(true, GL20.GL_ONE, GL20.GL_ONE);
//		}
		super.render(renderable);
	}

	@Override
	public void init( ) {
		final ShaderProgram program = this.program;
		this.program = null;
		init(program, renderable);
		renderable = null;
	}

	@Override
	public int compareTo(final Shader other) {
		return 0;
	}

	@Override
	public boolean canRender(final Renderable instance) {
		return true;
	}

	@Override
	public void render(final Renderable renderable, final Attributes combinedAttributes) {
		for (int i = 0; i < lights.size(); i++) {
			final int textureNum = 8;
			ShadowLightComponent lightComponent = ComponentsMapper.shadowLight.get(lights.get(i));
			lightComponent.getShadowFrameBuffer().getColorBufferTexture().bind(textureNum);
			program.setUniformf("u_type", 2);
			program.setUniformi("u_depthMapCube", textureNum);
			program.setUniformf("u_cameraFar", RenderSystemImpl.CAMERA_LIGHT_FAR);
			program.setUniformf("u_lightPosition", lightComponent.getPosition(auxVector));
			context.setBlending(true, GL20.GL_ONE, GL20.GL_ONE);
			super.render(renderable, combinedAttributes);
		}
	}

}