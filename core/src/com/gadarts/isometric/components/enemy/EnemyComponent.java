package com.gadarts.isometric.components.enemy;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.gadarts.isometric.components.GameComponent;
import com.gadarts.necromine.model.characters.Enemies;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnemyComponent implements GameComponent {
	public static final int ROAM_SOUND_INTERVAL_MINIMUM = 5000;
	public static final int ROAM_SOUND_INTERVAL_MAXIMUM = 10000;
	private long nextRoamSound;
	private Enemies enemyDefinition;
	private boolean awaken;
	private long lastTurn = -1;
	private int skill = 1;
	private long lastPrimaryAttack = -1;
	private Animation<TextureAtlas.AtlasRegion> bulletAnimation;

	public void init(final Enemies enemyDefinition, final int skill, final Animation<TextureAtlas.AtlasRegion> bulletAnimation) {
		calculateNextRoamSound();
		this.enemyDefinition = enemyDefinition;
		this.skill = skill;
		this.bulletAnimation = bulletAnimation;
	}

	@Override
	public void reset( ) {
		lastTurn = -1;
		lastPrimaryAttack = -1;
	}

	public void calculateNextRoamSound( ) {
		nextRoamSound = TimeUtils.millis() + MathUtils.random(ROAM_SOUND_INTERVAL_MINIMUM, ROAM_SOUND_INTERVAL_MAXIMUM);
	}

}
