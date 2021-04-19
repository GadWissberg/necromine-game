package com.gadarts.isometric.components.character;

import com.gadarts.isometric.components.character.data.CharacterHealthData;
import com.gadarts.necromine.model.characters.Agility;
import com.gadarts.necromine.model.characters.Strength;
import lombok.Getter;

@Getter
public class CharacterSkills {

	private final CharacterHealthData healthData = new CharacterHealthData();
	private Agility agility;
	private Strength strength;

	public void applyParameters(final CharacterSkillsParameters skills) {
		this.healthData.init(skills.getHealth());
		this.agility = skills.getAgility();
		this.strength = skills.getStrength();
	}
}
