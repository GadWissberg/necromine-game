package com.gadarts.isometric;

import com.badlogic.gdx.Screen;
import com.gadarts.isometric.services.GameServices;
import com.gadarts.isometric.systems.SystemsHandler;
import com.gadarts.isometric.systems.hud.console.CommandParameter;
import com.gadarts.isometric.systems.hud.console.ConsoleCommandResult;
import com.gadarts.isometric.systems.hud.console.ConsoleCommands;
import com.gadarts.isometric.systems.hud.console.ConsoleEventsSubscriber;

public class BattleScreen implements Screen, ConsoleEventsSubscriber {
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
		services.getAssetManager().dispose();
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
