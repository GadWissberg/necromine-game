package com.gadarts.isometric.systems;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.utils.Disposable;
import com.gadarts.isometric.systems.turns.GameSystem;
import com.gadarts.isometric.utils.map.MapGraph;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class GameEntitySystem<T extends SystemEventsSubscriber> extends EntitySystem
		implements Disposable,
		EventsNotifier<T> {

	protected final List<T> subscribers = new ArrayList<>();

	@Getter(AccessLevel.PROTECTED)
	private final MapGraph map;
	private Map<Class<? extends GameSystem>, GameSystem> mySystems = new HashMap<>();

	public GameEntitySystem(final MapGraph map) {
		this.map = map;
	}

	@Override
	public void subscribeForEvents(final T sub) {
		if (subscribers.contains(sub)) return;
		subscribers.add(sub);
	}

	public abstract void init();

	protected <V> V getSystem(Class<V> systemClass) {
		return (V) mySystems.get(systemClass);
	}

	protected void addSystem(Class<? extends GameSystem> systemClass, final GameSystem gameSystem) {
		mySystems.put(systemClass, gameSystem);
	}
}
