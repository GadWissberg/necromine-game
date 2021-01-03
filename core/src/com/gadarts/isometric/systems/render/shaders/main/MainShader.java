package com.gadarts.isometric.systems.render.shaders.main;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.LightComponent;
import com.gadarts.isometric.components.model.AdditionalRenderData;

import java.util.List;

public class MainShader extends DefaultShader {
	public static final String UNIFORM_LIGHTS_POSITIONS = "u_lights_positions[0]";
	public static final String UNIFORM_LIGHTS_EXTRA_DATA = "u_lights_extra_data[0]";
	public static final String UNIFORM_NUMBER_OF_LIGHTS = "u_number_of_lights";
	public static final String UNIFORM_MODEL_WIDTH = "u_model_width";
	public static final String UNIFORM_MODEL_HEIGHT = "u_model_height";
	public static final String UNIFORM_MODEL_X = "u_model_x";
	public static final String UNIFORM_MODEL_Y = "u_model_y";
	public static final String UNIFORM_FOW_MAP = "u_fow_map[0]";
	public static final int MAX_LIGHTS = 8;
	public static final int LIGHT_EXTRA_DATA_SIZE = 2;
	private static final Vector3 auxVector = new Vector3();
	private static final int MODEL_MAX_SIZE = 4 * 4;

	private final float[] lightsPositions = new float[MAX_LIGHTS * 3];
	private final float[] lightsExtraData = new float[MAX_LIGHTS * LIGHT_EXTRA_DATA_SIZE];
	private final float[] fowMapArray = new float[MODEL_MAX_SIZE];
	private final int[][] fowMap;
	private int lightsPositionsLocation;
	private int lightsExtraDataLocation;
	private int numberOfLightsLocation;
	private int modelWidthLocation;
	private int modelHeightLocation;
	private int fowMapLocation;
	private int modelXLocation;
	private int modelYLocation;

	public MainShader(final Renderable renderable, final Config shaderConfig, final int[][] fowMap) {
		super(renderable, shaderConfig);
		this.fowMap = fowMap;
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
		lightsPositionsLocation = program.getUniformLocation(UNIFORM_LIGHTS_POSITIONS);
		lightsExtraDataLocation = program.getUniformLocation(UNIFORM_LIGHTS_EXTRA_DATA);
		numberOfLightsLocation = program.getUniformLocation(UNIFORM_NUMBER_OF_LIGHTS);
		modelWidthLocation = program.getUniformLocation(UNIFORM_MODEL_WIDTH);
		modelHeightLocation = program.getUniformLocation(UNIFORM_MODEL_HEIGHT);
		modelXLocation = program.getUniformLocation(UNIFORM_MODEL_X);
		modelYLocation = program.getUniformLocation(UNIFORM_MODEL_Y);
		fowMapLocation = program.getUniformLocation(UNIFORM_FOW_MAP);
	}

	@Override
	public void render(final Renderable renderable) {
		boolean cancelRender = false;
		if (renderable.userData != null) {
			cancelRender = applyAdditionalRenderData(renderable);
		} else {
			program.setUniformi(numberOfLightsLocation, -1);
		}
		if (!cancelRender) {
			super.render(renderable);
		}
	}

	private boolean applyAdditionalRenderData(final Renderable renderable) {
		AdditionalRenderData additionalRenderData = (AdditionalRenderData) renderable.userData;
		applyLights(additionalRenderData);
		return applyFow(renderable, additionalRenderData);
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
		BoundingBox boundingBox = additionalRenderData.getBoundingBox();
		int width = MathUtils.ceil(boundingBox.getWidth());
		int height = MathUtils.ceil(boundingBox.getDepth());
		int x = (int) (position.x - width / 2f);
		int z = (int) (position.z - height / 2f);
		boolean isWholeHidden = true;
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				int relativeRow = Math.max(z + row, 0);
				int relativeCol = Math.max(x + col, 0);
				int fowMapValue = fowMap[relativeRow][relativeCol];
				fowMapArray[row * width + col] = fowMapValue;
				isWholeHidden &= !(fowMapValue == 1);
			}
		}
		if (isWholeHidden) {
			return true;
		}
		program.setUniformi(modelWidthLocation, width);
		program.setUniformi(modelHeightLocation, height);
		program.setUniformi(modelXLocation, x);
		program.setUniformi(modelYLocation, z);
		program.setUniform1fv(fowMapLocation, fowMapArray, 0, width * height);
		return false;
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
