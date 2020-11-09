package com.gadarts.isometric.systems.hud;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.gadarts.isometric.utils.assets.Assets;
import com.gadarts.isometric.utils.assets.GameAssetsManager;

import java.util.Map;

public class GameWindow extends Window {

	final Button closeButton;

	public GameWindow(final String windowNameStorage,
					  final WindowStyle windowStyle,
					  final GameAssetsManager assetsManager,
					  final Map<String, Window> windows) {
		super(windowNameStorage, windowStyle);
		Button.ButtonStyle style = new Button.ButtonStyle();
		style.up = new TextureRegionDrawable(assetsManager.getTexture(Assets.UiTextures.BUTTON_CLOSE));
		style.down = new TextureRegionDrawable(assetsManager.getTexture(Assets.UiTextures.BUTTON_CLOSE_DOWN));
		style.over = new TextureRegionDrawable(assetsManager.getTexture(Assets.UiTextures.BUTTON_CLOSE_HOVER));
		closeButton = new Button(style);
		add(closeButton).colspan(2).expand().top().right().row();
		closeButton.addListener(new ClickListener() {
			@Override
			public void clicked(final InputEvent event, final float x, final float y) {
				if (closeButton.isDisabled()) return;
				super.clicked(event, x, y);
				GameWindow.this.remove();
				windows.remove(windowNameStorage);
			}
		});
	}

}
