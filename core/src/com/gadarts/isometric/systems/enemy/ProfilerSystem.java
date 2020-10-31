package com.gadarts.isometric.systems.enemy;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.StringBuilder;
import com.gadarts.isometric.NecromineGame;
import com.gadarts.isometric.systems.GameEntitySystem;
import com.gadarts.isometric.systems.SystemEventsSubscriber;
import com.gadarts.isometric.systems.hud.HudSystem;
import com.gadarts.isometric.systems.hud.HudSystemEventsSubscriber;
import com.gadarts.isometric.systems.render.RenderSystem;
import com.gadarts.isometric.systems.render.RenderSystemEventsSubscriber;
import com.gadarts.isometric.utils.DefaultGameSettings;
import com.gadarts.isometric.utils.SoundPlayer;
import com.gadarts.isometric.utils.assets.GameAssetsManager;
import com.gadarts.isometric.utils.map.MapGraph;

/**
 * Aggregates rendering data.
 */
public class ProfilerSystem extends GameEntitySystem<SystemEventsSubscriber>
		implements RenderSystemEventsSubscriber,
		HudSystemEventsSubscriber {

	public static final String WARNING_COLOR = "[RED]";
	private static final String LABEL_FPS = "FPS: ";
	private static final String LABEL_UI_BATCH_RENDER_CALLS = "UI batch render calls: ";
	private static final String LABEL_GL_CALL = "Total openGL calls: ";
	private static final String LABEL_GL_DRAW_CALL = "Draw calls: ";
	private static final String LABEL_GL_SHADER_SWITCHES = "Shader switches: ";
	private static final String LABEL_GL_TEXTURE_BINDINGS = "Texture bindings: ";
	private static final String LABEL_GL_VERTEX_COUNT = "Vertex count: ";
	private static final String VISIBLE_OBJECTS_STRING = "Visible objects: ";
	private static final String LABEL_VERSION = "Version: ";
	private static final int VERTEX_COUNT_WARNING_LIMIT = 30000;
	private GLProfiler glProfiler;
	private StringBuilder stringBuilder;
	private Stage stage;
	private Label label;
	private RenderSystem renderSystem;

	@Override
	public void init(final MapGraph map, final SoundPlayer soundPlayer, final GameAssetsManager assetManager) {
		super.init(map, soundPlayer, assetManager);
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
			stringBuilder.append("\n").append(LABEL_VERSION).append(NecromineGame.getVersionName());
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
		displayLine(LABEL_GL_TEXTURE_BINDINGS, glProfiler.getTextureBindings() - 1);
		displayLine(LABEL_GL_VERTEX_COUNT, glProfiler.getVertexCount().total, VERTEX_COUNT_WARNING_LIMIT);
		if (renderSystem != null) {
			displayNumberOfVisibleObjects();
		}
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
		label.setPosition(0, Gdx.graphics.getHeight() - 90);
		stage.addActor(label);
		label.setZIndex(0);
		setGlProfiler();
	}

	@Override
	public void onPathCreated(final boolean pathToEnemy) {

	}

	@Override
	public void activate() {

	}
}