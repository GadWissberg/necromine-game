package com.gadarts.isometric.utils.map;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pools;
import com.gadarts.isometric.components.ObstacleComponent;
import com.gadarts.isometric.components.Obstacles;
import com.gadarts.isometric.components.WallComponent;
import com.gadarts.isometric.components.character.CharacterAnimations;
import com.gadarts.isometric.components.character.CharacterComponent;
import com.gadarts.isometric.components.character.CharacterSoundData;
import com.gadarts.isometric.components.character.CharacterSpriteData;
import com.gadarts.isometric.components.enemy.Enemies;
import com.gadarts.isometric.components.model.GameModelInstance;
import com.gadarts.isometric.components.player.PlayerComponent;
import com.gadarts.isometric.components.player.Weapon;
import com.gadarts.isometric.systems.hud.HudSystemImpl;
import com.gadarts.isometric.utils.EntityBuilder;
import com.gadarts.necromine.assets.Assets;
import com.gadarts.necromine.assets.GameAssetsManager;
import com.gadarts.necromine.model.WeaponsDefinitions;
import com.gadarts.necromine.model.characters.Direction;
import com.gadarts.necromine.model.characters.SpriteType;

import static com.badlogic.gdx.graphics.Texture.TextureWrap.Repeat;
import static com.gadarts.necromine.model.characters.CharacterTypes.BILLBOARD_Y;

/**
 * Creates the map.
 */
public final class MapBuilder {
	private static final CharacterSoundData auxCharacterSoundData = new CharacterSoundData();
	private static final Vector2 auxVector2_1 = new Vector2();
	private static final Vector2 auxVector2_2 = new Vector2();
	private static final Vector3 auxVector3_1 = new Vector3();
	private static final Vector3 auxVector3_2 = new Vector3();
	private static final Vector3 auxVector3_3 = new Vector3();
	private static final Vector3 auxVector3_4 = new Vector3();
	private static final Vector3 auxVector3_5 = new Vector3();
	private static final String REGION_NAME_BULLET = "bullet";

