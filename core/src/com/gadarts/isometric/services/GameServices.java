package com.gadarts.isometric.services;

import com.badlogic.ashley.core.PooledEngine;
import com.gadarts.isometric.systems.hud.HudSystemImpl;
import com.gadarts.isometric.systems.hud.console.ConsoleImpl;
import com.gadarts.isometric.utils.SoundPlayer;
import com.gadarts.isometric.utils.assets.Assets;
import com.gadarts.isometric.utils.assets.GameAssetsManager;
import com.gadarts.isometric.utils.map.MapBuilder;
import com.gadarts.isometric.utils.map.MapGraph;
import lombok.Getter;

@Getter
public class GameServices {
	private final PooledEngine engine;
	private final GameAssetsManager assetManager;
	private final ConsoleImpl consoleImpl;
	private final SoundPlayer soundPlayer;
	private final MapGraph map;

	public GameServices() {
		engine = new PooledEngine();
		assetManager = new GameAssetsManager();
		MapBuilder mapBuilder = new MapBuilder(assetManager, engine);
		assetManager.loadGameFiles();
		this.map = mapBuilder.createAndAddTestMap();
		consoleImpl = new ConsoleImpl();
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


}
