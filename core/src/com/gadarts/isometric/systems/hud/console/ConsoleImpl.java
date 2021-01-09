package com.gadarts.isometric.systems.hud.console;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.MoveByAction;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.StringBuilder;
import com.gadarts.isometric.systems.hud.console.commands.CommandInvoke;
import com.gadarts.isometric.systems.hud.console.commands.ConsoleCommandsList;
import com.gadarts.isometric.utils.assets.GameAssetsManager;

import java.util.Arrays;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public class ConsoleImpl extends Table implements Console, InputProcessor {
	public static final String INPUT_COLOR = "[YELLOW]";
	public static final String OUTPUT_COLOR = "[LIGHT_GRAY]";
	public static final String NAME = "console";
	public static final Interpolation.Pow INTERPOLATION = Interpolation.pow2;
	public static final String NOT_RECOGNIZED = "'%s' is not recognized as a command.";
	public static final String INPUT_FIELD_NAME = "input";
	public static final String INPUT_SIGN = ">";
	public static final char GRAVE_ASCII = '`';
	public static final String OPTIONS_DELIMITER = " | ";
	public static final String WARNING_COLOR = "[GOLD]";
	static final String TEXT_VIEW_NAME = "text";
	private static final float INPUT_HEIGHT = 20f;
	private static final float PADDING = 10f;
	private static final float TRANSITION_DURATION = 0.5f;
	private static final String PARAMETER_EXPECTED = "Failed to apply command! Parameter is expected at '%s'";
	private static final String PARAMETER_VALUE_EXPECTED = "Failed to apply command! Value is expected for " +
			"parameter '%s'";
	private static final String MSG_SUGGESTED = "Possible options:\n%s";
	private final ConsoleTextures consoleTextures = new ConsoleTextures();
	private final Array<ConsoleEventsSubscriber> subscribers = new Array<>();
	private final ConsoleTextData consoleTextData;
	private final ConsoleCommandResult consoleCommandResult = new ConsoleCommandResult();
	private final ConsoleInputHistoryHandler consoleInputHistoryHandler;
	private final StringBuilder stringBuilder = new StringBuilder();
	private boolean active;
	private TextField input;
	private ScrollPane scrollPane;
	private boolean scrollToEnd = true;
	private Image arrow;

	public ConsoleImpl(final GameAssetsManager assetManager) {
		consoleTextData = new ConsoleTextData(assetManager);
		setName(NAME);
		int screenHeight = Gdx.graphics.getHeight();
		float height = screenHeight / 3f;
		setVisible(false);
		setPosition(0, screenHeight);
		consoleTextures.init((int) height);
		TextureRegionDrawable textBackgroundTextureRegionDrawable = new TextureRegionDrawable(consoleTextures.getTextBackgroundTexture());
		addTextView(textBackgroundTextureRegionDrawable, (int) height);
		addInputField(textBackgroundTextureRegionDrawable);
		setBackground(new TextureRegionDrawable(consoleTextures.getBackgroundTexture()));
		setSize(Gdx.graphics.getWidth(), consoleTextures.getBackgroundTexture().getHeight());
		consoleInputHistoryHandler = new ConsoleInputHistoryHandler();
		InputMultiplexer multiplexer = (InputMultiplexer) Gdx.input.getInputProcessor();
		multiplexer.addProcessor(this);
		input.setTextFieldListener((textField, c) -> {
			if (c == GRAVE_ASCII) {
				textField.setText(null);
				if (!ConsoleImpl.this.hasActions()) if (isActive()) deactivate();
			}
			if (active) {
				if (c == '\r' || c == '\n') {
					applyInput(input);
				}
			}
		});
		input.addCaptureListener(new InputListener() {
			@Override
			public boolean keyDown(final InputEvent event, final int keycode) {
				boolean result = false;
				if (active) {
					result = true;
					if (keycode == Input.Keys.PAGE_UP) scroll(-consoleTextData.getFontHeight() * 2);
					else if (keycode == Input.Keys.PAGE_DOWN) scroll(consoleTextData.getFontHeight() * 2);
					else if (keycode == Input.Keys.TAB) tryFindingCommand();
					else if (keycode == Input.Keys.ESCAPE) deactivate();
					else consoleInputHistoryHandler.onKeyDown(keycode);
				}
				return result;
			}

			private void tryFindingCommand() {
				if (input.getText().isEmpty()) return;
				java.util.List<ConsoleCommandsList> options = Arrays.stream(ConsoleCommandsList.values())
						.filter(command -> command.name().startsWith(input.getText().toUpperCase()))
						.collect(toList());
				if (options.size() == 1) {
					input.setText(options.get(0).name().toLowerCase());
					input.setCursorPosition(input.getText().length());
				} else if (options.size() > 1) logSuggestedCommands(options);
			}

			private void logSuggestedCommands(final java.util.List<ConsoleCommandsList> options) {
				stringBuilder.clear();
				options.forEach(command -> stringBuilder.append(command.name().toLowerCase()).append(OPTIONS_DELIMITER));
				insertNewLog(String.format(MSG_SUGGESTED, stringBuilder), false);
			}

			private void scroll(final float step) {
				scrollPane.setScrollY(scrollPane.getScrollY() + step);
				scrollToEnd = false;
			}
		});
	}

	@Override
	protected void setStage(final Stage stage) {
		super.setStage(stage);
		consoleTextData.setStage(stage);
		consoleInputHistoryHandler.setStage(stage);
	}

	private void addInputField(final TextureRegionDrawable textBackgroundTexture) {
		TextField.TextFieldStyle style = new TextField.TextFieldStyle(consoleTextData.getFont(), Color.YELLOW, new TextureRegionDrawable(consoleTextures.getCursorTexture()),
				null, textBackgroundTexture);
		input = new TextField("", style);
		input.setName(INPUT_FIELD_NAME);
		Label arrow = new Label(INPUT_SIGN, consoleTextData.getTextStyle());
		add(arrow).padBottom(PADDING).padLeft(PADDING).size(10f, INPUT_HEIGHT);
		add(input).size(Gdx.graphics.getWidth() - PADDING * 3, INPUT_HEIGHT).padBottom(PADDING).padRight(PADDING).align(Align.left).row();
		input.setFocusTraversal(false);
	}

	private void applyInput(final TextField textField) {
		insertNewLog(textField.getText(), true, ConsoleImpl.INPUT_COLOR);
		String inputCommand = textField.getText();
		consoleInputHistoryHandler.applyInput(inputCommand);
		try {
			CommandInvoke commandToInvoke;
			commandToInvoke = parseCommandFromInput(inputCommand);
			ConsoleCommandResult result = commandToInvoke.getCommand().getCommandImpl().run(this, commandToInvoke.getParameters());
			insertNewLog(result.getMessage(), false);
		} catch (InputParsingFailureException e) {
			insertNewLog(e.getMessage(), false);
		}
		textField.setText(null);
	}

	private CommandInvoke parseCommandFromInput(final String text) throws InputParsingFailureException {
		if (text == null) return null;
		String[] entries = text.toUpperCase().split(" ");
		String commandName = entries[0];
		CommandInvoke command;
		command = new CommandInvoke(ConsoleCommandsList.findCommandByNameOrAlias(commandName));
		parseParameters(entries, command);
		return command;
	}

	private void parseParameters(final String[] entries, final CommandInvoke command) throws InputParsingFailureException {
		for (int i = 1; i < entries.length; i += 2) {
			String parameter = entries[i];
			if (!parameter.startsWith("-") || parameter.length() < 2)
				throw new InputParsingFailureException(String.format(PARAMETER_EXPECTED, parameter));
			if (i + 1 == entries.length || entries[i + 1].startsWith("-"))
				throw new InputParsingFailureException(String.format(PARAMETER_VALUE_EXPECTED, parameter));
			command.addParameter(parameter.substring(1), entries[i + 1]);
		}
	}

	@Override
	public void insertNewLog(final String text, final boolean logTime) {
		insertNewLog(text, logTime, null);
	}

	@Override
	public void insertNewLog(final String text, final boolean logTime, final String color) {
		if (text == null) return;
		consoleTextData.insertNewLog(text, logTime, color);
		scrollToEnd = true;
		arrow.setVisible(false);
	}

	@Override
	public ConsoleCommandResult notifyCommandExecution(final ConsoleCommands command) {
		return notifyCommandExecution(command, null);
	}

	@Override
	public ConsoleCommandResult notifyCommandExecution(final ConsoleCommands command, final CommandParameter commandParameter) {
		boolean result = false;
		consoleCommandResult.clear();
		Optional<CommandParameter> optional = Optional.ofNullable(commandParameter);
		for (ConsoleEventsSubscriber sub : subscribers) {
			if (optional.isPresent()) {
				result |= sub.onCommandRun(command, consoleCommandResult, optional.get());
			} else {
				result |= sub.onCommandRun(command, consoleCommandResult);
			}
		}
		consoleCommandResult.setResult(result);
		return consoleCommandResult;
	}

	@Override
	public void act(final float delta) {
		super.act(delta);
		if (!active) return;
		if (scrollToEnd || scrollPane.isBottomEdge()) {
			scrollToEnd = true;
			scrollPane.setScrollPercentY(1);
			arrow.setVisible(false);
		} else if (!arrow.isVisible()) {
			arrow.setVisible(true);
		}
	}


	private void addTextView(final TextureRegionDrawable textBackgroundTexture, final int consoleHeight) {
		Label.LabelStyle textStyle = consoleTextData.getTextStyle();
		textStyle.background = textBackgroundTexture;
		float width = Gdx.graphics.getWidth() - PADDING * 2;
		float height = consoleHeight - (INPUT_HEIGHT);
		Label textView = new Label(consoleTextData.getStringBuilder(), textStyle);
		textView.setAlignment(Align.bottomLeft);
		textView.setName(TEXT_VIEW_NAME);
		textView.setWrap(true);
		scrollPane = new ScrollPane(textView);
		scrollPane.setTouchable(Touchable.disabled);
		Stack textWindowStack = new Stack(scrollPane);
		arrow = new Image(consoleTextures.getArrowTexture());
		arrow.setAlign(Align.bottomRight);
		textWindowStack.add(arrow);
		arrow.setScaling(Scaling.none);
		arrow.setFillParent(false);
		arrow.setVisible(false);
		add(textWindowStack).colspan(2).size(width, height).align(Align.bottomLeft).padRight(PADDING).padLeft(PADDING).row();
	}


	@Override
	public void activate() {
		if (active || !getActions().isEmpty()) return;
		getStage().setKeyboardFocus(getStage().getRoot().findActor(INPUT_FIELD_NAME));
		active = true;
		float amountY = -Gdx.graphics.getHeight() / 3f;
		addAction(Actions.moveBy(0, amountY, TRANSITION_DURATION, INTERPOLATION));
		setVisible(true);
		subscribers.forEach(ConsoleEventsSubscriber::onConsoleActivated);
	}

	@Override
	public void deactivate() {
		if (!active || !getActions().isEmpty()) return;
		active = false;
		float amountY = Gdx.graphics.getHeight() / 3f;
		MoveByAction move = Actions.moveBy(0, amountY, TRANSITION_DURATION, Interpolation.pow2);
		addAction(Actions.sequence(move, Actions.visible(false)));
		subscribers.forEach(ConsoleEventsSubscriber::onConsoleDeactivated);
		getStage().unfocusAll();
	}

	@Override
	public boolean isActive() {
		return active;
	}

	public void dispose() {
		consoleTextData.dispose();
		consoleTextures.dispose();
	}

	public void subscribeForEvents(final ConsoleEventsSubscriber subscriber) {
		if (subscribers.contains(subscriber, true)) return;
		subscribers.add(subscriber);
	}

	@Override
	public boolean keyDown(final int key) {
		boolean result = false;
		if (key == Input.Keys.GRAVE) {
			if (!active) {
				activate();
			}
			result = true;
		}
		return result;
	}

	@Override
	public boolean keyUp(final int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(final char character) {
		return false;
	}

	@Override
	public boolean touchDown(final int screenX, final int screenY, final int pointer, final int button) {
		return false;
	}

	@Override
	public boolean touchUp(final int screenX, final int screenY, final int pointer, final int button) {
		return false;
	}

	@Override
	public boolean touchDragged(final int screenX, final int screenY, final int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(final int screenX, final int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(final float amountX, final float amountY) {
		return false;
	}

}
