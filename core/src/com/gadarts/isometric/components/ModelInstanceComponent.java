package com.gadarts.isometric.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import lombok.Getter;

@Getter
public class ModelInstanceComponent implements Component {
	private ModelInstance modelInstance;

	public void init(final ModelInstance modelInstance) {
		this.modelInstance = modelInstance;
	}
}
