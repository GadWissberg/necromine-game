package com.gadarts.isometric.systems.hud;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.TimeUtils;
import com.gadarts.isometric.services.GameServices;
import com.gadarts.isometric.systems.render.DrawFlags;
import com.gadarts.isometric.utils.map.MapGraph;
import com.gadarts.isometric.utils.map.MapGraphNode;
import lombok.Getter;

import java.util.List;

@Getter
public class InterfaceSystemHandlers implements Disposable {
	private final AttackNodesHandler attackNodesHandler = new AttackNodesHandler();
	private final HudHandler hudHandler = new HudHandler();
	private final MenuHandler menuHandler = new MenuHandler();
	private CursorHandler cursorHandler;
	private ToolTipHandler toolTipHandler;

	@Override
	public void dispose( ) {
		attackNodesHandler.dispose();
		toolTipHandler.dispose();
		cursorHandler.dispose();
	}


	public void initializeToolTipHandler(final GameStage stage, final DrawFlags drawFlags) {
		toolTipHandler = new ToolTipHandler(stage, drawFlags);
		toolTipHandler.addToolTipTable();
	}

	void onMouseEnteredNewNode(final MapGraphNode newNode, final GameServices services) {
		toolTipHandler.displayToolTip(null);
		toolTipHandler.setLastHighlightNodeChange(TimeUtils.millis());
		cursorHandler.onMouseEnteredNewNode(newNode, services);
	}

	void onUserSelectedNodeToApplyTurn(final GameServices services, final List<InterfaceSystemEventsSubscriber> subscribers) {
		MapGraphNode cursorNode = cursorHandler.getCursorNode(services);
		if (services.getMapService().getMap().getFowMap()[cursorNode.getRow()][cursorNode.getCol()] == 1) {
			for (InterfaceSystemEventsSubscriber sub : subscribers) {
				sub.onUserSelectedNodeToApplyTurn(cursorNode, attackNodesHandler);
			}
		}
	}

	public void initializeAttackNodesHandler(final Engine engine) {
		attackNodesHandler.init(engine);
	}

	public void update(final float deltaTime,
					   final GameServices services) {
		hudHandler.getStage().act();
		MapGraph map = services.getMapService().getMap();
		toolTipHandler.handleToolTip(map, cursorHandler.getCursorNode(services));
		cursorHandler.handleCursorFlicker(deltaTime);
	}

	public void initializeCursorHandler(final GameStage stage, final Engine engine) {
		cursorHandler = new CursorHandler(stage);
		getCursorHandler().init(engine);
	}
}
