package com.gadarts.isometric.systems.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.utils.Disposable;
import com.gadarts.isometric.systems.camera.CameraSystemImpl;
import lombok.Getter;

public class WorldEnvironment extends Environment implements Disposable {
	private static final Color ambientLightColor = new Color(0.1f, 0.1f, 0.1f, 1);

	@Getter
	private DirectionalShadowLight shadowLight;

	public void initializeModelShadowLight() {

		shadowLight = new DirectionalShadowLight(
				1024,
				1024,
				CameraSystemImpl.VIEWPORT_WIDTH,
				CameraSystemImpl.VIEWPORT_HEIGHT,
				1f,
				300
		);
		shadowLight.set(0.1f, 0.1f, 0.1f, 0, -1f, -1f);
		add(shadowLight);
		shadowMap = shadowLight;
	}

	public void initialize() {
		set(new ColorAttribute(ColorAttribute.AmbientLight, ambientLightColor));
		initializeModelShadowLight();
	}

	@Override
	public void dispose() {
		shadowLight.dispose();
	}
}
