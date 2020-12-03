package com.gadarts.isometric.components.model;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.collision.BoundingBox;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class GameModelInstance extends ModelInstance {
	private final BoundingBox boundingBox = new BoundingBox();
	private final List<Entity> nearbyLights = new ArrayList<>();

	public GameModelInstance(final Model model) {
		super(model);
		calculateBoundingBox(boundingBox);
	}
}
