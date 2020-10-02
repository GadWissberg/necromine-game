package com.gadarts.isometric.utils;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.collision.Ray;
import com.gadarts.isometric.components.CharacterComponent;

public class Utils {
	private static final Plane floorPlane = new Plane(new Vector3(0, 1, 0), 0);
	public static final float EPSILON = 0.025f;
	private final static Vector2 auxVector2_1 = new Vector2();

	public static Vector3 calculateGridPositionFromMouse(final Camera camera,
														 final float screenX,
														 final float screenY,
														 final Vector3 output) {
		Ray ray = camera.getPickRay(screenX, screenY);
		Intersector.intersectRayPlane(ray, floorPlane, output);
		return alignPositionToGrid(output);
	}

	public static Vector3 alignPositionToGrid(final Vector3 position) {
		position.x = MathUtils.round(position.x);
		position.y = 0;
		position.z = MathUtils.round(position.z);
		return position;
	}

	public static CharacterComponent.Direction getDirectionBetweenNodes(final MapGraphNode myNode,
																		final MapGraphNode targetNode) {
		Vector2 vector2 = auxVector2_1.set(targetNode.getX(), targetNode.getY()).sub(myNode.getX(), myNode.getY()).nor();
		return CharacterComponent.Direction.findDirection(vector2);
	}
}
