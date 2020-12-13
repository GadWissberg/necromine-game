package com.gadarts.isometric.systems.hud;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.ModelInstanceComponent;
import com.gadarts.isometric.components.model.GameModelInstance;
import com.gadarts.isometric.utils.EntityBuilder;
import com.gadarts.isometric.utils.map.MapGraphNode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class AttackNodesHandler implements Disposable {
	public static final int ATTACK_NODES_POOL_SIZE = 8;
	private static final Vector3 auxVector3_1 = new Vector3();
	private static final Vector3 auxVector3_2 = new Vector3();
	private static final Vector3 auxVector3_3 = new Vector3();
	private static final Vector3 auxVector3_4 = new Vector3();
	private static final Vector3 auxVector3_5 = new Vector3();
	private final List<Entity> attackNodesEntities = new ArrayList<>();
	private Model attackNodeModel;

	@Getter
	@Setter
	private MapGraphNode selectedAttackNode;

	private Engine engine;

	private void displayAttackNodes(final List<MapGraphNode> availableNodes) {
		for (int i = 0; i < availableNodes.size(); i++) {
			Entity attackNodeEntity = attackNodesEntities.get(i);
			ModelInstanceComponent modelInstanceComp = ComponentsMapper.modelInstance.get(attackNodeEntity);
			modelInstanceComp.setVisible(true);
			MapGraphNode availableNode = availableNodes.get(i);
			ModelInstance modelInstance = modelInstanceComp.getModelInstance();
			modelInstance.transform.setTranslation(availableNode.getX(), 0, availableNode.getY());
		}
	}

	public void hideAttackNodes() {
		for (Entity attackNodeEntity : attackNodesEntities) {
			ModelInstanceComponent modelInstanceComp = ComponentsMapper.modelInstance.get(attackNodeEntity);
			modelInstanceComp.setVisible(false);
		}
	}

	private void createAttackNodesForFutureUse() {
		attackNodeModel = createAttackNodeModelTest();
		createAttackNodesEntities();
	}

	private void createAttackNodesEntities() {
		IntStream.range(0, ATTACK_NODES_POOL_SIZE).forEach(i ->
				attackNodesEntities.add(EntityBuilder.beginBuildingEntity((PooledEngine) engine)
						.addModelInstanceComponent(
								new GameModelInstance(attackNodeModel),
								false,
								false,
								false)
						.finishAndAddToEngine()
				));
	}

	private Model createAttackNodeModelTest() {
		ModelBuilder builder = new ModelBuilder();
		builder.begin();
		Material material = new Material(ColorAttribute.createDiffuse(HudSystemImpl.CURSOR_ATTACK));
		MeshPartBuilder meshPartBuilder = builder.part(
				"attack_node_1",
				GL20.GL_LINES,
				VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
				material);

		meshPartBuilder.rect(
				auxVector3_1.set(1, 0, 1),
				auxVector3_2.set(1, 0, 0),
				auxVector3_3.set(0, 0, 0),
				auxVector3_4.set(0, 0, 1),
				auxVector3_5.set(0, -1, 0));

		return builder.end();
	}

	@Override
	public void dispose() {
		attackNodeModel.dispose();
	}

	public void onAttackModeActivated(final List<MapGraphNode> availableNodes) {
		hideAttackNodes();
		displayAttackNodes(availableNodes);
	}

	public void onAttackModeDeactivated() {
		hideAttackNodes();
	}

	public void init(final Engine engine) {
		this.engine = engine;
		createAttackNodesForFutureUse();
	}

	public void reset() {
		hideAttackNodes();
		setSelectedAttackNode(null);
	}
}
