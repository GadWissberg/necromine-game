package com.gadarts.isometric.components.model;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import lombok.Getter;

@Getter
public class GameModelInstance extends ModelInstance {
	private final AdditionalRenderData additionalRenderData;

	public GameModelInstance(final Model model) {
		this(model, true);
	}

	public GameModelInstance(final Model model, final boolean affectedByLight) {
		super(model);
		this.additionalRenderData = new AdditionalRenderData(affectedByLight);
		calculateBoundingBox();
		userData = additionalRenderData;
	}

	public void calculateBoundingBox() {
		calculateBoundingBox(additionalRenderData.getBoundingBox());
	}
}
