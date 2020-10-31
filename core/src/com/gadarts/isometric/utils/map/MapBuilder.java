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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.isometric.components.ObstacleComponent;
import com.gadarts.isometric.components.PlayerComponent;
import com.gadarts.isometric.components.WallComponent;
import com.gadarts.isometric.components.character.CharacterAnimations;
import com.gadarts.isometric.components.character.CharacterComponent;
import com.gadarts.isometric.components.character.SpriteType;
import com.gadarts.isometric.systems.hud.HudSystemImpl;
import com.gadarts.isometric.utils.EntityBuilder;
import com.gadarts.isometric.utils.assets.Assets;
import com.gadarts.isometric.utils.assets.Assets.Atlases;
import com.gadarts.isometric.utils.assets.GameAssetsManager;

import static com.badlogic.gdx.graphics.Texture.TextureWrap.Repeat;

/**
 * Creates the map.
 */
public final class MapBuilder {
	private static final float BILLBOARD_Y = 0.6f;
	private static final Vector2 auxVector2_1 = new Vector2();
	private static final Vector2 auxVector2_2 = new Vector2();
	private static final Vector3 auxVector3_1 = new Vector3();
	private static final Vector3 auxVector3_2 = new Vector3();
	private static final Vector3 auxVector3_3 = new Vector3();
	private static final Vector3 auxVector3_4 = new Vector3();
	private static final Vector3 auxVector3_5 = new Vector3();

