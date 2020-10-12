package com.gadarts.isometric.utils.map;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.isometric.components.PlayerComponent;
import com.gadarts.isometric.components.character.CharacterAnimations;
import com.gadarts.isometric.components.character.CharacterComponent;
import com.gadarts.isometric.components.character.SpriteType;
import com.gadarts.isometric.systems.HudSystemImpl;
import com.gadarts.isometric.utils.EntityBuilder;
import com.gadarts.isometric.utils.assets.Assets;
import com.gadarts.isometric.utils.assets.Assets.Atlases;
import com.gadarts.isometric.utils.assets.GameAssetsManager;

import static com.badlogic.gdx.graphics.Texture.TextureWrap.Repeat;

/**
 * Creates the map.
 */
public final class MapBuilder {
	private static final Vector3 auxVector3_1 = new Vector3();
	private static final Vector3 auxVector3_2 = new Vector3();
	private static final Vector3 auxVector3_3 = new Vector3();
	private static final Vector3 auxVector3_4 = new Vector3();
	private static final Vector3 auxVector3_5 = new Vector3();
	public static final float BILLBOARD_Y = 0.6f;

	private final GameAssetsManager assetManager;
	private final PooledEngine engine;
	private final ModelBuilder modelBuilder;

	public MapBuilder(final GameAssetsManager assetManager, final PooledEngine engine) {
		this.assetManager = assetManager;
		this.engine = engine;
		this.modelBuilder = new ModelBuilder();
	}

	/**
	 * Test map.
	 *
	 * @return The map graph.
	 */
	public MapGraph createAndAddTestMap() {
		MapGraph map = new MapGraph(engine.getEntitiesFor(Family.all(CharacterComponent.class).get()));
		addTestFloor();
		createAndAdd3dCursor();
		addPlayer();
		addEnemyTest();
		return map;
	}

	private void addPlayer() {
		EntityBuilder entityBuilder = EntityBuilder.beginBuildingEntity(engine).addPlayerComponent();
		addCharBaseComponents(entityBuilder, Atlases.PLAYER, auxVector3_1.set(0.5f, BILLBOARD_Y, 0.5f), null);
		entityBuilder.finishAndAddToEngine();
	}

	private void addEnemyTest() {
		EntityBuilder entityBuilder = EntityBuilder.beginBuildingEntity(engine).addEnemyComponent();
		Entity player = engine.getEntitiesFor(Family.all(PlayerComponent.class).get()).first();
		addCharBaseComponents(entityBuilder, Atlases.ZEALOT, auxVector3_1.set(2.5f, BILLBOARD_Y, 2.5f), player);
		entityBuilder.finishAndAddToEngine();
	}

	private void addCharBaseComponents(final EntityBuilder entityBuilder,
									   final Atlases atlas,
									   final Vector3 position,
									   final Entity target) {
		CharacterAnimations animations = assetManager.get(atlas.name());
		entityBuilder.addCharacterComponent(CharacterComponent.Direction.SOUTH, SpriteType.IDLE, target)
				.addDecalComponent(animations, SpriteType.IDLE, CharacterComponent.Direction.SOUTH, position)
				.addAnimationComponent();
	}




	private void addTestFloor() {
		EntityBuilder.beginBuildingEntity(engine)
				.addModelInstanceComponent(createTestFloorModelInstance(modelBuilder), true)
				.addFloorComponent()
				.finishAndAddToEngine();
	}

	private void createAndAdd3dCursor() {
		Model model = modelTestCursor();
		EntityBuilder.beginBuildingEntity(engine)
				.addModelInstanceComponent(new ModelInstance(model), true)
				.addCursorComponent()
				.finishAndAddToEngine();
	}

	private Model modelTestCursor() {
		modelBuilder.begin();

		Material material = new Material(ColorAttribute.createDiffuse(HudSystemImpl.CURSOR_REGULAR));

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
		Texture floor = assetManager.getTexture(Assets.Textures.FloorTextures.FLOOR_0);
		floor.setWrap(Repeat, Repeat);
		Material material = new Material(TextureAttribute.createDiffuse(floor));
		material.id = "floor_test";
		return material;
	}
}
