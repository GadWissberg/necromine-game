package com.gadarts.isometric.utils.map;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Gdx;
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
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pools;
import com.gadarts.isometric.components.ObstacleComponent;
import com.gadarts.isometric.components.WallComponent;
import com.gadarts.isometric.components.character.CharacterAnimations;
import com.gadarts.isometric.components.character.CharacterComponent;
import com.gadarts.isometric.components.character.CharacterSoundData;
import com.gadarts.isometric.components.character.CharacterSpriteData;
import com.gadarts.isometric.components.model.GameModelInstance;
import com.gadarts.isometric.components.player.PlayerComponent;
import com.gadarts.isometric.components.player.Weapon;
import com.gadarts.isometric.systems.hud.HudSystemImpl;
import com.gadarts.isometric.utils.EntityBuilder;
import com.gadarts.necromine.assets.Assets;
import com.gadarts.necromine.assets.Assets.Atlases;
import com.gadarts.necromine.assets.Assets.Sounds;
import com.gadarts.necromine.assets.GameAssetsManager;
import com.gadarts.necromine.model.EnvironmentDefinitions;
import com.gadarts.necromine.model.characters.CharacterTypes;
import com.gadarts.necromine.model.characters.Direction;
import com.gadarts.necromine.model.characters.Enemies;
import com.gadarts.necromine.model.characters.SpriteType;
import com.gadarts.necromine.model.pickups.WeaponsDefinitions;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Arrays;
import java.util.stream.IntStream;

import static com.badlogic.gdx.graphics.Texture.TextureWrap.Repeat;
import static com.gadarts.necromine.assets.MapJsonKeys.*;
import static com.gadarts.necromine.model.characters.CharacterTypes.*;
import static com.gadarts.necromine.model.characters.Direction.NORTH;
import static com.gadarts.necromine.model.characters.Direction.SOUTH;

/**
 * Creates the map.
 */
public final class MapBuilder implements Disposable {
	public static final String TEMP_PATH = "core/assets/maps/test_map.json";
	private static final CharacterSoundData auxCharacterSoundData = new CharacterSoundData();
	private static final Vector2 auxVector2_1 = new Vector2();
	private static final Vector2 auxVector2_2 = new Vector2();
	private static final Vector3 auxVector3_1 = new Vector3();
	private static final Vector3 auxVector3_2 = new Vector3();
	private static final Vector3 auxVector3_3 = new Vector3();
	private static final Vector3 auxVector3_4 = new Vector3();
	private static final Vector3 auxVector3_5 = new Vector3();
	private static final String REGION_NAME_BULLET = "bullet";
	private static final String KEY_LIGHTS = "lights";
	private static final String KEY_ENVIRONMENT = "environment";
	private static final String KEY_PICKUPS = "pickups";

	private final GameAssetsManager assetManager;
	private final PooledEngine engine;
	private final ModelBuilder modelBuilder;
	private final Model floorModel;
	private final Gson gson = new Gson();

	public MapBuilder(final GameAssetsManager assetManager, final PooledEngine engine) {
		this.assetManager = assetManager;
		this.engine = engine;
		this.modelBuilder = new ModelBuilder();
		floorModel = createFloorModel();
	}

	/**
	 * Test map.
	 *
	 * @return The map graph.
	 */
	public MapGraph createAndAddTestMap() {
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
		Atlases atlas = Atlases.findByRelatedWeapon(definition);
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
		addTestFloor(auxVector3_1.set(1.5f, 0, 1.5f), floorModel);
		addTestFloor(auxVector3_1.set(4.5f, 0, 1.5f), floorModel);
		addTestFloor(auxVector3_1.set(9.5f, 0, 1.5f), floorModel);
		addTestFloor(auxVector3_1.set(1.5f, 0, 4.5f), floorModel);
		addTestFloor(auxVector3_1.set(4.5f, 0, 4.5f), floorModel);
		return new MapGraph(
				engine.getEntitiesFor(Family.all(CharacterComponent.class).get()),
				engine.getEntitiesFor(Family.all(WallComponent.class).get()),
				engine.getEntitiesFor(Family.all(ObstacleComponent.class).get()),
				engine
		);
	}

	private void addObstacles() {
		addTestObstacle(3, 1, 0, EnvironmentDefinitions.PILLAR);
		addTestObstacle(6, 2, 270, EnvironmentDefinitions.CAVE_SUPPORTER_1);
	}

