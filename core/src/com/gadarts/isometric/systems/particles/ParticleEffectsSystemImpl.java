package com.gadarts.isometric.systems.particles;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;
import com.badlogic.gdx.graphics.g3d.particles.batches.PointSpriteParticleBatch;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.ParticleComponent;
import com.gadarts.isometric.systems.GameEntitySystem;
import com.gadarts.isometric.systems.camera.CameraSystem;
import com.gadarts.isometric.systems.camera.CameraSystemEventsSubscriber;
import com.gadarts.isometric.systems.render.RenderSystem;
import com.gadarts.isometric.systems.render.RenderSystemEventsSubscriber;
import com.gadarts.necromine.assets.Assets;

import java.util.ArrayList;

/**
 * Handles the particle effects.
 */
public class ParticleEffectsSystemImpl extends GameEntitySystem<ParticleEffectsSystemEventsSubscriber> implements
		ParticleEffectsSystem,
		CameraSystemEventsSubscriber,
		RenderSystemEventsSubscriber {

	private final ArrayList<Entity> auxParticleEntitiesList = new ArrayList<>();
	private ImmutableArray<Entity> particleEntities;

	@Override
	public void onRenderSystemReady(final RenderSystem renderSystem) {
		RenderSystemEventsSubscriber.super.onRenderSystemReady(renderSystem);
		addSystem(RenderSystem.class, renderSystem);
	}

	@Override
	public void activate( ) {
		PointSpriteParticleBatch pointSpriteBatch = Assets.Particles.getPointSpriteParticleBatch();
		ParticleSystem particleSystem = Assets.Particles.getParticleSystem();
		particleSystem.add(pointSpriteBatch);
		particleEntities = getEngine().getEntitiesFor(Family.all(ParticleComponent.class).get());
	}

	@Override
	public void onCameraSystemReady(final CameraSystem cameraSystem) {
		CameraSystemEventsSubscriber.super.onCameraSystemReady(cameraSystem);
		addSystem(CameraSystem.class, cameraSystem);
	}

	@Override
	public void update(final float deltaTime) {
		ParticleSystem particleSystem = Assets.Particles.getParticleSystem();
		particleSystem.update(deltaTime);
		particleSystem.begin();
		particleSystem.draw();
		particleSystem.end();
		handleCompletedParticleEffects(particleSystem);
	}

	private void handleCompletedParticleEffects(final ParticleSystem particleSystem) {
		auxParticleEntitiesList.clear();
		for (Entity entity : particleEntities) {
			if (ComponentsMapper.particle.get(entity).getParticleEffect().isComplete()) {
				particleSystem.remove(ComponentsMapper.particle.get(entity).getParticleEffect());
				auxParticleEntitiesList.add(entity);
			}
		}
		for (Entity entity : auxParticleEntitiesList) {
			entity.remove(ParticleComponent.class);
			getEngine().removeEntity(entity);
		}
	}

	@Override
	public void onBeginRenderingParticleEffects(final ModelBatch modelBatch) {
		RenderSystemEventsSubscriber.super.onBeginRenderingParticleEffects(modelBatch);
		modelBatch.render(Assets.Particles.getParticleSystem());
	}

	@Override
	public void dispose( ) {

	}
}
