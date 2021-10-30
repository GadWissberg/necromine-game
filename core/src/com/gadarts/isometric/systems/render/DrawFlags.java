package com.gadarts.isometric.systems.render;

import com.gadarts.isometric.systems.hud.console.ConsoleCommandParameter;
import com.gadarts.isometric.systems.hud.console.commands.types.DrawFlagSet;
import com.gadarts.isometric.systems.hud.console.commands.types.SkipRenderCommand;
import com.gadarts.isometric.utils.DefaultGameSettings;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class DrawFlags {
	private boolean drawGround = !DefaultGameSettings.HIDE_GROUND;
	private boolean drawEnemy = !DefaultGameSettings.HIDE_ENEMIES;
	private boolean drawEnv = !DefaultGameSettings.HIDE_ENVIRONMENT_OBJECTS;
	private boolean drawCursor = !DefaultGameSettings.HIDE_CURSOR;

	void applySkipRenderCommand(final ConsoleCommandParameter parameter) {
		String alias = parameter.getAlias();
		boolean value = !parameter.getParameterValue();
		Map<String, DrawFlagSet> map = SkipRenderCommand.getMap();
		if (map.containsKey(alias)) {
			map.get(alias).run(this, value);
		}
	}
}
