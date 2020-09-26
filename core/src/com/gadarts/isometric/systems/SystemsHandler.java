package com.gadarts.isometric.systems;

import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.utils.Disposable;
import com.gadarts.isometric.utils.MapGraph;

public class SystemsHandler implements Disposable {
	private final PooledEngine engine;

	public SystemsHandler(final PooledEngine engine, final MapGraph map) {
		this.engine = engine;
		CharacterSystem characterSystem = new CharacterSystem();
		engine.addSystem(characterSystem);
		CameraSystem cameraSystem = new CameraSystem();
		engine.addSystem(cameraSystem);
		PlayerSystem playerSystem = new PlayerSystem(map);
		engine.addSystem(playerSystem);
		engine.addSystem(new RenderSystem());
		InputSystem inputSystem = new InputSystem();
		engine.addSystem(inputSystem);
		HudSystem hudSystem = new HudSystem();
		engine.addSystem(hudSystem);
		inputSystem.subscribeForEvents(hudSystem);
		inputSystem.subscribeForEvents(cameraSystem);
		inputSystem.subscribeForEvents(playerSystem);
	}

	@Override
	public void dispose() {
		engine.getSystems().forEach(system -> ((GameEntitySystem) system).dispose());
	}
}
