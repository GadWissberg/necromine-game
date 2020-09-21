package com.gadarts.isometric.systems;

import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.utils.Disposable;

public class SystemsHandler implements Disposable {
	private final PooledEngine engine;

	public SystemsHandler(final PooledEngine engine) {
		this.engine = engine;
		engine.addSystem(new CameraSystem());
		engine.addSystem(new RenderSystem());
		engine.addSystem(new InputSystem());
	}

	@Override
	public void dispose() {
		engine.getSystems().forEach(system -> ((GameEntitySystem) system).dispose());
	}
}
