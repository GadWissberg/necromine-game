package com.gadarts.isometric.systems.hud;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.gadarts.isometric.GlobalApplicationService;
import com.gadarts.isometric.NecromineGame;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.CursorComponent;
import com.gadarts.isometric.components.enemy.EnemyComponent;
import com.gadarts.isometric.components.player.PlayerComponent;
import com.gadarts.isometric.services.GameServices;
import com.gadarts.isometric.systems.GameEntitySystem;
import com.gadarts.isometric.systems.camera.CameraSystem;
import com.gadarts.isometric.systems.camera.CameraSystemEventsSubscriber;
import com.gadarts.isometric.systems.character.CharacterSystem;
import com.gadarts.isometric.systems.character.CharacterSystemEventsSubscriber;
import com.gadarts.isometric.systems.hud.console.ConsoleCommandParameter;
import com.gadarts.isometric.systems.hud.console.ConsoleCommandResult;
import com.gadarts.isometric.systems.hud.console.ConsoleCommands;
import com.gadarts.isometric.systems.hud.console.ConsoleEventsSubscriber;
import com.gadarts.isometric.systems.hud.console.commands.ConsoleCommandsList;
import com.gadarts.isometric.systems.input.InputSystem;
import com.gadarts.isometric.systems.input.InputSystemEventsSubscriber;
import com.gadarts.isometric.systems.pickup.PickUpSystem;
import com.gadarts.isometric.systems.pickup.PickupSystemEventsSubscriber;
import com.gadarts.isometric.systems.player.PlayerSystem;
import com.gadarts.isometric.systems.player.PlayerSystemEventsSubscriber;
import com.gadarts.isometric.systems.render.DrawFlags;
import com.gadarts.isometric.systems.render.RenderSystem;
import com.gadarts.isometric.systems.render.RenderSystemEventsSubscriber;
import com.gadarts.isometric.systems.turns.Turns;
import com.gadarts.isometric.systems.turns.TurnsSystem;
import com.gadarts.isometric.systems.turns.TurnsSystemEventsSubscriber;
import com.gadarts.isometric.utils.DefaultGameSettings;
import com.gadarts.isometric.utils.map.MapGraphNode;
import com.gadarts.necromine.assets.Assets;
import com.gadarts.necromine.assets.GameAssetsManager;

import java.util.Arrays;
import java.util.List;

import static com.badlogic.gdx.Application.LOG_DEBUG;

