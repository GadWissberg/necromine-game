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
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
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
import com.gadarts.isometric.services.GameServices;
import com.gadarts.isometric.services.ModelBoundingBox;
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
	private static final Vector3 auxVector3_1 = new Vector3();
	private static final Vector3 auxVector3_2 = new Vector3();
	private static final Vector3 auxVector3_3 = new Vector3();
	private static final Vector3 auxVector3_4 = new Vector3();
	private static final Vector3 auxVector3_5 = new Vector3();
	private static final String REGION_NAME_BULLET = "bullet";
	private static final String KEY_LIGHTS = "lights";
	private static final String KEY_ENVIRONMENT = "environment";
	private static final String KEY_PICKUPS = "pickups";
	private static final BoundingBox auxBoundingBox = new BoundingBox();

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

	private void addCharBaseComponents(final EntityBuilder entityBuilder,
									   final Atlases atlas,
									   final Vector3 position,
									   final Entity target,
									   final Sounds painSound,
									   final Sounds deathSound,
									   final Direction direction, final int health) {
		SpriteType spriteType = SpriteType.IDLE;
		CharacterSpriteData characterSpriteData = Pools.obtain(CharacterSpriteData.class);
		characterSpriteData.init(direction, spriteType, 1);
		auxCharacterSoundData.set(painSound, deathSound);
		entityBuilder.addCharacterComponent(characterSpriteData, target, auxCharacterSoundData, health)
				.addCharacterDecalComponent(assetManager.get(atlas.name()), spriteType, direction, position)
				.addCollisionComponent()
				.addAnimationComponent();
	}

	private void createAndAdd3dCursor() {
		Model model = assetManager.getModel(Assets.Models.CURSOR);
		model.calculateBoundingBox(auxBoundingBox);
		EntityBuilder.beginBuildingEntity(engine)
				.addModelInstanceComponent(new GameModelInstance(model, auxBoundingBox, false), true, false)
				.addCursorComponent()
				.finishAndAddToEngine();
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
		Assets.Models modelDefinition = type.getModelDefinition();
		String fileName = GameServices.BOUNDING_BOX_PREFIX + modelDefinition.getFilePath();
		ModelBoundingBox boundingBox = assetManager.get(fileName, ModelBoundingBox.class);
		GameModelInstance modelInstance = new GameModelInstance(assetManager.getModel(modelDefinition), boundingBox);
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
			int halfDepth = type.getDepth() / 2;
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
		String fileName = GameServices.BOUNDING_BOX_PREFIX + type.getModelDefinition().getFilePath();
		ModelBoundingBox box = assetManager.get(fileName, ModelBoundingBox.class);
		GameModelInstance modelInstance = new GameModelInstance(assetManager.getModel(type.getModelDefinition()), box);
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
		floorModel.calculateBoundingBox(auxBoundingBox);
		IntStream.range(0, depth).forEach(row ->
				IntStream.range(0, width).forEach(col -> {
					char currentChar = matrix.charAt(row * width + col % width);
					if (currentChar != '0') {
						inflateTile(row, col, currentChar);
					}
				}));
	}

	private void inflateTile(final int row, final int col, final char currentChar) {
		GameModelInstance mi = new GameModelInstance(floorModel, auxBoundingBox);
		Texture texture = assetManager.getTexture(Assets.FloorsTextures.values()[currentChar - '1']);
		texture.setWrap(Repeat, Repeat);
		mi.materials.get(0).set(TextureAttribute.createDiffuse(texture));
		mi.transform.setTranslation(auxVector3_1.set(col + 0.5f, 0, row + 0.5f));
		EntityBuilder.beginBuildingEntity(engine)
				.addModelInstanceComponent(mi, true)
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
