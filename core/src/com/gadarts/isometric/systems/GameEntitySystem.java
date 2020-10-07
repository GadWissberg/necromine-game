package com.gadarts.isometric.systems;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.utils.Disposable;

import java.util.ArrayList;
import java.util.List;

public abstract class GameEntitySystem<T extends SystemEventsSubscriber> extends EntitySystem implements Disposable, EventsNotifier<T> {
	protected final List<T> subscribers = new ArrayList<>();

	@Override
	public void subscribeForEvents(final T sub) {
		if (subscribers.contains(sub)) return;
		subscribers.add(sub);
	}

	public abstract void init();
}
