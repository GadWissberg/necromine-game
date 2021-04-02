package com.gadarts.isometric;

import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Screen;
import com.gadarts.isometric.services.GameServices;
import com.gadarts.isometric.systems.SystemsHandler;
import com.gadarts.isometric.systems.hud.HudSystemImpl;
import com.gadarts.isometric.systems.player.PlayerSystemImpl;

public class BattleScreen implements Screen, GlobalGameService {
	private final GameServices services;
	private SystemsHandler systemsHandler;

	public BattleScreen() {
		services = new GameServices(this);
		systemsHandler = new SystemsHandler(services);
		services.init();
	}

	@Override
	public void show() {

	}

	@Override
	public void render(final float delta) {
		services.getEngine().update(delta);
	}

	@Override
	public void resize(final int width, final int height) {

	}

	@Override
	public void pause() {

	}

	@Override
	public void resume() {

	}

	@Override
	public void hide() {

	}

	@Override
	public void dispose() {
		systemsHandler.dispose();
		services.dispose();
	}


	@Override
	public void startNewGame() {
		services.createAndSetEngine();
		services.createAndSetMap();
		services.setInGame(true);
		systemsHandler.reset();
		PooledEngine engine = services.getEngine();
		engine.getSystem(PlayerSystemImpl.class).enablePlayer();
		engine.getSystem(HudSystemImpl.class).toggleMenu(false);
	}
}
