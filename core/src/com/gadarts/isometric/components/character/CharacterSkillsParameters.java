package com.gadarts.isometric.components.character;

import com.gadarts.necromine.model.characters.Agility;
import lombok.Getter;

@Getter
public class CharacterSkillsParameters {
	private final int health;
	private final Agility agility;

	public CharacterSkillsParameters(final int health, final Agility agility) {
		this.health = health;
		this.agility = agility;
	}
}
