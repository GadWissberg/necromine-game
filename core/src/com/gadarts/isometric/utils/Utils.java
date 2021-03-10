package com.gadarts.isometric.utils;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.collision.Ray;
import com.google.gson.JsonObject;

/**
 * General utils.
 */
public class Utils {
	/**
	 * Used for comparisons.
	 */
	public static final float EPSILON = 0.025f;
	private static final Plane floorPlane = new Plane(new Vector3(0, 1, 0), 0);

	/**
	 * Calculates the node's position based on screen mouse position.
	 *
	 * @param camera  The rendering camera.
	 * @param screenX
	 * @param screenY
	 * @param output  The result
	 * @return output argument for chaining.
	 */
	public static Vector3 calculateGridPositionFromMouse(final Camera camera,
														 final float screenX,
														 final float screenY,
														 final Vector3 output) {
		Ray ray = camera.getPickRay(screenX, screenY);
		Intersector.intersectRayPlane(ray, floorPlane, output);
		return alignPositionToGrid(output);
	}

	/**
	 * Floors x and z.
	 *
	 * @param position
	 * @return position argument for chaining.
	 */
	public static Vector3 alignPositionToGrid(final Vector3 position) {
		position.x = MathUtils.floor(position.x);
		position.y = 0;
		position.z = MathUtils.floor(position.z);
		return position;
	}

	/**
	 * Whether given contained is fully inside the container.
	 */
	public static boolean rectangleContainedInRectangleWithBoundaries(final Rectangle container,
																	  final Rectangle contained) {
		float xmin = contained.x;
		float xmax = xmin + contained.width;
		float ymin = contained.y;
		float ymax = ymin + contained.height;
		float x = container.getX();
		float y = container.getY();
		float width = container.getWidth();
		float height = container.getHeight();
		return ((xmin >= x && xmin <= x + width) && (xmax >= x && xmax <= x + width))
				&& ((ymin >= y && ymin <= y + height) && (ymax >= y && ymax <= y + height));
	}

	public static String getStringFromJsonOrDefault(final JsonObject jsonObject,
													final String key,
													final String defaultValue) {
		String result = defaultValue;
		if (jsonObject.has(key)) {
			result = jsonObject.get(key).getAsString();
		}
		return result;
	}

	public static float getFloatFromJsonOrDefault(final JsonObject jsonObject,
												  final String key,
												  final float defaultValue) {
		float result = defaultValue;
		if (jsonObject.has(key)) {
			result = jsonObject.get(key).getAsFloat();
		}
		return result;
	}
}
