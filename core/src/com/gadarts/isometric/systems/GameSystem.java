package com.gadarts.isometric.systems;

import com.gadarts.isometric.services.GameServices;

public interface GameSystem {
	void init(final GameServices gameServices);

	default boolean keepUpdatingWhenMenuIsOpen() {
		return false;
	}

	void setProcessing(boolean process);

	void activate();

	default void reset() {

	}
}
