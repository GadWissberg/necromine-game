package com.gadarts.isometric.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;
import com.gadarts.isometric.components.model.GameModelInstance;
import lombok.Getter;
import lombok.Setter;

@Getter
public class ModelInstanceComponent implements Component, Pool.Poolable {
	private static final Vector3 auxVector3_1 = new Vector3();
	private static final Vector3 auxVector3_2 = new Vector3();

	private GameModelInstance modelInstance;
	private ColorAttribute colorAttribute;

	@Setter
	private boolean visible;


	public void init(final GameModelInstance modelInstance, final boolean visible) {
		this.modelInstance = modelInstance;
		this.visible = visible;
		this.colorAttribute = null;
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
