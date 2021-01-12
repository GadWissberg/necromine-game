package com.gadarts.isometric.systems.hud.console.commands.types;

import com.gadarts.isometric.systems.hud.console.commands.ConsoleCommandImpl;
import com.gadarts.isometric.systems.hud.console.commands.ConsoleCommandsList;

public class BordersCommand extends ConsoleCommandImpl {
	@Override
	protected ConsoleCommandsList getCommandEnumValue() {
		return ConsoleCommandsList.BORDERS;
	}
}
