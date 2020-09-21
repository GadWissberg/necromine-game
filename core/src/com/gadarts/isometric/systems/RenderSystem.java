package com.gadarts.isometric.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.ModelInstanceComponent;

public class RenderSystem extends EntitySystem {
	private static final Vector3 auxVector3_1 = new Vector3();
	private static final Vector3 auxVector3_2 = new Vector3();
	private final ModelBatch modelBatch;
	private Camera camera;
	private ImmutableArray<Entity> modelInstanceEntities;

	public RenderSystem() {
		this.modelBatch = new ModelBatch();
	}

	public static void resetDisplay(final Color color) {
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glClearColor(color.r, color.g, color.b, 1);
	}

	@Override
	public void addedToEngine(final Engine engine) {
		super.addedToEngine(engine);
		ModelBuilder modelBuilder = new ModelBuilder();
		this.camera = engine.getSystem(CameraSystem.class).getCamera();
		engine.addEntity(createArrowEntity(modelBuilder, Color.RED, auxVector3_1.set(1, 0, 0)));
		engine.addEntity(createArrowEntity(modelBuilder, Color.GREEN, auxVector3_1.set(0, 1, 0)));
		engine.addEntity(createArrowEntity(modelBuilder, Color.BLUE, auxVector3_1.set(0, 0, 1)));
		modelInstanceEntities = engine.getEntitiesFor(Family.all(ModelInstanceComponent.class).get());
	}

	private Entity createArrowEntity(final ModelBuilder modelBuilder,
									 final Color color,
									 final Vector3 direction) {
		Material material = new Material(ColorAttribute.createDiffuse(color));
		int attributes = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;
		Model model = modelBuilder.createArrow(Vector3.Zero, direction, material, attributes);
		PooledEngine engine = (PooledEngine) getEngine();
		ModelInstanceComponent modelInsComp = engine.createComponent(ModelInstanceComponent.class);
		modelInsComp.init(new ModelInstance(model));
		return engine.createEntity().add(modelInsComp);
	}

	@Override
	public void update(final float deltaTime) {
		super.update(deltaTime);
		resetDisplay(Color.BLACK);
		modelBatch.begin(camera);
		for (Entity entity : modelInstanceEntities) {
			ModelInstance modelInstance = ComponentsMapper.modelInstance.get(entity).getModelInstance();
			modelBatch.render(modelInstance);
		}
		modelBatch.end();
	}
}
