package com.gadarts.isometric.systems.hud.console.commands.types;

import com.gadarts.isometric.systems.hud.console.Console;
import com.gadarts.isometric.systems.hud.console.ConsoleCommandResult;
import com.gadarts.isometric.systems.hud.console.commands.ConsoleCommandImpl;
import com.gadarts.isometric.systems.hud.console.commands.ConsoleCommandsList;

import java.util.Map;

public class ProfilerCommand extends ConsoleCommandImpl {
	public static final String PROFILING_ACTIVATED = "Profiling info is displayed.";
	public static final String PROFILING_DEACTIVATED = "Profiling info is hidden.";

	@Override
	public ConsoleCommandResult run(final Console console, final Map<String, String> parameters) {
		return super.run(console, parameters);
	}

	@Override
	protected ConsoleCommandsList getCommandEnumValue() {
		return ConsoleCommandsList.PROFILER;
	}
}
