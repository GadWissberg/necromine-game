package com.gadarts.isometric.systems.render;

import com.gadarts.isometric.systems.hud.console.ConsoleCommandParameter;
import com.gadarts.isometric.systems.hud.console.commands.types.SkipRenderCommand;
import com.gadarts.isometric.utils.DefaultGameSettings;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DrawFlags {
	private boolean drawGround = !DefaultGameSettings.HIDE_GROUND;
	private boolean drawEnemy = !DefaultGameSettings.HIDE_ENEMIES;
	private boolean drawEnv = !DefaultGameSettings.HIDE_ENVIRONMENT_OBJECTS;
	private boolean drawCursor = !DefaultGameSettings.HIDE_CURSOR;
	private boolean drawFow = !DefaultGameSettings.HIDE_FOW;

	void applySkipRenderCommand(final ConsoleCommandParameter parameter) {
		String alias = parameter.getAlias();
		boolean value = !parameter.getParameterValue();
		switch (alias) {
			case SkipRenderCommand.GroundParameter.ALIAS -> setDrawGround(value);
			case SkipRenderCommand.EnemyParameter.ALIAS -> setDrawEnemy(value);
			case SkipRenderCommand.EnvironmentObjectParameter.ALIAS -> setDrawEnv(value);
			case SkipRenderCommand.CursorParameter.ALIAS -> setDrawCursor(value);
			case SkipRenderCommand.FowParameter.ALIAS -> setDrawFow(value);
		}
	}
}
