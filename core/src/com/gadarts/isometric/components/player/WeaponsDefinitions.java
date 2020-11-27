package com.gadarts.isometric.components.player;

import com.gadarts.isometric.utils.assets.Assets;
import lombok.Getter;

public enum WeaponsDefinitions implements ItemDefinition {
	AXE_PICK(4, 5, 1, Assets.UiTextures.WEAPON_AXE_PICK, Assets.Sounds.ATTACK_CLAW, new int[]{
			1, 1, 1, 1,
			0, 1, 0, 0,
			0, 1, 0, 0,
			0, 1, 0, 0,
			0, 1, 0, 0,
	}, true),
	HAMMER(2, 4, 1, Assets.UiTextures.WEAPON_HAMMER, Assets.Sounds.ATTACK_CLAW, new int[]{
			1, 1,
			1, 1,
			1, 1,
			1, 1,
	}, true),
	COLT(2, 2, 2, Assets.UiTextures.WEAPON_COLT, Assets.Sounds.ATTACK_CLAW, new int[]{
			1, 1,
			1, 0
	});

	private final int width;
	private final int height;
	private final Assets.UiTextures image;
	private final int[] mask;

	@Getter
	private final Assets.Sounds attackSound;

	@Getter
	private final boolean melee;

	@Getter
	private final int hitFrameIndex;

	WeaponsDefinitions(final int width,
					   final int height,
					   final int hitFrameIndex,
					   final Assets.UiTextures image,
					   final Assets.Sounds attackSound,
					   final int[] mask) {
		this(width, height, hitFrameIndex, image, attackSound, mask, false);
	}

	WeaponsDefinitions(final int width,
					   final int height,
					   final int hitFrameIndex,
					   final Assets.UiTextures image,
					   final Assets.Sounds attackSound,
					   final int[] mask,
					   final boolean melee) {
		this.width = width;
		this.height = height;
		this.hitFrameIndex = hitFrameIndex;
		this.image = image;
		this.attackSound = attackSound;
		this.mask = flipMatrixVertically(mask);
		this.melee = melee;
	}

	private int[] flipMatrixVertically(final int[] mask) {
		int[] flipped = new int[mask.length];
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				flipped[row * width + col] = mask[((height - 1 - row) * width) + col];
			}
		}
		return flipped;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int[] getMask() {
		return mask;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public int getId() {
		return ordinal() + 1;
	}

	@Override
	public Assets.UiTextures getImage() {
		return image;
	}
}
