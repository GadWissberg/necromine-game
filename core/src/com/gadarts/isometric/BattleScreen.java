package com.gadarts.isometric;

import com.badlogic.gdx.Screen;
import com.gadarts.isometric.services.GameServices;
import com.gadarts.isometric.systems.SystemsHandler;

public class BattleScreen implements Screen {
	private final SystemsHandler systemsHandler;

	private final GameServices services;

	public BattleScreen() {
		services = new GameServices();
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

}
