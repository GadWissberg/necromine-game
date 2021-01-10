package com.gadarts.isometric.systems;

import com.gadarts.isometric.services.GameServices;

public interface GameSystem {
	void init(final GameServices gameServices);

	void activate();
}
