package com.gadarts.isometric.components;

import com.gadarts.isometric.systems.EventsNotifier;

import java.util.ArrayList;
import java.util.List;

public class CharacterComponent implements GameComponent, EventsNotifier<CharacterComponentEventsSubscriber> {
	private final List<CharacterComponentEventsSubscriber> subscribers = new ArrayList<>();

	@Override
	public void reset() {
		subscribers.clear();
	}

	@Override
	public void subscribeForEvents(final CharacterComponentEventsSubscriber sub) {
		if (subscribers.contains(sub)) return;
		subscribers.add(sub);
	}
}
