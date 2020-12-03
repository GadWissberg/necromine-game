package com.gadarts.isometric.components.model;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class GameModelInstance extends ModelInstance {
	List<Entity> nearbyLights = new ArrayList<>();

	public GameModelInstance(final Model model) {
		super(model);
	}
}
