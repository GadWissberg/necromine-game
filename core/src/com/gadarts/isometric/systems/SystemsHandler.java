package com.gadarts.isometric.systems;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.utils.Disposable;
import com.gadarts.isometric.services.GameServices;
import com.gadarts.isometric.systems.hud.console.ConsoleCommandParameter;
import com.gadarts.isometric.systems.hud.console.ConsoleCommandResult;
import com.gadarts.isometric.systems.hud.console.ConsoleCommands;
import com.gadarts.isometric.systems.hud.console.ConsoleEventsSubscriber;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SystemsHandler implements Disposable, ConsoleEventsSubscriber {

	@SuppressWarnings("rawtypes")
	private final Map<Class<? extends SystemEventsSubscriber>, Class<? extends GameEntitySystem>> subscribersInterfaces = new HashMap<>();
	private final GameServices services;

	public SystemsHandler(final GameServices services) {
		this.services = services;
		services.getConsoleImpl().subscribeForEvents(this);
		initSystems();
	}

	private void initSystems() {
		PooledEngine engine = services.getEngine();
		Arrays.stream(Systems.values()).forEach(system -> {
			GameSystem implementation = system.getImplementation();
			engine.addSystem((EntitySystem) implementation);
			subscribersInterfaces.put(system.getEventsSubscriberClass(), (Class<? extends GameEntitySystem>) implementation.getClass());
			implementation.init(services);
		});
		engine.getSystems().forEach((system) -> Arrays.stream(system.getClass().getInterfaces()).forEach(i -> {
			if (subscribersInterfaces.containsKey(i)) {
				EventsNotifier<SystemEventsSubscriber> s = engine.getSystem(subscribersInterfaces.get(i));
				s.subscribeForEvents((SystemEventsSubscriber) system);
			}
		}));
		Arrays.stream(Systems.values()).forEach(system -> system.getImplementation().activate());
	}


	@Override
	public void dispose() {
		services.getEngine().getSystems().forEach(system -> ((GameEntitySystem<? extends SystemEventsSubscriber>) system).dispose());
	}

	@Override
	public void onConsoleActivated() {
		services.getEngine().getSystems().forEach(system -> {
			if (system instanceof ConsoleEventsSubscriber) {
				ConsoleEventsSubscriber sub = (ConsoleEventsSubscriber) system;
				sub.onConsoleActivated();
			}
		});
	}

	@Override
	public boolean onCommandRun(final ConsoleCommands command, final ConsoleCommandResult consoleCommandResult) {
		services.getEngine().getSystems().forEach(system -> {
			if (system instanceof ConsoleEventsSubscriber) {
				ConsoleEventsSubscriber sub = (ConsoleEventsSubscriber) system;
				sub.onCommandRun(command, consoleCommandResult);
			}
		});
		return true;
	}

	@Override
	public boolean onCommandRun(final ConsoleCommands command,
								final ConsoleCommandResult consoleCommandResult,
								final ConsoleCommandParameter parameter) {
		services.getEngine().getSystems().forEach(system -> {
			if (system instanceof ConsoleEventsSubscriber) {
				ConsoleEventsSubscriber sub = (ConsoleEventsSubscriber) system;
				sub.onCommandRun(command, consoleCommandResult, parameter);
			}
		});
		return true;
	}

	@Override
	public void onConsoleDeactivated() {
		services.getEngine().getSystems().forEach(system -> {
			if (system instanceof ConsoleEventsSubscriber) {
				ConsoleEventsSubscriber sub = (ConsoleEventsSubscriber) system;
				sub.onConsoleDeactivated();
			}
		});
	}

	public void reset() {
		initSystems();
	}
}
