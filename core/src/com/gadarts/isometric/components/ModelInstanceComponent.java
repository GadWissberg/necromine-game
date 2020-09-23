package com.gadarts.isometric.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;
import lombok.Getter;

@Getter
public class ModelInstanceComponent implements Component, Pool.Poolable {
	private static final Vector3 auxVector3_1 = new Vector3();
	private static final Vector3 auxVector3_2 = new Vector3();
	private ModelInstance modelInstance;

	public void init(final ModelInstance modelInstance) {
		this.modelInstance = modelInstance;
	}

	public void init(final ModelInstance modelInstance, final boolean isBillboard) {
		this.modelInstance = modelInstance;
	}

	@Override
	public void reset() {
		modelInstance = null;
	}
}
