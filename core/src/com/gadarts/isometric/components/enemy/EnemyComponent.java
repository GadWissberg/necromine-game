package com.gadarts.isometric.components.enemy;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.gadarts.isometric.components.GameComponent;
import com.gadarts.isometric.utils.map.MapGraphNode;
import com.gadarts.necromine.model.characters.enemies.Enemies;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnemyComponent implements GameComponent {
	public static final int ROAM_SOUND_INTERVAL_MINIMUM = 5000;
	public static final int ROAM_SOUND_INTERVAL_MAXIMUM = 10000;

	@Setter(AccessLevel.NONE)
	private MapGraphNode targetLastVisibleNode;
	private long nextRoamSound;
	private Enemies enemyDefinition;

	private EnemyStatus status;
	private EnemyTimeStamps timeStamps = new EnemyTimeStamps();
	private int skill = 1;
	private Animation<TextureAtlas.AtlasRegion> bulletAnimation;
	private long iconDisplayInFlowerTimeStamp;
	private boolean displayIconInFlower;

	public void setTargetLastVisibleNode(final MapGraphNode coords) {
		targetLastVisibleNode = coords;
	}

	public void init(final Enemies enemyDefinition,
					 final int skill,
					 final Animation<TextureAtlas.AtlasRegion> bulletAnimation) {
		calculateNextRoamSound();
		this.enemyDefinition = enemyDefinition;
		this.skill = skill;
		this.bulletAnimation = bulletAnimation;
		timeStamps.reset();
		targetLastVisibleNode = null;
		status = EnemyStatus.IDLE;
		iconDisplayInFlowerTimeStamp = 0;
	}

	@Override
	public void reset( ) {

	}

	public void calculateNextRoamSound( ) {
		nextRoamSound = TimeUtils.millis() + MathUtils.random(ROAM_SOUND_INTERVAL_MINIMUM, ROAM_SOUND_INTERVAL_MAXIMUM);
	}

}
