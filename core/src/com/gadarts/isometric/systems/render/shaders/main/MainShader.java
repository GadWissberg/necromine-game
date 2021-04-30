package com.gadarts.isometric.systems.render.shaders.main;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.LightComponent;
import com.gadarts.isometric.components.ModelInstanceComponent;
import com.gadarts.isometric.components.WallComponent;
import com.gadarts.isometric.components.model.AdditionalRenderData;
import com.gadarts.isometric.components.model.GameModelInstance;
import com.gadarts.isometric.systems.render.WorldEnvironment;
import com.gadarts.isometric.utils.map.MapGraph;
import com.gadarts.necromine.assets.Assets;

import java.util.List;

/**
 * Handles all main shader's uniforms.
 */
public class MainShader extends DefaultShader {

	private static final String UNIFORM_LIGHTS_POSITIONS = "u_lights_positions[0]";
	private static final String UNIFORM_LIGHTS_EXTRA_DATA = "u_lights_extra_data[0]";
	private static final String UNIFORM_NUMBER_OF_LIGHTS = "u_number_of_lights";
	private static final String UNIFORM_MODEL_WIDTH = "u_model_width";
	private static final String UNIFORM_MODEL_HEIGHT = "u_model_height";
	private static final String UNIFORM_MODEL_DEPTH = "u_model_depth";
	private static final String UNIFORM_MODEL_X = "u_model_x";
	private static final String UNIFORM_MODEL_Y = "u_model_y";
	private static final String UNIFORM_MODEL_Z = "u_model_z";
	private static final String UNIFORM_FOW_MAP = "u_fow_map[0]";
	private static final String UNIFORM_AMBIENT_LIGHT = "u_ambient_light";
	private static final String UNIFORM_COLOR_WHEN_OUTSIDE = "u_color_when_outside";
	private static final String UNIFORM_APPLY_WALL_AMBIENT_OCCLUSION = "u_apply_wall_ambient_occlusion";
	private static final String UNIFORM_APPLY_FLOOR_AMBIENT_OCCLUSION = "u_apply_floor_ambient_occlusion";
	private static final String UNIFORM_SKIP_COLOR = "u_skip_color";
	private static final String UNIFORM_COMPLETE_BLACK = "u_complete_black";
	private static final int MAX_LIGHTS = 8;
	private static final int LIGHT_EXTRA_DATA_SIZE = 2;
	private static final Vector3 auxVector = new Vector3();
	private static final int MODEL_MAX_SIZE = 4 * 4;
	private static final int MASK_BOTTOM_RIGHT = 0B0000000010;
	private static final int MASK_BOTTOM = 0B0000000100;
	private static final int MASK_BOTTOM_LEFT = 0B0000001000;
	private static final int MASK_RIGHT = 0B0000010000;
	private static final int MASK_LEFT = 0B0000100000;
	private static final int MASK_TOP_RIGHT = 0B0001000000;
	private static final int MASK_TOP = 0B0010000000;
	private static final int MASK_TOP_LEFT = 0B0100000000;
	private final static BoundingBox auxBoundingBox = new BoundingBox();
	private final float[] lightsPositions = new float[MAX_LIGHTS * 3];
	private final float[] lightsExtraData = new float[MAX_LIGHTS * LIGHT_EXTRA_DATA_SIZE];
	private final float[] fowMapArray = new float[MODEL_MAX_SIZE];
	private final MapGraph map;
	private int lightsPositionsLocation;
	private int lightsExtraDataLocation;
	private int numberOfLightsLocation;
	private int modelWidthLocation;
	private int modelHeightLocation;
	private int modelDepthLocation;
	private int fowMapLocation;
	private int ambientLightLocation;
	private int colorWhenOutsideLocation;
	private int applyWallAmbientOcclusionLocation;
	private int applyFloorAmbientOcclusionLocation;
	private int skipColorLocation;
	private int modelXLocation;
	private int modelYLocation;
	private int modelZLocation;
	private int completeBlackLocation;

	public MainShader(final Renderable renderable,
					  final Config shaderConfig,
					  final MapGraph map) {
		super(renderable, shaderConfig);
		this.map = map;
	}

	@Override
	public void init() {
		super.init();
		fetchUniformsLocations();
		if (program.getLog().length() != 0) {
			System.out.println(program.getLog());
		}
	}

	private void fetchUniformsLocations() {
		fetchLightsRelatedUniformsLocations();
		fetchModelSizeUniformsLocations();
		fetchModelPositionUniformsLocations();
		fowMapLocation = program.getUniformLocation(UNIFORM_FOW_MAP);
		ambientLightLocation = program.getUniformLocation(UNIFORM_AMBIENT_LIGHT);
		fetchColorRelatedUniformsLocations();
		fetchAmbientOcclusionUniformsLocations();
	}

	private void fetchColorRelatedUniformsLocations() {
		colorWhenOutsideLocation = program.getUniformLocation(UNIFORM_COLOR_WHEN_OUTSIDE);
		skipColorLocation = program.getUniformLocation(UNIFORM_SKIP_COLOR);
		completeBlackLocation = program.getUniformLocation(UNIFORM_COMPLETE_BLACK);
	}

