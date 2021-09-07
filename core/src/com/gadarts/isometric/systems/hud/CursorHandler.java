package com.gadarts.isometric.systems.hud;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.CursorComponent;
import com.gadarts.isometric.services.GameServices;
import com.gadarts.isometric.utils.map.MapGraph;
import com.gadarts.isometric.utils.map.MapGraphNode;
import lombok.Getter;

@Getter
public class CursorHandler {
	public static final Color CURSOR_REGULAR = Color.YELLOW;
	public static final Color CURSOR_UNAVAILABLE = Color.DARK_GRAY;
	public static final Color CURSOR_ATTACK = Color.RED;
	private static final float CURSOR_FLICKER_STEP = 1.5f;
	private static final Vector3 auxVector3_1 = new Vector3();
	private Entity cursor;
	private ModelInstance cursorModelInstance;
	private float cursorFlickerChange = CURSOR_FLICKER_STEP;

	private void setCursorColor(final Color color) {
		Material material = cursorModelInstance.materials.get(0);
		ColorAttribute colorAttribute = (ColorAttribute) material.get(ColorAttribute.Diffuse);
		colorAttribute.color.set(color);
	}

	MapGraphNode getCursorNode(final GameServices services) {
		Vector3 dest = getCursorModelInstance().transform.getTranslation(auxVector3_1);
		return services.getMapService().getMap().getNode((int) dest.x, (int) dest.z);
	}

	void handleCursorFlicker(final float deltaTime) {
		Material material = ComponentsMapper.modelInstance.get(cursor).getModelInstance().materials.get(0);
		if (((ColorAttribute) material.get(ColorAttribute.Diffuse)).color.equals(CURSOR_ATTACK)) {
			BlendingAttribute blend = (BlendingAttribute) material.get(BlendingAttribute.Type);
			if (blend.opacity > 0.9) {
				cursorFlickerChange = -CURSOR_FLICKER_STEP;
			} else if (blend.opacity < 0.1) {
				cursorFlickerChange = CURSOR_FLICKER_STEP;
			}
			setCursorOpacity(blend.opacity + cursorFlickerChange * deltaTime);
		}
	}

	void colorizeCursor(final MapGraphNode newNode,
						final GameServices services) {
		MapGraph map = services.getMapService().getMap();
		if (map.getFowMap()[newNode.getRow()][newNode.getCol()] == 1) {
			if (map.getAliveEnemyFromNode(newNode) != null) {
				setCursorColor(CURSOR_ATTACK);
			} else {
				setCursorOpacity(1F);
				setCursorColor(CURSOR_REGULAR);
			}
		} else {
			setCursorOpacity(1F);
			setCursorColor(CURSOR_UNAVAILABLE);
		}
	}

	private void setCursorOpacity(final float opacity) {
		Material material = cursorModelInstance.materials.get(0);
		BlendingAttribute blend = (BlendingAttribute) material.get(BlendingAttribute.Type);
		blend.opacity = opacity;
		material.set(blend);
	}

	public void init(final Engine engine) {
		cursor = engine.getEntitiesFor(Family.all(CursorComponent.class).get()).first();
		cursorModelInstance = ComponentsMapper.modelInstance.get(cursor).getModelInstance();
	}
}
