package com.gadarts.isometric.systems.hud;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.gadarts.isometric.systems.GameSystem;

public interface UserInterfaceSystem extends GameSystem {
	Stage getStage( );

	boolean hasOpenWindows( );

	boolean isMenuClosed( );

	void toggleMenu(boolean active);

	void applyMenuOptions(final MenuOptionDefinition[] options);
}
