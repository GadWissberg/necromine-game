package com.gadarts.isometric.systems.render;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.particles.batches.PointSpriteParticleBatch;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.TimeUtils;
import com.gadarts.isometric.components.AnimationComponent;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.LightComponent;
import com.gadarts.isometric.components.ModelInstanceComponent;
import com.gadarts.isometric.components.ShadowLightComponent;
import com.gadarts.isometric.components.character.CharacterAnimations;
import com.gadarts.isometric.components.character.CharacterComponent;
import com.gadarts.isometric.components.character.data.CharacterSpriteData;
import com.gadarts.isometric.components.decal.character.CharacterAnimation;
import com.gadarts.isometric.components.decal.character.CharacterDecalComponent;
import com.gadarts.isometric.components.decal.simple.RelatedDecal;
import com.gadarts.isometric.components.decal.simple.SimpleDecalComponent;
import com.gadarts.isometric.components.enemy.EnemyAiStatus;
import com.gadarts.isometric.components.enemy.EnemyComponent;
import com.gadarts.isometric.components.model.AdditionalRenderData;
import com.gadarts.isometric.components.model.GameModelInstance;
import com.gadarts.isometric.services.GameServices;
import com.gadarts.isometric.systems.EventsNotifier;
import com.gadarts.isometric.systems.GameEntitySystem;
import com.gadarts.isometric.systems.camera.CameraSystem;
import com.gadarts.isometric.systems.camera.CameraSystemEventsSubscriber;
import com.gadarts.isometric.systems.hud.UserInterfaceSystem;
import com.gadarts.isometric.systems.hud.UserInterfaceSystemEventsSubscriber;
import com.gadarts.isometric.systems.hud.console.ConsoleCommandParameter;
import com.gadarts.isometric.systems.hud.console.ConsoleCommandResult;
import com.gadarts.isometric.systems.hud.console.ConsoleCommands;
import com.gadarts.isometric.systems.hud.console.ConsoleEventsSubscriber;
import com.gadarts.isometric.systems.hud.console.commands.ConsoleCommandsList;
import com.gadarts.isometric.systems.particles.ParticleEffectsSystemEventsSubscriber;
import com.gadarts.isometric.systems.player.PlayerSystem;
import com.gadarts.isometric.systems.player.PlayerSystemEventsSubscriber;
import com.gadarts.isometric.utils.DefaultGameSettings;
import com.gadarts.isometric.utils.map.MapGraph;
import com.gadarts.isometric.utils.map.MapGraphNode;
import com.gadarts.necromine.assets.Assets;
import com.gadarts.necromine.assets.GameAssetsManager;
import com.gadarts.necromine.model.characters.CharacterUtils;
import com.gadarts.necromine.model.characters.Direction;
import com.gadarts.necromine.model.characters.SpriteType;

import java.util.List;
import java.util.stream.IntStream;

import static com.gadarts.necromine.assets.Assets.Shaders.DEPTHMAP_FRAGMENT;
import static com.gadarts.necromine.assets.Assets.Shaders.DEPTHMAP_VERTEX;
import static com.gadarts.necromine.assets.Assets.Shaders.SHADOW_FRAGMENT;
import static com.gadarts.necromine.assets.Assets.Shaders.SHADOW_VERTEX;
import static java.lang.Math.max;

/**
 * Handles rendering.
 */
