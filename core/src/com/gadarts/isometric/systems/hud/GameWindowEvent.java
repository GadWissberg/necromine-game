package com.gadarts.isometric.systems.hud;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import lombok.Getter;

@Getter
public class GameWindowEvent extends ChangeListener.ChangeEvent {

	private GameWindowEventType type;

	public GameWindowEvent(final Actor target, final GameWindowEventType type) {
		setTarget(target);
		this.type = type;
	}
}
