package com.gadarts.isometric.systems.render;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.gadarts.isometric.components.*;
import com.gadarts.isometric.components.character.CharacterAnimations;
import com.gadarts.isometric.components.character.CharacterComponent;
import com.gadarts.isometric.components.character.CharacterComponent.Direction;
import com.gadarts.isometric.components.character.CharacterSpriteData;
import com.gadarts.isometric.components.character.SpriteType;
import com.gadarts.isometric.components.model.GameModelInstance;
import com.gadarts.isometric.systems.EventsNotifier;
import com.gadarts.isometric.systems.GameEntitySystem;
import com.gadarts.isometric.systems.camera.CameraSystem;
import com.gadarts.isometric.systems.camera.CameraSystemEventsSubscriber;
import com.gadarts.isometric.systems.hud.AttackNodesHandler;
import com.gadarts.isometric.systems.hud.HudSystem;
import com.gadarts.isometric.systems.hud.HudSystemEventsSubscriber;
import com.gadarts.isometric.utils.DefaultGameSettings;
import com.gadarts.isometric.utils.map.MapGraphNode;

import static java.lang.Math.max;

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
	private static final Vector3 auxVector3_2 = new Vector3();
	private static final Plane auxPlane = new Plane(new Vector3(0, 1, 0), 0);
	private static final Quaternion auxQuat = new Quaternion();
	private final static BoundingBox auxBoundingBox = new BoundingBox();

	private final WorldEnvironment environment = new WorldEnvironment();
	private RenderBatches renderBatches;
	private RenderSystemRelatedEntities renderSystemRelatedEntities;
	private boolean ready;
	private int numberOfVisible;
	private LightsRenderer lightsHandler;

	private void resetDisplay(final Color color) {
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		int sam = Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0;
		Gdx.gl.glClearColor(color.r, color.g, color.b, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | sam);
	}

	@Override
	public void addedToEngine(final Engine engine) {
		super.addedToEngine(engine);
		engine.addEntityListener(this);
		renderSystemRelatedEntities = new RenderSystemRelatedEntities(engine);
		lightsHandler = new LightsRenderer(renderSystemRelatedEntities.getLightsEntities());
	}

	@Override
	public void update(final float deltaTime) {
		super.update(deltaTime);
		if (ready) {
			numberOfVisible = 0;
			resetDisplay(DefaultGameSettings.BACKGROUND_COLOR);
			renderWorld(deltaTime, getSystem(CameraSystem.class).getCamera());
			getSystem(HudSystem.class).getStage().draw();
		}
	}

	private void renderWorld(final float deltaTime, final Camera camera) {
		if (!DefaultGameSettings.DISABLE_SHADOWS) {
			DirectionalShadowLight shadowLight = environment.getShadowLight();
			shadowLight.begin(Vector3.Zero, camera.direction);
			renderModels(shadowLight.getCamera(), renderBatches.getShadowBatch(), false, false);
			shadowLight.end();
		}
		resetDisplay(Color.BLACK);
		renderModels(camera, renderBatches.getModelBatch(), true, true);
		renderDecals(deltaTime, camera);
	}

	private void renderDecals(final float deltaTime, final Camera camera) {
		DecalBatch decalBatch = renderBatches.getDecalBatch();
		for (Entity entity : renderSystemRelatedEntities.getCharacterDecalsEntities()) {
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
			Vector3 decalPosition = decal.getPosition();
			if ((!sameSpriteType || !sameDirection)) {
				characterDecalComponent.initializeSprite(spriteType, direction);
				if (ComponentsMapper.animation.has(entity)) {
					AnimationComponent animationComponent = ComponentsMapper.animation.get(entity);
					if (spriteType.isSingleAnimation()) {
						if (!animationComponent.getAnimation().isAnimationFinished((float) animationComponent.getStateTime())) {
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
					TextureAtlas.AtlasRegion newFrame = animationComponent.calculateFrame();
					if (currentFrame.index != newFrame.index) {
						for (RenderSystemEventsSubscriber subscriber : subscribers) {
							subscriber.onFrameChanged(entity, deltaTime, newFrame);
						}
					}
					if (characterDecalComponent.getSpriteType() == spriteType && currentFrame != newFrame) {
						decal.setTextureRegion(newFrame);
						CharacterAnimation southAnimation;
						if (spriteType.isSingleAnimation()) {
							facingDirection = Direction.SOUTH;
						}
						if (animations.contains(spriteType)) {
							southAnimation = animations.get(spriteType, facingDirection);
						} else {
							CharacterAnimations generalAnim = ComponentsMapper.player.get(entity).getGeneralAnimations();
							southAnimation = generalAnim.get(spriteType, facingDirection);
						}
						shadowDecal.setTextureRegion(southAnimation.getKeyFrames()[max(newFrame.index, 0)]);
					} else if (characterSpriteData.getSpriteType() == SpriteType.DIE) {
						if (animationComponent.getAnimation().isAnimationFinished((float) animationComponent.getStateTime())) {
							characterComponent.getCharacterSpriteData().setFacingDirection(Direction.findDirection(auxVector2_1.set(camera.position.x, camera.position.z).sub(decalPosition.x, decalPosition.z).nor()));
							characterSpriteData.setSpriteType(SpriteType.DEAD);
						}
					}
				}
			}
			if (!DefaultGameSettings.HIDE_ENEMIES || !ComponentsMapper.enemy.has(entity)) {
				lightsHandler.setDecalColorAccordingToLights(entity);
				decal.lookAt(auxVector3_1.set(decalPosition).sub(camera.direction), camera.up);
				decalBatch.add(decal);
				decalBatch.add(shadowDecal);
			}
		}
		for (Entity entity : renderSystemRelatedEntities.getSimpleDecalsEntities()) {
			SimpleDecalComponent simpleDecalComponent = ComponentsMapper.simpleDecal.get(entity);
			if (simpleDecalComponent.isVisible()) {
				decalBatch.add(simpleDecalComponent.getDecal());
			}
		}
		Gdx.gl.glDepthMask(false);
		decalBatch.flush();
	}


	private boolean isVisible(final Camera camera, final Entity entity) {
		if (DefaultGameSettings.DISABLE_FRUSTUM_CULLING) return true;
		ModelInstanceComponent modelInstanceComponent = ComponentsMapper.modelInstance.get(entity);
		modelInstanceComponent.getModelInstance().transform.getTranslation(auxVector3_1);
		auxVector3_1.add(modelInstanceComponent.getBoundingBox(auxBoundingBox).getCenter(auxVector3_2));
		auxBoundingBox.getDimensions(auxVector3_2);
		float max = max(auxVector3_2.x, max(auxVector3_2.y, auxVector3_2.z)) * 2;
		auxVector3_2.set(max, max, max);
		return camera.frustum.boundsInFrustum(auxVector3_1, auxVector3_2);
	}

	private void renderModels(final Camera camera,
							  final ModelBatch modelBatch,
							  final boolean renderWallsAndFloor,
							  final boolean renderLight) {
		Gdx.gl.glDepthMask(true);
		modelBatch.begin(camera);
		for (Entity entity : renderSystemRelatedEntities.getModelInstanceEntities()) {
			ModelInstanceComponent modelInstanceComponent = ComponentsMapper.modelInstance.get(entity);
			boolean isWall = ComponentsMapper.wall.has(entity);
			if ((!modelInstanceComponent.isVisible())
					|| (DefaultGameSettings.HIDE_ENVIRONMENT_OBJECTS && ComponentsMapper.obstacle.has(entity))
					|| (camera == environment.getShadowLight().getCamera() && !modelInstanceComponent.isCastShadow())
					|| (!renderWallsAndFloor && (isWall || ComponentsMapper.floor.has(entity)))
					|| (DefaultGameSettings.HIDE_CURSOR && ComponentsMapper.cursor.has(entity))
					|| !isVisible(getSystem(CameraSystem.class).getCamera(), entity)) {
				continue;
			}
			GameModelInstance modelInstance = modelInstanceComponent.getModelInstance();
			if (isWall) {
				float angleAround = MathUtils.round(modelInstance.transform.getRotation(auxQuat).getAngleAround(Vector3.Y));
				Vector2 modelAngle = auxVector2_1.set(1, 0).setAngleDeg(angleAround).rotate90((angleAround < 90 || angleAround > 270) || angleAround == 180 ? 1 : -1);
				Vector2 cameraAngle = auxVector2_2.set(camera.direction.x, camera.direction.z);
				float angle = auxVector2_3.set(1, 0).setAngleDeg(modelAngle.angleDeg(cameraAngle)).angleDeg();
				if (angle < 90 || angle > 270) {
					continue;
				}
			}
			if ((!DefaultGameSettings.HIDE_GROUND || !ComponentsMapper.floor.has(entity))) {
				if (renderLight) {
					lightsHandler.applyLightsOnModel(modelInstanceComponent);
				}
				Vector3 translation = ComponentsMapper.modelInstance.get(entity).getModelInstance().transform.getTranslation(new Vector3());
				if (ComponentsMapper.obstacle.has(entity) && translation.x == 3f && translation.z == 1f) {
					Gdx.app.log("!", "!");
				}
				numberOfVisible++;
				modelBatch.render(modelInstance, environment);
			}
		}
		modelBatch.end();
	}

	@Override
	public void dispose() {
		renderBatches.dispose();
		environment.dispose();
	}

	@Override
	public void onCameraSystemReady(final CameraSystem cameraSystem) {
		addSystem(CameraSystem.class, cameraSystem);
		this.renderBatches = new RenderBatches(cameraSystem.getCamera(), assetsManager, map.getFowMap());
		environment.initialize();
		systemReady();
	}

	private void systemReady() {
		if (getSystem(CameraSystem.class) == null) return;
		ready = true;
		for (RenderSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onRenderSystemReady(this);
		}
	}

	@Override
	public void onHudSystemReady(final HudSystem hudSystem) {
		addSystem(HudSystem.class, hudSystem);
		systemReady();
	}

	@Override
	public void onUserSelectedNodeToApplyTurn(final MapGraphNode cursorNode, final AttackNodesHandler attackNodesHandler) {

	}


	@Override
	public void activate() {

	}

	@Override
	public void entityAdded(final Entity entity) {

	}

	@Override
	public void entityRemoved(final Entity entity) {

	}

	@Override
	public int getNumberOfVisible() {
		return numberOfVisible;
	}

	@Override
	public int getNumberOfModelInstances() {
		return renderSystemRelatedEntities.getModelInstanceEntities().size();
	}
}