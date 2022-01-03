package com.gadarts.isometric.utils.map;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pools;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.FloorComponent;
import com.gadarts.isometric.components.character.CharacterAnimations;
import com.gadarts.isometric.components.character.CharacterSkillsParameters;
import com.gadarts.isometric.components.character.data.CharacterData;
import com.gadarts.isometric.components.character.data.CharacterSoundData;
import com.gadarts.isometric.components.character.data.CharacterSpriteData;
import com.gadarts.isometric.components.decal.simple.RelatedDecal;
import com.gadarts.isometric.components.decal.simple.SimpleDecalComponent;
import com.gadarts.isometric.components.model.GameModelInstance;
import com.gadarts.isometric.components.player.PlayerComponent;
import com.gadarts.isometric.components.player.Weapon;
import com.gadarts.isometric.services.GameServices;
import com.gadarts.isometric.services.ModelBoundingBox;
import com.gadarts.isometric.systems.enemy.EnemySystemImpl;
import com.gadarts.isometric.utils.DefaultGameSettings;
import com.gadarts.isometric.utils.EntityBuilder;
import com.gadarts.isometric.utils.Utils;
import com.gadarts.necromine.WallCreator;
import com.gadarts.necromine.assets.Assets;
import com.gadarts.necromine.assets.Assets.Sounds;
import com.gadarts.necromine.assets.GameAssetsManager;
import com.gadarts.necromine.assets.MapJsonKeys;
import com.gadarts.necromine.model.Coords;
import com.gadarts.necromine.model.RelativeBillboard;
import com.gadarts.necromine.model.characters.CharacterDefinition;
import com.gadarts.necromine.model.characters.CharacterTypes;
import com.gadarts.necromine.model.characters.Direction;
import com.gadarts.necromine.model.characters.SpriteType;
import com.gadarts.necromine.model.characters.attributes.Accuracy;
import com.gadarts.necromine.model.characters.attributes.Agility;
import com.gadarts.necromine.model.characters.attributes.Strength;
import com.gadarts.necromine.model.characters.enemies.Enemies;
import com.gadarts.necromine.model.characters.enemies.EnemyWeaponsDefinitions;
import com.gadarts.necromine.model.env.EnvironmentDefinitions;
import com.gadarts.necromine.model.map.MapNodeData;
import com.gadarts.necromine.model.map.MapNodesTypes;
import com.gadarts.necromine.model.map.NodeWalls;
import com.gadarts.necromine.model.map.Wall;
import com.gadarts.necromine.model.pickups.WeaponsDefinitions;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.awt.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import static com.badlogic.gdx.graphics.g2d.Animation.PlayMode.LOOP;
import static com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import static com.gadarts.isometric.components.FloorComponent.AO_MASK_EAST;
import static com.gadarts.isometric.components.FloorComponent.AO_MASK_NORTH;
import static com.gadarts.isometric.components.FloorComponent.AO_MASK_NORTH_EAST;
import static com.gadarts.isometric.components.FloorComponent.AO_MASK_NORTH_WEST;
import static com.gadarts.isometric.components.FloorComponent.AO_MASK_SOUTH;
import static com.gadarts.isometric.components.FloorComponent.AO_MASK_SOUTH_EAST;
import static com.gadarts.isometric.components.FloorComponent.AO_MASK_SOUTH_WEST;
import static com.gadarts.isometric.components.FloorComponent.AO_MASK_WEST;
import static com.gadarts.necromine.assets.Assets.Atlases.ANUBIS;
import static com.gadarts.necromine.assets.Assets.Atlases.PLAYER_GENERIC;
import static com.gadarts.necromine.assets.Assets.Atlases.findByRelatedWeapon;
import static com.gadarts.necromine.assets.Assets.SurfaceTextures.MISSING;
import static com.gadarts.necromine.assets.MapJsonKeys.CHARACTERS;
import static com.gadarts.necromine.assets.MapJsonKeys.COL;
import static com.gadarts.necromine.assets.MapJsonKeys.DEPTH;
import static com.gadarts.necromine.assets.MapJsonKeys.DIRECTION;
import static com.gadarts.necromine.assets.MapJsonKeys.EAST;
import static com.gadarts.necromine.assets.MapJsonKeys.HEIGHT;
import static com.gadarts.necromine.assets.MapJsonKeys.HEIGHTS;
import static com.gadarts.necromine.assets.MapJsonKeys.H_OFFSET;
import static com.gadarts.necromine.assets.MapJsonKeys.MATRIX;
import static com.gadarts.necromine.assets.MapJsonKeys.ROW;
import static com.gadarts.necromine.assets.MapJsonKeys.TILES;
import static com.gadarts.necromine.assets.MapJsonKeys.TYPE;
import static com.gadarts.necromine.assets.MapJsonKeys.V_OFFSET;
import static com.gadarts.necromine.assets.MapJsonKeys.WEST;
import static com.gadarts.necromine.assets.MapJsonKeys.WIDTH;
import static com.gadarts.necromine.model.characters.CharacterTypes.BILLBOARD_SCALE;
import static com.gadarts.necromine.model.characters.CharacterTypes.BILLBOARD_Y;
import static com.gadarts.necromine.model.characters.CharacterTypes.ENEMY;
import static com.gadarts.necromine.model.characters.CharacterTypes.PLAYER;
import static com.gadarts.necromine.model.characters.Direction.NORTH;
import static com.gadarts.necromine.model.characters.Direction.SOUTH;
import static java.lang.String.format;