public class RenderSystemImpl extends GameEntitySystem<RenderSystemEventsSubscriber> implements
		RenderSystem,
		EntityListener,
		UserInterfaceSystemEventsSubscriber,
		PlayerSystemEventsSubscriber,
		CameraSystemEventsSubscriber,
		ParticleEffectsSystemEventsSubscriber,
		EventsNotifier<RenderSystemEventsSubscriber>,
		ConsoleEventsSubscriber {

	public static final String MSG_ACTIVATED = "activated";
	public static final String MSG_DISABLED = "disabled";
	public static final int DEPTH_MAP_SIZE = 1024;
	private static final Vector2 auxVector2_1 = new Vector2();
	private static final Vector2 auxVector2_2 = new Vector2();
	private static final Vector3 auxVector3_1 = new Vector3();
	private static final Vector3 auxVector3_2 = new Vector3();
	private static final Vector3 auxVector3_3 = new Vector3();
	private static final Quaternion auxQuat = new Quaternion();
	private static final BoundingBox auxBoundingBox = new BoundingBox();
	private static final String MSG_FC = "Frustum culling has been %s.";
	private static final int ICON_FLOWER_APPEARANCE_DURATION = 1000;
	public static final int CAMERA_LIGHT_FAR = 10;
	static public boolean take;
	public static FrameBuffer shadowFrameBuffer;
	private final DrawFlags drawFlags = new DrawFlags();
	private final StringBuilder stringBuilder = new StringBuilder();
	private WorldEnvironment environment;
	private RenderBatches renderBatches;
	private RenderSystemRelatedEntities renderSystemRelatedEntities;
	private boolean ready;
	private int numberOfVisible;
	private Camera camera;
	private BitmapFont skillFlowerFont;
	private GlyphLayout skillFlowerGlyph;
	private boolean frustumCull = !DefaultGameSettings.DISABLE_FRUSTUM_CULLING;
	private Texture iconFlowerLookingFor;
	private ImmutableArray<Entity> lights;
	private ShaderProgram shaderProgramShadows;
	private ShaderProgram depthShaderProgram;

	@Override
	public void init(final GameServices services) {
		super.init(services);
		GameAssetsManager am = services.getAssetManager();
		skillFlowerFont = new BitmapFont();
		skillFlowerGlyph = new GlyphLayout();
		iconFlowerLookingFor = am.getTexture(Assets.UiTextures.ICON_LOOKING_FOR);
		createShadowRelated();
		lights = getEngine().getEntitiesFor(Family.all(ShadowLightComponent.class).get());
	}

	private void createShadowMapForLight(final Entity light,
										 final PerspectiveCamera cameraLight) {
		GameFrameBufferCubeMap frameBuffer = new GameFrameBufferCubeMap(
				Format.RGBA8888,
				DEPTH_MAP_SIZE,
				DEPTH_MAP_SIZE,
				true);

		cameraLight.direction.set(0, 0, -1);
		cameraLight.up.set(0, 1, 0);
		cameraLight.position.set(ComponentsMapper.shadowLight.get(light).getPosition(auxVector3_1));
		cameraLight.rotate(Vector3.Y, 0);
		cameraLight.update();
		ShadowLightComponent lightComponent = ComponentsMapper.shadowLight.get(light);
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		depthShaderProgram.bind();
		depthShaderProgram.setUniformf("u_cameraFar", cameraLight.far);
		depthShaderProgram.setUniformf("u_lightPosition", cameraLight.position);
		for (int s = 0; s <= 5; s++) {
			Cubemap.CubemapSide side = Cubemap.CubemapSide.values()[s];
			frameBuffer.begin();
			frameBuffer.bindSide(side, cameraLight);
			Gdx.gl.glClearColor(0, 0, 0, 1);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

			renderModels(
					cameraLight,
					renderBatches.getDepthModelBatch(),
					true,
					false,
					lightComponent.getParent());
		}
		frameBuffer.end();
		lightComponent.setShadowFrameBuffer(frameBuffer);
	}

	private void createShadowRelated() {
		shadowFrameBuffer = new FrameBuffer(Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
	}

	@Override
	public void onPlayerSystemReady(final PlayerSystem playerSystem, final Entity player) {
		addSystem(PlayerSystem.class, playerSystem);
		if (ComponentsMapper.player.get(getSystem(PlayerSystem.class).getPlayer()).isDisabled()) {
			int[][] fowMap = services.getMapService().getMap().getFowMap();
			IntStream.range(0, fowMap.length)
					.forEach(row -> IntStream.range(0, fowMap[0].length)
							.forEach(col -> fowMap[row][col] = 1));
		}
	}

	@Override
	public void onPlayerStatusChanged(final boolean disabled) {

	}

	private void resetDisplay(@SuppressWarnings("SameParameterValue") final Color color) {
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
	}

	@Override
	public void update(final float deltaTime) {
		super.update(deltaTime);
		if (ready) {
			numberOfVisible = 0;
			resetDisplay(DefaultGameSettings.BACKGROUND_COLOR);
			CameraSystem system = getSystem(CameraSystem.class);
			renderWorld(deltaTime, system.getCamera());
			getSystem(UserInterfaceSystem.class).getStage().draw();
		}
	}

	private void renderWorld(final float deltaTime, final Camera camera) {
		renderShadows(camera);
		resetDisplay(DefaultGameSettings.BACKGROUND_COLOR);
		environment.getLightsHandler().updateLights((PooledEngine) getEngine());
		renderModels(camera, renderBatches.getModelBatch(), true, true);
		renderDecals(deltaTime, camera);
		renderParticleEffects(camera);
		renderSkillFlowersText();
	}

	private void renderShadows(final Camera camera) {
		shadowFrameBuffer.begin();
		Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 0.1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		renderModels(camera, renderBatches.getModelBatchShadows(), true, false);
		if (take) {
			ScreenshotFactory.saveScreenshot(shadowFrameBuffer.getWidth(), shadowFrameBuffer.getHeight(), "depthmap");
			take = false;
		}
		shadowFrameBuffer.end();
	}


	private void renderParticleEffects(final Camera camera) {
		renderBatches.getModelBatch().begin(camera);
		for (RenderSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onBeginRenderingParticleEffects(renderBatches.getModelBatch());
		}
		renderBatches.getModelBatch().end();
	}

	private void renderSkillFlowersText() {
		if (renderSystemRelatedEntities.getEnemyEntities().size() > 0) {
			renderBatches.getSpriteBatch().begin();
			for (Entity enemy : renderSystemRelatedEntities.getEnemyEntities()) {
				Vector2 nodePosition = ComponentsMapper.characterDecal.get(enemy).getNodePosition(auxVector2_1);
				if (isOutsideFow(services.getMapService().getMap().getNode((int) nodePosition.x, (int) nodePosition.y))) {
					if (ComponentsMapper.simpleDecal.has(enemy)) {
						renderSkillFlowerInsideContent(enemy, renderBatches.getSpriteBatch());
					}
				}
			}
			renderBatches.getSpriteBatch().end();
		}
	}

	private void renderDecals(final float deltaTime, final Camera camera) {
		Gdx.gl.glDepthMask(false);
		renderCorpses(deltaTime, camera);
		renderLiveCharacters(deltaTime, camera);
		renderSimpleDecals();
		Gdx.gl.glDepthMask(true);
	}

	private void renderSimpleDecals() {
		DecalBatch decalBatch = renderBatches.getDecalBatch();
		for (Entity entity : renderSystemRelatedEntities.getSimpleDecalsEntities()) {
			renderSimpleDecal(decalBatch, entity);
		}
		decalBatch.flush();
	}

	private void renderSimpleDecal(final DecalBatch decalBatch, final Entity entity) {
		SimpleDecalComponent simpleDecalComponent = ComponentsMapper.simpleDecal.get(entity);
		if (simpleDecalComponent != null && simpleDecalComponent.isVisible()) {
			Vector3 position = simpleDecalComponent.getDecal().getPosition();
			MapGraphNode node = services.getMapService().getMap().getNode((int) position.x, (int) position.z);
			if (!simpleDecalComponent.isAffectedByFow() || isOutsideFow(node)) {
				handleSimpleDecalAnimation(entity, simpleDecalComponent);
				faceDecalToCamera(simpleDecalComponent, simpleDecalComponent.getDecal());
				decalBatch.add(simpleDecalComponent.getDecal());
				renderRelatedDecals(decalBatch, simpleDecalComponent);
			}
		}
	}

	private void handleSimpleDecalAnimation(final Entity entity, final SimpleDecalComponent simpleDecalComponent) {
		if (ComponentsMapper.animation.has(entity) && simpleDecalComponent.isAnimatedByAnimationComponent()) {
			AnimationComponent animationComponent = ComponentsMapper.animation.get(entity);
			simpleDecalComponent.getDecal().setTextureRegion(animationComponent.calculateFrame());
		}
	}

	private void renderRelatedDecals(final DecalBatch decalBatch, final SimpleDecalComponent hudDecal) {
		List<RelatedDecal> relatedDecals = hudDecal.getRelatedDecals();
		if (!relatedDecals.isEmpty()) {
			for (RelatedDecal relatedDecal : relatedDecals) {
				if (relatedDecal.isVisible()) {
					faceDecalToCamera(hudDecal, relatedDecal);
					decalBatch.add(relatedDecal);
				}
			}
		}
	}

	private boolean isOutsideFow(final MapGraphNode node) {
		return services.getMapService().getMap().getFowMap()[node.getRow()][node.getCol()] != 0;
	}

	private void faceDecalToCamera(final SimpleDecalComponent simpleDecal, final Decal decal) {
		if (simpleDecal.isBillboard()) {
			decal.lookAt(auxVector3_1.set(decal.getPosition()).sub(camera.direction), camera.up);
		}
	}

	private void renderSkillFlowerInsideContent(final Entity enemy, final SpriteBatch spriteBatch) {
		EnemyComponent enemyComponent = ComponentsMapper.enemy.get(enemy);
		flipIconDisplayInFlower(enemyComponent);
		SimpleDecalComponent simpleDecalComponent = ComponentsMapper.simpleDecal.get(enemy);
		Vector3 screenPos = camera.project(auxVector3_1.set(simpleDecalComponent.getDecal().getPosition()));
		if (enemyComponent.getAiStatus() == EnemyAiStatus.SEARCHING && enemyComponent.isDisplayIconInFlower()) {
			renderSkillFlowerIcon(spriteBatch, screenPos);
		} else {
			renderSkillFlowerText(spriteBatch, enemyComponent, screenPos);
		}
	}

	private void renderSkillFlowerIcon(final SpriteBatch spriteBatch, final Vector3 screenPos) {
		float x = screenPos.x - iconFlowerLookingFor.getWidth() / 2F;
		float y = screenPos.y - iconFlowerLookingFor.getHeight() / 2F;
		spriteBatch.draw(iconFlowerLookingFor, x, y);
	}

	private void renderSkillFlowerText(final SpriteBatch spriteBatch,
									   final EnemyComponent enemyComponent,
									   final Vector3 screenPos) {
		stringBuilder.setLength(0);
		String text = stringBuilder.append(enemyComponent.getSkill()).toString();
		skillFlowerGlyph.setText(skillFlowerFont, text);
		float x = screenPos.x - skillFlowerGlyph.width / 2F;
		float y = screenPos.y + skillFlowerGlyph.height / 2F;
		skillFlowerFont.draw(spriteBatch, text, x, y);
	}

	private void flipIconDisplayInFlower(final EnemyComponent enemyComponent) {
		if (enemyComponent.getAiStatus() == EnemyAiStatus.SEARCHING) {
			long lastIconDisplayInFlower = enemyComponent.getIconDisplayInFlowerTimeStamp();
			if (TimeUtils.timeSinceMillis(lastIconDisplayInFlower) >= ICON_FLOWER_APPEARANCE_DURATION) {
				enemyComponent.setDisplayIconInFlower(!enemyComponent.isDisplayIconInFlower());
				enemyComponent.setIconDisplayInFlowerTimeStamp(TimeUtils.millis());
			}
		}
	}

	private void renderLiveCharacters(final float deltaTime, final Camera camera) {
		for (Entity entity : renderSystemRelatedEntities.getCharacterDecalsEntities()) {
			renderCharacterDecal(deltaTime, camera, entity, false);
		}
	}

	private void renderCorpses(final float deltaTime, final Camera camera) {
		DecalBatch decalBatch = renderBatches.getDecalBatch();
		for (Entity entity : renderSystemRelatedEntities.getCharacterDecalsEntities()) {
			renderCharacterDecal(deltaTime, camera, entity, true);
		}
		decalBatch.flush();
	}

	private void renderCharacterDecal(final float deltaTime,
									  final Camera camera,
									  final Entity entity,
									  final boolean renderCorpse) {
		CharacterComponent characterComponent = ComponentsMapper.character.get(entity);
		AnimationComponent animationComponent = ComponentsMapper.animation.get(entity);
		CharacterSpriteData characterSpriteData = characterComponent.getCharacterSpriteData();
		boolean dead = characterSpriteData.getSpriteType().isDeath() && animationComponent.getAnimation().isAnimationFinished(animationComponent.getStateTime());
		boolean isPlayerDisabled = ComponentsMapper.player.has(entity) && ComponentsMapper.player.get(entity).isDisabled();
		if ((renderCorpse && !dead) || (!renderCorpse && dead) || isPlayerDisabled) {
			return;
		}
		DecalBatch decalBatch = renderBatches.getDecalBatch();
		CharacterDecalComponent characterDecalComponent = ComponentsMapper.characterDecal.get(entity);
		Direction direction = CharacterUtils.calculateDirectionSeenFromCamera(camera, characterSpriteData.getFacingDirection());
		SpriteType spriteType = characterSpriteData.getSpriteType();
		boolean sameSpriteType = spriteType.equals(characterDecalComponent.getSpriteType());
		boolean sameDirection = characterDecalComponent.getDirection().equals(direction);
		Decal decal = characterDecalComponent.getDecal();
		CharacterAnimations animations = characterDecalComponent.getAnimations();
		Decal shadowDecal = characterDecalComponent.getShadowDecal();
		Vector3 decalPosition = decal.getPosition();
		boolean isEnemy = ComponentsMapper.enemy.has(entity);
		if ((!sameSpriteType || !sameDirection)) {
			characterDecalComponent.initializeSprite(spriteType, direction);
			if (ComponentsMapper.animation.has(entity)) {
				if (spriteType.isSingleAnimation()) {
					if (!animationComponent.getAnimation().isAnimationFinished(animationComponent.getStateTime())) {
						direction = Direction.SOUTH;
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
				if (getSystem(UserInterfaceSystem.class).isMenuClosed() && animationComponent.getAnimation() != null) {
					TextureAtlas.AtlasRegion currentFrame = (TextureAtlas.AtlasRegion) decal.getTextureRegion();
					TextureAtlas.AtlasRegion newFrame = animationComponent.calculateFrame();
					if (currentFrame.index != newFrame.index) {
						for (RenderSystemEventsSubscriber subscriber : subscribers) {
							subscriber.onFrameChanged(entity, deltaTime, newFrame);
						}
					}
					if (characterDecalComponent.getSpriteType() == spriteType && currentFrame != newFrame) {
						decal.setTextureRegion(newFrame);
						CharacterAnimation southAnimation = null;
						Direction facingDirection = characterSpriteData.getFacingDirection();
						if (spriteType.isSingleAnimation()) {
							facingDirection = Direction.SOUTH;
						}
						if (animations.contains(spriteType)) {
							southAnimation = animations.get(spriteType, facingDirection);
						} else {
							if (ComponentsMapper.player.has(entity)) {
								CharacterAnimations generalAnim = ComponentsMapper.player.get(entity).getGeneralAnimations();
								southAnimation = generalAnim.get(spriteType, facingDirection);
							}
						}
						if (southAnimation != null && !characterSpriteData.getSpriteType().isDeath()) {
							shadowDecal.setTextureRegion(southAnimation.getKeyFrames()[max(newFrame.index, 0)]);
						}
					}
				}
			}
		}
		if (drawFlags.isDrawEnemy() || !isEnemy) {
			MapGraph map = services.getMapService().getMap();
			MapGraphNode characterNode = map.getNode(characterDecalComponent.getNodePosition(auxVector2_1));
			if (map.getFowMap()[characterNode.getRow()][characterNode.getCol()] == 1) {
				environment.getLightsHandler().setDecalColorAccordingToLights(entity, environment);
				decal.lookAt(auxVector3_1.set(decalPosition).sub(camera.direction), camera.up);
				decalBatch.add(decal);
				if (!characterSpriteData.getSpriteType().isDeath()) {
					decalBatch.add(shadowDecal);
				}
			}
		}
	}

	private boolean isVisible(final Camera camera, final Entity entity) {
		if (!frustumCull) return true;
		ModelInstanceComponent modelInstanceComponent = ComponentsMapper.modelInstance.get(entity);
		Vector3 position = modelInstanceComponent.getModelInstance().transform.getTranslation(auxVector3_1);
		AdditionalRenderData additionalRenderData = modelInstanceComponent.getModelInstance().getAdditionalRenderData();
		BoundingBox boundingBox = additionalRenderData.getBoundingBox(auxBoundingBox);
		Vector3 center = boundingBox.getCenter(auxVector3_3);
		Vector3 dim = auxBoundingBox.getDimensions(auxVector3_2);
		return camera.frustum.boundsInFrustum(position.add(center), dim);
	}

	private void renderModels(final Camera camera,
							  final ModelBatch modelBatch,
							  final boolean renderWallsAndFloor,
							  final boolean renderLight) {
		renderModels(camera, modelBatch, renderWallsAndFloor, renderLight, null);
	}

	private void renderModels(final Camera camera,
							  final ModelBatch modelBatch,
							  final boolean renderWallsAndFloor,
							  final boolean renderLight,
							  final Entity exclude) {
		modelBatch.begin(camera);
		for (Entity entity : renderSystemRelatedEntities.getModelInstanceEntities()) {
			ModelInstanceComponent modelInstanceComponent = ComponentsMapper.modelInstance.get(entity);
			boolean isObstacle = ComponentsMapper.obstacle.has(entity);
			boolean isWall = isObstacle && ComponentsMapper.obstacle.get(entity).getType().isWall();
			if (entity == exclude
					|| (!modelInstanceComponent.isVisible())
					|| (!drawFlags.isDrawEnv() && isObstacle)
					|| (camera == environment.getShadowLight().getCamera() && !modelInstanceComponent.isCastShadow())
					|| (!renderWallsAndFloor && (isWall || ComponentsMapper.floor.has(entity)))
					|| (ComponentsMapper.cursor.has(entity) && (ComponentsMapper.cursor.get(entity).isDisabled() || !drawFlags.isDrawCursor()))
					|| !isVisible(camera, entity)) {
				continue;
			}
			GameModelInstance modelInstance = modelInstanceComponent.getModelInstance();
			if (isObstacle && ComponentsMapper.obstacle.get(entity).getType().isRenderWhenFrontOnly()) {
				float angleAround = MathUtils.round(modelInstance.transform.getRotation(auxQuat).getAngleAround(Vector3.Y));
				Vector2 cameraAngle = auxVector2_2.set(camera.direction.x, camera.direction.z);
				float angle = auxVector2_1.set(1, 0).setAngleDeg(angleAround + (angleAround < 90 || angleAround > 270 || angleAround == 180 ? 0 : 180)).angleDeg(cameraAngle);
				if (angle < 90 || angle > 270) {
					continue;
				}
			}
			if ((drawFlags.isDrawGround() || !ComponentsMapper.floor.has(entity))) {
				if (renderLight) {
					environment.getLightsHandler().applyLightsOnModel(modelInstanceComponent);
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
		depthShaderProgram.dispose();
		shaderProgramShadows.dispose();
		shadowFrameBuffer.dispose();
		for (Entity light : lights) {
			ComponentsMapper.light.get(light).getShadowFrameBuffer().dispose();
		}
	}

	@Override
	public void onCameraSystemReady(final CameraSystem cameraSystem) {
		addSystem(CameraSystem.class, cameraSystem);
		camera = cameraSystem.getCamera();
		environment = new WorldEnvironment(services.getMapService().getMap().getAmbient(), camera);
		environment.initialize(getEngine().getEntitiesFor(Family.all(LightComponent.class).get()));
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
	public void onHudSystemReady(final UserInterfaceSystem userInterfaceSystem) {
		addSystem(UserInterfaceSystem.class, userInterfaceSystem);
		systemReady();
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

	@Override
	public DrawFlags getDrawFlags() {
		return drawFlags;
	}

	@Override
	public RenderBatches getRenderBatches() {
		return renderBatches;
	}

	@Override
	public void onConsoleActivated() {

	}

	@Override
	public boolean onCommandRun(final ConsoleCommands command, final ConsoleCommandResult consoleCommandResult) {
		if (command == ConsoleCommandsList.FRUSTUM_CULLING) {
			handleFrustumCullingCommand(consoleCommandResult);
			return true;
		}
		return false;
	}

	private void handleFrustumCullingCommand(final ConsoleCommandResult consoleCommandResult) {
		frustumCull = !frustumCull;
		String msg = frustumCull ? String.format(MSG_FC, MSG_ACTIVATED) : String.format(MSG_FC, MSG_DISABLED);
		consoleCommandResult.setMessage(msg);
	}

	@Override
	public boolean onCommandRun(final ConsoleCommands command,
								final ConsoleCommandResult consoleCommandResult,
								final ConsoleCommandParameter parameter) {
		boolean result = false;
		if (command == ConsoleCommandsList.SKIP_RENDER) {
			drawFlags.applySkipRenderCommand(parameter);
			result = true;
		}
		return result;
	}


	@Override
	public void onConsoleDeactivated() {

	}

	@Override
	public void onParticleEffectsSystemReady(final PointSpriteParticleBatch pointSpriteBatch) {
		GameAssetsManager am = services.getAssetManager();
		shaderProgramShadows = new ShaderProgram(am.getShader(SHADOW_VERTEX), am.getShader(SHADOW_FRAGMENT));
		depthShaderProgram = new ShaderProgram(am.getShader(DEPTHMAP_VERTEX), am.getShader(DEPTHMAP_FRAGMENT));
		this.renderBatches = new RenderBatches(camera, pointSpriteBatch, services, new DefaultShaderProvider() {
			@Override
			protected Shader createShader(final Renderable renderable) {
				return new DepthMapShader(renderable, depthShaderProgram);
			}
		}, new DefaultShaderProvider() {
			@Override
			protected Shader createShader(final Renderable renderable) {
				return new ShadowMapShader(renderable, shaderProgramShadows, lights);
			}
		});
		createShadowMaps();
	}

	private void createShadowMaps() {
		PerspectiveCamera cameraLight = new PerspectiveCamera(90f, DEPTH_MAP_SIZE, DEPTH_MAP_SIZE);
		cameraLight.near = 0.0001F;
		cameraLight.far = CAMERA_LIGHT_FAR;
		for (Entity light : lights) {
			createShadowMapForLight(light, cameraLight);
		}
	}
}