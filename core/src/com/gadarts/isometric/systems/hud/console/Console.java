package com.gadarts.isometric.systems.hud.console;

public interface Console {
	String ERROR_COLOR = "[RED]";

	void insertNewLog(String text, boolean logTime);

	void insertNewLog(String text, boolean logTime, String color);

	ConsoleCommandResult notifyCommandExecution(ConsoleCommands command);

	ConsoleCommandResult notifyCommandExecution(ConsoleCommands command, CommandParameter parameter);

	void activate();

	void deactivate();

	boolean isActive();
}
