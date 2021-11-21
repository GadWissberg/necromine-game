package com.gadarts.isometric.services;

import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.utils.Disposable;
import com.gadarts.isometric.GlobalGameService;
import com.gadarts.isometric.components.character.CharacterAnimations;
import com.gadarts.isometric.components.decal.character.CharacterAnimation;
import com.gadarts.isometric.systems.hud.console.*;
import com.gadarts.isometric.systems.hud.console.commands.ConsoleCommandsList;
import com.gadarts.isometric.utils.SoundPlayer;
import com.gadarts.necromine.assets.Assets;
import com.gadarts.necromine.assets.GameAssetsManager;
import com.gadarts.necromine.model.characters.Direction;
import com.gadarts.necromine.model.characters.SpriteType;
import lombok.Getter;

import java.util.Arrays;

@Getter
public class GameServices implements ConsoleEventsSubscriber, Disposable, ServicesManager {
	public static final String BOUNDING_BOX_PREFIX = "box_";
	private static final String MSG_ENABLED = "%s enabled.";
	private static final String MSG_DISABLED = "%s disabled.";

	private final GlobalGameService globalGameService;
	private final ConsoleImpl consoleImpl;
	private final SoundPlayer soundPlayer;
	private final MapService mapService = new MapService();
	private PooledEngine engine;
	private GameAssetsManager assetManager;

	public GameServices(final GlobalGameService globalGameService, final String map) {
		this.globalGameService = globalGameService;
		createAndSetEngine();
		createAssetsManagerAndLoadAssets();
		mapService.createAndSetMap(map, assetManager, engine);
		consoleImpl = createConsole();
		soundPlayer = new SoundPlayer(assetManager);
	}

	private ConsoleImpl createConsole( ) {
		final ConsoleImpl consoleImpl;
		consoleImpl = new ConsoleImpl();
		consoleImpl.subscribeForEvents(this);
		consoleImpl.init(assetManager);
		return consoleImpl;
	}

	public void createAndSetEngine( ) {
		this.engine = new PooledEngine();
	}


	private void createAssetsManagerAndLoadAssets( ) {
		assetManager = new GameAssetsManager();
		assetManager.loadGameFiles();
		afterFilesAreLoaded();
	}

	private CharacterAnimations createCharacterAnimations(final Assets.Atlases zealot) {
		CharacterAnimations animations = new CharacterAnimations();
		TextureAtlas atlas = assetManager.getAtlas(zealot);
		Arrays.stream(SpriteType.values()).forEach(spriteType -> {
			if (spriteType.isSingleAnimation()) {
				inflateCharacterAnimation(animations, atlas, spriteType, Direction.SOUTH);
			} else {
				Direction[] directions = Direction.values();
				Arrays.stream(directions).forEach(dir -> inflateCharacterAnimation(animations, atlas, spriteType, dir));
			}
		});
		return animations;
	}

	private void inflateCharacterAnimation(final CharacterAnimations animations,
										   final TextureAtlas atlas,
										   final SpriteType spriteType,
										   final Direction dir) {
		String spriteTypeName = spriteType.name().toLowerCase();
		String name = (spriteType.isSingleAnimation()) ? spriteTypeName : spriteTypeName + "_" + dir.name().toLowerCase();
		CharacterAnimation a = createAnimation(atlas, spriteType, name, dir);
		if (a.getKeyFrames().length > 0) {
			animations.put(spriteType, dir, a);
		}
	}

	private CharacterAnimation createAnimation(final TextureAtlas atlas,
											   final SpriteType spriteType,
											   final String name,
											   final Direction dir) {
		return new CharacterAnimation(
				spriteType.getAnimationDuration(),
				atlas.findRegions(name),
				spriteType.getPlayMode(),
				dir);
	}

	private void afterFilesAreLoaded( ) {
		generateCharactersAnimations();
		generateModelsBoundingBoxes();
		applyAlphaOnModels();
		assetManager.applyRepeatWrapOnAllTextures();
	}

	private void generateModelsBoundingBoxes( ) {
		Arrays.stream(Assets.Models.values())
				.forEach(def -> assetManager.addAsset(
						BOUNDING_BOX_PREFIX + def.getFilePath(),
						ModelBoundingBox.class,
						(ModelBoundingBox) assetManager.get(def.getFilePath(), Model.class).calculateBoundingBox(new ModelBoundingBox(def))));
	}

	private void applyAlphaOnModels( ) {
		Arrays.stream(Assets.Models.values()).filter(def -> def.getAlpha() < 1.0f)
				.forEach(def -> {
					Material material = assetManager.getModel(def).materials.get(0);
					BlendingAttribute attribute = new BlendingAttribute();
					material.set(attribute);
					attribute.opacity = def.getAlpha();
				});
	}

	private void generateCharactersAnimations( ) {
		Arrays.stream(Assets.Atlases.values())
				.forEach(atlas -> assetManager.addAsset(
						atlas.name(),
						CharacterAnimations.class,
						createCharacterAnimations(atlas)));
	}

	public void init( ) {
		soundPlayer.playMusic(Assets.Melody.TEST);
		soundPlayer.playSound(Assets.Sounds.AMB_WIND, 0.5F);
	}

	@Override
	public void onConsoleActivated( ) {

	}

	@Override
	public boolean onCommandRun(final ConsoleCommands command, final ConsoleCommandResult consoleCommandResult) {
		if (command == ConsoleCommandsList.SFX) {
			applySfxCommand(consoleCommandResult);
			return true;
		} else if (command == ConsoleCommandsList.MELODY) {
			applyMusicCommand(consoleCommandResult);
			return true;
		}
		return false;
	}

	private void applyMusicCommand(final ConsoleCommandResult consoleCommandResult) {
		soundPlayer.setMusicEnabled(!soundPlayer.isMusicEnabled());
		logAudioMessage(consoleCommandResult, "Melodies", soundPlayer.isMusicEnabled());
	}

	private void applySfxCommand(final ConsoleCommandResult consoleCommandResult) {
		soundPlayer.setSfxEnabled(!soundPlayer.isSfxEnabled());
		logAudioMessage(consoleCommandResult, "Sound effects", soundPlayer.isSfxEnabled());
	}

	private void logAudioMessage(final ConsoleCommandResult consoleCommandResult,
								 final String label,
								 final boolean sfxEnabled) {
		String msg = sfxEnabled ? String.format(MSG_ENABLED, label) : String.format(MSG_DISABLED, label);
		consoleCommandResult.setMessage(msg);
	}

	@Override
	public boolean onCommandRun(final ConsoleCommands command,
								final ConsoleCommandResult consoleCommandResult,
								final ConsoleCommandParameter parameter) {
		return false;
	}

	@Override
	public void onConsoleDeactivated( ) {

	}

	@Override
	public void dispose( ) {
		assetManager.dispose();
		mapService.dispose();
	}
}