public class HudSystemImpl extends GameEntitySystem<HudSystemEventsSubscriber> implements HudSystem,
		InputSystemEventsSubscriber,
		PlayerSystemEventsSubscriber,
		CameraSystemEventsSubscriber,
		RenderSystemEventsSubscriber,
		TurnsSystemEventsSubscriber,
		CharacterSystemEventsSubscriber,
		PickupSystemEventsSubscriber,
		ConsoleEventsSubscriber {

	public static final Color CURSOR_REGULAR = Color.YELLOW;
	public static final Color CURSOR_UNAVAILABLE = Color.DARK_GRAY;
	public static final Color CURSOR_ATTACK = Color.RED;
	public static final String MSG_BORDERS = "UI borders are %s.";
	private static final Vector3 auxVector3_1 = new Vector3();
	private static final Vector3 auxVector3_2 = new Vector3();
	private static final float BUTTON_PADDING = 40;
	private static final String BUTTON_NAME_STORAGE = "button_storage";
	private static final String TABLE_NAME_MENU = "menu";
	private static final String TABLE_NAME_HUD = "hud";
	private static final float CURSOR_FLICKER_STEP = 1.5f;

	private final AttackNodesHandler attackNodesHandler = new AttackNodesHandler();
	private ImmutableArray<Entity> enemiesEntities;
	private ModelInstance cursorModelInstance;
	private GameStage stage;
	private ToolTipHandler toolTipHandler;
	private boolean showBorders = DefaultGameSettings.DISPLAY_HUD_OUTLINES;
	private DrawFlags drawFlags;
	private Table menuTable;
	private Entity cursor;
	private float cursorFlickerChange = CURSOR_FLICKER_STEP;


	@Override
	public void dispose() {
		attackNodesHandler.dispose();
		toolTipHandler.dispose();
	}

	@Override
	public void init(final GameServices services) {
		super.init(services);
		Entity player = getEngine().getEntitiesFor(Family.all(PlayerComponent.class).get()).first();
		FitViewport fitViewport = new FitViewport(NecromineGame.RESOLUTION_WIDTH, NecromineGame.RESOLUTION_HEIGHT);
		stage = new GameStage(fitViewport, ComponentsMapper.player.get(player), services.getSoundPlayer());
		addHudTable();
		addMenuTable();
		cursor = getEngine().getEntitiesFor(Family.all(CursorComponent.class).get()).first();
	}

	private void addMenuTable() {
		menuTable = addTable();
		menuTable.setName(TABLE_NAME_MENU);
		menuTable.add(createLogo()).row();
		addMenuOptions(menuTable);
		menuTable.toFront();
		toggleMenu(DefaultGameSettings.MENU_ON_STARTUP);
	}

	public void toggleMenu(final boolean active) {
		menuTable.setVisible(active);
		stage.getRoot().findActor(TABLE_NAME_HUD).setTouchable(active ? Touchable.disabled : Touchable.enabled);
	}

	private void addMenuOptions(final Table menuTable) {
		BitmapFont smallFont = services.getAssetManager().get("chubgothic_40.ttf", BitmapFont.class);
		Label.LabelStyle style = new Label.LabelStyle(smallFont, MenuOption.FONT_COLOR_REGULAR);
		GlobalApplicationService global = services.getGlobalApplicationService();
		Arrays.stream(MenuOptions.values()).forEach(o -> menuTable.add(new MenuOption(o, style, global, this)).row());
	}

	private Label createLogo() {
		BitmapFont largeFont = services.getAssetManager().get("chubgothic_72.ttf", BitmapFont.class);
		Label.LabelStyle logoStyle = new Label.LabelStyle(largeFont, MenuOption.FONT_COLOR_REGULAR);
		return new Label(NecromineGame.TITLE, logoStyle);
	}

	@Override
	public void onRenderSystemReady(final RenderSystem renderSystem) {
		drawFlags = renderSystem.getDrawFlags();
		toolTipHandler = new ToolTipHandler(stage, drawFlags);
		toolTipHandler.addToolTipTable();
	}

	@Override
	public void addedToEngine(final Engine engine) {
		super.addedToEngine(engine);
		Entity cursorEntity = engine.getEntitiesFor(Family.all(CursorComponent.class).get()).first();
		cursorModelInstance = ComponentsMapper.modelInstance.get(cursorEntity).getModelInstance();
		enemiesEntities = engine.getEntitiesFor(Family.all(EnemyComponent.class).get());
		attackNodesHandler.init(getEngine());
	}

	private void addHudTable() {
		Table hudTable = addTable();
		hudTable.setName(TABLE_NAME_HUD);
		addStorageButton(hudTable);
	}

	private Table addTable() {
		Table table = new Table();
		stage.setDebugAll(Gdx.app.getLogLevel() == LOG_DEBUG && showBorders);
		table.setFillParent(true);
		stage.addActor(table);
		return table;
	}


	private void addStorageButton(final Table table) {
		Button.ButtonStyle buttonStyle = new Button.ButtonStyle();
		GameAssetsManager assetsManager = services.getAssetManager();
		buttonStyle.up = new TextureRegionDrawable(assetsManager.getTexture(Assets.UiTextures.BUTTON_STORAGE));
		buttonStyle.down = new TextureRegionDrawable(assetsManager.getTexture(Assets.UiTextures.BUTTON_STORAGE_DOWN));
		buttonStyle.over = new TextureRegionDrawable(assetsManager.getTexture(Assets.UiTextures.BUTTON_STORAGE_HOVER));
		Button button = new Button(buttonStyle);
		button.setName(BUTTON_NAME_STORAGE);
		button.addListener(new ClickListener() {
			@Override
			public void clicked(final InputEvent event, final float x, final float y) {
				super.clicked(event, x, y);
				stage.openStorageWindow(assetsManager);
				services.getSoundPlayer().playSound(Assets.Sounds.UI_CLICK);
			}
		});
		table.add(button).expand().left().bottom().pad(BUTTON_PADDING);
	}


	@Override
	public void mouseMoved(final int screenX, final int screenY) {
		if (menuTable.isVisible()) return;
		MapGraphNode newNode = services.getMap().getRayNode(screenX, screenY, getSystem(CameraSystem.class).getCamera());
		MapGraphNode oldNode = services.getMap().getNode(cursorModelInstance.transform.getTranslation(auxVector3_2));
		if (newNode != null && !newNode.equals(oldNode)) {
			mouseEnteredNewNode(newNode);
		}
	}

	private void mouseEnteredNewNode(final MapGraphNode newNode) {
		toolTipHandler.displayToolTip(null);
		toolTipHandler.setLastHighlightNodeChange(TimeUtils.millis());
		int col = newNode.getCol();
		int row = newNode.getRow();
		cursorModelInstance.transform.setTranslation(col + 0.5f, newNode.getHeight(), row + 0.5f);
		colorizeCursor(newNode);
	}

	private void colorizeCursor(final MapGraphNode newNode) {
		if (!drawFlags.isDrawFow() || services.getMap().getFowMap()[newNode.getRow()][newNode.getCol()] == 1) {
			if (services.getMap().getAliveEnemyFromNode(enemiesEntities, newNode) != null) {
				setCursorColor(CURSOR_ATTACK);
			} else {
				setCursorOpacity(1F);
				setCursorColor(CURSOR_REGULAR);
			}
		} else {
			setCursorOpacity(1F);
			setCursorColor(CURSOR_UNAVAILABLE);
		}
	}

	private void setCursorColor(final Color color) {
		Material material = cursorModelInstance.materials.get(0);
		ColorAttribute colorAttribute = (ColorAttribute) material.get(ColorAttribute.Diffuse);
		colorAttribute.color.set(color);
	}

	private MapGraphNode getCursorNode() {
		Vector3 dest = getCursorModelInstance().transform.getTranslation(auxVector3_1);
		return services.getMap().getNode((int) dest.x, (int) dest.z);
	}

	@Override
	public void touchDown(final int screenX, final int screenY, final int button) {
		if (getSystem(CameraSystem.class).isCameraRotating()) return;
		Entity player = getSystem(PlayerSystem.class).getPlayer();
		Turns currentTurn = getSystem(TurnsSystem.class).getCurrentTurn();
		if (currentTurn == Turns.PLAYER && ComponentsMapper.character.get(player).getHealthData().getHp() > 0) {
			if (button == Input.Buttons.LEFT && !getSystem(CharacterSystem.class).isProcessingCommand()) {
				userSelectedNodeToApplyTurn();
			}
		}
	}

	private void userSelectedNodeToApplyTurn() {
		MapGraphNode cursorNode = getCursorNode();
		if (!drawFlags.isDrawFow() || services.getMap().getFowMap()[cursorNode.getRow()][cursorNode.getCol()] == 1) {
			for (HudSystemEventsSubscriber sub : subscribers) {
				sub.onUserSelectedNodeToApplyTurn(cursorNode, attackNodesHandler);
			}
		}
	}

	@Override
	public void update(final float deltaTime) {
		super.update(deltaTime);
		stage.act();
		toolTipHandler.handleToolTip(services.getMap(), getCursorNode(), enemiesEntities);
		handleCursorFlicker(deltaTime);
	}

	private void handleCursorFlicker(final float deltaTime) {
		Material material = ComponentsMapper.modelInstance.get(cursor).getModelInstance().materials.get(0);
		if (((ColorAttribute) material.get(ColorAttribute.Diffuse)).color.equals(CURSOR_ATTACK)) {
			BlendingAttribute blend = (BlendingAttribute) material.get(BlendingAttribute.Type);
			if (blend.opacity > 0.9) {
				cursorFlickerChange = -CURSOR_FLICKER_STEP;
			} else if (blend.opacity < 0.1) {
				cursorFlickerChange = CURSOR_FLICKER_STEP;
			}
			setCursorOpacity(blend.opacity + cursorFlickerChange * deltaTime);
		}
	}

	private void setCursorOpacity(final float opacity) {
		Material material = cursorModelInstance.materials.get(0);
		BlendingAttribute blend = (BlendingAttribute) material.get(BlendingAttribute.Type);
		blend.opacity = opacity;
		material.set(blend);
	}


	@Override
	public void inputSystemReady(final InputSystem inputSystem) {
		inputSystem.addInputProcessor(stage);
	}

	@Override
	public void keyDown(final int keycode) {
		if (keycode == Input.Keys.ESCAPE) {
			toggleMenu(!isMenuOpen());
		}
	}


	@Override
	public void onPlayerSystemReady(final PlayerSystem playerSystem) {
		addSystem(PlayerSystem.class, playerSystem);
	}

	@Override
	public void onAttackModeActivated(final List<MapGraphNode> availableNodes) {
		attackNodesHandler.onAttackModeActivated(availableNodes);
	}


	@Override
	public void onAttackModeDeactivated() {
		attackNodesHandler.onAttackModeDeactivated();
	}


	@Override
	public void onCameraSystemReady(final CameraSystem cameraSystem) {
		addSystem(CameraSystem.class, cameraSystem);
	}

	@Override
	public Stage getStage() {
		return stage;
	}

	@Override
	public ModelInstance getCursorModelInstance() {
		return cursorModelInstance;
	}

	@Override
	public boolean hasOpenWindows() {
		return stage.hasOpenWindows();
	}

	@Override
	public boolean isMenuOpen() {
		return menuTable.isVisible();
	}


	@Override
	public void onEnemyTurn(final long currentTurnId) {
		Button button = getStage().getRoot().findActor(BUTTON_NAME_STORAGE);
		button.setTouchable(Touchable.disabled);
	}

	@Override
	public void onPlayerTurn(final long currentTurnId) {
		Button button = getStage().getRoot().findActor(BUTTON_NAME_STORAGE);
		button.setTouchable(Touchable.enabled);
	}

	@Override
	public void onTurnsSystemReady(final TurnsSystem turnsSystem) {
		super.addSystem(TurnsSystem.class, turnsSystem);
	}


	@Override
	public void onCharacterSystemReady(final CharacterSystem characterSystem) {
		addSystem(CharacterSystem.class, characterSystem);
	}


	@Override
	public void activate() {
		for (HudSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onHudSystemReady(this);
		}
	}

	@Override
	public void onPickUpSystemReady(final PickUpSystem pickUpSystem) {
		addSystem(PickUpSystem.class, pickUpSystem);
	}

	@Override
	public void onConsoleActivated() {

	}

	@Override
	public boolean onCommandRun(final ConsoleCommands command, final ConsoleCommandResult consoleCommandResult) {
		boolean result = false;
		if (command == ConsoleCommandsList.BORDERS) {
			showBorders = !showBorders;
			stage.setDebugAll(showBorders);
			String msg = showBorders ? String.format(MSG_BORDERS, "displayed") : String.format(MSG_BORDERS, "hidden");
			consoleCommandResult.setMessage(msg);
			result = true;
		}
		return result;
	}

	@Override
	public boolean onCommandRun(final ConsoleCommands command,
								final ConsoleCommandResult consoleCommandResult,
								final ConsoleCommandParameter parameter) {
		return false;
	}

	@Override
	public void onConsoleDeactivated() {

	}
}
