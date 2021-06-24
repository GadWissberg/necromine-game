package com.gadarts.isometric.systems.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.gadarts.isometric.GlobalGameService;

public class MenuOption extends Label {
	static final Color FONT_COLOR_REGULAR = Color.RED;
	private static final Color FONT_COLOR_HOVER = Color.YELLOW;

	public MenuOption(final MenuOptionDefinition option,
					  final LabelStyle optionStyle,
					  final GlobalGameService globalGameService,
					  final InterfaceSystem interfaceSystem) {
		super(option.getLabel(), new LabelStyle(optionStyle));
		addListener(new ClickListener() {
			@Override
			public void enter(final InputEvent event,
							  final float x,
							  final float y,
							  final int pointer,
							  final Actor fromActor) {
				super.enter(event, x, y, pointer, fromActor);
				getStyle().fontColor = FONT_COLOR_HOVER;
			}

			@Override
			public void clicked(final InputEvent event, final float x, final float y) {
				super.clicked(event, x, y);
				MenuOptionAction action = option.getAction();
				if (action != null) {
					action.run(globalGameService, interfaceSystem);
				} else {
					MenuOptionDefinition[] subOptions = option.getSubOptions();
					if (subOptions != null) {
						interfaceSystem.applyMenuOptions(subOptions);
					}
				}
			}

			@Override
			public void exit(final InputEvent event,
							 final float x,
							 final float y,
							 final int pointer,
							 final Actor toActor) {
				super.exit(event, x, y, pointer, toActor);
				getStyle().fontColor = FONT_COLOR_REGULAR;
			}
		});
	}
}
