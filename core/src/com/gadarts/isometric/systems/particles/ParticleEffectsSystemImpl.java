package com.gadarts.isometric.systems.particles;

import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;
import com.gadarts.isometric.systems.GameEntitySystem;
import com.gadarts.isometric.systems.camera.CameraSystem;
import com.gadarts.isometric.systems.camera.CameraSystemEventsSubscriber;
import com.gadarts.isometric.systems.render.RenderSystem;
import com.gadarts.isometric.systems.render.RenderSystemEventsSubscriber;
import com.gadarts.isometric.utils.EntityBuilder;
import com.gadarts.necromine.assets.Assets;

/**
 * Handles the particle effects.
 */
public class ParticleEffectsSystemImpl extends GameEntitySystem<ParticleEffectsSystemEventsSubscriber> implements
		ParticleEffectsSystem,
		CameraSystemEventsSubscriber,
		RenderSystemEventsSubscriber {
	private ParticleSystem particleSystem;

	@Override
	public void onRenderSystemReady(RenderSystem renderSystem) {
		RenderSystemEventsSubscriber.super.onRenderSystemReady(renderSystem);
		addSystem(RenderSystem.class, renderSystem);
	}

	@Override
	public void activate() {
		particleSystem = new ParticleSystem();
		particleSystem.add(getSystem(RenderSystem.class).getRenderBatches().getPointSpriteBatch());

		ParticleEffect originalEffect = services.getAssetManager().getParticleEffect(Assets.Particles.BLOOD_SPLATTER);
		EntityBuilder.beginBuildingEntity((PooledEngine) getEngine())
				.addParticleComponent(originalEffect)
				.finishAndAddToEngine();
	}

	@Override
	public void onCameraSystemReady(CameraSystem cameraSystem) {
		CameraSystemEventsSubscriber.super.onCameraSystemReady(cameraSystem);
		addSystem(CameraSystem.class, cameraSystem);
	}

	@Override
	public void update(final float deltaTime) {
		particleSystem.update(); // technically not necessary for rendering
		particleSystem.begin();
		particleSystem.draw();
		particleSystem.end();
	}

	@Override
	public void onBeginRenderingModels(ModelBatch modelBatch) {
		RenderSystemEventsSubscriber.super.onBeginRenderingModels(modelBatch);
		modelBatch.render(particleSystem);
	}

	@Override
	public void dispose() {

	}
}