	private void addTestObstacle(final int x,
								 final int y,
								 final int rotation,
								 final EnvironmentDefinitions definition) {
		Assets.Models def = definition.getModelDefinition();
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
		CharacterAnimations general = assetManager.get(Atlases.PLAYER_GENERIC.name());
		EntityBuilder entityBuilder = EntityBuilder.beginBuildingEntity(engine).addPlayerComponent(weapon, general);
		Vector3 position = auxVector3_1.set(0.5f, BILLBOARD_Y, 0.5f);
		addCharBaseComponents(entityBuilder, Atlases.PLAYER_AXE_PICK, position, null, Sounds.PLAYER_PAIN, Sounds.PLAYER_DEATH, Direction.SOUTH_EAST, 16);
		entityBuilder.finishAndAddToEngine();
	}

	private void addEnemyTest(final int x, final int y, final Direction direction) {
		EntityBuilder entityBuilder = EntityBuilder.beginBuildingEntity(engine).addEnemyComponent(Enemies.ZEALOT);
		Entity player = engine.getEntitiesFor(Family.all(PlayerComponent.class).get()).first();
		Vector3 position = auxVector3_1.set(x + 0.5f, BILLBOARD_Y, y + 0.5f);
		addCharBaseComponents(entityBuilder, Atlases.ZEALOT, position, player, Sounds.ENEMY_PAIN, Sounds.ENEMY_DEATH, direction, 2);
		entityBuilder.finishAndAddToEngine();
	}

