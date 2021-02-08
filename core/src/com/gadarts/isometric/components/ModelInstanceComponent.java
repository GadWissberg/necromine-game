package com.gadarts.isometric.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Pool;
import com.gadarts.isometric.components.model.GameModelInstance;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
public class ModelInstanceComponent implements Component, Pool.Poolable {
	private static final Vector3 auxVector3_1 = new Vector3();
	private static final Vector3 auxVector3_2 = new Vector3();

	@Getter(AccessLevel.NONE)
	private final BoundingBox boundingBox = new BoundingBox();

	@Setter
	private boolean visible;

	private GameModelInstance modelInstance;
	private ColorAttribute colorAttribute;
	private boolean castShadow;

	public BoundingBox getBoundingBox(final BoundingBox auxBoundBox) {
		return auxBoundBox.set(boundingBox);
	}

	public void init(final GameModelInstance modelInstance,
					 final boolean visible,
					 final boolean castShadow) {
		this.modelInstance = modelInstance;
		this.visible = visible;
		this.colorAttribute = null;
		this.castShadow = castShadow;
		modelInstance.calculateBoundingBox(boundingBox);
	}

	public ColorAttribute getColorAttribute() {
		if (colorAttribute == null) {
			colorAttribute = modelInstance.materials.get(0).get(ColorAttribute.class, ColorAttribute.Diffuse);
		}
		return colorAttribute;
	}

	@Override
	public void reset() {
		modelInstance = null;
	}

}
