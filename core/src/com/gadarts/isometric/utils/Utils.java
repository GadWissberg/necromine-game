package com.gadarts.isometric.utils;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.collision.Ray;
import com.gadarts.isometric.components.character.CharacterComponent;
import com.gadarts.isometric.utils.map.MapGraphNode;

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
		position.x = MathUtils.floor(position.x);
		position.y = 0;
		position.z = MathUtils.floor(position.z);
		return position;
	}

	public static CharacterComponent.Direction getDirectionBetweenNodes(final MapGraphNode myNode,
																		final MapGraphNode targetNode) {
		Vector2 vector2 = auxVector2_1.set(targetNode.getX(), targetNode.getY()).sub(myNode.getX(), myNode.getY()).nor();
		return CharacterComponent.Direction.findDirection(vector2);
	}

	public static float closestMultiplication(final float value, final int multiplication) {
		return Math.round(value / multiplication) * multiplication;
	}

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
}