/**
 * Creates the map.
 */
public final class MapBuilder implements Disposable {
	public static final String MAP_PATH_TEMP = "core/assets/maps/%s.json";
	public static final int PLAYER_HEALTH = 64;
	public static final float INDEPENDENT_LIGHT_RADIUS = 4f;
	public static final float INDEPENDENT_LIGHT_INTENSITY = 1f;
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
	private static final BoundingBox auxBoundingBox = new BoundingBox();
	private static final Matrix4 auxMatrix = new Matrix4();
	private static final float INDEPENDENT_LIGHT_HEIGHT = 1F;
	private final GameAssetsManager assetManager;
	private final ModelBuilder modelBuilder;
	private final Model floorModel;
	private final Gson gson = new Gson();
	private final WallCreator wallCreator;
	private final Map<Enemies, Animation<TextureAtlas.AtlasRegion>> enemyBulletsTextureRegions = new HashMap<>();
	private PooledEngine engine;

	public MapBuilder(final GameAssetsManager assetManager, final PooledEngine engine) {
		this.assetManager = assetManager;
		this.engine = engine;
		this.modelBuilder = new ModelBuilder();
		this.wallCreator = new WallCreator(assetManager);
		floorModel = createFloorModel();
	}

	private void addCharBaseComponents(final EntityBuilder entityBuilder,
									   final CharacterData characterData,
									   final CharacterDefinition def,
									   final Assets.Atlases atlasDefinition) {
		CharacterSpriteData characterSpriteData = Pools.obtain(CharacterSpriteData.class);
		Direction direction = characterData.getDirection();
		characterSpriteData.init(direction,
				SpriteType.IDLE,
				def.getMeleeHitFrameIndex(),
				def.getPrimaryAttackHitFrameIndex(),
				def.isSingleDeathAnimation());
		Vector3 position = characterData.getPosition();
		CharacterAnimations animations = assetManager.get(atlasDefinition.name());
		entityBuilder.addCharacterComponent(characterSpriteData, characterData.getSoundData(), characterData.getSkills())
				.addCharacterDecalComponent(animations, SpriteType.IDLE, direction, position)
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

	/**
	 * Creates the test map.
	 *
	 * @return The Inflated map.
	 */
	public MapGraph inflateTestMap(final String map) {
		createAndAdd3dCursor();
		JsonObject mapJsonObj = gson.fromJson(Gdx.files.internal(format(MAP_PATH_TEMP, map)).reader(), JsonObject.class);
		MapGraph mapGraph = createMapGraph(mapJsonObj);
		inflateHeights(mapJsonObj, mapGraph);
		inflateAllElements(mapJsonObj, mapGraph);
		mapGraph.init();
		generateFloorAmbientOcclusions(mapGraph);
		return mapGraph;
	}

	private MapGraph createMapGraph(final JsonObject mapJsonObj) {
		return new MapGraph(
				Utils.getFloatFromJsonOrDefault(mapJsonObj, MapJsonKeys.AMBIENT, 0),
				inflateNodes(mapJsonObj.get(TILES).getAsJsonObject()),
				engine);
	}

	private void generateFloorAmbientOcclusions(final MapGraph mapGraph) {
		mapGraph.getNodes().forEach(node ->
				Optional.ofNullable(node.getEntity()).ifPresent(e -> generateFloorAmbientOcclusion(mapGraph, node)));
	}

	private void generateFloorAmbientOcclusion(final MapGraph mapGraph, final MapGraphNode node) {
		int nodeCol = node.getCol();
		int nodeRow = node.getRow();
		generateFloorEastWestAmbientOcclusions(mapGraph, node, nodeCol, nodeRow);
		generateFloorNorthSouthAmbientOcclusions(mapGraph, node, nodeCol, nodeRow);
		generateFloorDiagonalAmbientOcclusions(mapGraph, node, nodeCol, nodeRow);
	}

	private void generateFloorDiagonalAmbientOcclusions(final MapGraph mapGraph,
														final MapGraphNode node,
														final int nodeCol,
														final int nodeRow) {
		generateFloorDiagonalSouthAmbientOcclusions(mapGraph, node, nodeCol, nodeRow);
		generateFloorDiagonalNorthAmbientOcclusions(mapGraph, node, nodeCol, nodeRow);
	}

	private void generateFloorDiagonalNorthAmbientOcclusions(final MapGraph mapGraph,
															 final MapGraphNode node,
															 final int nodeCol,
															 final int nodeRow) {
		if (nodeCol - 1 >= 0 && nodeRow - 1 >= 0) {
			Optional.ofNullable(mapGraph.getNode(nodeCol - 1, nodeRow - 1))
					.ifPresent(northWest -> applyAmbientOcclusion(node, northWest, AO_MASK_NORTH_WEST));
		}
		if (nodeCol + 1 >= 0 && nodeRow - 1 >= 0) {
			Optional.ofNullable(mapGraph.getNode(nodeCol + 1, nodeRow - 1))
					.ifPresent(northEast -> applyAmbientOcclusion(node, northEast, AO_MASK_NORTH_EAST));
		}
	}

	private void generateFloorDiagonalSouthAmbientOcclusions(final MapGraph mapGraph,
															 final MapGraphNode node,
															 final int nodeCol,
															 final int nodeRow) {
		int depth = mapGraph.getDepth();
		if (nodeCol + 1 < mapGraph.getWidth() && nodeRow + 1 < depth) {
			Optional.ofNullable(mapGraph.getNode(nodeCol + 1, nodeRow + 1))
					.ifPresent(southEast -> applyAmbientOcclusion(node, southEast, AO_MASK_SOUTH_EAST));
		}
		if (nodeCol - 1 >= 0 && nodeRow + 1 < depth) {
			Optional.ofNullable(mapGraph.getNode(nodeCol - 1, nodeRow + 1))
					.ifPresent(southWest -> applyAmbientOcclusion(node, southWest, AO_MASK_SOUTH_WEST));
		}
	}

	private void generateFloorNorthSouthAmbientOcclusions(final MapGraph mapGraph,
														  final MapGraphNode node,
														  final int nodeCol,
														  final int nodeRow) {
		if (nodeRow + 1 < mapGraph.getDepth()) {
			Optional.ofNullable(mapGraph.getNode(nodeCol, nodeRow + 1))
					.ifPresent(south -> applyAmbientOcclusion(node, south, AO_MASK_SOUTH));
		}
		if (nodeRow - 1 >= 0) {
			Optional.ofNullable(mapGraph.getNode(nodeCol, nodeRow - 1))
					.ifPresent(north -> applyAmbientOcclusion(node, north, AO_MASK_NORTH));
		}
	}

	private void generateFloorEastWestAmbientOcclusions(final MapGraph mapGraph,
														final MapGraphNode node,
														final int nodeCol,
														final int nodeRow) {
		int eastCol = nodeCol + 1;
		if (eastCol < mapGraph.getWidth()) {
			Optional.ofNullable(mapGraph.getNode(eastCol, nodeRow))
					.ifPresent(east -> applyAmbientOcclusion(node, east, AO_MASK_EAST));
		}
		int westCol = nodeCol - 1;
		if (westCol >= 0) {
			Optional.ofNullable(mapGraph.getNode(westCol, nodeRow))
					.ifPresent(west -> applyAmbientOcclusion(node, west, AO_MASK_WEST));
		}
	}

	private void applyAmbientOcclusion(final MapGraphNode selected, final MapGraphNode neighbor, final int mask) {
		if (neighbor.getHeight() > selected.getHeight()) {
			FloorComponent floorComponent = ComponentsMapper.floor.get(selected.getEntity());
			floorComponent.setAmbientOcclusion(floorComponent.getAmbientOcclusion() | mask);
		}
	}

	private void inflateAllElements(final JsonObject mapJsonObject, final MapGraph mapGraph) {
		inflateCharacters(mapJsonObject, mapGraph);
		inflateLights(mapJsonObject, mapGraph);
		inflateEnvironment(mapJsonObject, mapGraph);
		inflatePickups(mapJsonObject, mapGraph);
	}

	private void inflateCharacters(final JsonObject mapJsonObject, final MapGraph mapGraph) {
		Arrays.stream(CharacterTypes.values()).forEach(type -> {
			String typeName = type.name().toLowerCase();
			JsonObject charactersJsonObject = mapJsonObject.getAsJsonObject(CHARACTERS);
			if (charactersJsonObject.has(typeName)) {
				JsonArray array = charactersJsonObject.get(typeName).getAsJsonArray();
				array.forEach(characterJsonElement -> {
					if (type == PLAYER) {
						inflatePlayer((JsonObject) characterJsonElement, mapGraph);
					} else if (type == ENEMY) {
						inflateEnemy((JsonObject) characterJsonElement, mapGraph);
					}
				});
			}
		});
	}

	private void inflateLights(final JsonObject mapJsonObject, final MapGraph mapGraph) {
		JsonArray lights = mapJsonObject.getAsJsonArray(KEY_LIGHTS);
		lights.forEach(element -> inflateLight(mapGraph, element));
	}

	private void inflateLight(final MapGraph mapGraph, final JsonElement element) {
		JsonObject lightJsonObject = element.getAsJsonObject();
		int row = lightJsonObject.get(ROW).getAsInt();
		int col = lightJsonObject.get(COL).getAsInt();
		Vector3 position = auxVector3_1.set(col + 0.5f, INDEPENDENT_LIGHT_HEIGHT, row + 0.5f);
		position.add(0, mapGraph.getNode(col, row).getHeight(), 0);
		EntityBuilder.beginBuildingEntity(engine)
				.addLightComponent(position, INDEPENDENT_LIGHT_INTENSITY, INDEPENDENT_LIGHT_RADIUS)
				.finishAndAddToEngine();
	}

	private void inflateEnvironment(final JsonObject mapJsonObject, final MapGraph mapGraph) {
		JsonArray envs = mapJsonObject.getAsJsonArray(KEY_ENVIRONMENT);
		envs.forEach(element -> {
			EntityBuilder builder = EntityBuilder.beginBuildingEntity(engine);
			JsonObject envJsonObject = element.getAsJsonObject();
			Coords coord = new Coords(envJsonObject.get(ROW).getAsInt(), envJsonObject.get(COL).getAsInt());
			inflateEnvComponents(mapGraph, builder, envJsonObject, coord);
			Entity entity = builder.finishAndAddToEngine();
			GameModelInstance modelInstance = ComponentsMapper.modelInstance.get(entity).getModelInstance();
			Vector3 position = modelInstance.transform.getTranslation(auxVector3_1);
			EnvironmentDefinitions type = EnvironmentDefinitions.valueOf(envJsonObject.get(TYPE).getAsString());
			RelativeBillboard relativeBillboard = type.getRelativeBillboard();
			Optional.ofNullable(relativeBillboard).ifPresent(r -> {
				TextureAtlas atlas = assetManager.getAtlas(r.getBillboard());
				Array<AtlasRegion> f = atlas.findRegions(r.getBillboard().getName().toLowerCase());
				Direction dir = Direction.values()[envJsonObject.get(DIRECTION).getAsInt()];
				float degrees = dir.getDirection(auxVector2_1).angleDeg() + ((dir == NORTH || dir == SOUTH) ? 180 : 0);
				Vector3 relativePosition = r.getRelativePosition(auxVector3_2).rotate(Vector3.Y, degrees);
				addRelativeBillboardEntity(position, r, f, relativePosition);
			});
		});
	}

	private void addRelativeBillboardEntity(final Vector3 position,
											final RelativeBillboard r,
											final Array<AtlasRegion> f,
											final Vector3 relativePosition) {
		Entity entity = EntityBuilder.beginBuildingEntity(engine)
				.addSimpleDecalComponent(position.add(relativePosition), f.get(0), true, true)
				.addAnimationComponent(r.getFrameDuration(), new Animation<>(r.getFrameDuration(), f, LOOP))
				.finishAndAddToEngine();
		ComponentsMapper.simpleDecal.get(entity).setAffectedByFow(true);
	}

	private void inflateEnvComponents(final MapGraph mapGraph,
									  final EntityBuilder builder,
									  final JsonObject envJsonObject,
									  final Coords coord) {
		int dirIndex = envJsonObject.get(DIRECTION).getAsInt();
		EnvironmentDefinitions type = EnvironmentDefinitions.valueOf(envJsonObject.get(TYPE).getAsString());
		inflateEnvSpecifiedComponent(coord, type, builder, Direction.values()[dirIndex]);
		MapGraphNode node = mapGraph.getNode(coord.getCol(), coord.getRow());
		GameModelInstance mi = inflateEnvModelInstanceComponent(node, envJsonObject, type, builder);
		inflateEnvLightComponent(builder, type, mi, dirIndex);
		builder.addCollisionComponent();
	}

	private void inflateEnvLightComponent(final EntityBuilder builder,
										  final EnvironmentDefinitions type,
										  final GameModelInstance mi,
										  final int dirIndex) {
		Optional.ofNullable(type.getLightEmission()).ifPresent(l -> {
			float degrees = Direction.values()[dirIndex].getDirection(auxVector2_1).angleDeg();
			Vector3 relativePosition = l.getRelativePosition(auxVector3_2).rotate(Vector3.Y, degrees);
			Vector3 position = mi.transform.getTranslation(auxVector3_1).add(relativePosition);
			builder.addShadowLightComponent(position, l.getIntensity(), l.getRadius(), Color.WHITE);
		});
	}

	private void inflatePickups(final JsonObject mapJsonObject, final MapGraph mapGraph) {
		JsonArray pickups = mapJsonObject.getAsJsonArray(KEY_PICKUPS);
		pickups.forEach(element -> {
			JsonObject pickJsonObject = element.getAsJsonObject();
			WeaponsDefinitions type = WeaponsDefinitions.valueOf(pickJsonObject.get(TYPE).getAsString());
			AtlasRegion bulletRegion = null;
			if (!type.isMelee()) {
				bulletRegion = assetManager.getAtlas(findByRelatedWeapon(type)).findRegion(REGION_NAME_BULLET);
			}
			inflatePickupEntity(pickJsonObject, type, bulletRegion, mapGraph);
		});
	}

	private void inflatePickupEntity(final JsonObject pickJsonObject,
									 final WeaponsDefinitions type,
									 final AtlasRegion bulletRegion,
									 final MapGraph mapGraph) {
		EntityBuilder builder = EntityBuilder.beginBuildingEntity(engine);
		inflatePickupModel(builder, pickJsonObject, type, mapGraph);
		builder.addPickUpComponentAsWeapon(type, assetManager.getTexture(type.getImage()), bulletRegion)
				.finishAndAddToEngine();
	}

	private void inflatePickupModel(final EntityBuilder builder,
									final JsonObject pickJsonObject,
									final WeaponsDefinitions type, final MapGraph mapGraph) {
		Coords coord = new Coords(pickJsonObject.get(ROW).getAsInt(), pickJsonObject.get(COL).getAsInt());
		Assets.Models modelDefinition = type.getModelDefinition();
		String fileName = GameServices.BOUNDING_BOX_PREFIX + modelDefinition.getFilePath();
		ModelBoundingBox boundingBox = assetManager.get(fileName, ModelBoundingBox.class);
		GameModelInstance modelInstance = new GameModelInstance(assetManager.getModel(modelDefinition), boundingBox);
		modelInstance.transform.setTranslation(auxVector3_1.set(coord.getCol() + 0.5f, 0, coord.getRow() + 0.5f));
		modelInstance.transform.translate(0, mapGraph.getNode(coord).getHeight(), 0);
		builder.addModelInstanceComponent(modelInstance, true);
	}

	private GameModelInstance inflateEnvModelInstanceComponent(final MapGraphNode node,
															   final JsonObject envJsonObject,
															   final EnvironmentDefinitions type,
															   final EntityBuilder builder) {
		int dirIndex = envJsonObject.get(DIRECTION).getAsInt();
		float height = envJsonObject.get(HEIGHT).getAsFloat();
		GameModelInstance mi = inflateEnvironmentModelInstance(node, dirIndex, type, height);
		mi.getAdditionalRenderData().setColorWhenOutside(Color.WHITE);
		builder.addModelInstanceComponent(mi, true, type.isCastShadow());
		return mi;
	}

	private void inflateEnvSpecifiedComponent(final Coords coord,
											  final EnvironmentDefinitions type,
											  final EntityBuilder builder,
											  final Direction facingDirection) {
		int col = coord.getCol();
		int row = coord.getRow();
		int halfWidth = type.getWidth() / 2;
		int halfDepth = type.getDepth() / 2;
		if (facingDirection == NORTH || facingDirection == SOUTH) {
			int swap = halfWidth;
			halfWidth = halfDepth;
			halfDepth = swap;
		}
		Vector2 topLeft = auxVector2_1.set(col - halfWidth, row - halfDepth);
		Vector2 bottomRight = auxVector2_2.set(col + Math.max(halfWidth, 1) - 1, row + Math.max(halfDepth, 1) - 1);
		builder.addObstacleWallComponent(topLeft, bottomRight, type);
	}

	private GameModelInstance inflateEnvironmentModelInstance(final MapGraphNode node,
															  final int directionIndex,
															  final EnvironmentDefinitions type,
															  final float height) {
		String fileName = GameServices.BOUNDING_BOX_PREFIX + type.getModelDefinition().getFilePath();
		ModelBoundingBox box = assetManager.get(fileName, ModelBoundingBox.class);
		GameModelInstance modelInstance = new GameModelInstance(assetManager.getModel(type.getModelDefinition()), box, type.getModelDefinition());
		Direction direction = Direction.values()[directionIndex];
		modelInstance.transform.setTranslation(auxVector3_1.set(node.getCol() + 0.5f, 0, node.getRow() + 0.5f));
		modelInstance.transform.rotate(Vector3.Y, -1 * direction.getDirection(auxVector2_1).angleDeg());
		modelInstance.transform.translate(type.getOffset(auxVector3_1));
		EnvironmentDefinitions.handleEvenSize(type, modelInstance, direction);
		modelInstance.transform.translate(0, node.getHeight() + height, 0);
		return modelInstance;
	}

	private Dimension inflateNodes(final JsonObject tilesJsonObject) {
		Dimension mapSize = new Dimension(tilesJsonObject.get(WIDTH).getAsInt(), tilesJsonObject.get(DEPTH).getAsInt());
		String matrix = tilesJsonObject.get(MATRIX).getAsString();
		byte[] matrixByte = Base64.getDecoder().decode(matrix.getBytes());
		floorModel.calculateBoundingBox(auxBoundingBox);
		IntStream.range(0, mapSize.height).forEach(row ->
				IntStream.range(0, mapSize.width).forEach(col -> {
					byte currentValue = matrixByte[row * mapSize.width + col % mapSize.width];
					if (currentValue != 0) {
						inflateNode(row, col, currentValue);
					}
				}));
		return mapSize;
	}

	private void inflateHeights(final JsonObject mapJsonObject, final MapGraph mapGraph) {
		JsonElement heightsElement = mapJsonObject.get(TILES).getAsJsonObject().get(HEIGHTS);
		Optional.ofNullable(heightsElement).ifPresent(element -> {
			JsonArray heights = element.getAsJsonArray();
			heights.forEach(jsonElement -> {
				JsonObject tileJsonObject = jsonElement.getAsJsonObject();
				MapGraphNode node = getNodeByJson(mapGraph, tileJsonObject);
				float height = tileJsonObject.get(HEIGHT).getAsFloat();
				node.setHeight(height);
				Optional.ofNullable(node.getEntity()).ifPresent(e ->
						ComponentsMapper.modelInstance.get(e).getModelInstance().transform.translate(0, height, 0));
			});
			heights.forEach(jsonElement -> {
				JsonObject tileJsonObject = jsonElement.getAsJsonObject();
				MapGraphNode node = getNodeByJson(mapGraph, tileJsonObject);
				float height = tileJsonObject.get(HEIGHT).getAsFloat();
				inflateWalls(tileJsonObject, node, height, mapGraph);
			});
		});
	}

	private MapGraphNode getNodeByJson(final MapGraph mapGraph, final JsonObject tileJsonObject) {
		int row = tileJsonObject.get(ROW).getAsInt();
		int col = tileJsonObject.get(COL).getAsInt();
		return mapGraph.getNode(col, row);
	}

	private void inflateWalls(final JsonObject tileJsonObject,
							  final MapGraphNode node,
							  final float height,
							  final MapGraph mapGraph) {
		inflateEastWall(tileJsonObject, node, height, mapGraph);
		inflateSouthWall(tileJsonObject, node, height, mapGraph);
		inflateWestWall(tileJsonObject, node, height, mapGraph);
		inflateNorthWall(tileJsonObject, node, height, mapGraph);
	}

	private void inflateEastWall(final JsonObject tileJsonObject,
								 final MapGraphNode node,
								 final float height,
								 final MapGraph mapGraph) {
		int row = node.getRow();
		int col = node.getCol();
		if (col >= mapGraph.getWidth() - 1) return;
		int eastCol = col + 1;
		JsonElement east = tileJsonObject.get(EAST);
		if (height != mapGraph.getNode(eastCol, node.getRow()).getHeight() && east != null) {
			JsonObject asJsonObject = east.getAsJsonObject();
			Assets.SurfaceTextures definition = Assets.SurfaceTextures.valueOf(asJsonObject.get(MapJsonKeys.TEXTURE).getAsString());
			if (definition != MISSING) {
				MapNodeData nodeData = new MapNodeData(row, col, MapNodesTypes.OBSTACLE_KEY_DIAGONAL_FORBIDDEN);
				NodeWalls walls = nodeData.getWalls();
				walls.setEastWall(WallCreator.createEastWall(nodeData, wallCreator.getWallModel(), assetManager, definition));
				MapNodeData eastNode = new MapNodeData(row, eastCol, MapNodesTypes.OBSTACLE_KEY_DIAGONAL_FORBIDDEN);
				nodeData.lift(height);
				eastNode.setHeight(mapGraph.getNode(eastNode.getCoords()).getHeight());
				float vScale = asJsonObject.has(MapJsonKeys.V_SCALE) ? asJsonObject.get(MapJsonKeys.V_SCALE).getAsFloat() : 0;
				float hOffset = asJsonObject.has(H_OFFSET) ? asJsonObject.get(H_OFFSET).getAsFloat() : 0;
				float vOffset = asJsonObject.has(V_OFFSET) ? asJsonObject.get(V_OFFSET).getAsFloat() : 0;
				WallCreator.adjustWallBetweenEastAndWest(eastNode, nodeData, vScale, hOffset, vOffset);
				boolean westHigherThanEast = node.getHeight() > eastNode.getHeight();
				inflateWall(walls.getEastWall(), westHigherThanEast ? eastNode : nodeData);
			}
		}
	}

	private void inflateSouthWall(final JsonObject tileJsonObject,
								  final MapGraphNode node,
								  final float height,
								  final MapGraph mapGraph) {
		int row = node.getRow();
		int col = node.getCol();
		if (row >= mapGraph.getDepth() - 1) return;
		int southNodeRow = row + 1;
		JsonElement south = tileJsonObject.get(MapJsonKeys.SOUTH);
		if (height != mapGraph.getNode(col, southNodeRow).getHeight() && south != null) {
			JsonObject asJsonObject = south.getAsJsonObject();
			Assets.SurfaceTextures definition = Assets.SurfaceTextures.valueOf(asJsonObject.get(MapJsonKeys.TEXTURE).getAsString());
			if (definition != MISSING) {
				MapNodeData nodeData = new MapNodeData(row, col, MapNodesTypes.OBSTACLE_KEY_DIAGONAL_FORBIDDEN);
				NodeWalls walls = nodeData.getWalls();
				walls.setSouthWall(WallCreator.createSouthWall(nodeData, wallCreator.getWallModel(), assetManager, definition));
				MapNodeData southNode = new MapNodeData(southNodeRow, nodeData.getCoords().getCol(), MapNodesTypes.OBSTACLE_KEY_DIAGONAL_FORBIDDEN);
				nodeData.lift(height);
				southNode.setHeight(mapGraph.getNode(southNode.getCoords()).getHeight());
				float vScale = asJsonObject.has(MapJsonKeys.V_SCALE) ? asJsonObject.get(MapJsonKeys.V_SCALE).getAsFloat() : 0;
				float hOffset = asJsonObject.has(H_OFFSET) ? asJsonObject.get(H_OFFSET).getAsFloat() : 0;
				float vOffset = asJsonObject.has(V_OFFSET) ? asJsonObject.get(V_OFFSET).getAsFloat() : 0;
				WallCreator.adjustWallBetweenNorthAndSouth(southNode, nodeData, vScale, hOffset, vOffset);
				boolean northHigherThanSouth = node.getHeight() > southNode.getHeight();
				inflateWall(walls.getSouthWall(), northHigherThanSouth ? southNode : nodeData);
			}
		}
	}

	private void inflateWestWall(final JsonObject tileJsonObject,
								 final MapGraphNode node,
								 final float height,
								 final MapGraph mapGraph) {
		int col = node.getCol();
		int row = node.getRow();
		if (col == 0) return;
		int westNodeCol = col - 1;
		JsonElement west = tileJsonObject.get(WEST);
		if (westNodeCol >= 0 && height != mapGraph.getNode(westNodeCol, row).getHeight() && west != null) {
			JsonObject asJsonObject = west.getAsJsonObject();
			Assets.SurfaceTextures definition = Assets.SurfaceTextures.valueOf(asJsonObject.get(MapJsonKeys.TEXTURE).getAsString());
			if (definition != MISSING) {
				MapNodeData nodeData = new MapNodeData(row, col, MapNodesTypes.OBSTACLE_KEY_DIAGONAL_FORBIDDEN);
				NodeWalls walls = nodeData.getWalls();
				walls.setWestWall(WallCreator.createWestWall(nodeData, wallCreator.getWallModel(), assetManager, definition));
				MapNodeData westNodeData = new MapNodeData(nodeData.getCoords().getRow(), westNodeCol, MapNodesTypes.OBSTACLE_KEY_DIAGONAL_FORBIDDEN);
				nodeData.lift(height);
				westNodeData.setHeight(mapGraph.getNode(westNodeData.getCoords()).getHeight());
				float vScale = asJsonObject.has(MapJsonKeys.V_SCALE) ? asJsonObject.get(MapJsonKeys.V_SCALE).getAsFloat() : 0;
				float hOffset = asJsonObject.has(H_OFFSET) ? asJsonObject.get(H_OFFSET).getAsFloat() : 0;
				float vOffset = asJsonObject.has(V_OFFSET) ? asJsonObject.get(V_OFFSET).getAsFloat() : 0;
				WallCreator.adjustWallBetweenEastAndWest(nodeData, westNodeData, vScale, hOffset, vOffset);
				boolean eastHigherThanWest = node.getHeight() > westNodeData.getHeight();
				inflateWall(walls.getWestWall(), eastHigherThanWest ? westNodeData : nodeData);
			}
		}
	}

	private void inflateNorthWall(final JsonObject tileJsonObject,
								  final MapGraphNode node,
								  final float height,
								  final MapGraph mapGraph) {
		int col = node.getCol();
		int row = node.getRow();
		if (row == 0) return;
		int northNodeRow = row - 1;
		JsonElement north = tileJsonObject.get(MapJsonKeys.NORTH);
		if (height != mapGraph.getNode(col, northNodeRow).getHeight() && north != null) {
			JsonObject asJsonObject = north.getAsJsonObject();
			Assets.SurfaceTextures definition = Assets.SurfaceTextures.valueOf(asJsonObject.get(MapJsonKeys.TEXTURE).getAsString());
			if (definition != MISSING) {
				MapNodeData n = new MapNodeData(row, col, MapNodesTypes.OBSTACLE_KEY_DIAGONAL_FORBIDDEN);
				n.getWalls().setNorthWall(WallCreator.createNorthWall(n, wallCreator.getWallModel(), assetManager, definition));
				MapNodeData northNode = new MapNodeData(northNodeRow, n.getCoords().getCol(), MapNodesTypes.OBSTACLE_KEY_DIAGONAL_FORBIDDEN);
				n.lift(height);
				northNode.setHeight(mapGraph.getNode(northNode.getCoords()).getHeight());
				float vScale = asJsonObject.has(MapJsonKeys.V_SCALE) ? asJsonObject.get(MapJsonKeys.V_SCALE).getAsFloat() : 0;
				float hOffset = asJsonObject.has(H_OFFSET) ? asJsonObject.get(H_OFFSET).getAsFloat() : 0;
				float vOffset = asJsonObject.has(V_OFFSET) ? asJsonObject.get(V_OFFSET).getAsFloat() : 0;
				WallCreator.adjustWallBetweenNorthAndSouth(n, northNode, vScale, hOffset, vOffset);
				boolean northHigherThanSouth = node.getHeight() > northNode.getHeight();
				inflateWall(n.getWalls().getNorthWall(), northHigherThanSouth ? northNode : n);
			}
		}
	}

	private void inflateWall(final Wall wall, final MapNodeData parentNode) {
		BoundingBox bBox = wall.getModelInstance().calculateBoundingBox(new BoundingBox());
		avoidZeroDimensions(bBox);
		bBox.mul(auxMatrix.set(wall.getModelInstance().transform).setTranslation(Vector3.Zero));
		GameModelInstance modelInstance = new GameModelInstance(wall.getModelInstance(), bBox, true, Color.WHITE);
		EntityBuilder.beginBuildingEntity(engine).addModelInstanceComponent(modelInstance, true, false)
				.addWallComponent(parentNode)
				.finishAndAddToEngine();
	}

	private void avoidZeroDimensions(final BoundingBox bBox) {
		Vector3 center = bBox.getCenter(auxVector3_1);
		if (bBox.getWidth() == 0) {
			center.x += 0.01;
		}
		if (bBox.getHeight() == 0) {
			center.y += 0.01;
		}
		if (bBox.getDepth() == 0) {
			center.z += 0.01;
		}
		bBox.ext(center);
	}

	private void inflateNode(final int row, final int col, final byte chr) {
		Assets.SurfaceTextures definition = Assets.SurfaceTextures.values()[chr - 1];
		if (definition != MISSING) {
			GameModelInstance mi = new GameModelInstance(floorModel);
			defineNode(row, col, definition, mi);
			EntityBuilder.beginBuildingEntity(engine).addModelInstanceComponent(mi, true)
					.addFloorComponent()
					.finishAndAddToEngine();
		}
	}

	private void defineNode(final int row, final int col, final Assets.SurfaceTextures definition, final GameModelInstance mi) {
		mi.materials.get(0).set(TextureAttribute.createDiffuse(assetManager.getTexture(definition)));
		mi.transform.setTranslation(auxVector3_1.set(col + 0.5f, 0, row + 0.5f));
		mi.getAdditionalRenderData().setBoundingBox(mi.calculateBoundingBox(auxBoundingBox));
		mi.getAdditionalRenderData().setColorWhenOutside(Color.WHITE);
	}

	private void inflateEnemy(final JsonObject characterJsonObject, final MapGraph mapGraph) {
		Enemies type;
		try {
			String asString = characterJsonObject.get(TYPE).getAsString();
			type = Optional.of(Arrays.stream(Enemies.values())
							.filter(def -> def.name().equalsIgnoreCase(asString))
							.findFirst())
					.orElseThrow()
					.get();
		} catch (Exception e) {
			int index = characterJsonObject.get(TYPE).getAsInt();
			type = Enemies.values()[index];
		}
		int skill = 5;
		Animation<AtlasRegion> bulletAnimation = enemyBulletsTextureRegions.get(type);
		if (type.getPrimaryAttack() != null && !enemyBulletsTextureRegions.containsKey(type)) {
			String name = EnemyWeaponsDefinitions.ENERGY_BALL.name().toLowerCase();
			Array<AtlasRegion> regions = assetManager.getAtlas(ANUBIS).findRegions(name);
			bulletAnimation = new Animation<>(type.getPrimaryAttack().getFrameDuration(), regions);
			enemyBulletsTextureRegions.put(type, bulletAnimation);
		}
		EntityBuilder builder = EntityBuilder.beginBuildingEntity(engine).addEnemyComponent(type, skill, bulletAnimation);
		Entity player = engine.getEntitiesFor(Family.all(PlayerComponent.class).get()).first();
		Vector3 position = inflateCharacterPosition(characterJsonObject, mapGraph);
		auxCharacterSoundData.set(type.getPainSound(), type.getDeathSound(), type.getStepSound());
		CharacterSkillsParameters skills = new CharacterSkillsParameters(
				type.getHealth().get(skill - 1),
				type.getAgility().get(skill - 1),
				type.getStrength().get(skill - 1),
				type.getAccuracy() != null ? type.getAccuracy()[skill - 1] : null);
		CharacterData data = new CharacterData(position, Direction.values()[characterJsonObject.get(DIRECTION).getAsInt()], skills, auxCharacterSoundData);
		addCharBaseComponents(builder, data, type, type.getAtlasDefinition());
		Texture skillFlowerTexture = assetManager.getTexture(Assets.UiTextures.SKILL_FLOWER_CENTER_IDLE);
		position.y += type.getHeight() + EnemySystemImpl.SKILL_FLOWER_HEIGHT_RELATIVE;
		builder.addSimpleDecalComponent(position, skillFlowerTexture, true, true);
		Entity entity = builder.finishAndAddToEngine();
		ComponentsMapper.character.get(entity).setTarget(player);
		SimpleDecalComponent simpleDecalComponent = entity.getComponent(SimpleDecalComponent.class);
		simpleDecalComponent.setAffectedByFow(true);
		addSkillFlowerDecal(simpleDecalComponent, Assets.UiTextures.SKILL_FLOWER_1, position);
		addSkillFlowerDecal(simpleDecalComponent, Assets.UiTextures.SKILL_FLOWER_2, position);
		addSkillFlowerDecal(simpleDecalComponent, Assets.UiTextures.SKILL_FLOWER_3, position);
		addSkillFlowerDecal(simpleDecalComponent, Assets.UiTextures.SKILL_FLOWER_4, position);
		addSkillFlowerDecal(simpleDecalComponent, Assets.UiTextures.SKILL_FLOWER_5, position);
		addSkillFlowerDecal(simpleDecalComponent, Assets.UiTextures.SKILL_FLOWER_6, position);
		addSkillFlowerDecal(simpleDecalComponent, Assets.UiTextures.SKILL_FLOWER_7, position);
		addSkillFlowerDecal(simpleDecalComponent, Assets.UiTextures.SKILL_FLOWER_8, position);
	}

	private void addSkillFlowerDecal(final SimpleDecalComponent simpleDecalComponent,
									 final Assets.UiTextures skillFlower,
									 final Vector3 position) {
		RelatedDecal skillFlowerDecal = RelatedDecal.newDecal(new TextureRegion(assetManager.getTexture(skillFlower)), true);
		skillFlowerDecal.setScale(BILLBOARD_SCALE);
		skillFlowerDecal.setPosition(position);
		simpleDecalComponent.addRelatedDecal(skillFlowerDecal);
	}

	private void inflatePlayer(final JsonObject characterJsonObject, final MapGraph mapGraph) {
		Weapon weapon = initializeStartingWeapon();
		CharacterAnimations general = assetManager.get(PLAYER_GENERIC.name());
		EntityBuilder builder = EntityBuilder.beginBuildingEntity(engine).addPlayerComponent(weapon, general);
		Vector3 position = inflateCharacterPosition(characterJsonObject, mapGraph);
		auxCharacterSoundData.set(Sounds.PLAYER_PAIN, Sounds.PLAYER_DEATH, Sounds.STEP);
		CharacterSkillsParameters skills = new CharacterSkillsParameters(
				PLAYER_HEALTH,
				Agility.HIGH,
				new Strength(1, 3),
				Accuracy.LOW);
		CharacterData data = new CharacterData(
				position,
				Direction.values()[characterJsonObject.get(DIRECTION).getAsInt()],
				skills,
				auxCharacterSoundData);
		Assets.Atlases atlas = findByRelatedWeapon(DefaultGameSettings.STARTING_WEAPON);
		addCharBaseComponents(builder, data, CharacterTypes.PLAYER.getDefinitions()[0], atlas);
		builder.finishAndAddToEngine();
	}

	private Vector3 inflateCharacterPosition(final JsonElement characterJsonElement, final MapGraph mapGraph) {
		JsonObject asJsonObject = characterJsonElement.getAsJsonObject();
		int col = asJsonObject.get(COL).getAsInt();
		int row = asJsonObject.get(ROW).getAsInt();
		float floorHeight = mapGraph.getNode(col, row).getHeight();
		return auxVector3_1.set(col + 0.5f, floorHeight + BILLBOARD_Y, row + 0.5f);
	}

	private Weapon initializeStartingWeapon() {
		Weapon weapon = Pools.obtain(Weapon.class);
		Texture image = assetManager.getTexture(DefaultGameSettings.STARTING_WEAPON.getImage());
		weapon.init(DefaultGameSettings.STARTING_WEAPON, 0, 0, image);
		return weapon;
	}

	@Override
	public void dispose() {
		floorModel.dispose();
		wallCreator.dispose();
	}

	public void reset(final PooledEngine engine) {
		this.engine = engine;
	}
}
