package com.gadarts.isometric.utils;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.isometric.components.*;
import com.gadarts.isometric.utils.Assets.CharacterDirectionsRegions;

import java.util.Arrays;
import java.util.HashMap;

import static com.badlogic.gdx.graphics.Texture.TextureWrap.Repeat;

public class MapBuilder {
	private static final Vector3 auxVector3_1 = new Vector3();
	private static final Vector3 auxVector3_2 = new Vector3();
	private static final Vector3 auxVector3_3 = new Vector3();
	private static final Vector3 auxVector3_4 = new Vector3();
	private static final Vector3 auxVector3_5 = new Vector3();
	private final GameAssetsManager assetManager;
	private final PooledEngine engine;
	private final ModelBuilder modelBuilder;

	public MapBuilder(final GameAssetsManager assetManager, final PooledEngine engine) {
		this.assetManager = assetManager;
		this.engine = engine;
		this.modelBuilder = new ModelBuilder();
	}

	public void createAndAddTestMap() {
		addTestFloor();
		createAndAdd3dCursor();
		addPlayer();
	}

	private void addPlayer() {
		Entity entity = engine.createEntity();
		TextureAtlas playerAtlas = assetManager.getAtlas(Assets.Atlases.PLAYER);
		entity.add(engine.createComponent(PlayerComponent.class));
		DecalComponent decalComponent = engine.createComponent(DecalComponent.class);

		HashMap<CharacterDirectionsRegions, Animation<TextureAtlas.AtlasRegion>> playerAnimations = new HashMap<>();
		Arrays.stream(CharacterDirectionsRegions.values()).forEach(dir -> {
			Animation<TextureAtlas.AtlasRegion> animation = new Animation<>(1, playerAtlas.findRegions(dir.getRegionName()));
			playerAnimations.put(dir, animation);
		});


		decalComponent.init(playerAnimations, CharacterDirectionsRegions.SOUTH_IDLE);
		Decal decal = decalComponent.getDecal();
		decal.setPosition(1, 0.3f, 1);
		decal.setScale(0.01f);
		CharacterComponent characterComponent = engine.createComponent(CharacterComponent.class);
		characterComponent.init(CharacterComponent.Direction.SOUTH);
		AnimationComponent animationComponent = engine.createComponent(AnimationComponent.class);
		animationComponent.init(0.4f, decalComponent.getAnimations().get(CharacterDirectionsRegions.SOUTH_IDLE));
		entity.add(animationComponent);
		entity.add(characterComponent);
		entity.add(decalComponent);
		engine.addEntity(entity);
	}

	private Model createBillboardModel(final TextureAtlas.AtlasRegion region) {
		modelBuilder.begin();

		Material material = new Material(TextureAttribute.createDiffuse(region));

		MeshPartBuilder meshPartBuilder = modelBuilder.part(
				"player",
				GL20.GL_TRIANGLES,
				Usage.Position | Usage.Normal | Usage.TextureCoordinates,
				material);

		createRect(meshPartBuilder, 1, 1, -0.5f, -0.5f);

		return modelBuilder.end();
	}

	private void addTestFloor() {
		Entity testFloor = engine.createEntity();
		ModelInstanceComponent component = engine.createComponent(ModelInstanceComponent.class);
		component.init(createTestFloorModelInstance(modelBuilder));
		testFloor.add(component);
		engine.addEntity(testFloor);
	}

	private void createAndAdd3dCursor() {
		Model model = modelTestCursor();
		ModelInstanceComponent modelInstanceComponent = engine.createComponent(ModelInstanceComponent.class);
		modelInstanceComponent.init(new ModelInstance(model));
		CursorComponent cursorComponent = engine.createComponent(CursorComponent.class);
		Entity entity = engine.createEntity();
		entity.add(modelInstanceComponent);
		entity.add(cursorComponent);
		engine.addEntity(entity);
	}

	private Model modelTestCursor() {
		modelBuilder.begin();

		Material material = new Material(ColorAttribute.createDiffuse(Color.YELLOW));

		MeshPartBuilder meshPartBuilder = modelBuilder.part(
				"test_cursor",
				GL20.GL_LINES,
				Usage.Position | Usage.Normal,
				material);

		meshPartBuilder.rect(
				auxVector3_1.set(1, 0, 1),
				auxVector3_2.set(1, 0, 0),
				auxVector3_3.set(0, 0, 0),
				auxVector3_4.set(0, 0, 1),
				auxVector3_5.set(0, -1, 0));

		meshPartBuilder.rect(
				auxVector3_1.set(1, 1.5f, 1),
				auxVector3_2.set(1, 1.5f, 0),
				auxVector3_3.set(0, 1.5f, 0),
				auxVector3_4.set(0, 1.5f, 1),
				auxVector3_5.set(0, 1, 0));

		meshPartBuilder.rect(
				auxVector3_1.set(1, 0, 0),
				auxVector3_2.set(1, 1.5f, 0),
				auxVector3_3.set(1, 1.5f, 1),
				auxVector3_4.set(1, 0, 1),
				auxVector3_5.set(1, 0, 0));

		meshPartBuilder.rect(
				auxVector3_1.set(1, 0, 1),
				auxVector3_2.set(1, 1.5f, 1),
				auxVector3_3.set(0, 1.5f, 1),
				auxVector3_4.set(0, 0, 1),
				auxVector3_5.set(0, 0, 1));

		meshPartBuilder.rect(
				auxVector3_1.set(0, 0, 1),
				auxVector3_2.set(0, 1.5f, 1),
				auxVector3_3.set(0, 1.5f, 0),
				auxVector3_4.set(0, 0, 0),
				auxVector3_5.set(-1, 0, 0));

		meshPartBuilder.rect(
				auxVector3_1.set(0, 0, 0),
				auxVector3_2.set(0, 1.5f, 0),
				auxVector3_3.set(1, 1.5f, 0),
				auxVector3_4.set(1, 0, 0),
				auxVector3_5.set(0, 0, -1));

		return modelBuilder.end();
	}

	private ModelInstance createTestFloorModelInstance(final ModelBuilder modelBuilder) {
		modelBuilder.begin();
		MeshPartBuilder meshPartBuilder = modelBuilder.part("test_floor",
				GL20.GL_TRIANGLES,
				Usage.Position | Usage.Normal | Usage.TextureCoordinates,
				createTestFloorMaterial());
		createRect(meshPartBuilder, 4, 4, 0, 0);
		Model testFloorModel = modelBuilder.end();
		return new ModelInstance(testFloorModel);
	}

	private void createRect(final MeshPartBuilder meshPartBuilder,
							final int size,
							final int uv,
							final float xOffset,
							final float zOffset) {
		meshPartBuilder.setUVRange(0, 0, uv, uv);
		meshPartBuilder.rect(
				auxVector3_4.set(xOffset, 0, size + zOffset),
				auxVector3_1.set(size + xOffset, 0, size + zOffset),
				auxVector3_2.set(size + xOffset, 0, zOffset),
				auxVector3_3.set(xOffset, 0, zOffset),
				auxVector3_5.set(0, 1, 0));
	}

	private Material createTestFloorMaterial() {
		Texture floor = assetManager.get("textures/floor.png");
		floor.setWrap(Repeat, Repeat);
		Material material = new Material(TextureAttribute.createDiffuse(floor));
		material.id = "floor_test";
		return material;

	}
}
