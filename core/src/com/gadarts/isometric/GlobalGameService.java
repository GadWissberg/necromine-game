package com.gadarts.isometric;

public interface GlobalGameService {
	boolean isInGame( );

	void setInGame(boolean value);

	void startNewGame(String map);
}
