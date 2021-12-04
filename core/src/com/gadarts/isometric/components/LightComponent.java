package com.gadarts.isometric.components;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.TimeUtils;
import com.gadarts.isometric.systems.render.GameFrameBufferCubeMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import static com.gadarts.isometric.systems.render.RenderSystemImpl.DEPTHMAPIZE;

@Getter
public class LightComponent implements GameComponent {
	public static final float LIGHT_MAX_RADIUS = 7f;
	private static final int DEPTHMAPSIZE = 1024;

	@Getter(AccessLevel.NONE)
	private final Vector3 position = new Vector3();
	private final Color color = new Color(Color.WHITE);
	@Setter
	private float intensity;
	@Setter
	private float radius;
	@Setter
	private long nextFlicker;
	private boolean flicker;


	private float duration;
	private float originalRadius;
	private float originalIntensity;
	private Entity parent;
	private long beginTime;
	private GameFrameBufferCubeMap frameBuffer;
	private PerspectiveCamera cameraLight;

	@Setter
	private Cubemap depthMap;

	@Override
	public void reset( ) {
	}

	public void init(final Vector3 position,
					 final float intensity,
					 final float radius,
					 final boolean flicker,
					 final Entity parent) {
		this.position.set(position);
		this.originalIntensity = intensity;
		this.intensity = intensity;
		this.originalRadius = radius;
		this.radius = radius;
		this.flicker = flicker;
		this.parent = parent;
		color.set(Color.WHITE);
		duration = -1L;
		frameBuffer = new GameFrameBufferCubeMap(Pixmap.Format.RGBA8888, DEPTHMAPSIZE, DEPTHMAPSIZE, true);
		cameraLight = new PerspectiveCamera(90f, DEPTHMAPIZE, DEPTHMAPIZE);
		cameraLight.near = 0.1F;
		cameraLight.far = 100;
		cameraLight.position.set(position);
		cameraLight.rotate(Vector3.Y, 0);
		cameraLight.update();
	}

	public void setPosition(final Vector3 newPosition) {
		position.set(newPosition);
	}

	public Vector3 getPosition(final Vector3 output) {
		return output.set(position);
	}


	public void applyColor(final Color color) {
		color.set(color);
	}

	public void applyDuration(final float inSeconds) {
		if (inSeconds <= 0) return;
		this.duration = inSeconds;
		this.beginTime = TimeUtils.millis();
	}
}
