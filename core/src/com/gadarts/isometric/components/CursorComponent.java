package com.gadarts.isometric.components;

import lombok.Getter;
import lombok.Setter;

@Getter
public class CursorComponent implements GameComponent {

	@Setter
	private boolean disabled;

	@Override
	public void reset() {

	}
}
