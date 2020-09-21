package com.gadarts.isometric.utils;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.isometric.components.ModelInstanceComponent;

import java.io.File;

import static com.badlogic.gdx.graphics.Texture.TextureWrap.Repeat;

public class MapBuilder {
	private static final Vector3 auxVector3_1 = new Vector3();
	private static final Vector3 auxVector3_2 = new Vector3();
	private static final Vector3 auxVector3_3 = new Vector3();
	private static final Vector3 auxVector3_4 = new Vector3();
	private static final Vector3 auxVector3_5 = new Vector3();
	private final GameAssetsManager assetManager;
	private final PooledEngine engine;

	public MapBuilder(final GameAssetsManager assetManager, final PooledEngine engine) {
		this.assetManager = assetManager;
		this.engine = engine;
	}

	public void createTestMap() {
		ModelBuilder modelBuilder = new ModelBuilder();
		ModelInstance modelInstance = createTestFloorModelInstance(modelBuilder);
		Entity testFloor = engine.createEntity();
		ModelInstanceComponent component = engine.createComponent(ModelInstanceComponent.class);
		component.init(modelInstance);
		testFloor.add(component);
		engine.addEntity(testFloor);
	}

	private ModelInstance createTestFloorModelInstance(final ModelBuilder modelBuilder) {
		modelBuilder.begin();
		MeshPartBuilder meshPartBuilder = modelBuilder.part("test_floor",
				GL20.GL_TRIANGLES,
				Usage.Position | Usage.Normal | Usage.TextureCoordinates,
				createTestFloorMaterial());
		createFloorTestFace(meshPartBuilder);
		Model testFloorModel = modelBuilder.end();
		return new ModelInstance(testFloorModel);
	}

	private void createFloorTestFace(final MeshPartBuilder meshPartBuilder) {
		meshPartBuilder.setUVRange(0,0,4,4);
		meshPartBuilder.rect(
				auxVector3_1.set(4, 0, 4),
				auxVector3_2.set(4, 0, 0),
				auxVector3_3.set(0, 0, 0),
				auxVector3_4.set(0, 0, 4),
				auxVector3_5.set(0, 1, 0));
	}

	private Material createTestFloorMaterial() {
		Texture skyTexture = assetManager.get("textures/floor.png");
		skyTexture.setWrap(Repeat, Repeat);
		Material material = new Material(TextureAttribute.createDiffuse(skyTexture));
		material.id = "floor_test";
		return material;

	}
}
