package com.gadarts.isometric.systems.character.commands;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.character.CharacterComponent;
import com.gadarts.isometric.systems.character.CharacterSystemEventsSubscriber;
import com.gadarts.isometric.systems.character.CharacterSystemGraphData;
import com.gadarts.isometric.utils.SoundPlayer;
import com.gadarts.isometric.utils.map.MapGraph;
import com.gadarts.isometric.utils.map.MapGraphNode;
import com.gadarts.necromine.model.characters.SpriteType;
import com.gadarts.necromine.model.characters.attributes.Agility;
import lombok.Getter;

import java.util.List;

import static com.gadarts.necromine.model.characters.SpriteType.PAIN;


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
			applyMovementOfCommandWithAgility(command, character);
		}
		if (graphData.getCurrentPath().nodes.size > 1) {
			commandSet(command, character);
		} else {
			destinationReached(character);
		}
	}

	private void applyMovementOfCommandWithAgility(final CharacterCommand command, final Entity character) {
		Agility agility = ComponentsMapper.character.get(character).getSkills().getAgility();
		Array<MapGraphNode> nodes = command.getPath().nodes;
		int agilityValue = agility.getValue();
		if (nodes.size > agilityValue) {
			nodes.removeRange(agilityValue, nodes.size - 1);
		}
		graphData.getCurrentPath().nodes.addAll(nodes);
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

	public void commandDone(final Entity character) {
		graphData.getCurrentPath().clear();
		CharacterComponent characterComponent = ComponentsMapper.character.get(character);
		characterComponent.setMotivation(null);
		characterComponent.getCharacterSpriteData().setSpriteType(SpriteType.IDLE);
		CharacterCommand lastCommand = currentCommand;
		currentCommand = null;
		for (CharacterSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onCharacterCommandDone(character, lastCommand);
		}
	}

	private void commandSet(final CharacterCommand command, final Entity character) {
		for (CharacterSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onNewCharacterCommandSet(command);
		}
		initDestinationNode(ComponentsMapper.character.get(character), graphData.getCurrentPath().get(1));
	}

	public void initDestinationNode(final CharacterComponent characterComponent,
									final MapGraphNode destNode) {
		characterComponent.getRotationData().setRotating(true);
		characterComponent.setDestinationNode(destNode);
	}
}
