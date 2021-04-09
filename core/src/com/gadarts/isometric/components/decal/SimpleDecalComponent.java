package com.gadarts.isometric.components.decal;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.gadarts.isometric.components.GameComponent;
import lombok.Getter;
import lombok.Setter;

@Getter
public class SimpleDecalComponent implements GameComponent {
	private Decal decal;
	private boolean billboard;

	@Setter
	private boolean affectedByFow;

	@Setter
	private boolean visible;

	@Override
	public void reset() {

	}

	//Optimize this - it creates an object each time.
	public void init(final Texture texture, final boolean visible) {
		init(new TextureRegion(texture), visible);
	}

	public void init(final Texture texture, final boolean visible, final boolean billboard) {
		init(new TextureRegion(texture), visible, billboard);
	}

	public void init(final TextureRegion textureRegion, final boolean visible) {
		init(textureRegion, visible, false);
	}

	public void init(final TextureRegion textureRegion, final boolean visible, final boolean billboard) {
		decal = Decal.newDecal(textureRegion, true);
		this.visible = visible;
		this.billboard = billboard;
	}
}
