package com.gadarts.isometric.components.model;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.collision.BoundingBox;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class AdditionalRenderData {
	List<Entity> nearbyLights = new ArrayList<>();
	BoundingBox boundingBox = new BoundingBox();
	boolean affectedByLight;

	public AdditionalRenderData(final boolean affectedByLight) {
		this.affectedByLight = affectedByLight;
	}
}
