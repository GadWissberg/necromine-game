package com.gadarts.isometric.systems;

import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.utils.Disposable;
import com.gadarts.isometric.systems.camera.CameraSystemEventsSubscriber;
import com.gadarts.isometric.systems.character.CharacterSystemEventsSubscriber;
import com.gadarts.isometric.systems.character.CharacterSystemImpl;
import com.gadarts.isometric.systems.enemy.EnemySystem;
import com.gadarts.isometric.systems.enemy.EnemySystemEventsSubscriber;
import com.gadarts.isometric.systems.enemy.ProfilerSystem;
import com.gadarts.isometric.systems.hud.HudSystemEventsSubscriber;
import com.gadarts.isometric.systems.input.InputSystem;
import com.gadarts.isometric.systems.input.InputSystemEventsSubscriber;
import com.gadarts.isometric.systems.player.PlayerSystem;
import com.gadarts.isometric.systems.player.PlayerSystemEventsSubscriber;
import com.gadarts.isometric.systems.render.RenderSystemEventsSubscriber;
import com.gadarts.isometric.systems.render.RenderSystemImpl;
import com.gadarts.isometric.systems.turns.TurnsSystemEventsSubscriber;
import com.gadarts.isometric.systems.turns.TurnsSystemImpl;
import com.gadarts.isometric.utils.SoundPlayer;
import com.gadarts.isometric.utils.map.MapGraph;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SystemsHandler implements Disposable {
	private final PooledEngine engine;

	@SuppressWarnings("rawtypes")
	private final Map<Class<? extends SystemEventsSubscriber>, Class<? extends GameEntitySystem>> subscribersInterfaces = new HashMap<>();

	public SystemsHandler(final PooledEngine engine, final MapGraph map, final SoundPlayer soundPlayer) {
		this.engine = engine;
		addSystem(new CharacterSystemImpl(map, soundPlayer), CharacterSystemEventsSubscriber.class);
		addSystem(new EnemySystem(map, soundPlayer), EnemySystemEventsSubscriber.class);
		addSystem(new TurnsSystemImpl(), TurnsSystemEventsSubscriber.class);
		addSystem(new PlayerSystem(map), PlayerSystemEventsSubscriber.class);
		addSystem(new RenderSystemImpl(), RenderSystemEventsSubscriber.class);
		addSystem(new InputSystem(), InputSystemEventsSubscriber.class);
		addSystem(new CameraSystemImpl(), CameraSystemEventsSubscriber.class);
		addSystem(new HudSystemImpl(map), HudSystemEventsSubscriber.class);
		addSystem(new ProfilerSystem(), SystemEventsSubscriber.class);
		engine.getSystems().forEach((system) -> Arrays.stream(system.getClass().getInterfaces()).forEach(i -> {
			if (subscribersInterfaces.containsKey(i)) {
				//noinspection unchecked
				EventsNotifier<SystemEventsSubscriber> s = engine.getSystem(subscribersInterfaces.get(i));
				s.subscribeForEvents((SystemEventsSubscriber) system);
			}
		}));
		engine.getSystems().forEach((system) -> ((GameEntitySystem<? extends SystemEventsSubscriber>) system).init());
	}

	private void addSystem(final GameEntitySystem<? extends SystemEventsSubscriber> system,
						   final Class<? extends SystemEventsSubscriber> systemEventsSubscriberClass) {
		engine.addSystem(system);

		//noinspection rawtypes
		Class<? extends GameEntitySystem> systemClass = system.getClass();

		subscribersInterfaces.put(systemEventsSubscriberClass, systemClass);
	}

	@Override
	public void dispose() {
		engine.getSystems().forEach(system -> ((GameEntitySystem<? extends SystemEventsSubscriber>) system).dispose());
	}
}
