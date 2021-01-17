package com.gadarts.isometric.systems.player;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.utils.EntityBuilder;
import com.gadarts.isometric.utils.assets.GameAssetsManager;
import com.gadarts.isometric.utils.map.MapGraphNode;
import com.gadarts.isometric.utils.map.MapGraphPath;
import com.gadarts.necromine.Assets;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class PathPlanHandler {
	public static final int ARROWS_POOL_SIZE = 20;
	private static final Vector2 auxVector2 = new Vector2();
	private static final Vector3 auxVector3_1 = new Vector3();
	public static final float ARROW_HEIGHT = 0.2f;
	private final GameAssetsManager assetManager;
	private final List<Entity> arrowsEntities = new ArrayList<>();

	@Getter
	private final MapGraphPath path = new MapGraphPath();

	public PathPlanHandler(final GameAssetsManager assetManager) {
		this.assetManager = assetManager;
	}

	private void createArrowsEntities(final PooledEngine engine) {
		Texture texture = assetManager.getTexture(Assets.UiTextures.PATH_ARROW);
		IntStream.range(0, ARROWS_POOL_SIZE).forEach(i -> {
			Entity entity = EntityBuilder.beginBuildingEntity(engine)
					.addSimpleDecalComponent(auxVector3_1.setZero(), texture, false)
					.finishAndAddToEngine();
			arrowsEntities.add(entity);
		});
	}

	public void hideAllArrows() {
		arrowsEntities.forEach(arrow -> ComponentsMapper.simpleDecal.get(arrow).setVisible(false));
	}

	void displayPathPlan() {
		hideAllArrows();
		IntStream.range(0, getPath().getCount()).forEach(i -> {
			if (i < arrowsEntities.size() && i < path.getCount() - 1) {
				MapGraphNode n = path.get(i);
				MapGraphNode next = path.get(i + 1);
				Vector2 dirVector = auxVector2.set(next.getX(), next.getY()).sub(n.getX(), n.getY()).nor().scl(0.5f);
				transformArrowDecal(n, dirVector, ComponentsMapper.simpleDecal.get(arrowsEntities.get(i)).getDecal());
				ComponentsMapper.simpleDecal.get(arrowsEntities.get(i)).setVisible(true);
			}
		});
	}

	private void transformArrowDecal(final MapGraphNode currentNode, final Vector2 directionVector, final Decal decal) {
		decal.getRotation().idt();
		decal.rotateX(90);
		decal.rotateZ(directionVector.angleDeg());
		Vector3 pos = auxVector3_1.set(currentNode.getX() + 0.5f, ARROW_HEIGHT, currentNode.getY() + 0.5f);
		decal.setPosition(pos.add(directionVector.x, 0, directionVector.y));
	}

	public void init(final PooledEngine engine) {
		createArrowsEntities(engine);
	}

	public void resetPlan() {
		hideAllArrows();
		path.clear();
	}
}
