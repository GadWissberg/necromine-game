package com.gadarts.isometric.components;

import com.gadarts.necromine.model.MapNodeData;
import lombok.Getter;

@Getter
public class WallComponent implements GameComponent {
	private MapNodeData parentNode;

	public void init(final MapNodeData parentNode) {
		this.parentNode = parentNode;
	}

	@Override
	public void reset() {

	}
}
