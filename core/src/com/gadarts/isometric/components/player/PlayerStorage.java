package com.gadarts.isometric.components.player;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class PlayerStorage {

	public static final int WIDTH = 8;
	public static final int HEIGHT = 8;
	public static final int SIZE = WIDTH * HEIGHT;
	@Getter
	private final List<Item> items = new ArrayList<>();

	@Getter
	private final int[] storageMap = new int[SIZE];

	public void clear() {
		items.clear();
		IntStream.range(0, storageMap.length).forEach(i -> storageMap[i] = 0);
	}

}
