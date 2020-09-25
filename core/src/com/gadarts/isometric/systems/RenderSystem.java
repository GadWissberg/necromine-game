package com.gadarts.isometric.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.isometric.components.*;
import com.gadarts.isometric.utils.Assets;

public class RenderSystem extends EntitySystem implements EntityListener {

	private static final Vector2 auxVector2_1 = new Vector2();
	private static final Vector2 auxVector2_2 = new Vector2();
	private static final Vector2 auxVector2_3 = new Vector2();
	private static final Vector3 auxVector3_1 = new Vector3();
	private static final Vector3 auxVector3_2 = new Vector3();
	public static final int DECALS_POOL_SIZE = 200;
	private final ModelBatch modelBatch;
	private DecalBatch decalBatch;
	private Camera camera;
	private ImmutableArray<Entity> modelInstanceEntities;
	private ImmutableArray<Entity> decalEntities;

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
		this.camera = engine.getSystem(CameraSystem.class).getCamera();
		decalBatch = new DecalBatch(DECALS_POOL_SIZE, new CameraGroupStrategy(camera));
		engine.addEntityListener(this);
		ModelBuilder modelBuilder = new ModelBuilder();
		engine.addEntity(createArrowEntity(modelBuilder, Color.RED, auxVector3_1.set(1, 0, 0)));
		engine.addEntity(createArrowEntity(modelBuilder, Color.GREEN, auxVector3_1.set(0, 1, 0)));
		engine.addEntity(createArrowEntity(modelBuilder, Color.BLUE, auxVector3_1.set(0, 0, 1)));
		modelInstanceEntities = engine.getEntitiesFor(Family.all(ModelInstanceComponent.class).get());
		decalEntities = engine.getEntitiesFor(Family.all(DecalComponent.class).get());
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
			ModelInstanceComponent modelInstanceComponent = ComponentsMapper.modelInstance.get(entity);
			ModelInstance modelInstance = modelInstanceComponent.getModelInstance();
			modelBatch.render(modelInstance);
		}
		modelBatch.end();
		for (Entity entity : decalEntities) {
			CharacterComponent characterComponent = ComponentsMapper.character.get(entity);
			DecalComponent decalComponent = ComponentsMapper.decal.get(entity);
			Decal decal = decalComponent.getDecal();
			auxVector2_3.set(1, 0);
			auxVector2_3.setAngle(characterComponent.getDirection().getDirection(auxVector2_1).angle() - auxVector2_2.set(camera.position.x, camera.position.z).sub(decal.getX(), decal.getZ()).angle());
			float angleDiff = auxVector2_3.angle();
			Assets.CharacterDirectionsRegions region;
			if ((angleDiff >= 0 && angleDiff <= 22.5) || (angleDiff > 337.5 && angleDiff <= 360)) {
				region = Assets.CharacterDirectionsRegions.SOUTH_IDLE;
			} else if (angleDiff > 22.5 && angleDiff <= 67.5) {
				region = Assets.CharacterDirectionsRegions.SOUTH_WEST_IDLE;
			} else if (angleDiff > 67.5 && angleDiff <= 112.5) {
				region = Assets.CharacterDirectionsRegions.WEST_IDLE;
			} else if (angleDiff > 112.5 && angleDiff <= 157.5) {
				region = Assets.CharacterDirectionsRegions.NORTH_WEST_IDLE;
			} else if (angleDiff > 157.5 && angleDiff <= 202.5) {
				region = Assets.CharacterDirectionsRegions.NORTH_IDLE;
			} else if (angleDiff > 202.5 && angleDiff <= 247.5) {
				region = Assets.CharacterDirectionsRegions.NORTH_EAST_IDLE;
			} else if (angleDiff > 247.5 && angleDiff <= 292.5) {
				region = Assets.CharacterDirectionsRegions.EAST_IDLE;
			} else {
				region = Assets.CharacterDirectionsRegions.SOUTH_EAST_IDLE;
			}
			if (!decalComponent.getCurrentRegion().getRegionName().equals(region.getRegionName())) {
				decalComponent.initializeRegion(region);
				if (ComponentsMapper.animation.has(entity)) {
					AnimationComponent animationComponent = ComponentsMapper.animation.get(entity);
					animationComponent.init(0.5f, decalComponent.getAnimations().get(region));
				}
			} else {
				if (ComponentsMapper.animation.has(entity)) {
					AnimationComponent animationComponent = ComponentsMapper.animation.get(entity);
					TextureAtlas.AtlasRegion currentFrame = animationComponent.getCurrentFrame(deltaTime);
					decal.setTextureRegion(currentFrame);
				}
			}
			decal.lookAt(camera.position, camera.up);
			decalBatch.add(decal);
		}
		decalBatch.flush();
	}

	@Override
	public void entityAdded(final Entity entity) {
	}

	@Override
	public void entityRemoved(final Entity entity) {

	}
}
