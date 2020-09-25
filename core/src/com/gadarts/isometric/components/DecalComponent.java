package com.gadarts.isometric.components;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.gadarts.isometric.utils.Assets;
import lombok.Getter;

import java.util.HashMap;

@Getter
public class DecalComponent implements GameComponent {

	private Decal decal;
	private HashMap<Assets.CharacterDirectionsRegions, Animation<TextureAtlas.AtlasRegion>> animations;
	private Assets.CharacterDirectionsRegions currentRegion;

	@Override
	public void reset() {
		animations.clear();
	}

	public void init(final HashMap<Assets.CharacterDirectionsRegions, Animation<TextureAtlas.AtlasRegion>> animations,
					 final Assets.CharacterDirectionsRegions region) {
		this.animations = animations;
		currentRegion = region;
		decal = Decal.newDecal(animations.get(region).getKeyFrames()[0], true);//Optimize this - it creates an object each time.
	}


	public void initializeRegion(final Assets.CharacterDirectionsRegions region) {
		this.currentRegion = region;
		decal.setTextureRegion(animations.get(region).getKeyFrames()[0]);
	}
}
