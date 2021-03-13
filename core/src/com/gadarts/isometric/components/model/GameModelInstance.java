package com.gadarts.isometric.components.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.collision.BoundingBox;
import lombok.Getter;

@Getter
public class GameModelInstance extends ModelInstance {
	private final AdditionalRenderData additionalRenderData;

	public GameModelInstance(final ModelInstance modelInstance,
							 final BoundingBox boundingBox,
							 final boolean affectedByLight,
							 final Color colorWhenOutside) {
		super(modelInstance);
		this.additionalRenderData = new AdditionalRenderData(affectedByLight, boundingBox, colorWhenOutside);
	}

	public GameModelInstance(final Model model, final BoundingBox boundingBox) {
		this(model, boundingBox, true);
	}

	public GameModelInstance(final Model model, final BoundingBox boundingBox, final boolean affectedByLight) {
		super(model);
		this.additionalRenderData = new AdditionalRenderData(affectedByLight, boundingBox, Color.BLACK);
	}

}
