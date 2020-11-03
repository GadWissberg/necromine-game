package com.gadarts.isometric.components.player;

import com.gadarts.isometric.components.GameComponent;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class PlayerComponent implements GameComponent {
	List<Item> storage = new ArrayList<>();

	@Override
	public void reset() {
		storage.clear();
	}

	public void init(final Item item) {
		storage.add(item);
	}
}
