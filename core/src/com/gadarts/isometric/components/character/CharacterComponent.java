package com.gadarts.isometric.components.character;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.gadarts.isometric.components.GameComponent;
import com.gadarts.isometric.components.character.data.CharacterMotivationData;
import com.gadarts.isometric.components.character.data.CharacterRotationData;
import com.gadarts.isometric.components.character.data.CharacterSoundData;
import com.gadarts.isometric.components.character.data.CharacterSpriteData;
import com.gadarts.isometric.utils.map.MapGraphNode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CharacterComponent implements GameComponent {

	private static final Vector2 auxVector = new Vector2();

	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private MapGraphNode destinationNode;

	private CharacterMotivationData motivationData = new CharacterMotivationData();
	private Entity target;
	private CharacterRotationData rotationData = new CharacterRotationData();
	private CharacterSpriteData characterSpriteData;
	private CharacterSoundData soundData = new CharacterSoundData();
	private CharacterSkills skills = new CharacterSkills();

	public MapGraphNode getDestinationNode() {
		return destinationNode;
	}

	public void setDestinationNode(final MapGraphNode newValue) {
		this.destinationNode = newValue;
	}

	public void setMotivation(final CharacterMotivation characterMotivation) {
		setMotivation(characterMotivation, null);
	}

	public void setMotivation(final CharacterMotivation characterMotivation, final Object additionalData) {
		motivationData.setMotivation(characterMotivation);
		motivationData.setMotivationAdditionalData(additionalData);
	}

	@Override
	public void reset() {
		destinationNode = null;
		motivationData.reset();
		target = null;
		rotationData.reset();
	}

	public void init(final CharacterSpriteData characterSpriteData,
					 final CharacterSoundData soundData,
					 final CharacterSkillsParameters skills) {
		this.characterSpriteData = characterSpriteData;
		this.skills.applyParameters(skills);
		this.soundData.set(soundData);
	}

	public void dealDamage(final int damagePoints) {
		skills.getHealthData().dealDamage(damagePoints);
	}

}
