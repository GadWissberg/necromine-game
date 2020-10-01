package com.gadarts.isometric.utils;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.isometric.components.*;

import java.util.Arrays;

import static com.badlogic.gdx.graphics.Texture.TextureWrap.Repeat;

/**
 * Creates the map.
 */
public final class MapBuilder {
	private static final Vector3 auxVector3_1 = new Vector3();
	private static final Vector3 auxVector3_2 = new Vector3();
	private static final Vector3 auxVector3_3 = new Vector3();
	private static final Vector3 auxVector3_4 = new Vector3();
	private static final Vector3 auxVector3_5 = new Vector3();

	private final GameAssetsManager assetManager;
	private final PooledEngine engine;
	private final ModelBuilder modelBuilder;

	public MapBuilder(final GameAssetsManager assetManager, final PooledEngine engine) {
		this.assetManager = assetManager;
		this.engine = engine;
		this.modelBuilder = new ModelBuilder();
	}

	/**
	 * Test map.
	 *
	 * @return The map graph.
	 */
	public MapGraph createAndAddTestMap() {
		MapGraph map = new MapGraph();
		addTestFloor();
		createAndAdd3dCursor();
		addPlayer();
		addEnemyTest();
		return map;
	}

	private void addPlayer() {
		Entity entity = engine.createEntity();
		entity.add(engine.createComponent(PlayerComponent.class));
		addCharacterBaseComponents(entity, Assets.Atlases.PLAYER, auxVector3_1.set(0.5f, 0.3f, 0.5f), null);
		engine.addEntity(entity);
	}

	private void addEnemyTest() {
		Entity entity = engine.createEntity();
		entity.add(engine.createComponent(EnemyComponent.class));
		Entity player = engine.getEntitiesFor(Family.all(PlayerComponent.class).get()).first();
		addCharacterBaseComponents(entity, Assets.Atlases.ZEALOT, auxVector3_1.set(2.5f, 0.3f, 2.5f), player);
		engine.addEntity(entity);
	}

	private void addCharacterBaseComponents(final Entity entity,
											final Assets.Atlases atlas,
											final Vector3 position,
											final Entity target) {
		CharacterAnimations animations = createCharacterAnimations(atlas);
		CharacterComponent charComponent = addCharacterComponent(entity, target);
		charComponent.init(CharacterComponent.Direction.SOUTH, SpriteType.IDLE);
		addCharacterDecalComponent(entity, animations, charComponent, position);
		AnimationComponent animComponent = engine.createComponent(AnimationComponent.class);
		animComponent.init(0.4f, animations.get(charComponent.getSpriteType(), charComponent.getDirection()));
		entity.add(animComponent);
	}

	private CharacterComponent addCharacterComponent(final Entity entity, final Entity target) {
		CharacterComponent charComponent = engine.createComponent(CharacterComponent.class);
		charComponent.setTarget(target);
		entity.add(charComponent);
		return charComponent;
	}

	private DecalComponent addCharacterDecalComponent(final Entity entity,
													  final CharacterAnimations animations,
													  final CharacterComponent characterComponent,
													  final Vector3 position) {
		DecalComponent decalComponent = engine.createComponent(DecalComponent.class);
		decalComponent.init(animations, characterComponent.getSpriteType(), characterComponent.getDirection());
		Decal decal = decalComponent.getDecal();
		decal.setPosition(position);
		decal.setScale(0.01f);
		entity.add(decalComponent);
		return decalComponent;
	}

	private CharacterAnimations createCharacterAnimations(final Assets.Atlases zealot) {
		CharacterAnimations animations = new CharacterAnimations();
		TextureAtlas atlas = assetManager.getAtlas(zealot);
		Arrays.stream(SpriteType.values()).forEach(spriteType -> Arrays.stream(CharacterComponent.Direction.values())
				.forEach(dir -> {
					String name = spriteType.name().toLowerCase() + "_" + dir.name().toLowerCase();
					float animaDur = spriteType.getAnimationDuration();
					Animation<TextureAtlas.AtlasRegion> a = new Animation<>(
							animaDur,
							atlas.findRegions(name),
							spriteType.getPlayMode()
					);
					animations.put(spriteType, dir, a);
				})
		);
		return animations;
	}