	private void fetchLightsRelatedUniformsLocations() {
		lightsPositionsLocation = program.getUniformLocation(UNIFORM_LIGHTS_POSITIONS);
		lightsExtraDataLocation = program.getUniformLocation(UNIFORM_LIGHTS_EXTRA_DATA);
		numberOfLightsLocation = program.getUniformLocation(UNIFORM_NUMBER_OF_LIGHTS);
	}

	private void fetchAmbientOcclusionUniformsLocations() {
		applyWallAmbientOcclusionLocation = program.getUniformLocation(UNIFORM_APPLY_WALL_AMBIENT_OCCLUSION);
		applyFloorAmbientOcclusionLocation = program.getUniformLocation(UNIFORM_APPLY_FLOOR_AMBIENT_OCCLUSION);
	}

	private void fetchModelPositionUniformsLocations() {
		modelXLocation = program.getUniformLocation(UNIFORM_MODEL_X);
		modelYLocation = program.getUniformLocation(UNIFORM_MODEL_Y);
		modelZLocation = program.getUniformLocation(UNIFORM_MODEL_Z);
	}

	private void fetchModelSizeUniformsLocations() {
		modelWidthLocation = program.getUniformLocation(UNIFORM_MODEL_WIDTH);
		modelHeightLocation = program.getUniformLocation(UNIFORM_MODEL_HEIGHT);
		modelDepthLocation = program.getUniformLocation(UNIFORM_MODEL_DEPTH);
	}

	@Override
	public void render(final Renderable renderable) {
		boolean cancelRender = false;
		WorldEnvironment environment = (WorldEnvironment) renderable.environment;
		Color ambientColor = environment.getAmbientColor();
		program.setUniformf(ambientLightLocation, ambientColor.r, ambientColor.g, ambientColor.b);
		Entity entity = (Entity) renderable.userData;
		GameModelInstance modelInstance = ComponentsMapper.modelInstance.get(entity).getModelInstance();
		AdditionalRenderData additionalRenderData = modelInstance.getAdditionalRenderData();
		if (renderable.userData != null && additionalRenderData.isAffectedByLight()) {
			cancelRender = applyAdditionalRenderData(renderable, modelInstance);
		} else {
			program.setUniformi(numberOfLightsLocation, -1);
		}
		if (!cancelRender) {
			super.render(renderable);
		}
	}

	private boolean applyAdditionalRenderData(final Renderable renderable, final GameModelInstance modelInstance) {
		Entity entity = (Entity) renderable.userData;
		ModelInstanceComponent modelInstanceComponent = ComponentsMapper.modelInstance.get(entity);
		AdditionalRenderData additionalRenderData = modelInstanceComponent.getModelInstance().getAdditionalRenderData();
		program.setUniformf(colorWhenOutsideLocation, additionalRenderData.getColorWhenOutside());
		applyAmbientOcclusion(entity);
		applyLights(additionalRenderData);
		applySkipColor(modelInstance);
		return applyFow(renderable, additionalRenderData);
	}

	private void applySkipColor(final GameModelInstance modelInstance) {
		Assets.Models modelDefinition = modelInstance.getModelDefinition();
		if (modelDefinition != null && modelDefinition.getSkipColor() != null) {
			Color skipColor = modelDefinition.getSkipColor();
			program.setUniformf(skipColorLocation, auxVector.set(skipColor.r, skipColor.g, skipColor.b));
		} else {
			program.setUniformf(skipColorLocation, auxVector.setZero());
		}
	}

	private void applyAmbientOcclusion(final Entity entity) {
		if (ComponentsMapper.wall.has(entity)) {
			applyWallAmbientOcclusion(entity);
		} else if (ComponentsMapper.floor.has(entity)) {
			program.setUniformi(applyWallAmbientOcclusionLocation, 0);
			program.setUniformi(applyFloorAmbientOcclusionLocation, ComponentsMapper.floor.get(entity).getAmbientOcclusion());
		} else {
			program.setUniformi(applyWallAmbientOcclusionLocation, 0);
			program.setUniformi(applyFloorAmbientOcclusionLocation, 0);
		}
	}

	private void applyWallAmbientOcclusion(final Entity entity) {
		WallComponent wallComponent = ComponentsMapper.wall.get(entity);
		int row = wallComponent.getParentNode().getRow();
		int col = wallComponent.getParentNode().getCol();
		program.setUniformf(modelYLocation, map.getNode(col, row).getHeight());
		program.setUniformi(applyWallAmbientOcclusionLocation, 1);
		program.setUniformi(applyFloorAmbientOcclusionLocation, 0);
	}

	private void applyLights(final AdditionalRenderData additionalRenderData) {
		List<Entity> nearbyLights = additionalRenderData.getNearbyLights();
		int size = nearbyLights.size();
		program.setUniformi(numberOfLightsLocation, size);
		if (size > 0) {
			for (int i = 0; i < size; i++) {
				insertToLightsArray(nearbyLights, i);
			}
			program.setUniform3fv(lightsPositionsLocation, lightsPositions, 0, size * 3);
			program.setUniform2fv(lightsExtraDataLocation, lightsExtraData, 0, size * LIGHT_EXTRA_DATA_SIZE);
		}
	}

