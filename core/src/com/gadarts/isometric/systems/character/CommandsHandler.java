package com.gadarts.isometric.systems.character;

import com.badlogic.ashley.core.Entity;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.character.CharacterComponent;
import com.gadarts.isometric.components.character.SpriteType;
import com.gadarts.isometric.utils.SoundPlayer;
import com.gadarts.isometric.utils.map.MapGraph;
import com.gadarts.isometric.utils.map.MapGraphNode;
import lombok.Getter;

import java.util.List;

import static com.gadarts.isometric.components.character.SpriteType.PAIN;

public class CommandsHandler {
	private final CharacterSystemGraphData graphData;
	private final List<CharacterSystemEventsSubscriber> subscribers;
	private final SoundPlayer soundPlayer;
	private final MapGraph map;

	@Getter
	private CharacterCommand currentCommand;

	public CommandsHandler(final CharacterSystemGraphData graphData,
						   final List<CharacterSystemEventsSubscriber> subscribers,
						   final SoundPlayer soundPlayer,
						   final MapGraph map) {
		this.graphData = graphData;
		this.subscribers = subscribers;
		this.soundPlayer = soundPlayer;
		this.map = map;
	}

	/**
	 * Applies a given command on the given character.
	 *
	 * @param command
	 * @param character
	 */
	@SuppressWarnings("JavaDoc")
	public void applyCommand(final CharacterCommand command, final Entity character) {
		currentCommand = command;
		currentCommand.setStarted(false);
		if (ComponentsMapper.character.get(character).getCharacterSpriteData().getSpriteType() != PAIN) {
			beginProcessingCommand(command, character);
		}
	}

	private void beginProcessingCommand(final CharacterCommand command, final Entity character) {
		currentCommand.setStarted(true);
		graphData.getCurrentPath().clear();
		if (command.getType().isRequiresMovement()) {
			graphData.getCurrentPath().nodes.addAll(command.getPath().nodes);
		}
		if (graphData.getCurrentPath().nodes.size > 1) {
			commandSet(command, character);
		} else {
			destinationReached(character);
		}
	}

	private void executeActionsAfterDestinationReached(final Entity character) {
		for (CharacterSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onDestinationReached(character);
		}
		currentCommand.getType()
				.getToDoAfterDestinationReached()
				.run(character, map, soundPlayer, currentCommand.getAdditionalData());
	}

	public void destinationReached(final Entity character) {
		if (currentCommand.getType().getToDoAfterDestinationReached() != null) {
			executeActionsAfterDestinationReached(character);
		} else {
			commandDone(character);
		}
	}

	void commandDone(final Entity character) {
		graphData.getCurrentPath().clear();
		CharacterComponent characterComponent = ComponentsMapper.character.get(character);
		characterComponent.setMotivation(null);
		characterComponent.getCharacterSpriteData().setSpriteType(SpriteType.IDLE);
		currentCommand = null;
		for (CharacterSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onCommandDone(character);
		}
	}

	private void commandSet(final CharacterCommand command, final Entity character) {
		for (CharacterSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onNewCommandSet(command);
		}
		initDestinationNode(ComponentsMapper.character.get(character), graphData.getCurrentPath().get(1));
	}

	void initDestinationNode(final CharacterComponent characterComponent,
							 final MapGraphNode destNode) {
		characterComponent.getRotationData().setRotating(true);
		characterComponent.setDestinationNode(destNode);
	}
}
