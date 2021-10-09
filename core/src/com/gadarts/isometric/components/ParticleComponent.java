package com.gadarts.isometric.components;

import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import lombok.Getter;

@Getter
public class ParticleComponent implements GameComponent {
	private ParticleEffect particleEffect;

	@Override
	public void reset( ) {

	}

	public void init(final ParticleEffect originalEffect) {
		this.particleEffect = originalEffect;
	}
}
