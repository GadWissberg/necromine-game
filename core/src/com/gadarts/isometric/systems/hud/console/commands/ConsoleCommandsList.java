package com.gadarts.isometric.systems.hud.console.commands;

import com.gadarts.isometric.systems.hud.console.ConsoleCommandParameter;
import com.gadarts.isometric.systems.hud.console.ConsoleCommands;
import com.gadarts.isometric.systems.hud.console.ConsoleImpl;
import com.gadarts.isometric.systems.hud.console.InputParsingFailureException;
import com.gadarts.isometric.systems.hud.console.commands.types.*;

import java.util.Arrays;
import java.util.Optional;

public enum ConsoleCommandsList implements ConsoleCommands {
	PROFILER(new ProfilerCommand(), "Toggles profiler and GL operations stats."),
	SFX("sound", new SfxCommand(), "Toggles sound effects."),
	MELODY("music", new MusicCommand(), "Toggles background melody."),
	SKIP_RENDER("skip-render", new SkipRenderCommand(), "Toggles drawing skipping mode for given categories.",
			new SkipRenderCommand.GroundParameter(),
			new SkipRenderCommand.EnemyParameter(),
			new SkipRenderCommand.EnvironmentObjectParameter()),
	HELP("?", new HelpCommand(), "Displays commands list.");

	public static final String DESCRIPTION_PARAMETERS = " Parameters:%s";
	private final ConsoleCommandImpl command;
	private final String alias;
	private final ConsoleCommandParameter[] parameters;
	private String description;

	ConsoleCommandsList(final ConsoleCommandImpl command, final String description) {
		this(null, command, description);
	}

	ConsoleCommandsList(final String alias, final ConsoleCommandImpl command, final String description, final ConsoleCommandParameter... parameters) {
		this.alias = alias;
		this.command = command;
		this.parameters = parameters;
		this.description = description;
		extendDescriptionWithParameters(parameters);
	}

	public static ConsoleCommandsList findCommandByNameOrAlias(final String input) throws InputParsingFailureException {
		Optional<ConsoleCommandsList> result;
		try {
			result = Optional.of(valueOf(input));
		} catch (IllegalArgumentException e) {
			ConsoleCommandsList[] values = values();
			result = Arrays.stream(values).filter(command ->
					Optional.ofNullable(command.getAlias()).isPresent() &&
							command.getAlias().equalsIgnoreCase(input)).findFirst();
			if (result.isEmpty()) {
				throw new InputParsingFailureException(String.format(ConsoleImpl.NOT_RECOGNIZED, input.toLowerCase()));
			}
		}
		return result.get();
	}

	private void extendDescriptionWithParameters(final ConsoleCommandParameter[] parameters) {
		if (parameters.length > 0) {
			StringBuilder stringBuilder = new StringBuilder();
			Arrays.stream(parameters).forEach(parameter -> stringBuilder
					.append("\n")
					.append("   * ")
					.append(parameter.getAlias())
					.append(": ")
					.append(parameter.getDescription()));
			this.description += String.format(DESCRIPTION_PARAMETERS, stringBuilder);
		}
	}

	public String getAlias() {
		return alias;
	}

	public ConsoleCommandImpl getCommandImpl() {
		return command;
	}

	public ConsoleCommandParameter[] getParameters() {
		return parameters;
	}

	public String getDescription() {
		return description;
	}
}
