package com.gadarts.isometric.systems.render;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.gadarts.isometric.components.*;
import com.gadarts.isometric.components.character.CharacterAnimations;
import com.gadarts.isometric.components.character.CharacterComponent;
import com.gadarts.isometric.components.decal.CharacterDecalComponent;
import com.gadarts.isometric.components.decal.RelatedDecal;
import com.gadarts.isometric.components.decal.SimpleDecalComponent;
import com.gadarts.isometric.components.model.AdditionalRenderData;
import com.gadarts.isometric.components.model.GameModelInstance;
import com.gadarts.isometric.services.GameServices;
import com.gadarts.isometric.systems.EventsNotifier;
import com.gadarts.isometric.systems.GameEntitySystem;
import com.gadarts.isometric.systems.camera.CameraSystem;
import com.gadarts.isometric.systems.camera.CameraSystemEventsSubscriber;
import com.gadarts.isometric.systems.hud.HudSystem;
import com.gadarts.isometric.systems.hud.HudSystemEventsSubscriber;
import com.gadarts.isometric.systems.hud.console.ConsoleCommandParameter;
import com.gadarts.isometric.systems.hud.console.ConsoleCommandResult;
import com.gadarts.isometric.systems.hud.console.ConsoleCommands;
import com.gadarts.isometric.systems.hud.console.ConsoleEventsSubscriber;
import com.gadarts.isometric.systems.hud.console.commands.ConsoleCommandsList;
import com.gadarts.isometric.systems.player.PlayerSystem;
import com.gadarts.isometric.systems.player.PlayerSystemEventsSubscriber;
import com.gadarts.isometric.utils.DefaultGameSettings;
import com.gadarts.isometric.utils.map.MapGraph;
import com.gadarts.isometric.utils.map.MapGraphNode;
import com.gadarts.necromine.assets.GameAssetsManager;
import com.gadarts.necromine.model.characters.CharacterUtils;
import com.gadarts.necromine.model.characters.Direction;
import com.gadarts.necromine.model.characters.SpriteType;

import java.util.List;

import static java.lang.Math.max;

/**
 * Handles rendering.
 */
