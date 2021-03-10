package com.gadarts.isometric.systems.render;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.utils.Disposable;
import com.gadarts.isometric.systems.camera.CameraSystemImpl;
import lombok.Getter;

public class WorldEnvironment extends Environment implements Disposable {
	@Getter
	private final Color ambientColor;

	@Getter
	private LightsRenderer lightsRenderer;

	@Getter
	private DirectionalShadowLight shadowLight;

	public WorldEnvironment(final float ambient) {
		this.ambientColor = new Color(ambient, ambient, ambient, 1);
	}

	public void initializeModelShadowLight() {
		shadowLight = new DirectionalShadowLight(
				1024,
				1024,
				CameraSystemImpl.VIEWPORT_WIDTH * 2f,
				CameraSystemImpl.VIEWPORT_HEIGHT * 2f,
				1f,
				300
		);
		shadowLight.set(0.1f, 0.1f, 0.1f, 0, -1f, -0.5f);
		add(shadowLight);
		shadowMap = shadowLight;
	}

	public void initialize(final ImmutableArray<Entity> lightsEntities) {
		lightsRenderer = new LightsRenderer(lightsEntities);
		DirectionalLight directionalLight = new DirectionalLight();
		directionalLight.direction.set(-0.3f, -0.5f, -1);
		float r = 0.3f;
		directionalLight.color.set(r, r, r, 1f);
		add(directionalLight);
		initializeModelShadowLight();
	}

	@Override
	public void dispose() {
		shadowLight.dispose();
	}
}
