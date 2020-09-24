package com.gadarts.isometric.systems;

public interface InputSystemEventsSubscriber {
	void mouseMoved(final int screenX, final int screenY);

	void touchDown(int screenX, int screenY, int button);

	void touchUp(int screenX, int screenY, int button);

	void touchDragged(int screenX, int screenY);
}
