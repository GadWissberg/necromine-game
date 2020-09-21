package com.gadarts.isometric.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

import java.util.ArrayList;
import java.util.List;

public class InputSystem extends GameEntitySystem implements
		Disposable,
		GestureDetector.GestureListener,
		EventsNotifier<InputSystemEventsSubscriber> {

	private CameraInputController debugInput;
	private final List<InputSystemEventsSubscriber> subscribers = new ArrayList<>();

	@Override
	public void addedToEngine(final Engine engine) {
		super.addedToEngine(engine);
		InputProcessor inputProcessor;
		debugInput = new CameraInputController(getEngine().getSystem(CameraSystem.class).getCamera());
		debugInput.autoUpdate = true;
		inputProcessor = debugInput;
		Gdx.input.setInputProcessor(inputProcessor);
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
	public boolean touchDown(final float x, final float y, final int pointer, final int button) {
		return false;
	}

	@Override
	public boolean tap(final float x, final float y, final int count, final int button) {
		return false;
	}

	@Override
	public boolean longPress(final float x, final float y) {
		return false;
	}

	@Override
	public boolean fling(final float velocityX, final float velocityY, final int button) {
		return false;
	}

	@Override
	public boolean pan(final float x, final float y, final float deltaX, final float deltaY) {
		return false;
	}

	@Override
	public boolean panStop(final float x, final float y, final int pointer, final int button) {
		return false;
	}

	@Override
	public boolean zoom(final float initialDistance, final float distance) {
		return false;
	}

	@Override
	public boolean pinch(final Vector2 initialPointer1,
						 final Vector2 initialPointer2,
						 final Vector2 pointer1,
						 final Vector2 pointer2) {
		return false;
	}

	@Override
	public void pinchStop() {
	}

}