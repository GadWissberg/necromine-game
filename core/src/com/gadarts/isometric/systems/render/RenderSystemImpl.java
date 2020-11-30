package com.gadarts.isometric.systems.render;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.gadarts.isometric.components.*;
import com.gadarts.isometric.components.character.CharacterAnimations;
import com.gadarts.isometric.components.character.CharacterComponent;
import com.gadarts.isometric.components.character.CharacterComponent.Direction;
import com.gadarts.isometric.components.character.CharacterSpriteData;
import com.gadarts.isometric.components.character.SpriteType;
import com.gadarts.isometric.systems.EventsNotifier;
import com.gadarts.isometric.systems.GameEntitySystem;
import com.gadarts.isometric.systems.camera.CameraSystem;
import com.gadarts.isometric.systems.camera.CameraSystemEventsSubscriber;
import com.gadarts.isometric.systems.hud.HudSystem;
import com.gadarts.isometric.systems.hud.HudSystemEventsSubscriber;
import com.gadarts.isometric.utils.DefaultGameSettings;
import com.gadarts.isometric.utils.map.MapGraphNode;

/**
 * Handles rendering.
 */
public class RenderSystemImpl extends GameEntitySystem<RenderSystemEventsSubscriber> implements
		RenderSystem,
		EntityListener,
		HudSystemEventsSubscriber,
		CameraSystemEventsSubscriber,
		EventsNotifier<RenderSystemEventsSubscriber> {

	private static final Vector2 auxVector2_1 = new Vector2();
	private static final Vector2 auxVector2_2 = new Vector2();
	private static final Vector2 auxVector2_3 = new Vector2();
	private static final Vector3 auxVector3_1 = new Vector3();
	private static final Plane auxPlane = new Plane(new Vector3(0, 1, 0), 0);
	private static final Quaternion auxQuat = new Quaternion();
	private static final Color ambientLightColor = new Color(0.4f, 0.4f, 0.4f, 1);
	private static final Vector3 directionalLightDirection = new Vector3(Vector3.Z).scl(-1);
	private static final Color directionalLightColor = new Color(0.7f, 0.7f, 0.7f, 1f);

	private final Environment environment = new Environment();
	private RenderBatches renderBatches;
	private ImmutableArray<Entity> modelInstanceEntities;
	private ImmutableArray<Entity> characterDecalsEntities;
	private boolean ready;
	private CameraSystem cameraSystem;
	private Stage stage;
	private ImmutableArray<Entity> simpleDecalsEntities;

	private void resetDisplay(final Color color) {
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		int sam = Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0;
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | sam);
		Gdx.gl.glClearColor(color.r, color.g, color.b, 1);
	}

	@Override
	public void addedToEngine(final Engine engine) {
		super.addedToEngine(engine);
		engine.addEntityListener(this);
		createAxis(engine);
		modelInstanceEntities = engine.getEntitiesFor(Family.all(ModelInstanceComponent.class).get());
		characterDecalsEntities = engine.getEntitiesFor(Family.all(CharacterDecalComponent.class).get());
		simpleDecalsEntities = engine.getEntitiesFor(Family.all(SimpleDecalComponent.class).get());
		initializeEnvironment();
	}

	private void initializeEnvironment() {
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, ambientLightColor));
		DirectionalLight directionalLight = new DirectionalLight();
		directionalLight.set(directionalLightColor, directionalLightDirection);
		environment.add(directionalLight);
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
		resetDisplay(DefaultGameSettings.BACKGROUND_COLOR);
		OrthographicCamera camera = cameraSystem.getCamera();
		renderModels(camera);
		DecalBatch decalBatch = renderBatches.getDecalBatch();
		for (Entity entity : characterDecalsEntities) {
			CharacterComponent characterComponent = ComponentsMapper.character.get(entity);
			CharacterDecalComponent characterDecalComponent = ComponentsMapper.characterDecal.get(entity);
			auxVector2_3.set(1, 0);
			CharacterSpriteData characterSpriteData = characterComponent.getCharacterSpriteData();
			Direction facingDirection = characterSpriteData.getFacingDirection();
			float playerAngle = facingDirection.getDirection(auxVector2_1).angleDeg();
			Ray ray = camera.getPickRay(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f);
			Intersector.intersectRayPlane(ray, auxPlane, auxVector3_1);
			float cameraAngle = auxVector2_2.set(camera.position.x, camera.position.z).sub(auxVector3_1.x, auxVector3_1.z).angleDeg();
			auxVector2_3.setAngleDeg(playerAngle - cameraAngle);
			float angleDiff = auxVector2_3.angleDeg();
			Direction direction;
			if ((angleDiff >= 0 && angleDiff <= 22.5) || (angleDiff > 337.5f && angleDiff <= 360)) {
				direction = Direction.SOUTH;
			} else if (angleDiff > 22.5 && angleDiff <= 67.5) {
				direction = Direction.SOUTH_WEST;
			} else if (angleDiff > 67.5 && angleDiff <= 112.5) {
				direction = Direction.WEST;
			} else if (angleDiff > 112.5 && angleDiff <= 157.5) {
				direction = Direction.NORTH_WEST;
			} else if (angleDiff > 157.5 && angleDiff <= 202.5) {
				direction = Direction.NORTH;
			} else if (angleDiff > 202.5 && angleDiff <= 247.5) {
				direction = Direction.NORTH_EAST;
			} else if (angleDiff > 247.5 && angleDiff <= 292.5) {
				direction = Direction.EAST;
			} else {
				direction = Direction.SOUTH_EAST;
			}
			SpriteType spriteType = characterSpriteData.getSpriteType();
			boolean sameSpriteType = characterSpriteData.getSpriteType().equals(characterDecalComponent.getSpriteType());
			boolean sameDirection = characterDecalComponent.getDirection().equals(direction);
			Decal decal = characterDecalComponent.getDecal();
			CharacterAnimations animations = characterDecalComponent.getAnimations();
			Decal shadowDecal = characterDecalComponent.getShadowDecal();
			if ((!sameSpriteType || !sameDirection)) {
				characterDecalComponent.initializeSprite(spriteType, direction);
				if (ComponentsMapper.animation.has(entity)) {
					AnimationComponent animationComponent = ComponentsMapper.animation.get(entity);
					if (spriteType.isSingleAnimation()) {
						if (!animationComponent.getAnimation().isAnimationFinished(animationComponent.getStateTime())) {
							direction = Direction.SOUTH;
						} else if (spriteType == SpriteType.DIE) {
							spriteType = SpriteType.DEAD;
						}
					}
					CharacterAnimation animation = null;
					if (animations.contains(spriteType)) {
						animation = animations.get(spriteType, direction);
					} else if (ComponentsMapper.player.has(entity)) {
						animation = ComponentsMapper.player.get(entity).getGeneralAnimations().get(spriteType, direction);
					}
					if (animation != null) {
						animationComponent.init(spriteType.getAnimationDuration(), animation);
					}
					if (!sameSpriteType) {
						animationComponent.resetStateTime();
					}
				}
			} else {
				if (ComponentsMapper.animation.has(entity)) {
					AnimationComponent animationComponent = ComponentsMapper.animation.get(entity);
					TextureAtlas.AtlasRegion currentFrame = (TextureAtlas.AtlasRegion) decal.getTextureRegion();
					TextureAtlas.AtlasRegion newFrame = animationComponent.calculateFrame(deltaTime);
					if (currentFrame.index != newFrame.index) {
						for (RenderSystemEventsSubscriber subscriber : subscribers) {
							subscriber.onFrameChanged(entity, deltaTime, newFrame);
						}
					}
					if (characterDecalComponent.getSpriteType() == spriteType && currentFrame != newFrame) {
						decal.setTextureRegion(newFrame);
						CharacterAnimation southAnimation;
						if (animations.contains(spriteType)) {
							southAnimation = animations.get(spriteType, facingDirection);
						} else {
							CharacterAnimations generalAnim = ComponentsMapper.player.get(entity).getGeneralAnimations();
							southAnimation = generalAnim.get(spriteType, facingDirection);
						}
						shadowDecal.setTextureRegion(southAnimation.getKeyFrames()[Math.max(newFrame.index, 0)]);
					} else if (characterSpriteData.getSpriteType() == SpriteType.DIE) {
						if (animationComponent.getAnimation().isAnimationFinished(animationComponent.getStateTime())) {
							characterSpriteData.setSpriteType(SpriteType.DEAD);
						}
					}
				}
			}
			if (!DefaultGameSettings.HIDE_ENEMIES || !ComponentsMapper.enemy.has(entity)) {
				decal.lookAt(auxVector3_1.set(decal.getPosition()).sub(camera.direction), camera.up);
				decalBatch.add(decal);
				decalBatch.add(shadowDecal);
			}
		}
		for (Entity entity : simpleDecalsEntities) {
			SimpleDecalComponent simpleDecalComponent = ComponentsMapper.simpleDecal.get(entity);
			if (simpleDecalComponent.isVisible()) {
				decalBatch.add(simpleDecalComponent.getDecal());
			}
		}
		Gdx.gl.glDepthMask(false);
		decalBatch.flush();
		stage.draw();
	}

	private void renderModels(final OrthographicCamera camera) {
		Gdx.gl.glDepthMask(true);
		ModelBatch modelBatch = renderBatches.getModelBatch();
		modelBatch.begin(camera);
		for (Entity entity : modelInstanceEntities) {
			ModelInstanceComponent modelInstanceComponent = ComponentsMapper.modelInstance.get(entity);
			if (ComponentsMapper.wall.has(entity)) {
				float angleAround = MathUtils.round(modelInstanceComponent.getModelInstance().transform.getRotation(auxQuat).getAngleAround(Vector3.Y));
				Vector2 modelAngle = auxVector2_1.set(1, 0).setAngleDeg(angleAround).rotate90((angleAround < 90 || angleAround > 270) || angleAround == 180 ? 1 : -1);
				Vector2 cameraAngle = auxVector2_2.set(camera.direction.x, camera.direction.z);
				float angle = auxVector2_3.set(1, 0).setAngleDeg(modelAngle.angleDeg(cameraAngle)).angleDeg();
				if (angle < 90 || angle > 270) {
					continue;
				}
			}
			if (modelInstanceComponent.isVisible() && (!DefaultGameSettings.HIDE_GROUND || !ComponentsMapper.floor.has(entity))) {
				ModelInstance modelInstance = modelInstanceComponent.getModelInstance();
				modelBatch.render(modelInstance, environment);
			}
		}
		modelBatch.end();
	}

	@Override
	public void entityAdded(final Entity entity) {
	}

	@Override
	public void entityRemoved(final Entity entity) {

	}

	@Override
	public void dispose() {
		renderBatches.dispose();
	}

	@Override
	public void onCameraSystemReady(final CameraSystem cameraSystem) {
		this.cameraSystem = cameraSystem;
		this.renderBatches = new RenderBatches(cameraSystem.getCamera());
		systemReady();
	}

	private void systemReady() {
		if (stage == null || cameraSystem == null) return;
		ready = true;
		for (RenderSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onRenderSystemReady(this);
		}
	}

	@Override
	public void onHudSystemReady(final HudSystem hudSystem) {
		this.stage = hudSystem.getStage();
		systemReady();
	}

	@Override
	public void onPathCreated(final boolean pathToEnemy) {

	}

	@Override
	public void onEnemySelectedWithRangeWeapon(final MapGraphNode node) {

	}

	@Override
	public void activate() {

	}
}
