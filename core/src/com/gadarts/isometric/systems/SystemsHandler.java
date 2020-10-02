package com.gadarts.isometric.systems;

import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.utils.Disposable;
import com.gadarts.isometric.utils.MapGraph;

public class SystemsHandler implements Disposable {
	private final PooledEngine engine;

	public SystemsHandler(final PooledEngine engine, final MapGraph map) {
		this.engine = engine;
		CharacterSystem characterSystem = new CharacterSystem(map);
		engine.addSystem(characterSystem);
		CameraSystem cameraSystem = new CameraSystem();
		engine.addSystem(cameraSystem);
		EnemySystem enemySystem = new EnemySystem(map);
		TurnsSystem turnsSystem = new TurnsSystem();
		engine.addSystem(turnsSystem);
		turnsSystem.subscribeForEvents(enemySystem);
		enemySystem.subscribeForEvents(turnsSystem);
		HudSystem hudSystem = new HudSystem();
		engine.addSystem(hudSystem);
		PlayerSystem playerSystem = new PlayerSystem(map);
		characterSystem.subscribeForEvents(map);
		characterSystem.subscribeForEvents(playerSystem);
		characterSystem.subscribeForEvents(enemySystem);
		playerSystem.subscribeForEvents(turnsSystem);
		engine.addSystem(playerSystem);
		RenderSystem renderSystem = new RenderSystem();
		renderSystem.subscribeForEvents(characterSystem);
		engine.addSystem(renderSystem);
		engine.addSystem(enemySystem);
		InputSystem inputSystem = new InputSystem();
		engine.addSystem(inputSystem);
		inputSystem.subscribeForEvents(hudSystem);
		inputSystem.subscribeForEvents(cameraSystem);
		inputSystem.subscribeForEvents(playerSystem);
	}

	@Override
	public void dispose() {
		engine.getSystems().forEach(system -> ((GameEntitySystem) system).dispose());
	}
}
