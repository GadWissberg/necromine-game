package com.gadarts.isometric.systems.hud.console.commands;

import com.badlogic.gdx.utils.StringBuilder;
import com.gadarts.isometric.systems.hud.console.Console;
import com.gadarts.isometric.systems.hud.console.ConsoleCommandResult;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.IntStream;

public abstract class ConsoleCommandImpl {
	private static final String NO_PARAMETERS = "No parameters were supplied. Please supply one or more of the following: %s";
	private final StringBuilder stringBuilder = new StringBuilder();

	public ConsoleCommandResult run(Console console, Map<String, String> parameters) {
		ConsoleCommandResult result = console.notifyCommandExecution(getCommandEnumValue());
		if (!parameters.isEmpty()) {
			parameters.forEach((key, value) -> Arrays.stream(getCommandEnumValue().getParameters()).forEach(parameter -> {
				if (key.equalsIgnoreCase(parameter.getAlias())) {
					parameter.run(value, console);
				}
			}));
		} else {
			printNoParameters(result);
		}
		return result;
	}

	private void printNoParameters(ConsoleCommandResult result) {
		int length = getCommandEnumValue().getParameters().length;
		IntStream.range(0, length).forEach(i -> {
			stringBuilder.append(getCommandEnumValue().getParameters()[i].getAlias());
			if (i < length - 1) {
				stringBuilder.append(", ");
			}
		});
		result.setMessage(String.format(NO_PARAMETERS, stringBuilder));
	}

	protected abstract ConsoleCommandsList getCommandEnumValue();

}
