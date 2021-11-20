package com.gadarts.isometric;

import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Screen;
import com.gadarts.isometric.services.GameServices;
import com.gadarts.isometric.systems.SystemsHandler;
import com.gadarts.isometric.systems.hud.UserUserInterfaceSystemImpl;
import com.gadarts.isometric.systems.player.PlayerSystemImplUser;
import lombok.Setter;

public class BattleScreen implements Screen, GlobalGameService {
	private final GameServices services;
	private final SystemsHandler systemsHandler;

	@Setter
	private boolean inGame;

	public BattleScreen( ) {
		services = new GameServices(this, "mastaba");
		systemsHandler = new SystemsHandler(services);
		services.init();
	}

	@Override
	public void show( ) {

	}

	@Override
	public void render(final float delta) {
		services.getEngine().update(delta);
	}

	@Override
	public void resize(final int width, final int height) {

	}

	@Override
	public void pause( ) {

	}

	@Override
	public void resume( ) {

	}

	@Override
	public void hide( ) {

	}

	@Override
	public void dispose( ) {
		systemsHandler.dispose();
		services.dispose();
	}


	@Override
	public boolean isInGame( ) {
		return inGame;
	}

	@Override
	public void startNewGame(final String map) {
		System.gc();
		services.createAndSetEngine();
		services.getMapService().createAndSetMap(map, services.getAssetManager(), services.getEngine());
		services.getGlobalGameService().setInGame(true);
		systemsHandler.reset(services);
		PooledEngine engine = services.getEngine();
		engine.getSystem(PlayerSystemImplUser.class).enablePlayer();
		engine.getSystem(UserUserInterfaceSystemImpl.class).toggleMenu(false);
	}
}