public class RenderSystemImpl extends GameEntitySystem<RenderSystemEventsSubscriber> implements
		RenderSystem,
		EntityListener,
		HudSystemEventsSubscriber,
		PlayerSystemEventsSubscriber,
		CameraSystemEventsSubscriber,
		EventsNotifier<RenderSystemEventsSubscriber>,
		ConsoleEventsSubscriber {

	private static final Vector2 auxVector2_1 = new Vector2();
	private static final Vector2 auxVector2_2 = new Vector2();
	private static final Vector3 auxVector3_1 = new Vector3();
	private static final Vector3 auxVector3_2 = new Vector3();
	private static final Vector3 auxVector3_3 = new Vector3();
	private static final Quaternion auxQuat = new Quaternion();
	private static final BoundingBox auxBoundingBox = new BoundingBox();
	private static final String MSG_FC = "Frustum culling has been %s.";
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

	@Override
	public void init(final GameServices services) {
		super.init(services);
		environment = new WorldEnvironment(services.getMap().getAmbient());
		skillFlowerFont = new BitmapFont();
		skillFlowerGlyph = new GlyphLayout();
	}

	@Override
	public void onPlayerSystemReady(final PlayerSystem playerSystem, final Entity player) {
		addSystem(PlayerSystem.class, playerSystem);
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
			renderWorld(deltaTime, system.getCamera(), system.getRotationPoint(auxVector3_1));
			getSystem(HudSystem.class).getStage().draw();
		}
	}

	private void renderWorld(final float deltaTime, final Camera camera, final Vector3 lastRotationPoint) {
		if (!DefaultGameSettings.DISABLE_SHADOWS) {
			renderModelsShadows(camera, lastRotationPoint);
		}
		resetDisplay(DefaultGameSettings.BACKGROUND_COLOR);
		renderModels(camera, renderBatches.getModelBatch(), true, true);
		renderDecals(deltaTime, camera);
		renderSkillFlowersText();
	}

	private void renderSkillFlowersText() {
		if (renderSystemRelatedEntities.getEnemyEntities().size() > 0) {
			SpriteBatch spriteBatch = renderBatches.getSpriteBatch();
			spriteBatch.begin();
			for (Entity enemy : renderSystemRelatedEntities.getEnemyEntities()) {
				Vector2 nodePosition = ComponentsMapper.characterDecal.get(enemy).getNodePosition(auxVector2_1);
				if (isOutsideFow(services.getMap().getNode((int) nodePosition.x, (int) nodePosition.y))) {
					renderSkillFlowerText(enemy, renderBatches.getSpriteBatch());
				}
			}
			spriteBatch.end();
		}
	}

	private void renderModelsShadows(final Camera camera, final Vector3 lastRotationPoint) {
		DirectionalShadowLight shadowLight = environment.getShadowLight();
		Camera shadowLightCamera = shadowLight.getCamera();
		shadowLight.begin(lastRotationPoint.set(lastRotationPoint.x, camera.position.y, lastRotationPoint.z + 10f), shadowLightCamera.direction);
		renderModels(shadowLightCamera, renderBatches.getShadowBatch(), false, false);
		shadowLight.end();
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
		if (simpleDecalComponent.isVisible()) {
			Vector3 position = simpleDecalComponent.getDecal().getPosition();
			MapGraphNode node = services.getMap().getNode((int) position.x, (int) position.z);
			if (!simpleDecalComponent.isAffectedByFow() || isOutsideFow(node)) {
				handleSimpleDecalAnimation(entity, simpleDecalComponent.getDecal());
				faceDecalToCamera(simpleDecalComponent, simpleDecalComponent.getDecal());
				decalBatch.add(simpleDecalComponent.getDecal());
				renderRelatedDecals(decalBatch, simpleDecalComponent);
			}
		}
	}

	private void handleSimpleDecalAnimation(final Entity entity, final Decal decal) {
		if (ComponentsMapper.animation.has(entity)) {
			AnimationComponent animationComponent = ComponentsMapper.animation.get(entity);
			decal.setTextureRegion(animationComponent.calculateFrame());
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
		return services.getMap().getFowMap()[node.getRow()][node.getCol()] != 0;
	}

	private void faceDecalToCamera(final SimpleDecalComponent simpleDecal, final Decal decal) {
		if (simpleDecal.isBillboard()) {
			decal.lookAt(auxVector3_1.set(decal.getPosition()).sub(camera.direction), camera.up);
		}
	}

	private void renderSkillFlowerText(final Entity entity, final SpriteBatch spriteBatch) {
		SimpleDecalComponent simpleDecalComponent = ComponentsMapper.simpleDecal.get(entity);
		Vector3 screenPos = camera.project(auxVector3_1.set(simpleDecalComponent.getDecal().getPosition()));
		stringBuilder.setLength(0);
		String text = stringBuilder.append(ComponentsMapper.enemy.get(entity).getSkill()).toString();
		skillFlowerGlyph.setText(skillFlowerFont, text);
		float x = screenPos.x - skillFlowerGlyph.width / 2F;
		float y = screenPos.y + skillFlowerGlyph.height / 2F;
		skillFlowerFont.draw(spriteBatch, text, x, y);
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
		boolean dead = characterComponent.getCharacterSpriteData().getSpriteType() == SpriteType.DEAD;
		boolean isPlayerDisabled = ComponentsMapper.player.has(entity) && ComponentsMapper.player.get(entity).isDisabled();
		if ((renderCorpse && !dead) || (!renderCorpse && dead) || isPlayerDisabled) {
			return;
		}
		DecalBatch decalBatch = renderBatches.getDecalBatch();
		AnimationComponent animationComponent = ComponentsMapper.animation.get(entity);
		CharacterDecalComponent characterDecalComponent = ComponentsMapper.characterDecal.get(entity);
		Direction direction = CharacterUtils.calculateDirectionSeenFromCamera(camera, characterComponent.getCharacterSpriteData().getFacingDirection());
		SpriteType spriteType = characterComponent.getCharacterSpriteData().getSpriteType();
		boolean sameSpriteType = characterComponent.getCharacterSpriteData().getSpriteType().equals(characterDecalComponent.getSpriteType());
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
					Direction facingDirection = characterComponent.getCharacterSpriteData().getFacingDirection();
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
				} else if (characterComponent.getCharacterSpriteData().getSpriteType() == SpriteType.DIE) {
					if (animationComponent.getAnimation().isAnimationFinished((float) animationComponent.getStateTime())) {
						characterComponent.getCharacterSpriteData().setFacingDirection(Direction.findDirection(auxVector2_1.set(camera.position.x, camera.position.z).sub(decalPosition.x, decalPosition.z).nor()));
						characterComponent.getCharacterSpriteData().setSpriteType(SpriteType.DEAD);
					}
				}
			}
		}
		if (drawFlags.isDrawEnemy() || !isEnemy) {
			MapGraph map = services.getMap();
			MapGraphNode characterNode = map.getNode(characterDecalComponent.getNodePosition(auxVector2_1));
			if (map.getFowMap()[characterNode.getRow()][characterNode.getCol()] == 1) {
				environment.getLightsRenderer().setDecalColorAccordingToLights(entity, environment);
				decal.lookAt(auxVector3_1.set(decalPosition).sub(camera.direction), camera.up);
				decalBatch.add(decal);
				decalBatch.add(shadowDecal);
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
		modelBatch.begin(camera);
		for (Entity entity : renderSystemRelatedEntities.getModelInstanceEntities()) {
			ModelInstanceComponent modelInstanceComponent = ComponentsMapper.modelInstance.get(entity);
			boolean isObstacle = ComponentsMapper.obstacle.has(entity);
			boolean isWall = isObstacle && ComponentsMapper.obstacle.get(entity).getType().isWall();
			if ((!modelInstanceComponent.isVisible())
					|| (!drawFlags.isDrawEnv() && isObstacle)
					|| (camera == environment.getShadowLight().getCamera() && !modelInstanceComponent.isCastShadow())
					|| (!renderWallsAndFloor && (isWall || ComponentsMapper.floor.has(entity)))
					|| (ComponentsMapper.cursor.has(entity) && (ComponentsMapper.cursor.get(entity).isDisabled() || !drawFlags.isDrawCursor()))
					|| !isVisible(getSystem(CameraSystem.class).getCamera(), entity)) {
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
					environment.getLightsRenderer().applyLightsOnModel(modelInstanceComponent);
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
		camera = cameraSystem.getCamera();
		GameAssetsManager assetManager = services.getAssetManager();
		this.renderBatches = new RenderBatches(camera, assetManager, services.getMap());
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
	public void onHudSystemReady(final HudSystem hudSystem) {
		addSystem(HudSystem.class, hudSystem);
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
	public void onConsoleActivated() {

	}

	@Override
	public boolean onCommandRun(final ConsoleCommands command, final ConsoleCommandResult consoleCommandResult) {
		boolean result = false;
		if (command == ConsoleCommandsList.FRUSTUM_CULLING) {
			frustumCull = !frustumCull;
			String msg = frustumCull ? String.format(MSG_FC, "activated") : String.format(MSG_FC, "disabled");
			consoleCommandResult.setMessage(msg);
			result = true;
		}
		return result;
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
}