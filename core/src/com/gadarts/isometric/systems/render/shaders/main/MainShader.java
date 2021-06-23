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
import com.gadarts.necromine.model.Coords;

import java.util.List;

import static com.gadarts.isometric.systems.render.shaders.main.UniformsLocationsHandler.*;

/**
 * Handles all main shader's uniforms.
 */
public class MainShader extends DefaultShader {

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
	private final UniformsLocationsHandler uniformsLocationsHandler = new UniformsLocationsHandler();

	public MainShader(final Renderable renderable,
					  final Config shaderConfig,
					  final MapGraph map) {
		super(renderable, shaderConfig);
		this.map = map;
	}

	@Override
	public void init() {
		super.init();
		uniformsLocationsHandler.fetchUniformsLocations(program);
		if (program.getLog().length() != 0) {
			System.out.println(program.getLog());
		}
	}


	@Override
	public void render(final Renderable renderable) {
		boolean cancelRender = false;
		WorldEnvironment environment = (WorldEnvironment) renderable.environment;
		Color ambColor = environment.getAmbientColor();
		program.setUniformf(uniformsLocationsHandler.getLocation(UNIFORM_AMBIENT_LIGHT), ambColor.r, ambColor.g, ambColor.b);
		Entity entity = (Entity) renderable.userData;
		GameModelInstance modelInstance = ComponentsMapper.modelInstance.get(entity).getModelInstance();
		AdditionalRenderData additionalRenderData = modelInstance.getAdditionalRenderData();
		if (renderable.userData != null && additionalRenderData.isAffectedByLight()) {
			cancelRender = applyAdditionalRenderData(renderable, modelInstance);
		} else {
			program.setUniformi(uniformsLocationsHandler.getLocation(UNIFORM_NUMBER_OF_LIGHTS), -1);
		}
		if (!cancelRender) {
			super.render(renderable);
		}
	}

	private boolean applyAdditionalRenderData(final Renderable renderable, final GameModelInstance modelInstance) {
		ModelInstanceComponent modelInstanceComponent = ComponentsMapper.modelInstance.get((Entity) renderable.userData);
		AdditionalRenderData renderData = modelInstanceComponent.getModelInstance().getAdditionalRenderData();
		int location = uniformsLocationsHandler.getLocation(UNIFORM_COLOR_WHEN_OUTSIDE);
		program.setUniformf(location, renderData.getColorWhenOutside());
		applyAmbientOcclusion((Entity) renderable.userData);
		applyLights(renderData);
		applySkipColor(modelInstance);
		return applyFow(renderable, renderData);
	}

	private void applySkipColor(final GameModelInstance modelInstance) {
		Assets.Models modelDefinition = modelInstance.getModelDefinition();
		int skipColorLocation = uniformsLocationsHandler.getLocation(UNIFORM_SKIP_COLOR);
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
			applyFloorAmbientOcclusion(entity);
		} else {
			program.setUniformi(uniformsLocationsHandler.getLocation(UNIFORM_APPLY_WALL_AMBIENT_OCCLUSION), 0);
			program.setUniformi(uniformsLocationsHandler.getLocation(UNIFORM_APPLY_FLOOR_AMBIENT_OCCLUSION), 0);
		}
	}

	private void applyFloorAmbientOcclusion(final Entity entity) {
		program.setUniformi(uniformsLocationsHandler.getLocation(UNIFORM_APPLY_WALL_AMBIENT_OCCLUSION), 0);
		int ambientOcclusion = ComponentsMapper.floor.get(entity).getAmbientOcclusion();
		program.setUniformi(uniformsLocationsHandler.getLocation(UNIFORM_APPLY_FLOOR_AMBIENT_OCCLUSION), ambientOcclusion);
	}

	private void applyWallAmbientOcclusion(final Entity entity) {
		WallComponent wallComponent = ComponentsMapper.wall.get(entity);
		Coords coords = wallComponent.getParentNode().getCoords();
		int row = coords.getRow();
		int col = coords.getCol();
		program.setUniformf(uniformsLocationsHandler.getLocation(UNIFORM_MODEL_Y), map.getNode(col, row).getHeight());
		program.setUniformi(uniformsLocationsHandler.getLocation(UNIFORM_APPLY_WALL_AMBIENT_OCCLUSION), 1);
		program.setUniformi(uniformsLocationsHandler.getLocation(UNIFORM_APPLY_FLOOR_AMBIENT_OCCLUSION), 0);
	}

	private void applyLights(final AdditionalRenderData renderData) {
		int location = uniformsLocationsHandler.getLocation(UNIFORM_NUMBER_OF_LIGHTS);
		program.setUniformi(location, renderData.getNearbyLights().size());
		if (renderData.getNearbyLights().size() > 0) {
			for (int i = 0; i < renderData.getNearbyLights().size(); i++) {
				insertToLightsArray(renderData.getNearbyLights(), i);
			}
			applyLightsDataUniforms(renderData);
		}
	}

	private void applyLightsDataUniforms(final AdditionalRenderData renderData) {
		int lightsPosLoc = uniformsLocationsHandler.getLocation(UNIFORM_LIGHTS_POSITIONS);
		int lightsDataLoc = uniformsLocationsHandler.getLocation(UNIFORM_LIGHTS_EXTRA_DATA);
		int size = renderData.getNearbyLights().size();
		program.setUniform3fv(lightsPosLoc, lightsPositions, 0, size * 3);
		program.setUniform2fv(lightsDataLoc, lightsExtraData, 0, size * LIGHT_EXTRA_DATA_SIZE);
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
					program.setUniformi(uniformsLocationsHandler.getLocation(UNIFORM_COMPLETE_BLACK), 1);
				}
			}
		}
		if (!isWholeHidden) {
			program.setUniformi(uniformsLocationsHandler.getLocation(UNIFORM_COMPLETE_BLACK), 0);
		}
		program.setUniformi(uniformsLocationsHandler.getLocation(UNIFORM_MODEL_WIDTH), width);
		program.setUniformi(uniformsLocationsHandler.getLocation(UNIFORM_MODEL_HEIGHT), height);
		program.setUniformi(uniformsLocationsHandler.getLocation(UNIFORM_MODEL_DEPTH), depth);
		program.setUniformi(uniformsLocationsHandler.getLocation(UNIFORM_MODEL_X), x);
		program.setUniformi(uniformsLocationsHandler.getLocation(UNIFORM_MODEL_Z), z);
		program.setUniform1fv(uniformsLocationsHandler.getLocation(UNIFORM_FOW_MAP), fowMapArray, 0, width * depth);
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
		int extraDataInd = i * LIGHT_EXTRA_DATA_SIZE;
		float intensity = lightComponent.getIntensity();
		float radius = lightComponent.getRadius();
		lightsExtraData[extraDataInd] = intensity;
		lightsExtraData[extraDataInd + 1] = radius;
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
