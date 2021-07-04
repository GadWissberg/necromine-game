package com.gadarts.isometric.systems.render.shaders.main;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class UniformsLocationsHandler {
	static final String UNIFORM_MODEL_WIDTH = "u_model_width";
	static final String UNIFORM_MODEL_HEIGHT = "u_model_height";
	static final String UNIFORM_MODEL_DEPTH = "u_model_depth";
	static final String UNIFORM_MODEL_X = "u_model_x";
	static final String UNIFORM_MODEL_Y = "u_model_y";
	static final String UNIFORM_MODEL_Z = "u_model_z";
	static final String UNIFORM_FOW_MAP = "u_fow_map[0]";
	static final String UNIFORM_AMBIENT_LIGHT = "u_ambient_light";
	static final String UNIFORM_COLOR_WHEN_OUTSIDE = "u_color_when_outside";
	static final String UNIFORM_APPLY_WALL_AMBIENT_OCCLUSION = "u_apply_wall_ambient_occlusion";
	static final String UNIFORM_APPLY_FLOOR_AMBIENT_OCCLUSION = "u_apply_floor_ambient_occlusion";
	static final String UNIFORM_SKIP_COLOR = "u_skip_color";
	static final String UNIFORM_COMPLETE_BLACK = "u_complete_black";
	static final String UNIFORM_LIGHTS_POSITIONS = "u_lights_positions[0]";
	static final String UNIFORM_LIGHTS_EXTRA_DATA = "u_lights_extra_data[0]";
	static final String UNIFORM_LIGHTS_COLORS = "u_lights_colors[0]";
	static final String UNIFORM_NUMBER_OF_LIGHTS = "u_number_of_lights";
	private final Map<String, Integer> locations = new HashMap<>();

	public void fetchUniformsLocations(final ShaderProgram program) {
		fetchLightsRelatedUniformsLocations(program);
		fetchModelSizeUniformsLocations(program);
		fetchModelPositionUniformsLocations(program);
		locations.put(UNIFORM_FOW_MAP, program.getUniformLocation(UNIFORM_FOW_MAP));
		locations.put(UNIFORM_AMBIENT_LIGHT, program.getUniformLocation(UNIFORM_AMBIENT_LIGHT));
		fetchColorRelatedUniformsLocations(program);
		fetchAmbientOcclusionUniformsLocations(program);
	}

	private void fetchLightsRelatedUniformsLocations(final ShaderProgram program) {
		locations.put(UNIFORM_LIGHTS_POSITIONS, program.getUniformLocation(UNIFORM_LIGHTS_POSITIONS));
		locations.put(UNIFORM_LIGHTS_EXTRA_DATA, program.getUniformLocation(UNIFORM_LIGHTS_EXTRA_DATA));
		locations.put(UNIFORM_LIGHTS_COLORS, program.getUniformLocation(UNIFORM_LIGHTS_COLORS));
		locations.put(UNIFORM_NUMBER_OF_LIGHTS, program.getUniformLocation(UNIFORM_NUMBER_OF_LIGHTS));
	}

	private void fetchColorRelatedUniformsLocations(final ShaderProgram program) {
		locations.put(UNIFORM_COLOR_WHEN_OUTSIDE, program.getUniformLocation(UNIFORM_COLOR_WHEN_OUTSIDE));
		locations.put(UNIFORM_SKIP_COLOR, program.getUniformLocation(UNIFORM_SKIP_COLOR));
		locations.put(UNIFORM_COMPLETE_BLACK, program.getUniformLocation(UNIFORM_COMPLETE_BLACK));
	}

	private void fetchAmbientOcclusionUniformsLocations(final ShaderProgram program) {
		locations.put(
				UNIFORM_APPLY_WALL_AMBIENT_OCCLUSION,
				program.getUniformLocation(UNIFORM_APPLY_WALL_AMBIENT_OCCLUSION));
		locations.put(
				UNIFORM_APPLY_FLOOR_AMBIENT_OCCLUSION,
				program.getUniformLocation(UNIFORM_APPLY_FLOOR_AMBIENT_OCCLUSION));
	}

	private void fetchModelPositionUniformsLocations(final ShaderProgram program) {
		locations.put(UNIFORM_MODEL_X, program.getUniformLocation(UNIFORM_MODEL_X));
		locations.put(UNIFORM_MODEL_Y, program.getUniformLocation(UNIFORM_MODEL_Y));
		locations.put(UNIFORM_MODEL_Z, program.getUniformLocation(UNIFORM_MODEL_Z));
	}

	private void fetchModelSizeUniformsLocations(final ShaderProgram program) {
		locations.put(UNIFORM_MODEL_WIDTH, program.getUniformLocation(UNIFORM_MODEL_WIDTH));
		locations.put(UNIFORM_MODEL_HEIGHT, program.getUniformLocation(UNIFORM_MODEL_HEIGHT));
		locations.put(UNIFORM_MODEL_DEPTH, program.getUniformLocation(UNIFORM_MODEL_DEPTH));
	}

	public int getLocation(String uniform) {
		return locations.get(uniform);
	}
}
