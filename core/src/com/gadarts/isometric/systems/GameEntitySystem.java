package com.gadarts.isometric.systems;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.utils.Disposable;
import com.gadarts.isometric.services.GameServices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class GameEntitySystem<T extends SystemEventsSubscriber> extends EntitySystem
		implements Disposable,
		EventsNotifier<T>,
		GameSystem {

	protected final List<T> subscribers = new ArrayList<>();
	private final Map<Class<? extends GameSystem>, GameSystem> mySystems = new HashMap<>();

	protected GameServices services;

	@Override
	public void init(final GameServices services) {
		this.services = services;
	}

	@Override
	public void subscribeForEvents(final T sub) {
		if (subscribers.contains(sub)) return;
		subscribers.add(sub);
	}

	@SuppressWarnings("unchecked")
	protected <V extends GameSystem> V getSystem(final Class<V> systemClass) {
		return (V) mySystems.get(systemClass);
	}

	protected void addSystem(final Class<? extends GameSystem> systemClass, final GameSystem gameSystem) {
		mySystems.put(systemClass, gameSystem);
	}
}
