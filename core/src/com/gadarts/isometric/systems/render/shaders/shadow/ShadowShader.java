package com.gadarts.isometric.systems.render.shaders.shadow;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class ShadowShader extends BaseShader {


	private final Renderable renderable;

	public ShadowShader(final Renderable renderable,
						final ShaderProgram shaderProgramDepthMap) {
		this.renderable = renderable;
		this.program = shaderProgramDepthMap;
		register(DefaultShader.Inputs.worldTrans, DefaultShader.Setters.worldTrans);
		register(DefaultShader.Inputs.projViewTrans, DefaultShader.Setters.projViewTrans);
		register(DefaultShader.Inputs.normalMatrix, DefaultShader.Setters.normalMatrix);
	}

	@Override
	public void begin(final Camera camera, final RenderContext context) {
		super.begin(camera, context);
		context.setDepthTest(GL20.GL_LEQUAL);
		context.setCullFace(GL20.GL_BACK);
	}

	@Override
	public void init() {
		if (program.getLog().length() != 0) {
			Gdx.app.log("shaders", program.getLog());
		}
	}

	@Override
	public int compareTo(final Shader other) {
		return 0;
	}

	@Override
	public void render(final Renderable renderable) {
		if (!renderable.material.has(BlendingAttribute.Type)) {
			context.setBlending(false, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		} else {
			context.setBlending(true, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		}
		super.render(renderable);
	}

	@Override
	public boolean canRender(final Renderable instance) {
		return true;
	}


}
