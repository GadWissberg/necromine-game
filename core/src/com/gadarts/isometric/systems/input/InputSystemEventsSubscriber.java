package com.gadarts.isometric.systems.input;

import com.gadarts.isometric.systems.SystemEventsSubscriber;

public interface InputSystemEventsSubscriber extends SystemEventsSubscriber {
	void mouseMoved(final int screenX, final int screenY);

	void touchDown(int screenX, int screenY, int button);

	void touchUp(int screenX, int screenY, int button);

	void touchDragged(int screenX, int screenY);
}
