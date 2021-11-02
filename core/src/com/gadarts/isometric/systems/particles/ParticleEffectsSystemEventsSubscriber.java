package com.gadarts.isometric.systems.particles;

import com.badlogic.gdx.graphics.g3d.particles.batches.PointSpriteParticleBatch;
import com.gadarts.isometric.systems.SystemEventsSubscriber;

public interface ParticleEffectsSystemEventsSubscriber extends SystemEventsSubscriber {
	void onParticleEffectsSystemReady(PointSpriteParticleBatch pointSpriteBatch);
}