	private void addCharBaseComponents(final EntityBuilder entityBuilder,
									   final Atlases atlas,
									   final Vector3 position,
									   final Entity target,
									   final Sounds painSound,
									   final Sounds deathSound,
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

	private Model createFloorModel() {
		modelBuilder.begin();
		MeshPartBuilder meshPartBuilder = modelBuilder.part("floor",
				GL20.GL_TRIANGLES,
				Usage.Position | Usage.Normal | Usage.TextureCoordinates,
				createFloorMaterial());
		createRect(meshPartBuilder);
		return modelBuilder.end();
	}

	private void createRect(final MeshPartBuilder meshPartBuilder) {
		meshPartBuilder.setUVRange(0, 0, 1, 1);
		final float OFFSET = -0.5f;
		meshPartBuilder.rect(
				auxVector3_4.set(OFFSET, 0, 1 + OFFSET),
				auxVector3_1.set(1 + OFFSET, 0, 1 + OFFSET),
				auxVector3_2.set(1 + OFFSET, 0, OFFSET),
				auxVector3_3.set(OFFSET, 0, OFFSET),
				auxVector3_5.set(0, 1, 0));
	}

	private Material createFloorMaterial() {
		Material material = new Material();
		material.id = "floor_test";
		return material;
	}

	public MapGraph inflateTestMap() {
		createAndAdd3dCursor();
		JsonObject mapJsonObject = gson.fromJson(Gdx.files.internal(TEMP_PATH).reader(), JsonObject.class);
		inflateAllElements(mapJsonObject);
		return new MapGraph(engine.getEntitiesFor(Family.all(CharacterComponent.class).get()),
				engine.getEntitiesFor(Family.all(WallComponent.class).get()),
				engine.getEntitiesFor(Family.all(ObstacleComponent.class).get()),
				engine);
	}

	private void inflateAllElements(final JsonObject mapJsonObject) {
		inflateTiles(mapJsonObject);
		inflateCharacters(mapJsonObject);
		inflateLights(mapJsonObject);
		inflateEnvironment(mapJsonObject);
		inflatePickups(mapJsonObject);
	}

	private void inflateCharacters(final JsonObject mapJsonObject) {
		Arrays.stream(CharacterTypes.values()).forEach(type -> {
			String typeName = type.name().toLowerCase();
			JsonObject charactersJsonObject = mapJsonObject.getAsJsonObject(KEY_CHARACTERS);
			if (charactersJsonObject.has(typeName)) {
				JsonArray array = charactersJsonObject.get(typeName).getAsJsonArray();
				array.forEach(characterJsonElement -> {
					if (type == PLAYER) {
						inflatePlayer((JsonObject) characterJsonElement);
					} else if (type == ENEMY) {
						inflateEnemy((JsonObject) characterJsonElement);
					}
				});
			}
		});
	}

	private void inflateLights(final JsonObject mapJsonObject) {
		JsonArray lights = mapJsonObject.getAsJsonArray(KEY_LIGHTS);
		lights.forEach(element -> {
			JsonObject lightJsonObject = element.getAsJsonObject();
			int row = lightJsonObject.get(KEY_ROW).getAsInt();
			int col = lightJsonObject.get(KEY_COL).getAsInt();
			EntityBuilder.beginBuildingEntity(engine)
					.addLightComponent(auxVector3_1.set(col + 0.5f, 2f, row + 0.5f), 1f, 3f)
					.finishAndAddToEngine();
		});
	}

	private void inflateEnvironment(final JsonObject mapJsonObject) {
		JsonArray envs = mapJsonObject.getAsJsonArray(KEY_ENVIRONMENT);
		envs.forEach(element -> {
			JsonObject envJsonObject = element.getAsJsonObject();
			int dirIndex = envJsonObject.get(KEY_DIRECTION).getAsInt();
			EnvironmentDefinitions type = EnvironmentDefinitions.values()[envJsonObject.get(KEY_TYPE).getAsInt()];
			EntityBuilder builder = EntityBuilder.beginBuildingEntity(engine);
			MapCoord coord = new MapCoord(envJsonObject.get(KEY_ROW).getAsInt(), envJsonObject.get(KEY_COL).getAsInt());
			inflateEnvSpecifiedComponent(coord, type, builder, Direction.values()[dirIndex]);
			inflateEnvModelInstanceComponent(coord, dirIndex, type, builder);
			inflateEnvironmentEntity(builder);
		});
	}

	private void inflatePickups(final JsonObject mapJsonObject) {
		JsonArray pickups = mapJsonObject.getAsJsonArray(KEY_PICKUPS);
		pickups.forEach(element -> {
			JsonObject pickJsonObject = element.getAsJsonObject();
			WeaponsDefinitions type = WeaponsDefinitions.values()[pickJsonObject.get(KEY_TYPE).getAsInt()];
			TextureAtlas.AtlasRegion bulletRegion = null;
			if (!type.isMelee()) {
				bulletRegion = assetManager.getAtlas(Atlases.findByRelatedWeapon(type)).findRegion(REGION_NAME_BULLET);
			}
			inflatePickupEntity(pickJsonObject, type, bulletRegion);
		});
	}

	private void inflatePickupEntity(final JsonObject pickJsonObject,
									 final WeaponsDefinitions type,
									 final TextureAtlas.AtlasRegion bulletRegion) {
		EntityBuilder builder = EntityBuilder.beginBuildingEntity(engine);
		inflatePickupModel(builder, pickJsonObject, type);
		builder.addPickUpComponentAsWeapon(type, assetManager.getTexture(type.getImage()), bulletRegion)
				.finishAndAddToEngine();
	}

	private void inflatePickupModel(final EntityBuilder builder,
									final JsonObject pickJsonObject,
									final WeaponsDefinitions type) {
		MapCoord coord = new MapCoord(pickJsonObject.get(KEY_ROW).getAsInt(), pickJsonObject.get(KEY_COL).getAsInt());
		GameModelInstance modelInstance = new GameModelInstance(assetManager.getModel(type.getModelDefinition()));
		modelInstance.transform.setTranslation(auxVector3_1.set(coord.getCol() + 0.5f, 0, coord.getRow() + 0.5f));
		builder.addModelInstanceComponent(modelInstance, true);
	}

	private void inflateEnvModelInstanceComponent(final MapCoord coord,
												  final int directionIndex,
												  final EnvironmentDefinitions type,
												  final EntityBuilder builder) {
		GameModelInstance mi = inflateEnvironmentModelInstance(coord.getRow(), coord.getCol(), directionIndex, type);
		builder.addModelInstanceComponent(mi, true, type.isCastShadow());
	}

	private void inflateEnvironmentEntity(final EntityBuilder builder) {
		builder.addCollisionComponent()
				.finishAndAddToEngine();
	}

	private void inflateEnvSpecifiedComponent(final MapCoord coord,
											  final EnvironmentDefinitions type,
											  final EntityBuilder builder,
											  final Direction facingDirection) {
		int col = coord.getCol();
		int row = coord.getRow();
		if (type.isWall()) {
			int halfWidth = type.getWidth() / 2;
			int halfDepth = type.getHeight() / 2;
			if (facingDirection == NORTH || facingDirection == SOUTH) {
				int swap = halfWidth;
				halfWidth = halfDepth;
				halfDepth = swap;
			}
			builder.addWallComponent(col - halfWidth, row - halfDepth, col + Math.max(halfWidth, 1) - 1, row + Math.max(halfDepth, 1) - 1);
		} else {
			builder.addObstacleComponent(col, row, type);
		}
	}

	private GameModelInstance inflateEnvironmentModelInstance(final int row,
															  final int col,
															  final int directionIndex,
															  final EnvironmentDefinitions type) {
		GameModelInstance modelInstance = new GameModelInstance(assetManager.getModel(type.getModelDefinition()));
		Direction direction = Direction.values()[directionIndex];
		modelInstance.transform.setTranslation(auxVector3_1.set(col + 0.5f, 0, row + 0.5f));
		modelInstance.transform.rotate(Vector3.Y, -1 * direction.getDirection(auxVector2_1).angleDeg());
		modelInstance.transform.translate(type.getOffset(auxVector3_1));
		EnvironmentDefinitions.handleEvenSize(type, modelInstance, direction);
		modelInstance.getAdditionalRenderData().getBoundingBox().mul(modelInstance.transform);
		return modelInstance;
	}

	private void inflateTiles(final JsonObject mapJsonObject) {
		JsonObject tilesJsonObject = mapJsonObject.get(KEY_TILES).getAsJsonObject();
		int width = tilesJsonObject.get(KEY_WIDTH).getAsInt();
		int depth = tilesJsonObject.get(KEY_DEPTH).getAsInt();
		String matrix = tilesJsonObject.get(KEY_MATRIX).getAsString();
		IntStream.range(0, depth).forEach(row ->
				IntStream.range(0, width).forEach(col -> {
					char currentChar = matrix.charAt(row * width + col % width);
					if (currentChar != '0') {
						inflateTile(row, col, currentChar);
					}
				}));
	}

	private void inflateTile(final int row, final int col, final char currentChar) {
		GameModelInstance modelInstance = new GameModelInstance(floorModel);
		Texture texture = assetManager.getTexture(Assets.FloorsTextures.values()[currentChar - '1']);
		texture.setWrap(Repeat, Repeat);
		modelInstance.materials.get(0).set(TextureAttribute.createDiffuse(texture));
		modelInstance.transform.setTranslation(auxVector3_1.set(col + 0.5f, 0, row + 0.5f));
		EntityBuilder.beginBuildingEntity(engine)
				.addModelInstanceComponent(modelInstance, true)
				.addFloorComponent()
				.finishAndAddToEngine();
	}

	private void inflateEnemy(final JsonObject characterJsonObject) {
		int index = characterJsonObject.get(KEY_TYPE).getAsInt();
		Enemies type = Enemies.values()[index];
		EntityBuilder entityBuilder = EntityBuilder.beginBuildingEntity(engine).addEnemyComponent(type);
		Entity player = engine.getEntitiesFor(Family.all(PlayerComponent.class).get()).first();
		Vector3 position = inflateCharacterPosition(characterJsonObject);
		addCharBaseComponents(
				entityBuilder,
				type.getAtlasDefinition(),
				position,
				player,
				Sounds.ENEMY_PAIN,
				Sounds.ENEMY_DEATH,
				Direction.values()[characterJsonObject.get(KEY_DIRECTION).getAsInt()],
				2);
		entityBuilder.finishAndAddToEngine();
	}

	private void inflatePlayer(final JsonObject characterJsonObject) {
		Weapon weapon = initializeStartingWeapon();
		CharacterAnimations general = assetManager.get(Atlases.PLAYER_GENERIC.name());
		EntityBuilder builder = EntityBuilder.beginBuildingEntity(engine).addPlayerComponent(weapon, general);
		addCharBaseComponents(
				builder,
				Atlases.PLAYER_AXE_PICK,
				inflateCharacterPosition(characterJsonObject),
				null,
				Sounds.PLAYER_PAIN,
				Sounds.PLAYER_DEATH,
				Direction.values()[characterJsonObject.get(KEY_DIRECTION).getAsInt()],
				16);
		builder.finishAndAddToEngine();
	}

	private Vector3 inflateCharacterPosition(final com.google.gson.JsonElement characterJsonElement) {
		JsonObject asJsonObject = characterJsonElement.getAsJsonObject();
		int col = asJsonObject.get(KEY_COL).getAsInt();
		int row = asJsonObject.get(KEY_ROW).getAsInt();
		return auxVector3_1.set(col + 0.5f, BILLBOARD_Y, row + 0.5f);
	}

	private Weapon initializeStartingWeapon() {
		Weapon weapon = Pools.obtain(Weapon.class);
		Texture image = assetManager.getTexture(WeaponsDefinitions.AXE_PICK.getImage());
		weapon.init(WeaponsDefinitions.AXE_PICK, 0, 0, image);
		return weapon;
	}

	@Override
	public void dispose() {
		floorModel.dispose();
	}
}
