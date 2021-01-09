package com.gadarts.isometric;

import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Screen;
import com.gadarts.isometric.systems.SystemsHandler;
import com.gadarts.isometric.systems.hud.HudSystemImpl;
import com.gadarts.isometric.systems.hud.console.*;
import com.gadarts.isometric.utils.SoundPlayer;
import com.gadarts.isometric.utils.assets.Assets;
import com.gadarts.isometric.utils.assets.GameAssetsManager;
import com.gadarts.isometric.utils.map.MapBuilder;
import com.gadarts.isometric.utils.map.MapGraph;

public class BattleScreen implements Screen, ConsoleEventsSubscriber {
	private final PooledEngine engine;
	private final SystemsHandler systemsHandler;
	private final GameAssetsManager assetManager;
	private final ConsoleImpl consoleImpl;

	public BattleScreen() {
		this.engine = new PooledEngine();
		assetManager = new GameAssetsManager();
		assetManager.loadGameFiles();
		MapBuilder mapBuilder = new MapBuilder(assetManager, engine);
		MapGraph map = mapBuilder.createAndAddTestMap();
		SoundPlayer soundPlayer = new SoundPlayer(assetManager);
		soundPlayer.playMusic(Assets.Melody.TEST);
		systemsHandler = new SystemsHandler(engine, map, soundPlayer, assetManager);
		consoleImpl = new ConsoleImpl(assetManager);
		consoleImpl.subscribeForEvents(this);
		engine.getSystem(HudSystemImpl.class).getStage().addActor(consoleImpl);
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

	@Override
	public void onConsoleActivated() {

	}

	@Override
	public boolean onCommandRun(final ConsoleCommands command, final ConsoleCommandResult consoleCommandResult) {
		return false;
	}

	@Override
	public boolean onCommandRun(final ConsoleCommands command, final ConsoleCommandResult consoleCommandResult, final CommandParameter parameter) {
		return false;
	}

	@Override
	public void onConsoleDeactivated() {

	}
}
