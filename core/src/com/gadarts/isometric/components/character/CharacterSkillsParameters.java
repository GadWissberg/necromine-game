package com.gadarts.isometric.components.character;

import com.gadarts.necromine.model.characters.attributes.Agility;
import com.gadarts.necromine.model.characters.attributes.Strength;
import lombok.Getter;

@Getter
public class CharacterSkillsParameters {
	private final int health;
	private final Agility agility;
	private final Strength strength;

	public CharacterSkillsParameters(final int health, final Agility agility, final Strength strength) {
		this.health = health;
		this.agility = agility;
		this.strength = strength;
	}
}
