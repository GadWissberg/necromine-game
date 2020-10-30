package com.gadarts.isometric.systems;

import com.gadarts.isometric.utils.SoundPlayer;
import com.gadarts.isometric.utils.assets.GameAssetsManager;
import com.gadarts.isometric.utils.map.MapGraph;

public interface GameSystem {
	void init(MapGraph map, SoundPlayer soundPlayer, GameAssetsManager assetManager);

	void activate();
}
