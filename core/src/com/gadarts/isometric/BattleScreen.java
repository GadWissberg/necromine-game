package com.gadarts.isometric;

import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Screen;
import com.gadarts.isometric.systems.SystemsHandler;
import com.gadarts.isometric.utils.assets.GameAssetsManager;
import com.gadarts.isometric.utils.map.MapBuilder;
import com.gadarts.isometric.utils.map.MapGraph;

public class BattleScreen implements Screen {
	private final PooledEngine engine;
	private final SystemsHandler systemsHandler;
	private final GameAssetsManager assetManager;

	public BattleScreen() {
		this.engine = new PooledEngine();
		assetManager = new GameAssetsManager();
		assetManager.loadGameFiles();
		MapBuilder mapBuilder = new MapBuilder(assetManager, engine);
		MapGraph map = mapBuilder.createAndAddTestMap();
		systemsHandler = new SystemsHandler(engine, map);
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
		assetManager.dispose();
	}
}
