package com.gadarts.isometric.systems.hud.console.commands.types;

import com.badlogic.gdx.utils.StringBuilder;
import com.gadarts.isometric.NecromineGame;
import com.gadarts.isometric.systems.hud.console.Console;
import com.gadarts.isometric.systems.hud.console.ConsoleCommandResult;
import com.gadarts.isometric.systems.hud.console.commands.ConsoleCommandImpl;
import com.gadarts.isometric.systems.hud.console.commands.ConsoleCommandsList;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

public class HelpCommand extends ConsoleCommandImpl {

	private static final String INTRO = "Welcome to necromine v%s. The command pattern is '<COMMAND_NAME> " +
			"-<PARAMETER_1> <PARAMETER_1_VALUE>'. The following commands are available:\n%s";

	private static String output;

	@Override
	protected ConsoleCommandsList getCommandEnumValue() {
		return ConsoleCommandsList.HELP;
	}

	@Override
	public ConsoleCommandResult run(final Console console, final Map<String, String> parameters) {
		if (Optional.ofNullable(output).isEmpty()) {
			initializeMessage();
		}
		ConsoleCommandResult consoleCommandResult = new ConsoleCommandResult();
		consoleCommandResult.setMessage(output);
		return consoleCommandResult;
	}

	private void initializeMessage() {
		StringBuilder builder = new StringBuilder();
		Arrays.stream(ConsoleCommandsList.values()).forEach(command -> {
			builder.append(" - ").append(command.name().toLowerCase());
			if (Optional.ofNullable(command.getAlias()).isPresent()) {
				builder.append(" (also '").append(command.getAlias()).append("')");
			}
			builder.append(": ").append(command.getDescription()).append("\n");
		});
		output = String.format(INTRO, NecromineGame.getVersionName(), builder);
	}
}
