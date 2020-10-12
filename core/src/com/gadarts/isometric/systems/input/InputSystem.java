package com.gadarts.isometric.systems.input;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.utils.Disposable;
import com.gadarts.isometric.systems.CameraSystemImpl;
import com.gadarts.isometric.systems.GameEntitySystem;
import com.gadarts.isometric.utils.DefaultGameSettings;

public class InputSystem extends GameEntitySystem<InputSystemEventsSubscriber> implements
		Disposable,
		InputProcessor {

	private CameraInputController debugInput;

	@Override
	public void addedToEngine(final Engine engine) {
		super.addedToEngine(engine);
		initializeInputProcessor();
	}

	private void initializeInputProcessor() {
		InputProcessor input;
		if (DefaultGameSettings.DEBUG_INPUT) {
			input = createDebugInput();
		} else {
			input = createMultiplexer();
		}
		Gdx.input.setInputProcessor(input);
	}

	private InputProcessor createDebugInput() {
		InputProcessor input;
		debugInput = new CameraInputController(getEngine().getSystem(CameraSystemImpl.class).getCamera());
		input = debugInput;
		debugInput.autoUpdate = true;
		return input;
	}

	private InputProcessor createMultiplexer() {
		InputProcessor input;
		InputMultiplexer multiplexer = new InputMultiplexer();
		input = multiplexer;
		multiplexer.addProcessor(this);
		return input;
	}

	@Override
	public void update(final float deltaTime) {
		super.update(deltaTime);
		if (debugInput != null) {
			debugInput.update();
		}
	}

	@Override
	public void dispose() {

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
		for (InputSystemEventsSubscriber subscriber : subscribers) {
			subscriber.touchDown(screenX, screenY, button);
		}
		return true;
	}

	@Override
	public boolean touchUp(final int screenX, final int screenY, final int pointer, final int button) {
		for (InputSystemEventsSubscriber subscriber : subscribers) {
			subscriber.touchUp(screenX, screenY, button);
		}
		return true;
	}

	@Override
	public boolean touchDragged(final int screenX, final int screenY, final int pointer) {
		for (InputSystemEventsSubscriber subscriber : subscribers) {
			subscriber.touchDragged(screenX, screenY);
		}
		return true;
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

	@Override
	public void init() {

	}
}