	private void addTestFloor() {
		Entity testFloor = engine.createEntity();
		ModelInstanceComponent component = engine.createComponent(ModelInstanceComponent.class);
		component.init(createTestFloorModelInstance(modelBuilder));
		testFloor.add(component);
		engine.addEntity(testFloor);
	}

	private void createAndAdd3dCursor() {
		Model model = modelTestCursor();
		ModelInstanceComponent modelInstanceComponent = engine.createComponent(ModelInstanceComponent.class);
		modelInstanceComponent.init(new ModelInstance(model));
		CursorComponent cursorComponent = engine.createComponent(CursorComponent.class);
		Entity entity = engine.createEntity();
		entity.add(modelInstanceComponent);
		entity.add(cursorComponent);
		engine.addEntity(entity);
	}

	private Model modelTestCursor() {
		modelBuilder.begin();

		Material material = new Material(ColorAttribute.createDiffuse(Color.YELLOW));

		MeshPartBuilder meshPartBuilder = modelBuilder.part(
				"test_cursor",
				GL20.GL_LINES,
				Usage.Position | Usage.Normal,
				material);

		meshPartBuilder.rect(
				auxVector3_1.set(1, 0, 1),
				auxVector3_2.set(1, 0, 0),
				auxVector3_3.set(0, 0, 0),
				auxVector3_4.set(0, 0, 1),
				auxVector3_5.set(0, -1, 0));

		meshPartBuilder.rect(
				auxVector3_1.set(1, 1.5f, 1),
				auxVector3_2.set(1, 1.5f, 0),
				auxVector3_3.set(0, 1.5f, 0),
				auxVector3_4.set(0, 1.5f, 1),
				auxVector3_5.set(0, 1, 0));

		meshPartBuilder.rect(
				auxVector3_1.set(1, 0, 0),
				auxVector3_2.set(1, 1.5f, 0),
				auxVector3_3.set(1, 1.5f, 1),
				auxVector3_4.set(1, 0, 1),
				auxVector3_5.set(1, 0, 0));

		meshPartBuilder.rect(
				auxVector3_1.set(1, 0, 1),
				auxVector3_2.set(1, 1.5f, 1),
				auxVector3_3.set(0, 1.5f, 1),
				auxVector3_4.set(0, 0, 1),
				auxVector3_5.set(0, 0, 1));

		meshPartBuilder.rect(
				auxVector3_1.set(0, 0, 1),
				auxVector3_2.set(0, 1.5f, 1),
				auxVector3_3.set(0, 1.5f, 0),
				auxVector3_4.set(0, 0, 0),
				auxVector3_5.set(-1, 0, 0));

		meshPartBuilder.rect(
				auxVector3_1.set(0, 0, 0),
				auxVector3_2.set(0, 1.5f, 0),
				auxVector3_3.set(1, 1.5f, 0),
				auxVector3_4.set(1, 0, 0),
				auxVector3_5.set(0, 0, -1));

		return modelBuilder.end();
	}

	private ModelInstance createTestFloorModelInstance(final ModelBuilder modelBuilder) {
		modelBuilder.begin();
		MeshPartBuilder meshPartBuilder = modelBuilder.part("test_floor",
				GL20.GL_TRIANGLES,
				Usage.Position | Usage.Normal | Usage.TextureCoordinates,
				createTestFloorMaterial());
		createRect(meshPartBuilder, 4, 4, 0, 0);
		Model testFloorModel = modelBuilder.end();
		return new ModelInstance(testFloorModel);
	}

	private void createRect(final MeshPartBuilder meshPartBuilder,
							final int size,
							final int uv,
							final float xOffset,
							final float zOffset) {
		meshPartBuilder.setUVRange(0, 0, uv, uv);
		meshPartBuilder.rect(
				auxVector3_4.set(xOffset, 0, size + zOffset),
				auxVector3_1.set(size + xOffset, 0, size + zOffset),
				auxVector3_2.set(size + xOffset, 0, zOffset),
				auxVector3_3.set(xOffset, 0, zOffset),
				auxVector3_5.set(0, 1, 0));
	}

	private Material createTestFloorMaterial() {
		Texture floor = assetManager.getTexture(Assets.Textures.FloorTextures.FLOOR_0);
		floor.setWrap(Repeat, Repeat);
		Material material = new Material(TextureAttribute.createDiffuse(floor));
		material.id = "floor_test";
		return material;

	}
}
