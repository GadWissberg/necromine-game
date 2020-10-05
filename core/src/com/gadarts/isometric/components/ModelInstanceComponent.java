package com.gadarts.isometric.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;
import lombok.Getter;
import lombok.Setter;

@Getter
public class ModelInstanceComponent implements Component, Pool.Poolable {
	private static final Vector3 auxVector3_1 = new Vector3();
	private static final Vector3 auxVector3_2 = new Vector3();
	private ModelInstance modelInstance;

	@Setter
	private boolean visible;

	public void init(final ModelInstance modelInstance, final boolean visible) {
		this.modelInstance = modelInstance;
		this.visible = visible;
	}

	@Override
	public void reset() {
		modelInstance = null;
	}
}
