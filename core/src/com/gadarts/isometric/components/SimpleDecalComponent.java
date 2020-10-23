package com.gadarts.isometric.components;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import lombok.Getter;
import lombok.Setter;

@Getter
public class SimpleDecalComponent implements GameComponent {
	private Decal decal;

	@Setter
	private boolean visible;

	@Override
	public void reset() {

	}

	public void init(final Texture texture, final boolean visible) {
		decal = Decal.newDecal(new TextureRegion(texture), true);//Optimize this - it creates an object each time.
		this.visible = visible;
	}
}
