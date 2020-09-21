package com.gadarts.isometric.systems;

public interface EventsNotifier<T> {
	void subscribeForEvents(T sub);
}
