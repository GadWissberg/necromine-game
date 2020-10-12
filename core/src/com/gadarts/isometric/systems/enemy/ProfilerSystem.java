package com.gadarts.isometric.systems.enemy;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.StringBuilder;
import com.gadarts.isometric.IsometricGame;
import com.gadarts.isometric.systems.GameEntitySystem;
import com.gadarts.isometric.systems.SystemEventsSubscriber;
import com.gadarts.isometric.systems.hud.HudSystem;
import com.gadarts.isometric.systems.hud.HudSystemEventsSubscriber;
import com.gadarts.isometric.systems.render.RenderSystem;
import com.gadarts.isometric.systems.render.RenderSystemEventsSubscriber;
import com.gadarts.isometric.utils.DefaultGameSettings;

public class ProfilerSystem extends GameEntitySystem<SystemEventsSubscriber>
		implements RenderSystemEventsSubscriber,
		HudSystemEventsSubscriber {

	private static final char SEPARATOR = '/';
	private static final String LABEL_FPS = "FPS: ";
	private static final String LABEL_UI_BATCH_RENDER_CALLS = "UI batch render calls: ";
	private static final String LABEL_GL_CALL = "Total openGL calls: ";
	private static final String LABEL_GL_DRAW_CALL = "Draw calls: ";
	private static final String LABEL_GL_SHADER_SWITCHES = "Shader switches: ";
	private static final String LABEL_GL_TEXTURE_BINDINGS = "Texture bindings: ";
	private static final String LABEL_GL_VERTEX_COUNT = "Vertex count: ";
	private static final String VISIBLE_OBJECTS_STRING = "Visible objects: ";
	private static final String LABEL_VERSION = "Version: ";
	private final GLProfiler glProfiler;
	private final StringBuilder stringBuilder;
	private Stage stage;
	private Label label;
	private RenderSystem renderSystem;

	public ProfilerSystem() {
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
			stringBuilder.setLength(0);
			displayLine(LABEL_FPS, Gdx.graphics.getFramesPerSecond());
			displayGlProfiling();
			displayBatchCalls();
			stringBuilder.append("\n").append(LABEL_VERSION).append(IsometricGame.VERSION_NAME);
			label.setText(stringBuilder);
		}
	}

	private void displayBatchCalls() {
		displayLine(LABEL_UI_BATCH_RENDER_CALLS, ((SpriteBatch) stage.getBatch()).renderCalls);
	}

	private void displayGlProfiling() {
		displayLine(LABEL_GL_CALL, glProfiler.getCalls());
		displayLine(LABEL_GL_DRAW_CALL, glProfiler.getDrawCalls());
		displayLine(LABEL_GL_SHADER_SWITCHES, glProfiler.getShaderSwitches());
		int valueWithoutText = glProfiler.getTextureBindings() - 1;
		displayLine(LABEL_GL_TEXTURE_BINDINGS, valueWithoutText);
		displayLine(LABEL_GL_VERTEX_COUNT, glProfiler.getVertexCount().total);
		if (renderSystem != null) displayNumberOfVisibleObjects();
		glProfiler.reset();
	}

	private void displayNumberOfVisibleObjects() {
//		stringBuilder.append(VISIBLE_OBJECTS_STRING);
//		stringBuilder.append(renderSystem.getNumberOfVisible());
//		stringBuilder.append(SEPARATOR);
//		stringBuilder.append(renderSystem.getNumberOfModelInstances());
		stringBuilder.append('\n');
	}

	private void displayLine(final String label, final Object value) {
		stringBuilder.append(label);
		stringBuilder.append(value);
		stringBuilder.append('\n');
	}

	public void reset() {
		glProfiler.reset();
	}

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

	public boolean isEnabled() {
		return glProfiler.isEnabled();
	}

	@Override
	public void init() {

	}

	@Override
	public void dispose() {
		stage.dispose();

	}

	@Override
	public void onRunFrameChanged(final Entity entity, final float deltaTime) {

	}

	@Override
	public void onRenderSystemReady(final RenderSystem renderSystem) {
		this.renderSystem = renderSystem;
	}

	@Override
	public void onHudSystemReady(final HudSystem hudSystem) {
		this.stage = hudSystem.getStage();
		label = new Label(stringBuilder, new Label.LabelStyle(new BitmapFont(), Color.WHITE));
		label.setPosition(0, Gdx.graphics.getHeight() - 90);
		stage.addActor(label);
		label.setZIndex(0);
		setGlProfiler();
	}
}