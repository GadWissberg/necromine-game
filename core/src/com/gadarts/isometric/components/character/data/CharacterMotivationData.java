package com.gadarts.isometric.components.character.data;

import com.gadarts.isometric.components.character.CharacterMotivation;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CharacterMotivationData {
	private CharacterMotivation motivation;
	private Object motivationAdditionalData;

	public void reset() {
		motivation = null;
		motivationAdditionalData = null;
	}
}
