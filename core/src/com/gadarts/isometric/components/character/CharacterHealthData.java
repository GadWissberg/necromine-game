package com.gadarts.isometric.components.character;

import com.badlogic.gdx.utils.TimeUtils;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CharacterHealthData {
	private int hp;
	private long lastDamage;

	public void dealDamage(final int damagePoints) {
		hp -= damagePoints;
		lastDamage = TimeUtils.millis();
	}

	public void reset() {
		hp = 0;
		lastDamage = 0;
	}
}
