package com.gadarts.isometric.systems.input;

import com.gadarts.isometric.systems.SystemEventsSubscriber;

public interface InputSystemEventsSubscriber extends SystemEventsSubscriber {
	default void mouseMoved(final int screenX, final int screenY) {

	}

	default void touchDown(final int screenX, final int screenY, final int button) {

	}

	default void touchUp(final int screenX, final int screenY, final int button) {

	}

	default void touchDragged(final int screenX, final int screenY) {

	}

	default void inputSystemReady(final InputSystem inputSystem) {

	}
}
