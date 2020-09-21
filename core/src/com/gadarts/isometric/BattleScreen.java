package com.gadarts.isometric;

import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Screen;
import com.gadarts.isometric.systems.SystemsHandler;

public class BattleScreen implements Screen {
	private final PooledEngine engine;
	private final SystemsHandler systemsHandler;

	public BattleScreen() {
		this.engine = new PooledEngine();
		this.systemsHandler = new SystemsHandler(engine);
	}

	@Override
	public void show() {

	}

	@Override
	public void render(final float delta) {
		engine.update(delta);
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
	}
}
