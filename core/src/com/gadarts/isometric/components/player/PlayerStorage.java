package com.gadarts.isometric.components.player;

import lombok.Getter;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.IntStream;

public class PlayerStorage {

	public static final int WIDTH = 8;
	public static final int HEIGHT = 8;
	public static final int SIZE = WIDTH * HEIGHT;
	@Getter
	private final Set<Item> items = new LinkedHashSet<>();

	@Getter
	private final int[] storageMap = new int[SIZE];
	private final int[] storageMapSketch = new int[SIZE];

	public void clear() {
		items.clear();
		IntStream.range(0, storageMap.length).forEach(i -> storageMap[i] = 0);
	}

	public void addItem(final Item item) {
		initializeStorageArray(storageMap, storageMapSketch);
		int index = 0;
		while (index < SIZE) {
			if (tryToFillItemArea(index, item.getDefinition())) {
				applyItemAddition(item);
				break;
			} else {
				index++;
			}
		}
	}

	private void applyItemAddition(final Item item) {
		initializeStorageArray(storageMapSketch, storageMap);
		items.add(item);
	}

	private boolean tryToFillItemArea(final int index, final ItemDefinition definition) {
		for (int row = 0; row < definition.getHeight(); row++) {
			if (index % (WIDTH) + definition.getWidth() < WIDTH) {
				if (!tryToFillRow(index, definition, row)) {
					initializeStorageArray(storageMap, storageMapSketch);
					return false;
				}
			} else {
				return false;
			}
		}
		return true;
	}

	private boolean tryToFillRow(final int index, final ItemDefinition definition, final int row) {
		int leftMost = index % (WIDTH);
		int rightMost = leftMost + definition.getWidth();
		for (int col = leftMost; col < rightMost; col++) {
			if (!tryToFillCell(definition, row, col)) {
				initializeStorageArray(storageMap, storageMapSketch);
				return false;
			}
		}
		return true;
	}

	private boolean tryToFillCell(final ItemDefinition definition, final int row, final int col) {
		if (definition.getMask()[row * definition.getWidth() + col] == 1) {
			int currentCellInStorage = row * WIDTH + col;
			if (storageMap[currentCellInStorage] == 0) {
				storageMapSketch[currentCellInStorage] = 1;
				return true;
			} else {
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("ManualArrayCopy")
	private void initializeStorageArray(final int[] source, final int[] destination) {
		for (int i = 0; i < source.length; i++) {
			destination[i] = source[i];
		}
	}
}
