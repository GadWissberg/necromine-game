package com.gadarts.isometric.systems.particles;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;
import com.badlogic.gdx.graphics.g3d.particles.batches.PointSpriteParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.emitters.RegularEmitter;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.ParticleComponent;
import com.gadarts.isometric.systems.GameEntitySystem;
import com.gadarts.isometric.systems.camera.CameraSystem;
import com.gadarts.isometric.systems.camera.CameraSystemEventsSubscriber;
import com.gadarts.isometric.systems.render.RenderSystem;
import com.gadarts.isometric.systems.render.RenderSystemEventsSubscriber;

import java.util.ArrayList;

/**
 * Handles the particle effects.
 */
public class ParticleEffectsSystemImpl extends GameEntitySystem<ParticleEffectsSystemEventsSubscriber> implements
		ParticleEffectsSystem,
		CameraSystemEventsSubscriber,
		RenderSystemEventsSubscriber {

	private final static Matrix4 auxMatrix = new Matrix4();
	private final ArrayList<ParticleEffect> particleEffectsToFollow = new ArrayList<>();
	private final ArrayList<ParticleEffect> particleEffectsToRemove = new ArrayList<>();
	private final ArrayList<Entity> particleEntitiesToRemove = new ArrayList<>();
	private ImmutableArray<Entity> particleEntities;
	private ParticleSystem particleSystem;
	private PointSpriteParticleBatch pointSpriteBatch;

	@Override
	public void onRenderSystemReady(final RenderSystem renderSystem) {
		RenderSystemEventsSubscriber.super.onRenderSystemReady(renderSystem);
		addSystem(RenderSystem.class, renderSystem);
	}

	@Override
	public void activate( ) {
		pointSpriteBatch = new PointSpriteParticleBatch();
		subscribers.forEach(sub -> sub.onParticleEffectsSystemReady(pointSpriteBatch));
		particleSystem = new ParticleSystem();
		services.getAssetManager().loadParticleEffects(pointSpriteBatch);
		particleSystem.add(pointSpriteBatch);
		particleEntities = getEngine().getEntitiesFor(Family.all(ParticleComponent.class).get());
		getEngine().addEntityListener(new EntityListener() {
			@Override
			public void entityAdded(final Entity entity) {

			}

			@Override
			public void entityRemoved(final Entity entity) {
				if (ComponentsMapper.particlesParent.has(entity)) {
					Array<Entity> children = ComponentsMapper.particlesParent.get(entity).getChildren();
					for (Entity child : children) {
						finalizeEffect(child);
					}
				} else if (ComponentsMapper.particle.has(entity)) {
					finalizeEffect(entity);
					particleEffectsToFollow.add(ComponentsMapper.particle.get(entity).getParticleEffect());
				}
			}
		});
	}

	private void finalizeEffect(final Entity effect) {
		for (ParticleController con : ComponentsMapper.particle.get(effect).getParticleEffect().getControllers()) {
			RegularEmitter emitter = (RegularEmitter) con.emitter;
			emitter.setContinuous(false);
		}
	}

	@Override
	public void onCameraSystemReady(final CameraSystem cameraSystem) {
		CameraSystemEventsSubscriber.super.onCameraSystemReady(cameraSystem);
		addSystem(CameraSystem.class, cameraSystem);
	}

	@Override
	public void update(final float deltaTime) {
		updatesEffectsWithParentsAccordingly();
		particleSystem.update(deltaTime);
		particleSystem.begin();
		particleSystem.draw();
		particleSystem.end();
		handleCompletedParticleEffects(particleSystem);
	}

	private void updatesEffectsWithParentsAccordingly( ) {
		for (Entity particleEntity : particleEntities) {
			ParticleComponent particleComponent = ComponentsMapper.particle.get(particleEntity);
			ParticleEffect particleEffect = particleComponent.getParticleEffect();
			Entity parent = particleComponent.getParent();
			if (parent != null && ComponentsMapper.simpleDecal.has(parent)) {
				particleEffect.setTransform(auxMatrix.idt());
				particleEffect.translate(ComponentsMapper.simpleDecal.get(parent).getDecal().getPosition());
			}
		}
	}

	private void handleCompletedParticleEffects(final ParticleSystem particleSystem) {
		for (Entity entity : particleEntities) {
			ParticleEffect particleEffect = ComponentsMapper.particle.get(entity).getParticleEffect();
			if (particleEffect.isComplete()) {
				particleEntitiesToRemove.add(entity);
			}
		}
		for (Entity entity : particleEntitiesToRemove) {
			particleSystem.remove(ComponentsMapper.particle.get(entity).getParticleEffect());
			entity.remove(ParticleComponent.class);
			getEngine().removeEntity(entity);
		}
		particleEntitiesToRemove.clear();
		for (ParticleEffect effect : particleEffectsToFollow) {
			if (effect.isComplete()) {
				particleEffectsToRemove.add(effect);
			}
		}
		for (ParticleEffect effect : particleEffectsToRemove) {
			particleSystem.remove(effect);
		}
		particleEffectsToRemove.clear();
	}

	@Override
	public void onBeginRenderingParticleEffects(final ModelBatch modelBatch) {
		RenderSystemEventsSubscriber.super.onBeginRenderingParticleEffects(modelBatch);
		modelBatch.render(particleSystem);
	}

	@Override
	public void dispose( ) {

	}
}
