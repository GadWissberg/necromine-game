package com.gadarts.isometric.systems.hud.console.commands.types;

import com.gadarts.isometric.systems.hud.console.Console;
import com.gadarts.isometric.systems.hud.console.ConsoleCommandParameter;
import com.gadarts.isometric.systems.hud.console.commands.ConsoleCommandImpl;
import com.gadarts.isometric.systems.hud.console.commands.ConsoleCommandsList;

public class SkipRenderCommand extends ConsoleCommandImpl {
	@Override
	protected ConsoleCommandsList getCommandEnumValue() {
		return ConsoleCommandsList.SKIP_RENDER;
	}

	public static class GroundParameter extends SkipRenderCommandParameter {

		public static final String ALIAS = "ground";

		public GroundParameter() {
			super(DESCRIPTION, ALIAS);
		}

	}

	public static class EnemyParameter extends SkipRenderCommandParameter {

		public static final String ALIAS = "enemy";

		public EnemyParameter() {
			super(DESCRIPTION, ALIAS);
		}

	}

	public static class EnvironmentObjectParameter extends SkipRenderCommandParameter {

		public static final String ALIAS = "env";

		public EnvironmentObjectParameter() {
			super(DESCRIPTION, ALIAS);
		}

	}

	public static class CursorParameter extends SkipRenderCommandParameter {

		public static final String ALIAS = "cursor";

		public CursorParameter() {
			super(DESCRIPTION, ALIAS);
		}

	}

	public static class FowParameter extends SkipRenderCommandParameter {

		public static final String ALIAS = "fow";

		public FowParameter() {
			super(DESCRIPTION, ALIAS);
		}

	}

	private abstract static class SkipRenderCommandParameter extends ConsoleCommandParameter {
		public static final String DESCRIPTION = "0 - Renders as normal. 1 - Skips.";

		public SkipRenderCommandParameter(final String description, final String alias) {
			super(description, alias);
		}

		@Override
		public void run(final String value, final Console console) {
			String alias = getAlias();
			defineParameterValue(
					value,
					console,
					String.format("%s rendering is skipped.", alias),
					String.format("%s rendering is back to normal.", alias)
			);
			console.notifyCommandExecution(ConsoleCommandsList.SKIP_RENDER, this);
		}
	}
}
