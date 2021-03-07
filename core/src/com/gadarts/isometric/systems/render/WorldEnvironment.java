package com.gadarts.isometric.systems.render;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.utils.Disposable;
import com.gadarts.isometric.systems.camera.CameraSystemImpl;
import lombok.Getter;

public class WorldEnvironment extends Environment implements Disposable {
	public static final float AMBIENT = 0.1f;
	private static final Color ambientLightColor = new Color(AMBIENT, AMBIENT, AMBIENT, 1);

	@Getter
	private LightsRenderer lightsRenderer;

	@Getter
	private DirectionalShadowLight shadowLight;

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
		set(new ColorAttribute(ColorAttribute.AmbientLight, ambientLightColor));
		initializeModelShadowLight();
	}

	@Override
	public void dispose() {
		shadowLight.dispose();
	}
}
