package com.gadarts.isometric.utils.assets;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.Model;
import com.gadarts.isometric.components.CharacterAnimation;
import com.gadarts.isometric.components.character.CharacterAnimations;
import com.gadarts.isometric.components.character.CharacterComponent;
import com.gadarts.isometric.components.character.SpriteType;
import com.gadarts.isometric.utils.assets.Assets.Atlases;
import com.gadarts.isometric.utils.assets.definitions.AtlasDefinition;
import com.gadarts.isometric.utils.assets.definitions.ModelDefinition;
import com.gadarts.isometric.utils.assets.definitions.TextureDefinition;

import java.util.Arrays;

public class GameAssetsManager extends AssetManager {

	public void loadGameFiles() {
		Arrays.stream(Assets.AssetsTypes.values()).forEach(type ->
				Arrays.stream(type.getAssetDefinitions()).forEach(def ->
						load(Gdx.files.getFileHandle(def.getFilePath(), FileType.Internal).path(), def.getTypeClass()))
		);
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
		CharacterAnimation a = createAnimation(atlas, spriteType, name, dir);
		if (a.getKeyFrames().length > 0) {
			animations.put(spriteType, dir, a);
		}
	}

	private CharacterAnimation createAnimation(final TextureAtlas atlas,
											   final SpriteType spriteType,
											   final String name,
											   final CharacterComponent.Direction dir) {
		return new CharacterAnimation(
				spriteType.getAnimationDuration(),
				atlas.findRegions(name),
				spriteType.getPlayMode(),
				dir
		);
	}

	public TextureAtlas getAtlas(final AtlasDefinition atlas) {
		return get(atlas.getFilePath(), TextureAtlas.class);
	}

	public Model getModel(final ModelDefinition model) {
		return get(model.getFilePath(), Model.class);
	}

	public Texture getTexture(final TextureDefinition definition) {
		return get(definition.getFilePath(), Texture.class);
	}

	public Music getMelody(final Assets.Melody definition) {
		return get(definition.getFilePath(), Music.class);
	}

	public Sound getSound(final Assets.Sounds sound) {
		return get(sound.getFilePath(), Sound.class);
	}
}