	private boolean applyFow(final Renderable renderable, final AdditionalRenderData additionalRenderData) {
		Vector3 position = renderable.worldTransform.getTranslation(auxVector);
		position.set(Math.max(position.x, 0), 0, Math.max(position.z, 0));
		BoundingBox boundingBox = additionalRenderData.getBoundingBox(auxBoundingBox);
		int width = MathUtils.ceil(boundingBox.getWidth());
		int height = MathUtils.ceil(boundingBox.getHeight());
		int depth = MathUtils.ceil(boundingBox.getDepth());
		int x = (int) (position.x - width / 2f);
		int z = (int) (position.z - depth / 2f);
		boolean isWholeHidden = true;
		for (int row = 0; row < depth; row++) {
			for (int col = 0; col < width; col++) {
				int mapRow = Math.max(z + row, 0);
				int mapCol = Math.max(x + col, 0);
				boolean isValidCoordinate = mapRow < map.getDepth() && mapCol < map.getWidth();
				int fowMapValue = isValidCoordinate ? map.getFowMap()[mapRow][mapCol] : 0;
				int currentIndex = row * width + col;
				if (fowMapValue == 1) {
					fowMapArray[currentIndex] = calculateFowValueBasedOnNeighbours(mapRow, mapCol);
				} else {
					fowMapArray[currentIndex] = 0;
				}
				isWholeHidden &= !(fowMapValue == 1);
			}
			if (isWholeHidden) {
				Entity entity = (Entity) renderable.userData;
				boolean isFloor = ComponentsMapper.floor.has(entity);
				ModelInstanceComponent modelInstanceComponent = ComponentsMapper.modelInstance.get(entity);
				float y = modelInstanceComponent.getModelInstance().transform.getTranslation(auxVector).y;
				if (!ComponentsMapper.wall.has(entity) && !(isFloor && y > 0)) {
					return true;
				} else {
					program.setUniformi(completeBlackLocation, 1);
				}
			}
		}
		if (!isWholeHidden) {
			program.setUniformi(completeBlackLocation, 0);
		}
		program.setUniformi(modelWidthLocation, width);
		program.setUniformi(modelHeightLocation, height);
		program.setUniformi(modelDepthLocation, depth);
		program.setUniformi(modelXLocation, x);
		program.setUniformi(modelZLocation, z);
		program.setUniform1fv(fowMapLocation, fowMapArray, 0, width * depth);
		return false;
	}

	private float calculateFowValueBasedOnNeighbours(final int relativeRow, final int relativeCol) {
		int result = 1;
		result = applyTopMasks(relativeRow, relativeCol, result);
		result = applyMask(relativeRow, relativeCol - 1, result, MASK_LEFT);
		result = applyMask(relativeRow, relativeCol + 1, result, MASK_RIGHT);
		result = applyBottomMasks(relativeRow, relativeCol, result);
		return result;
	}

	private int applyBottomMasks(final int relativeRow, final int relativeCol, int result) {
		result = applyMask(relativeRow + 1, relativeCol - 1, result, MASK_BOTTOM_LEFT);
		result = applyMask(relativeRow + 1, relativeCol, result, MASK_BOTTOM);
		result = applyMask(relativeRow + 1, relativeCol + 1, result, MASK_BOTTOM_RIGHT);
		return result;
	}

	private int applyTopMasks(final int relativeRow, final int relativeCol, int result) {
		result = applyMask(relativeRow - 1, relativeCol - 1, result, MASK_TOP_LEFT);
		result = applyMask(relativeRow - 1, relativeCol, result, MASK_TOP);
		result = applyMask(relativeRow - 1, relativeCol + 1, result, MASK_TOP_RIGHT);
		return result;
	}

	private int applyMask(final int relativeRow, final int relativeCol, int result, final int mask) {
		if (relativeRow < 0 || relativeCol < 0) return result;
		int[][] fowMap = map.getFowMap();
		if (fowMap[Math.min(relativeRow, fowMap.length - 1)][Math.min(relativeCol, fowMap[0].length - 1)] == 1) {
			result |= mask;
		}
		return result;
	}

	private void insertToLightsArray(final List<Entity> nearbyLights, final int i) {
		insertLightPositionToArray(nearbyLights, i);
		LightComponent lightComponent = ComponentsMapper.light.get(nearbyLights.get(i));
		int extraDataIndex = i * LIGHT_EXTRA_DATA_SIZE;
		lightsExtraData[extraDataIndex] = lightComponent.getIntensity();
		lightsExtraData[extraDataIndex + 1] = lightComponent.getRadius();
	}

	private void insertLightPositionToArray(final List<Entity> nearbyLights, final int i) {
		LightComponent lightComponent = ComponentsMapper.light.get(nearbyLights.get(i));
		Vector3 position = lightComponent.getPosition(auxVector);
		int positionIndex = i * 3;
		lightsPositions[positionIndex] = position.x;
		lightsPositions[positionIndex + 1] = position.y;
		lightsPositions[positionIndex + 2] = position.z;
	}

}
