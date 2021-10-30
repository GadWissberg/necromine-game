package com.gadarts.isometric.systems.hud.console.commands.types;

import com.gadarts.isometric.systems.render.DrawFlags;

public interface DrawFlagSet {
	void run(DrawFlags drawFlags, boolean value);
}
