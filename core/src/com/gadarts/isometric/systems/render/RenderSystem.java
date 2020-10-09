package com.gadarts.isometric.systems.render;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.gadarts.isometric.components.AnimationComponent;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.DecalComponent;
import com.gadarts.isometric.components.ModelInstanceComponent;
import com.gadarts.isometric.components.character.CharacterComponent;
import com.gadarts.isometric.components.character.SpriteType;
import com.gadarts.isometric.systems.EventsNotifier;
import com.gadarts.isometric.systems.GameEntitySystem;
import com.gadarts.isometric.systems.camera.CameraSystem;
import com.gadarts.isometric.systems.camera.CameraSystemEventsSubscriber;

/**
 * Handles rendering.
 */
public class RenderSystem extends GameEntitySystem<RenderSystemEventsSubscriber> implements
		EntityListener,
		CameraSystemEventsSubscriber,
		EventsNotifier<RenderSystemEventsSubscriber> {

	private static final Vector2 auxVector2_1 = new Vector2();
	private static final Vector2 auxVector2_2 = new Vector2();
	private static final Vector2 auxVector2_3 = new Vector2();
	private static final Vector3 auxVector3_1 = new Vector3();
	private static final int DECALS_POOL_SIZE = 200;
	private static final Plane auxPlane = new Plane(new Vector3(0, 1, 0), 0);

	private ModelBatch modelBatch;
	private DecalBatch decalBatch;
	private ImmutableArray<Entity> modelInstanceEntities;
	private ImmutableArray<Entity> decalEntities;
	private boolean ready;
	private CameraSystem cameraSystem;

	private void resetDisplay(final Color color) {
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glClearColor(color.r, color.g, color.b, 1);
	}

	@Override
	public void addedToEngine(final Engine engine) {
		super.addedToEngine(engine);
		engine.addEntityListener(this);
		createAxis(engine);
		modelInstanceEntities = engine.getEntitiesFor(Family.all(ModelInstanceComponent.class).get());
		decalEntities = engine.getEntitiesFor(Family.all(DecalComponent.class).get());
	}

	private void createAxis(final Engine engine) {
		ModelBuilder modelBuilder = new ModelBuilder();
		engine.addEntity(createArrowEntity(modelBuilder, Color.RED, auxVector3_1.set(1, 0, 0)));
		engine.addEntity(createArrowEntity(modelBuilder, Color.GREEN, auxVector3_1.set(0, 1, 0)));
		engine.addEntity(createArrowEntity(modelBuilder, Color.BLUE, auxVector3_1.set(0, 0, 1)));
	}

	private Entity createArrowEntity(final ModelBuilder modelBuilder,
									 final Color color,
									 final Vector3 direction) {
		Material material = new Material(ColorAttribute.createDiffuse(color));
		int attributes = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;
		Model model = modelBuilder.createArrow(Vector3.Zero, direction, material, attributes);
		PooledEngine engine = (PooledEngine) getEngine();
		ModelInstanceComponent modelInsComp = engine.createComponent(ModelInstanceComponent.class);
		modelInsComp.init(new ModelInstance(model), true);
		return engine.createEntity().add(modelInsComp);
	}

	@Override
	public void update(final float deltaTime) {
		super.update(deltaTime);
		if (!ready) return;
		resetDisplay(Color.BLACK);
		OrthographicCamera camera = cameraSystem.getCamera();
		modelBatch.begin(camera);
		for (Entity entity : modelInstanceEntities) {
			ModelInstanceComponent modelInstanceComponent = ComponentsMapper.modelInstance.get(entity);
			if (modelInstanceComponent.isVisible()) {
				ModelInstance modelInstance = modelInstanceComponent.getModelInstance();
				modelBatch.render(modelInstance);
			}
		}
		modelBatch.end();
		for (Entity entity : decalEntities) {
			CharacterComponent characterComponent = ComponentsMapper.character.get(entity);
			DecalComponent decalComponent = ComponentsMapper.decal.get(entity);
			Decal decal = decalComponent.getDecal();
			auxVector2_3.set(1, 0);
			float playerAngle = characterComponent.getDirection().getDirection(auxVector2_1).angle();
			Ray ray = camera.getPickRay(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f);
			Intersector.intersectRayPlane(ray, auxPlane, auxVector3_1);
			float cameraAngle = auxVector2_2.set(camera.position.x, camera.position.z).sub(auxVector3_1.x, auxVector3_1.z).angle();
			auxVector2_3.setAngle(playerAngle - cameraAngle);
			float angleDiff = auxVector2_3.angle();
			CharacterComponent.Direction direction;
			if ((angleDiff >= 0 && angleDiff <= 22.5) || (angleDiff > 337.5f && angleDiff <= 360)) {
				direction = CharacterComponent.Direction.SOUTH;
			} else if (angleDiff > 22.5 && angleDiff <= 67.5) {
				direction = CharacterComponent.Direction.SOUTH_WEST;
			} else if (angleDiff > 67.5 && angleDiff <= 112.5) {
				direction = CharacterComponent.Direction.WEST;
			} else if (angleDiff > 112.5 && angleDiff <= 157.5) {
				direction = CharacterComponent.Direction.NORTH_WEST;
			} else if (angleDiff > 157.5 && angleDiff <= 202.5) {
				direction = CharacterComponent.Direction.NORTH;
			} else if (angleDiff > 202.5 && angleDiff <= 247.5) {
				direction = CharacterComponent.Direction.NORTH_EAST;
			} else if (angleDiff > 247.5 && angleDiff <= 292.5) {
				direction = CharacterComponent.Direction.EAST;
			} else {
				direction = CharacterComponent.Direction.SOUTH_EAST;
			}
			SpriteType spriteType = characterComponent.getSpriteType();
			if (!characterComponent.getSpriteType().equals(decalComponent.getType()) || !decalComponent.getDirection().equals(direction)) {
				if (decalComponent.getType() != SpriteType.DIE) {
					decalComponent.initializeSprite(spriteType, direction);
					if (ComponentsMapper.animation.has(entity)) {
						AnimationComponent animationComponent = ComponentsMapper.animation.get(entity);
						if (spriteType.isSingleAnimation()) {
							direction = CharacterComponent.Direction.SOUTH;
						}
						animationComponent.init(spriteType.getAnimationDuration(), decalComponent.getAnimations().get(spriteType, direction));
					}
				}
			} else {
				if (ComponentsMapper.animation.has(entity)) {
					AnimationComponent animationComponent = ComponentsMapper.animation.get(entity);
					TextureRegion currentFrame = decal.getTextureRegion();
					TextureAtlas.AtlasRegion newFrame = animationComponent.calculateFrame(deltaTime);
					if (spriteType == SpriteType.RUN && currentFrame != newFrame) {
						for (RenderSystemEventsSubscriber subscriber : subscribers) {
							subscriber.onRunFrameChanged(entity, deltaTime);
						}
					}
					decal.setTextureRegion(newFrame);
				}
			}
			decal.lookAt(auxVector3_1.set(decal.getPosition()).sub(camera.direction), camera.up);
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

	@Override
	public void init() {
		this.modelBatch = new ModelBatch();
	}

	@Override
	public void dispose() {
		decalBatch.dispose();
		modelBatch.dispose();
	}

	@Override
	public void onCameraSystemReady(final CameraSystem cameraSystem) {
		this.cameraSystem = cameraSystem;
		this.decalBatch = new DecalBatch(DECALS_POOL_SIZE, new CameraGroupStrategy(cameraSystem.getCamera()));
		ready = true;
	}

}
