package com.gadarts.isometric.services;

import com.badlogic.ashley.core.PooledEngine;
import com.gadarts.isometric.systems.hud.HudSystemImpl;
import com.gadarts.isometric.systems.hud.console.*;
import com.gadarts.isometric.systems.hud.console.commands.ConsoleCommandsList;
import com.gadarts.isometric.utils.SoundPlayer;
import com.gadarts.isometric.utils.assets.Assets;
import com.gadarts.isometric.utils.assets.GameAssetsManager;
import com.gadarts.isometric.utils.map.MapBuilder;
import com.gadarts.isometric.utils.map.MapGraph;
import lombok.Getter;

@Getter
public class GameServices implements ConsoleEventsSubscriber {
	private final PooledEngine engine;
	private final GameAssetsManager assetManager;
	private final ConsoleImpl consoleImpl;
	private final SoundPlayer soundPlayer;
	private final MapGraph map;
	private final String MSG_ENABLED = "%s enabled.";
	private final String MSG_DISABLED = "%s disabled.";

	public GameServices() {
		engine = new PooledEngine();
		assetManager = new GameAssetsManager();
		MapBuilder mapBuilder = new MapBuilder(assetManager, engine);
		assetManager.loadGameFiles();
		this.map = mapBuilder.createAndAddTestMap();
		consoleImpl = new ConsoleImpl();
		consoleImpl.subscribeForEvents(this);
		soundPlayer = new SoundPlayer(assetManager);
	}

	public void init() {
		initializeConsole();
		soundPlayer.playMusic(Assets.Melody.TEST);
	}

	private void initializeConsole() {
		consoleImpl.init(assetManager);
		engine.getSystem(HudSystemImpl.class).getStage().addActor(consoleImpl);
	}


	@Override
	public void onConsoleActivated() {

	}

	@Override
	public boolean onCommandRun(final ConsoleCommands command, final ConsoleCommandResult consoleCommandResult) {
		boolean result = false;
		if (command == ConsoleCommandsList.SFX) {
			applySfxCommand(consoleCommandResult);
			result = true;
		} else if (command == ConsoleCommandsList.MELODY) {
			applyMusicCommand(consoleCommandResult);
			result = true;
		}
		return result;
	}

	private void applyMusicCommand(final ConsoleCommandResult consoleCommandResult) {
		soundPlayer.setMusicEnabled(!soundPlayer.isMusicEnabled());
		logAudioMessage(consoleCommandResult, "Melodies", soundPlayer.isMusicEnabled());
	}

	private void applySfxCommand(final ConsoleCommandResult consoleCommandResult) {
		soundPlayer.setSfxEnabled(!soundPlayer.isSfxEnabled());
		logAudioMessage(consoleCommandResult, "Sound effects", soundPlayer.isSfxEnabled());
	}

	private void logAudioMessage(final ConsoleCommandResult consoleCommandResult,
								 final String label,
								 final boolean sfxEnabled) {
		String msg = sfxEnabled ? String.format(MSG_ENABLED, label) : String.format(MSG_DISABLED, label);
		consoleCommandResult.setMessage(msg);
	}

	@Override
	public boolean onCommandRun(final ConsoleCommands command,
								final ConsoleCommandResult consoleCommandResult,
								final ConsoleCommandParameter parameter) {
		return false;
	}

	@Override
	public void onConsoleDeactivated() {

	}
}
