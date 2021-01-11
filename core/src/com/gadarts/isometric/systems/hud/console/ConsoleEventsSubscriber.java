package com.gadarts.isometric.systems.hud.console;

public interface ConsoleEventsSubscriber {
	void onConsoleActivated();

	boolean onCommandRun(ConsoleCommands command, ConsoleCommandResult consoleCommandResult);

	boolean onCommandRun(ConsoleCommands command, ConsoleCommandResult consoleCommandResult, ConsoleCommandParameter parameter);

	void onConsoleDeactivated();
}
