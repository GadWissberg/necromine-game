package com.gadarts.isometric.components.character;

import com.gadarts.necromine.model.characters.Agility;
import lombok.Getter;

@Getter
public class CharacterSkills {

	private final CharacterHealthData healthData = new CharacterHealthData();
	private Agility agility;

	public void applyParameters(final CharacterSkillsParameters skills) {
		this.healthData.init(skills.getHealth());
		this.agility = skills.getAgility();
	}
}
