package com.gadarts.isometric.services;

import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.utils.Disposable;
import com.gadarts.isometric.utils.map.MapBuilder;
import com.gadarts.isometric.utils.map.MapGraph;
import com.gadarts.necromine.assets.GameAssetsManager;
import lombok.Getter;

@Getter
public class MapService implements Disposable {

	private MapGraph map;
	private MapBuilder mapBuilder;

	public void createAndSetMap(final String map,
								final GameAssetsManager assetManager,
								final PooledEngine engine) {
		if (mapBuilder == null) {
			mapBuilder = new MapBuilder(assetManager, engine);
		} else {
			mapBuilder.reset(engine);
		}
		this.map = mapBuilder.inflateTestMap(map);
	}

	@Override
	public void dispose( ) {
		mapBuilder.dispose();
	}
}