	private final GameAssetsManager assetManager;
	private final PooledEngine engine;
	private final ModelBuilder modelBuilder;
	private Model testFloorModel3_3;
	private Model testFloorModel1_2;

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
		testFloorModel3_3 = createTestFloorModel(modelBuilder, 3, 3, 3, 3);
		testFloorModel1_2 = createTestFloorModel(modelBuilder, 1, 2, 1, 3);
		createAndAdd3dCursor();
		addPlayer();
		addEnemyTest();
		addWeaponTest();
		return createTestMap();
	}

	private void addWeaponTest() {
		ModelInstance modelInstance = new ModelInstance(assetManager.getModel(Assets.Models.COLT));
		modelInstance.transform.setTranslation(auxVector3_1.set(1.5f, 0, 2.5f));
		EntityBuilder.beginBuildingEntity(engine)
				.addModelInstanceComponent(modelInstance, true)
				.addPickUpComponent()
				.finishAndAddToEngine();
	}

	private MapGraph createTestMap() {
		addTestWalls();
		addObstacles();
		addTestFloor(auxVector3_1.setZero(), testFloorModel3_3);
		addTestFloor(auxVector3_1.set(0, 0, 3), testFloorModel3_3);
		addTestFloor(auxVector3_1.set(3, 0, 0), testFloorModel3_3);
		addTestFloor(auxVector3_1.set(3, 0, 3), testFloorModel3_3);
		addTestFloor(auxVector3_1.set(2, 0, 6), testFloorModel1_2);
		return new MapGraph(
				engine.getEntitiesFor(Family.all(CharacterComponent.class).get()),
				engine.getEntitiesFor(Family.all(WallComponent.class).get()),
				engine.getEntitiesFor(Family.all(ObstacleComponent.class).get()),
				engine);
	}

	private void addObstacles() {
		addTestObstacle(1, 1, Assets.Models.PILLAR, 0, true);
		addTestObstacle(1, 3, Assets.Models.PILLAR, 0, true);
		addTestObstacle(2, 6, Assets.Models.CAVE_SUPPORTER_3, 90, false);
	}

	private void addTestObstacle(final int x, final int y, final Assets.Models model, final int rotation, final boolean blockPath) {
		ModelInstance modelInstance = new ModelInstance(assetManager.getModel(model));
		modelInstance.transform.setTranslation(x, 0, y);
		modelInstance.transform.rotate(Vector3.Y, rotation);
		EntityBuilder.beginBuildingEntity(engine)
				.addModelInstanceComponent(modelInstance, true)
				.addObstacleComponent(x, y, blockPath)
				.finishAndAddToEngine();
	}

	private void addTestWalls() {
		auxVector2_1.set(-1, -1);
		addTestWall(auxVector3_1.setZero(), 0, auxVector2_1, auxVector2_1, Assets.Models.WALL_1);
		addTestWall(auxVector3_1.set(4, 0, 0), 0, auxVector2_1, auxVector2_1, Assets.Models.WALL_2);
		addTestWall(auxVector3_1.set(0, 0, 4), 90, auxVector2_1, auxVector2_1, Assets.Models.WALL_1);
		addTestWall(auxVector3_1.set(0, 0, 6), 90, auxVector2_1, auxVector2_1, Assets.Models.WALL_2);
		addTestWall(auxVector3_1.set(2, 0, 8), 90, auxVector2_1.set(1, 6), auxVector2_1.set(1, 7), Assets.Models.WALL_2);
		addTestWall(auxVector3_1.set(2, 0, 6), 180, auxVector2_1.set(0, 6), auxVector2_2.set(1, 6), Assets.Models.WALL_2);
		addTestWall(auxVector3_1.set(5, 0, 6), 180, auxVector2_1.set(3, 6), auxVector2_2.set(5, 8), Assets.Models.WALL_2);
		addTestWall(auxVector3_1.set(6, 0, 4), 270, auxVector2_1.set(6, 4), auxVector2_2.set(6, 6), Assets.Models.WALL_2);
		addTestWall(auxVector3_1.set(6, 0, 0), 270, auxVector2_1.set(6, 0), auxVector2_2.set(6, 4), Assets.Models.WALL_1);
	}

	private void addTestWall(final Vector3 position,
							 final float rotation,
							 final Vector2 topLeft,
							 final Vector2 bottomRight,
							 final Assets.Models model) {
		ModelInstance modelInstance = new ModelInstance(assetManager.getModel(model));
		modelInstance.transform.setTranslation(position);
		modelInstance.transform.rotate(Vector3.Y, rotation);
		EntityBuilder.beginBuildingEntity(engine)
				.addWallComponent((int) topLeft.x, (int) topLeft.y, (int) bottomRight.x, (int) bottomRight.y)
				.addModelInstanceComponent(modelInstance, true)
				.finishAndAddToEngine();
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
				.addCharacterDecalComponent(animations, SpriteType.IDLE, CharacterComponent.Direction.SOUTH, position)
				.addAnimationComponent();
	}


	private void addTestFloor(final Vector3 position, Model model) {
		ModelInstance testFloorModelInstance = new ModelInstance(model);
		testFloorModelInstance.transform.setTranslation(position);
		EntityBuilder.beginBuildingEntity(engine)
				.addModelInstanceComponent(testFloorModelInstance, true)
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

	private Model createTestFloorModel(final ModelBuilder modelBuilder, int width, int height, int u, int v) {
		modelBuilder.begin();
		MeshPartBuilder meshPartBuilder = modelBuilder.part("test_floor",
				GL20.GL_TRIANGLES,
				Usage.Position | Usage.Normal | Usage.TextureCoordinates,
				createTestFloorMaterial());
		createRect(meshPartBuilder, width, height, u, v, 0, 0);
		return modelBuilder.end();
	}

	private void createRect(final MeshPartBuilder meshPartBuilder,
							final int width,
							final int height,
							final int u,
							final int v,
							final float xOffset,
							final float zOffset) {
		meshPartBuilder.setUVRange(0, 0, u, v);
		meshPartBuilder.rect(
				auxVector3_4.set(xOffset, 0, height + zOffset),
				auxVector3_1.set(width + xOffset, 0, height + zOffset),
				auxVector3_2.set(width + xOffset, 0, zOffset),
				auxVector3_3.set(xOffset, 0, zOffset),
				auxVector3_5.set(0, 1, 0));
	}

	private Material createTestFloorMaterial() {
		Texture floor = assetManager.getTexture(Assets.FloorsTextures.FLOOR_3);
		floor.setWrap(Repeat, Repeat);
		Material material = new Material(TextureAttribute.createDiffuse(floor));
		material.id = "floor_test";
		return material;
	}
}
