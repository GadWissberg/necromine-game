package com.gadarts.isometric.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.StringBuilder;
import com.gadarts.isometric.NecromineGame;
import com.gadarts.isometric.services.GameServices;
import com.gadarts.isometric.systems.hud.AttackNodesHandler;
import com.gadarts.isometric.systems.hud.HudSystem;
import com.gadarts.isometric.systems.hud.HudSystemEventsSubscriber;
import com.gadarts.isometric.systems.hud.console.ConsoleCommandParameter;
import com.gadarts.isometric.systems.hud.console.ConsoleCommandResult;
import com.gadarts.isometric.systems.hud.console.ConsoleCommands;
import com.gadarts.isometric.systems.hud.console.ConsoleEventsSubscriber;
import com.gadarts.isometric.systems.hud.console.commands.ConsoleCommandsList;
import com.gadarts.isometric.systems.hud.console.commands.types.ProfilerCommand;
import com.gadarts.isometric.systems.render.RenderSystem;
import com.gadarts.isometric.systems.render.RenderSystemEventsSubscriber;
import com.gadarts.isometric.utils.DefaultGameSettings;
import com.gadarts.isometric.utils.map.MapGraphNode;

/**
 * Aggregates rendering data.
 */
public class ProfilerSystem extends GameEntitySystem<SystemEventsSubscriber>
		implements RenderSystemEventsSubscriber,
		HudSystemEventsSubscriber,
		ConsoleEventsSubscriber {
	public static final String WARNING_COLOR = "[RED]";
	private static final char SEPARATOR = '/';
	private static final String LABEL_FPS = "FPS: ";
	public static final int LABELS_ORIGIN_OFFSET_FROM_TOP = 100;
	private static final String LABEL_JAVA_HEAP_USAGE = "Java heap usage: ";
	private static final String LABEL_GL_CALL = "Total openGL calls: ";
	private static final String LABEL_GL_DRAW_CALL = "Draw calls: ";
	private static final String LABEL_GL_SHADER_SWITCHES = "Shader switches: ";
	private static final String LABEL_GL_TEXTURE_BINDINGS = "Texture bindings: ";
	private static final String LABEL_GL_VERTEX_COUNT = "Vertex count: ";
	private static final String VISIBLE_OBJECTS_STRING = "Visible objects: ";
	private static final String LABEL_VERSION = "Version: ";
	private static final int VERTEX_COUNT_WARNING_LIMIT = 35000;
	private static final String SUFFIX_MB = "MB";
	private static final String LABEL_NATIVE_HEAP_USAGE = "Native heap usage: ";
	private GLProfiler glProfiler;
	private StringBuilder stringBuilder;
	private Stage stage;
	private Label label;
	private RenderSystem renderSystem;

	@Override
	public void init(final GameServices services) {
		super.init(services);
		glProfiler = new GLProfiler(Gdx.graphics);
		stringBuilder = new StringBuilder();
	}


	private void setGlProfiler() {
		if (Gdx.app.getLogLevel() == Application.LOG_DEBUG && DefaultGameSettings.SHOW_GL_PROFILING) {
			glProfiler.enable();
		}
	}

	@Override
	public void update(final float deltaTime) {
		super.update(deltaTime);
		if (Gdx.app.getLogLevel() == Application.LOG_DEBUG && glProfiler.isEnabled()) {
			displayLabels();
		}
	}

	private void displayLabels() {
		stringBuilder.setLength(0);
		displayLine(LABEL_FPS, Gdx.graphics.getFramesPerSecond());
		displayMemoryLabels();
		displayGlProfiling();
		stringBuilder.append("\n").append(LABEL_VERSION).append(NecromineGame.getVersionName());
		label.setText(stringBuilder);
	}

	private void displayMemoryLabels() {
		displayLine(LABEL_JAVA_HEAP_USAGE, Gdx.app.getJavaHeap() / (1024L * 1024L), false);
		stringBuilder.append(' ').append(SUFFIX_MB).append('\n');
		displayLine(LABEL_NATIVE_HEAP_USAGE, Gdx.app.getNativeHeap() / (1024L * 1024L), false);
		stringBuilder.append(' ').append(SUFFIX_MB).append('\n');
	}

	private void displayGlProfiling() {
		displayLine(LABEL_GL_CALL, glProfiler.getCalls());
		displayLine(LABEL_GL_DRAW_CALL, glProfiler.getDrawCalls());
		displayLine(LABEL_GL_SHADER_SWITCHES, glProfiler.getShaderSwitches());
		displayLine(LABEL_GL_TEXTURE_BINDINGS, glProfiler.getTextureBindings() - 1);
		displayLine(LABEL_GL_VERTEX_COUNT, glProfiler.getVertexCount().total, VERTEX_COUNT_WARNING_LIMIT);
		if (renderSystem != null) {
			displayNumberOfVisibleObjects();
		}
		glProfiler.reset();
	}

	private void displayNumberOfVisibleObjects() {
		stringBuilder.append(VISIBLE_OBJECTS_STRING);
		stringBuilder.append(renderSystem.getNumberOfVisible());
		stringBuilder.append(SEPARATOR);
		stringBuilder.append(renderSystem.getNumberOfModelInstances());
		stringBuilder.append('\n');
	}

	private void displayLine(final String label, final Object value, final boolean addEndOfLine) {
		stringBuilder.append(label);
		stringBuilder.append(value);
		if (addEndOfLine) {
			stringBuilder.append('\n');
		}
	}

	private void displayLine(final String label, final Object value) {
		displayLine(label, value, true);
	}

	private void displayLine(final String label, final Object value, final int warningThreshold) {
		stringBuilder.append(label);
		boolean displayWarning = value instanceof Float && warningThreshold <= ((float) value);
		if (displayWarning) {
			stringBuilder.append(WARNING_COLOR);
		}
		stringBuilder.append(value);
		if (displayWarning) {
			stringBuilder.append("[WHITE]");
		}
		stringBuilder.append('\n');
	}

	/**
	 * Resets the GLProfiler.
	 */
	public void reset() {
		glProfiler.reset();
	}

	/**
	 * Toggles the GLProfiler.
	 */
	public void toggle() {
		if (glProfiler.isEnabled()) {
			glProfiler.disable();
		} else {
			glProfiler.enable();
			reset();
		}
		stringBuilder.clear();
		label.setVisible(glProfiler.isEnabled());
	}

	/**
	 * @return Whether GLProfiler is enabled.
	 */
	public boolean isEnabled() {
		return glProfiler.isEnabled();
	}


	@Override
	public void dispose() {
		stage.dispose();

	}

	@Override
	public void onFrameChanged(final Entity entity, final float deltaTime, final TextureAtlas.AtlasRegion newFrame) {

	}

	@Override
	public void onRenderSystemReady(final RenderSystem renderSystem) {
		this.renderSystem = renderSystem;
	}

	@Override
	public void onHudSystemReady(final HudSystem hudSystem) {
		this.stage = hudSystem.getStage();
		addSystem(HudSystem.class, hudSystem);
		BitmapFont font = new BitmapFont();
		font.getData().markupEnabled = true;
		label = new Label(stringBuilder, new Label.LabelStyle(font, Color.WHITE));
		label.setPosition(0, Gdx.graphics.getHeight() - LABELS_ORIGIN_OFFSET_FROM_TOP);
		stage.addActor(label);
		label.setZIndex(0);
		setGlProfiler();
	}

	@Override
	public void onUserSelectedNodeToApplyTurn(final MapGraphNode cursorNode, final AttackNodesHandler attackNodesHandler) {

	}


	@Override
	public void activate() {

	}

	@Override
	public void onConsoleActivated() {

	}

	@Override
	public boolean onCommandRun(final ConsoleCommands command, final ConsoleCommandResult consoleCommandResult) {
		return onCommandRun(command, consoleCommandResult, null);
	}

	@Override
	public boolean onCommandRun(final ConsoleCommands command,
								final ConsoleCommandResult consoleCommandResult,
								final ConsoleCommandParameter parameter) {
		String msg = reactToCommand(command);
		boolean result = false;
		if (msg != null) {
			consoleCommandResult.setMessage(msg);
			result = true;
		}
		return result;
	}

	private String reactToCommand(final ConsoleCommands command) {
		String msg = null;
		if (command == ConsoleCommandsList.PROFILER) {
			toggle();
			msg = glProfiler.isEnabled() ? ProfilerCommand.PROFILING_ACTIVATED : ProfilerCommand.PROFILING_DEACTIVATED;
		}
		return msg;
	}

	@Override
	public void onConsoleDeactivated() {

	}
}