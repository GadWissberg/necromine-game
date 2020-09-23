package com.gadarts.isometric.components;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import lombok.Getter;

@Getter
public class DecalComponent implements GameComponent {

	private Decal decal;

	@Override
	public void reset() {

	}

	public void init(final TextureAtlas.AtlasRegion region) {
		decal = Decal.newDecal(region, true);//Optimize this - it creates an object each time.
	}
}
