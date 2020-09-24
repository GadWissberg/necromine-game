package com.gadarts.isometric.systems;

import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.utils.Disposable;

public class SystemsHandler implements Disposable {
	private final PooledEngine engine;

	public SystemsHandler(final PooledEngine engine) {
		this.engine = engine;
		CameraSystem cameraSystem = new CameraSystem();
		engine.addSystem(cameraSystem);
		engine.addSystem(new RenderSystem());
		InputSystem inputSystem = new InputSystem();
		engine.addSystem(inputSystem);
		HudSystem hudSystem = new HudSystem();
		engine.addSystem(hudSystem);
		inputSystem.subscribeForEvents(hudSystem);
		inputSystem.subscribeForEvents(cameraSystem);
	}

	@Override
	public void dispose() {
		engine.getSystems().forEach(system -> ((GameEntitySystem) system).dispose());
	}
}