	private final GameAssetsManager assetManager;
	private final PooledEngine engine;
	private final ModelBuilder modelBuilder;
	private Model testFloorModel3_3;
	private Model testFloorModel1_2;
	private Model testFloorModel2_1;

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
		testFloorModel1_2 = createTestFloorModel(modelBuilder, 1, 2, 1, 2);
		testFloorModel2_1 = createTestFloorModel(modelBuilder, 2, 1, 2, 1);
		createAndAdd3dCursor();
		addPlayer();
		addEnemyTest(1, 10, Direction.EAST);
		addEnemyTest(2, 2, Direction.SOUTH_EAST);
		addEnemyTest(8, 0, Direction.SOUTH);
		addEnemyTest(9, 0, Direction.SOUTH_WEST);
		addWeaponPickupTest(0, 10, Assets.Models.COLT, WeaponsDefinitions.COLT, REGION_NAME_BULLET);
		addWeaponPickupTest(1, 1, Assets.Models.HAMMER, WeaponsDefinitions.HAMMER, null);
		addTestLights();
		return createTestMap();
	}

	private void addTestLights() {
		EntityBuilder.beginBuildingEntity(engine).addLightComponent(auxVector3_1.set(3, 2f, 3), 1f, 3f).finishAndAddToEngine();
		EntityBuilder.beginBuildingEntity(engine).addLightComponent(auxVector3_1.set(10, 2f, 2), 0.5f, 3f).finishAndAddToEngine();
		EntityBuilder.beginBuildingEntity(engine).addLightComponent(auxVector3_1.set(1, 2f, 11), 0.5f, 3f).finishAndAddToEngine();
	}

	private void addWeaponPickupTest(final int x,
									 final int y,
									 final Assets.Models model,
									 final WeaponsDefinitions definition,
									 final String regionNameBullet) {
		GameModelInstance modelInstance = new GameModelInstance(assetManager.getModel(model));
		modelInstance.transform.setTranslation(auxVector3_1.set(x + 0.5f, 0, y + 0.5f));
		Assets.Atlases atlas = Assets.Atlases.findByRelatedWeapon(definition);
		TextureAtlas.AtlasRegion bulletRegion = null;
		if (regionNameBullet != null) {
			bulletRegion = assetManager.getAtlas(atlas).findRegion(regionNameBullet);
		}
		Texture displayImage = assetManager.getTexture(definition.getImage());
		EntityBuilder.beginBuildingEntity(engine)
				.addModelInstanceComponent(modelInstance, true)
				.addPickUpComponentAsWeapon(definition, displayImage, bulletRegion)
				.finishAndAddToEngine();
	}

	private MapGraph createTestMap() {
		addTestWalls();
		addObstacles();
		addTestFloor(auxVector3_1.set(1.5f, 0, 1.5f), testFloorModel3_3);
		addTestFloor(auxVector3_1.set(4.5f, 0, 1.5f), testFloorModel3_3);
		addTestFloor(auxVector3_1.set(7f, 0, 2.5f), testFloorModel2_1);
		addTestFloor(auxVector3_1.set(2.5f, 0, 7f), testFloorModel1_2);
		addTestFloor(auxVector3_1.set(2.5f, 0, 9f), testFloorModel1_2);
		addTestFloor(auxVector3_1.set(0.5f, 0, 11f), testFloorModel1_2);
		addTestFloor(auxVector3_1.set(1.5f, 0, 11f), testFloorModel1_2);
		addTestFloor(auxVector3_1.set(2.5f, 0, 11f), testFloorModel1_2);
		addTestFloor(auxVector3_1.set(3.5f, 0, 11f), testFloorModel1_2);
		addTestFloor(auxVector3_1.set(9.5f, 0, 1.5f), testFloorModel3_3);
		addTestFloor(auxVector3_1.set(1.5f, 0, 4.5f), testFloorModel3_3);
		addTestFloor(auxVector3_1.set(4.5f, 0, 4.5f), testFloorModel3_3);
		return new MapGraph(
				engine.getEntitiesFor(Family.all(CharacterComponent.class).get()),
				engine.getEntitiesFor(Family.all(WallComponent.class).get()),
				engine.getEntitiesFor(Family.all(ObstacleComponent.class).get()),
				engine
		);
	}

	private void addObstacles() {
		addTestObstacle(3, 1, 0, Obstacles.PILLAR);
		addTestObstacle(6, 2, 270, Obstacles.CAVE_SUPPORTER_1);
	}

	private void addTestObstacle(final int x,
								 final int y,
								 final int rotation,
								 final Obstacles definition) {
		Assets.Models def = definition.getModel();
		GameModelInstance modelInstance = new GameModelInstance(assetManager.getModel(def));
		modelInstance.transform.setTranslation(x + 0.5f, 0, y + 0.5f);
		modelInstance.transform.rotate(Vector3.Y, rotation);
		modelInstance.getAdditionalRenderData().getBoundingBox().set(auxVector3_1.setZero(), auxVector3_2.set(1, 1, 1));
		EntityBuilder.beginBuildingEntity(engine)
				.addModelInstanceComponent(modelInstance, true, def.isCastShadow())
				.addObstacleComponent(x, y, definition)
				.addCollisionComponent()
				.finishAndAddToEngine();
	}

	private void addTestWalls() {
		auxVector2_1.set(-1, -1);
		addTestWall(auxVector3_1.set(2, 0, 0), 0, auxVector2_1, auxVector2_1, Assets.Models.WALL_1);
		addTestWall(auxVector3_1.set(1, 0, 6), 180, auxVector2_1.set(0, 6), auxVector2_2.set(1, 6), Assets.Models.WALL_2);
		addTestWall(auxVector3_1.set(2, 0, 8), 90, auxVector2_1.set(1, 6), auxVector2_2.set(1, 9), Assets.Models.WALL_1);
		addTestWall(auxVector3_1.set(1, 0, 10), 0, auxVector2_1.set(0, 9), auxVector2_2.set(1, 9), Assets.Models.WALL_2);
		addTestWall(auxVector3_1.set(0, 0, 11), 90, auxVector2_1.set(0, 9), auxVector2_2.set(1, 9), Assets.Models.WALL_2);
		addTestWall(auxVector3_1.set(2, 0, 12), 180, auxVector2_1.set(0, 12), auxVector2_2.set(3, 12), Assets.Models.WALL_1);
		addTestWall(auxVector3_1.set(4, 0, 11), 270, auxVector2_1.set(4, 10), auxVector2_2.set(4, 13), Assets.Models.WALL_2);
		addTestWall(auxVector3_1.set(3, 0, 8), 270, auxVector2_1.set(3, 6), auxVector2_2.set(3, 9), Assets.Models.WALL_1);
		addTestWall(auxVector3_1.set(4, 0, 6), 180, auxVector2_1.set(3, 6), auxVector2_2.set(5, 6), Assets.Models.WALL_2);
		addTestWall(auxVector3_1.set(5, 0, 0), 0, auxVector2_1, auxVector2_1, Assets.Models.WALL_2);
		addTestWall(auxVector3_1.set(7, 0, 2), 0, auxVector2_1.set(6, 1), auxVector2_2.set(6, 1), Assets.Models.WALL_2);
		addTestWall(auxVector3_1.set(7, 0, 3), 180, auxVector2_1.set(6, 3), auxVector2_2.set(10, 3), Assets.Models.WALL_2);
		addTestWall(auxVector3_1.set(9, 0, 3), 180, auxVector2_1.set(8, 3), auxVector2_2.set(10, 3), Assets.Models.WALL_2);
		addTestWall(auxVector3_1.set(11, 0, 2), 270, auxVector2_1.set(11, 0), auxVector2_2.set(11, 3), Assets.Models.WALL_2);
		addTestWall(auxVector3_1.set(10, 0, 0), 0, auxVector2_1.set(11, 0), auxVector2_2.set(11, 3), Assets.Models.WALL_2);
		addTestWall(auxVector3_1.set(6, 0, 1), 270, auxVector2_1.set(6, 0), auxVector2_2.set(6, 1), Assets.Models.WALL_2);
		addTestWall(auxVector3_1.set(8, 0, 1), 90, auxVector2_1.set(7, 0), auxVector2_2.set(7, 1), Assets.Models.WALL_2);
		addTestWall(auxVector3_1.set(6, 0, 4), 270, auxVector2_1.set(6, 3), auxVector2_2.set(6, 5), Assets.Models.WALL_2);
		addTestWall(auxVector3_1.set(0, 0, 2), 90, auxVector2_1, auxVector2_1, Assets.Models.WALL_1);
		addTestWall(auxVector3_1.set(0, 0, 5), 90, auxVector2_1.set(-1, -1), auxVector2_2.set(-1, -1), Assets.Models.WALL_2);
	}

	private void addTestWall(final Vector3 position,
							 final float rotation,
							 final Vector2 topLeft,
							 final Vector2 bottomRight,
							 final Assets.Models model) {
		GameModelInstance modelInstance = new GameModelInstance(assetManager.getModel(model));
		modelInstance.transform.setTranslation(position);
		modelInstance.transform.rotate(Vector3.Y, rotation);
		modelInstance.getAdditionalRenderData().getBoundingBox().mul(modelInstance.transform);
		EntityBuilder.beginBuildingEntity(engine)
				.addWallComponent((int) topLeft.x, (int) topLeft.y, (int) bottomRight.x, (int) bottomRight.y)
				.addModelInstanceComponent(modelInstance, true)
				.addCollisionComponent()
				.finishAndAddToEngine();
	}

	private void addPlayer() {
		Texture image = assetManager.getTexture(WeaponsDefinitions.AXE_PICK.getImage());
		Weapon weapon = Pools.obtain(Weapon.class);
		weapon.init(WeaponsDefinitions.AXE_PICK, 0, 0, image);
		CharacterAnimations general = assetManager.get(Assets.Atlases.PLAYER_GENERIC.name());
		EntityBuilder entityBuilder = EntityBuilder.beginBuildingEntity(engine).addPlayerComponent(weapon, general);
		Vector3 position = auxVector3_1.set(0.5f, BILLBOARD_Y, 0.5f);
		addCharBaseComponents(entityBuilder, Assets.Atlases.PLAYER_AXE_PICK, position, null, Assets.Sounds.PLAYER_PAIN, Assets.Sounds.PLAYER_DEATH, Direction.SOUTH_EAST, 16);
		entityBuilder.finishAndAddToEngine();
	}

	private void addEnemyTest(final int x, final int y, final Direction direction) {
		EntityBuilder entityBuilder = EntityBuilder.beginBuildingEntity(engine).addEnemyComponent(Enemies.ZEALOT);
		Entity player = engine.getEntitiesFor(Family.all(PlayerComponent.class).get()).first();
		Vector3 position = auxVector3_1.set(x + 0.5f, BILLBOARD_Y, y + 0.5f);
		addCharBaseComponents(entityBuilder, Assets.Atlases.ZEALOT, position, player, Assets.Sounds.ENEMY_PAIN, Assets.Sounds.ENEMY_DEATH, direction, 2);
		entityBuilder.finishAndAddToEngine();
	}

	private void addCharBaseComponents(final EntityBuilder entityBuilder,
									   final Assets.Atlases atlas,
									   final Vector3 position,
									   final Entity target,
									   final Assets.Sounds painSound,
									   final Assets.Sounds deathSound,
									   final Direction direction, final int health) {
		CharacterAnimations animations = assetManager.get(atlas.name());
		SpriteType spriteType = SpriteType.IDLE;
		CharacterSpriteData characterSpriteData = Pools.obtain(CharacterSpriteData.class);
		characterSpriteData.init(direction, spriteType, 1);
		auxCharacterSoundData.set(painSound, deathSound);
		entityBuilder.addCharacterComponent(characterSpriteData, target, auxCharacterSoundData, health)
				.addCharacterDecalComponent(animations, spriteType, direction, position)
				.addCollisionComponent()
				.addAnimationComponent();
	}


	private void addTestFloor(final Vector3 position, final Model model) {
		GameModelInstance testFloorModelInstance = new GameModelInstance(model);
		testFloorModelInstance.transform.setTranslation(position);
		EntityBuilder.beginBuildingEntity(engine)
				.addModelInstanceComponent(testFloorModelInstance, true)
				.addFloorComponent()
				.finishAndAddToEngine();
	}

	private void createAndAdd3dCursor() {
		Model model = modelTestCursor();
		EntityBuilder.beginBuildingEntity(engine)
				.addModelInstanceComponent(new GameModelInstance(model, false), true, false)
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

	private Model createTestFloorModel(final ModelBuilder modelBuilder, final int width, final int height, final int u, final int v) {
		modelBuilder.begin();
		MeshPartBuilder meshPartBuilder = modelBuilder.part("test_floor",
				GL20.GL_TRIANGLES,
				Usage.Position | Usage.Normal | Usage.TextureCoordinates,
				createTestFloorMaterial());
		createRect(meshPartBuilder, width, height, u, v, -width / 2f, -height / 2f);
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
