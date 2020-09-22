package com.gadarts.isometric.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.utils.Disposable;

import java.util.ArrayList;
import java.util.List;

public class InputSystem extends GameEntitySystem implements
		Disposable,
		InputProcessor,
		EventsNotifier<InputSystemEventsSubscriber> {

	private CameraInputController debugInput;
	private final List<InputSystemEventsSubscriber> subscribers = new ArrayList<>();

	@Override
	public void addedToEngine(final Engine engine) {
		super.addedToEngine(engine);
		InputMultiplexer multiplexer = new InputMultiplexer();
		debugInput = new CameraInputController(getEngine().getSystem(CameraSystem.class).getCamera());
		debugInput.autoUpdate = true;
		multiplexer.addProcessor(debugInput);
		multiplexer.addProcessor(this);
		Gdx.input.setInputProcessor(multiplexer);
	}

	@Override
	public void update(final float deltaTime) {
		super.update(deltaTime);
		debugInput.update();
	}

	@Override
	public void dispose() {

	}

	@Override
	public void subscribeForEvents(final InputSystemEventsSubscriber sub) {
		if (subscribers.contains(sub)) return;
		subscribers.add(sub);
	}

	@Override
	public boolean keyDown(final int keycode) {
		return false;
	}

	@Override
	public boolean keyUp(final int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(final char character) {
		return false;
	}

	@Override
	public boolean touchDown(final int screenX, final int screenY, final int pointer, final int button) {
		return false;
	}

	@Override
	public boolean touchUp(final int screenX, final int screenY, final int pointer, final int button) {
		return false;
	}

	@Override
	public boolean touchDragged(final int screenX, final int screenY, final int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(final int screenX, final int screenY) {
		for (InputSystemEventsSubscriber subscriber : subscribers) {
			subscriber.mouseMoved(screenX, screenY);
		}
		return true;
	}

	@Override
	public boolean scrolled(final int amount) {
		return false;
	}
}