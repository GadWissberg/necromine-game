package com.gadarts.isometric.components.decal.simple;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.utils.Pools;
import com.gadarts.isometric.components.GameComponent;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class SimpleDecalComponent implements GameComponent {
	private Decal decal;
	private boolean billboard;
	private boolean animatedByAnimationComponent;

	@Setter
	private boolean affectedByFow;

	@Setter
	private boolean visible;

	@Getter
	private final List<RelatedDecal> relatedDecals = new ArrayList<>();

	@Override
	public void reset() {

	}

	//Optimize this - it creates an object each time.
	public void init(final Texture texture, final boolean visible) {
		init(new TextureRegion(texture), visible);
	}

	public void init(final Texture texture, final boolean visible, final boolean billboard) {
		init(new TextureRegion(texture), visible, billboard, animatedByAnimationComponent);
	}

	public void init(final TextureRegion textureRegion, final boolean visible) {
		init(textureRegion, visible, false, animatedByAnimationComponent);
	}

	public void init(final TextureRegion textureRegion,
					 final boolean visible,
					 final boolean billboard,
					 final boolean animatedByAnimationComponent) {
		this.animatedByAnimationComponent = animatedByAnimationComponent;
		decal = Decal.newDecal(textureRegion, true);
		this.visible = visible;
		this.billboard = billboard;
		if (relatedDecals.size() > 0) {
			for (Decal decal : relatedDecals) {
				Pools.get(Decal.class).free(decal);
			}
			relatedDecals.clear();
		}
	}

	public void addRelatedDecal(final RelatedDecal decal) {
		relatedDecals.add(decal);
	}
}
