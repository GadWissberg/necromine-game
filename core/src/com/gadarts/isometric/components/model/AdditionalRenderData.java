package com.gadarts.isometric.components.model;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.collision.BoundingBox;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * More data needed mainly for shader.
 */
@Getter
public class AdditionalRenderData {
	private final List<Entity> nearbyLights = new ArrayList<>();
	private final BoundingBox boundingBox = new BoundingBox();
	private final boolean affectedByLight;

	@Setter
	private boolean applyAmbientOcclusion;

	@Setter
	private Color colorWhenOutside;

	public AdditionalRenderData(final boolean affectedByLight,
								final BoundingBox boundingBox,
								final Color colorWhenOutside) {
		this.affectedByLight = affectedByLight;
		this.boundingBox.set(boundingBox);
		this.colorWhenOutside = colorWhenOutside;
	}
}
