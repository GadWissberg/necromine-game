package com.gadarts.isometric.systems;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.utils.Disposable;
import com.gadarts.isometric.utils.SoundPlayer;
import com.gadarts.isometric.utils.assets.GameAssetsManager;
import com.gadarts.isometric.utils.map.MapGraph;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SystemsHandler implements Disposable {
	private final PooledEngine engine;

	@SuppressWarnings("rawtypes")
	private final Map<Class<? extends SystemEventsSubscriber>, Class<? extends GameEntitySystem>> subscribersInterfaces = new HashMap<>();

	@SuppressWarnings({"unchecked", "rawtypes"})
	public SystemsHandler(final PooledEngine engine,
						  final MapGraph map,
						  final SoundPlayer soundPlayer,
						  final GameAssetsManager assetManager) {
		this.engine = engine;
		Arrays.stream(Systems.values()).forEach(system -> {
			GameSystem implementation = system.getImplementation();
			engine.addSystem((EntitySystem) implementation);
			subscribersInterfaces.put(system.getEventsSubscriberClass(), (Class<? extends GameEntitySystem>) implementation.getClass());
			implementation.init(map, soundPlayer, assetManager);
		});
		engine.getSystems().forEach((system) -> Arrays.stream(system.getClass().getInterfaces()).forEach(i -> {
			if (subscribersInterfaces.containsKey(i)) {
				EventsNotifier<SystemEventsSubscriber> s = engine.getSystem(subscribersInterfaces.get(i));
				s.subscribeForEvents((SystemEventsSubscriber) system);
			}
		}));
		Arrays.stream(Systems.values()).forEach(system -> {
			system.getImplementation().activate();
		});
	}


	@Override
	public void dispose() {
		engine.getSystems().forEach(system -> ((GameEntitySystem<? extends SystemEventsSubscriber>) system).dispose());
	}
}
