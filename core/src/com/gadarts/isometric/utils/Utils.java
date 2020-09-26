package com.gadarts.isometric.utils;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

public class Utils {
	private static final Plane floorPlane = new Plane(new Vector3(0, 1, 0), 0);
	public static final float EPSILON = 0.01f;

	public static Vector3 calculateGridPositionFromMouse(final Camera camera,
														 final float screenX,
														 final float screenY,
														 final Vector3 output) {
		Ray ray = camera.getPickRay(screenX, screenY);
		Intersector.intersectRayPlane(ray, floorPlane, output);
		output.x = MathUtils.round(output.x);
		output.y = 0;
		output.z = MathUtils.round(output.z);
		return output;
	}
}
