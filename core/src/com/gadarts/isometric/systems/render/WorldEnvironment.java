package com.gadarts.isometric.systems.render;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.utils.Disposable;
import lombok.Getter;

public class WorldEnvironment extends Environment implements Disposable {
	@Getter
	private final Color ambientColor;
	private final Camera camera;

	@Getter
	private LightsHandler lightsHandler;

	@Getter
	private DirectionalShadowLight shadowLight;

	public WorldEnvironment(final float ambient, final Camera camera) {
		this.ambientColor = new Color(ambient, ambient, ambient, 1);
		this.camera = camera;
	}

	public void initializeModelShadowLight() {
		shadowLight = new DirectionalShadowLight(
				1024,
				1024,
				camera.viewportWidth * 2f,
				camera.viewportHeight * 2f,
				1f,
				300
		);
		shadowLight.set(0.1f, 0.1f, 0.1f, 0, -1f, -0.5f);
		add(shadowLight);
		shadowMap = shadowLight;
	}

	public void initialize(final ImmutableArray<Entity> lightsEntities) {
		lightsHandler = new LightsHandler(lightsEntities);
//		DirectionalLight directionalLight = new DirectionalLight();
//		directionalLight.direction.set(-0.3f, -0.5f, -1);
		float r = 0.3f;
//		directionalLight.color.set(r, r, r, 1f);
//		add(directionalLight);
		initializeModelShadowLight();
	}

	@Override
	public void dispose() {
		shadowLight.dispose();
	}
}
