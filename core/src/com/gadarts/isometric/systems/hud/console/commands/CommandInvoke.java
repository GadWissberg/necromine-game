package com.gadarts.isometric.systems.hud.console.commands;

import java.util.HashMap;
import java.util.Map;

public class CommandInvoke {
	private final ConsoleCommandsList command;
	private Map<String, String> parameters = new HashMap<>();

	public CommandInvoke(ConsoleCommandsList command) {
		this.command = command;
	}

	public ConsoleCommandsList getCommand() {
		return command;
	}

	public void addParameter(String parameter, String value) {
		parameters.put(parameter, value);
	}

	public Map<String, String> getParameters() {
		return parameters;
	}
}
