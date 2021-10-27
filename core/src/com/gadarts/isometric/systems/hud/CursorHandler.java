package com.gadarts.isometric.systems.hud;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Disposable;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.CursorComponent;
import com.gadarts.isometric.services.GameServices;
import com.gadarts.isometric.utils.DefaultGameSettings;
import com.gadarts.isometric.utils.map.MapGraph;
import com.gadarts.isometric.utils.map.MapGraphNode;
import lombok.Getter;

import java.util.Optional;

@Getter
public class CursorHandler implements Disposable {
	public static final Color CURSOR_REGULAR = Color.YELLOW;
	public static final Color CURSOR_UNAVAILABLE = Color.DARK_GRAY;
	public static final Color CURSOR_ATTACK = Color.RED;
	private static final float CURSOR_FLICKER_STEP = 1.5f;
	private static final Vector3 auxVector3_1 = new Vector3();
	public static final String POSITION_LABEL_FORMAT = "Row: %s , Col: %s";
	public static final Color POSITION_LABEL_COLOR = Color.WHITE;
	public static final float POSITION_LABEL_Y = 10F;
	private final GameStage stage;
	private BitmapFont cursorCellPositionLabelFont;
	private Label cursorCellPositionLabel;
	private Entity cursor;
	private ModelInstance cursorModelInstance;
	private float cursorFlickerChange = CURSOR_FLICKER_STEP;

	public CursorHandler(final GameStage stage) {
		this.stage = stage;
		if (DefaultGameSettings.DISPLAY_CURSOR_POSITION) {
			cursorCellPositionLabelFont = new BitmapFont();
			Label.LabelStyle style = new Label.LabelStyle(cursorCellPositionLabelFont, POSITION_LABEL_COLOR);
			cursorCellPositionLabel = new Label(null, style);
			stage.addActor(cursorCellPositionLabel);
			cursorCellPositionLabel.setPosition(0, POSITION_LABEL_Y);
		}
	}

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

	@Override
	public void dispose( ) {
		Optional.ofNullable(cursorCellPositionLabelFont).ifPresent(BitmapFont::dispose);
	}

	public void onMouseEnteredNewNode(final MapGraphNode newNode, final GameServices services) {
		int col = newNode.getCol();
		int row = newNode.getRow();
		getCursorModelInstance().transform.setTranslation(col + 0.5f, newNode.getHeight(), row + 0.5f);
		colorizeCursor(newNode, services);
		if (cursorCellPositionLabel != null) {
			cursorCellPositionLabel.setText(String.format(POSITION_LABEL_FORMAT, row, col));
		}
	}
}
