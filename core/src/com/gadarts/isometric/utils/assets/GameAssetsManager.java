package com.gadarts.isometric.utils.assets;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.Model;
import com.gadarts.isometric.components.character.CharacterAnimations;
import com.gadarts.isometric.components.character.CharacterComponent;
import com.gadarts.isometric.components.character.SpriteType;
import com.gadarts.isometric.utils.AtlasDefinition;
import com.gadarts.isometric.utils.ModelDefinition;
import com.gadarts.isometric.utils.TextureDefinition;
import com.gadarts.isometric.utils.assets.Assets.Atlases;
import com.gadarts.isometric.utils.assets.Assets.Textures.FloorTextures;

import java.util.Arrays;

public class GameAssetsManager extends AssetManager {

	public void loadGameFiles() {
		Arrays.stream(FloorTextures.values()).forEach(floor -> load(
				Gdx.files.getFileHandle(floor.getFilePath(), FileType.Internal).path(),
				Texture.class
		));
		Arrays.stream(Assets.Models.values()).forEach(model -> load(
				Gdx.files.getFileHandle(model.getFilePath(), FileType.Internal).path(),
				Model.class
		));
		Arrays.stream(Atlases.values()).forEach(atlas -> load(
				Gdx.files.getFileHandle(atlas.getFilePath(), FileType.Internal).path(),
				TextureAtlas.class
		));
		finishLoading();
		Arrays.stream(Atlases.values()).forEach(atlas -> {
					CharacterAnimations animations = createCharacterAnimations(atlas);
					addAsset(atlas.name(), CharacterAnimations.class, animations);
				}
		);
	}

	private CharacterAnimations createCharacterAnimations(final Atlases zealot) {
		CharacterAnimations animations = new CharacterAnimations();
		TextureAtlas atlas = getAtlas(zealot);
		Arrays.stream(SpriteType.values()).forEach(spriteType -> {
			if (spriteType.isSingleAnimation()) {
				inflateCharacterAnimation(animations, atlas, spriteType, CharacterComponent.Direction.SOUTH);
			} else {
				CharacterComponent.Direction[] directions = CharacterComponent.Direction.values();
				Arrays.stream(directions).forEach(dir -> inflateCharacterAnimation(animations, atlas, spriteType, dir));
			}
		});
		return animations;
	}

	private void inflateCharacterAnimation(final CharacterAnimations animations,
										   final TextureAtlas atlas,
										   final SpriteType spriteType,
										   final CharacterComponent.Direction dir) {
		String name;
		String spriteTypeName = spriteType.name().toLowerCase();
		if (spriteType.isSingleAnimation()) {
			name = spriteTypeName;
		} else {
			name = spriteTypeName + "_" + dir.name().toLowerCase();
		}
		Animation<TextureAtlas.AtlasRegion> a = createAnimation(atlas, spriteType, name);
		animations.put(spriteType, dir, a);
	}

	private Animation<TextureAtlas.AtlasRegion> createAnimation(final TextureAtlas atlas,
																final SpriteType spriteType,
																final String name) {
		return new Animation<>(
				spriteType.getAnimationDuration(),
				atlas.findRegions(name),
				spriteType.getPlayMode()
		);
	}

	public TextureAtlas getAtlas(final AtlasDefinition atlas) {
		return get(atlas.getFilePath(), TextureAtlas.class);
	}

	public Model getModel(final ModelDefinition model) {
		return get(model.getFilePath(), Model.class);
	}

	public Texture getTexture(final TextureDefinition definition) {
		return get(definition.getFilePath());
	}
}
