package com.gadarts.isometric.systems.hud.console;


import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public abstract class CommandParameter {
	private final String description;
	private final String alias;
	private boolean parameterValue;

	public CommandParameter(final String description, final String alias) {
		this.description = description;
		this.alias = alias;
	}

	public boolean defineParameterValue(final String value,
										final Console console,
										final String messageOnParameterActivation,
										final String messageOnParameterDeactivation) {
		boolean result;
		try {
			result = Integer.parseInt(value) == 1;
		} catch (NumberFormatException e) {
			result = false;
		}
		String msg = String.format(result ? messageOnParameterActivation : messageOnParameterDeactivation, alias);
		console.insertNewLog(msg, false);
		setParameterValue(result);
		return result;
	}

	public abstract void run(String value, Console console);

	public boolean getParameterValue() {
		return parameterValue;
	}
}
