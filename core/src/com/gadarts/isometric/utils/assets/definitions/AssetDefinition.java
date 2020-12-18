package com.gadarts.isometric.utils.assets.definitions;

public interface AssetDefinition {
	String getFilePath();

	Class<? extends Object> getTypeClass();
}
