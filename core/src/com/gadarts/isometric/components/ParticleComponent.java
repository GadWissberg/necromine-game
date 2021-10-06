package com.gadarts.isometric.components;

import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;

public class ParticleComponent implements GameComponent {
	private ParticleEffect originalEffect;

	@Override
	public void reset() {

	}

	public void init(ParticleEffect originalEffect) {
		this.originalEffect = originalEffect;
	}
}
