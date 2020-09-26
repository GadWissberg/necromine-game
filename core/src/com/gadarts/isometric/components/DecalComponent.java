package com.gadarts.isometric.components;

import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.gadarts.isometric.utils.Assets;
import lombok.Getter;

@Getter
public class DecalComponent implements GameComponent {

	private Decal decal;
	private CharacterAnimations animations;
	private Assets.CharacterDirRegions currentRegion;

	@Override
	public void reset() {
		animations.clear();
	}

	public void init(final CharacterAnimations animations,
					 final Assets.CharacterDirRegions region) {
		this.animations = animations;
		currentRegion = region;
		decal = Decal.newDecal(animations.get(region).getKeyFrames()[0], true);//Optimize this - it creates an object each time.
	}


	public void initializeRegion(final Assets.CharacterDirRegions region) {
		this.currentRegion = region;
		decal.setTextureRegion(animations.get(region).getKeyFrames()[0]);
	}
}
