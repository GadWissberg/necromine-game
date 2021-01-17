package com.gadarts.isometric.systems.player;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.gadarts.isometric.components.*;
import com.gadarts.isometric.components.character.CharacterAnimations;
import com.gadarts.isometric.components.character.CharacterComponent;
import com.gadarts.isometric.components.character.SpriteType;
import com.gadarts.isometric.components.player.*;
import com.gadarts.isometric.systems.GameEntitySystem;
import com.gadarts.isometric.systems.camera.CameraSystemEventsSubscriber;
import com.gadarts.isometric.systems.character.CharacterSystem;
import com.gadarts.isometric.systems.character.CharacterSystemEventsSubscriber;
import com.gadarts.isometric.systems.character.commands.CharacterCommand;
import com.gadarts.isometric.systems.character.commands.CharacterCommands;
import com.gadarts.isometric.systems.enemy.EnemySystemEventsSubscriber;
import com.gadarts.isometric.systems.hud.AttackNodesHandler;
import com.gadarts.isometric.systems.hud.HudSystem;
import com.gadarts.isometric.systems.hud.HudSystemEventsSubscriber;
import com.gadarts.isometric.systems.input.InputSystem;
import com.gadarts.isometric.systems.input.InputSystemEventsSubscriber;
import com.gadarts.isometric.systems.pickup.PickUpSystem;
import com.gadarts.isometric.systems.pickup.PickupSystemEventsSubscriber;
import com.gadarts.isometric.systems.render.RenderSystem;
import com.gadarts.isometric.systems.render.RenderSystemEventsSubscriber;
import com.gadarts.isometric.systems.turns.TurnsSystem;
import com.gadarts.isometric.systems.turns.TurnsSystemEventsSubscriber;
import com.gadarts.isometric.utils.map.MapGraphNode;
import com.gadarts.isometric.utils.map.MapGraphPath;
import com.gadarts.necromine.Assets;
import com.gadarts.necromine.WeaponsDefinitions;

import java.util.List;

public class PlayerSystemImpl extends GameEntitySystem<PlayerSystemEventsSubscriber> implements
		PlayerSystem,
		PickupSystemEventsSubscriber,
		CameraSystemEventsSubscriber,
		TurnsSystemEventsSubscriber,
		HudSystemEventsSubscriber,
		InputSystemEventsSubscriber,
		CharacterSystemEventsSubscriber,
		RenderSystemEventsSubscriber,
		PlayerStorageEventsSubscriber,
		EnemySystemEventsSubscriber {

	private static final CharacterCommand auxCommand = new CharacterCommand();
	private static final Vector3 auxVector3 = new Vector3();
	private static final Vector2 auxVector2_1 = new Vector2();
	private static final Vector2 auxVector2_2 = new Vector2();
	private static final int PLAYER_VISION_RAD = 3;
	private static final Rectangle auxRect = new Rectangle();
	private static final int ENEMY_AWAKEN_RADIUS = 2;

	private Entity player;
	private ImmutableArray<Entity> enemies;
	private PathPlanHandler pathPlanHandler;
	private ImmutableArray<Entity> walls;

	private void applyPlayerTurn(final MapGraphNode cursorNode, final AttackNodesHandler attackNodesHandler) {
		int pathSize = pathPlanHandler.getPath().getCount();
		if (!pathPlanHandler.getPath().nodes.isEmpty() && pathPlanHandler.getPath().get(pathSize - 1).equals(cursorNode)) {
			applyPlayerCommandAccordingToPlan(cursorNode, attackNodesHandler);
		} else {
			planPath(cursorNode, attackNodesHandler);
		}
	}

	private void planPath(final MapGraphNode cursorNode, final AttackNodesHandler attackNodesHandler) {
		Entity enemyAtNode = services.getMap().getAliveEnemyFromNode(enemies, cursorNode);
		if (calculatePathAccordingToSelection(cursorNode, enemyAtNode)) {
			MapGraphNode selectedAttackNode = attackNodesHandler.getSelectedAttackNode();
			if (getSystem(PickUpSystem.class).getCurrentHighLightedPickup() != null || (selectedAttackNode != null && !isNodeInAvailableNodes(cursorNode, services.getMap().getAvailableNodesAroundNode(enemies, selectedAttackNode)))) {
				attackNodesHandler.reset();
			}
			pathHasCreated(cursorNode, enemyAtNode, attackNodesHandler);
		}
	}

	private void pathHasCreated(final MapGraphNode cursorNode, final Entity enemyAtNode, final AttackNodesHandler attackNodesHandler) {
		if (enemyAtNode != null) {
			enemySelected(cursorNode, enemyAtNode, attackNodesHandler);
		}
		for (PlayerSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onPathCreated(enemyAtNode != null);
		}
		pathPlanHandler.displayPathPlan();
	}

	private boolean calculatePathAccordingToSelection(final MapGraphNode cursorNode, final Entity enemyAtNode) {
		CharacterDecalComponent characterDecalComponent = ComponentsMapper.characterDecal.get(player);
		Vector2 cellPosition = characterDecalComponent.getNodePosition(auxVector2_1);
		MapGraphNode playerNode = services.getMap().getNode(cellPosition);
		CharacterSystem characterSystem = getSystem(CharacterSystem.class);
		MapGraphPath plannedPath = pathPlanHandler.getPath();
		return ((enemyAtNode != null && ComponentsMapper.character.get(enemyAtNode).getHealthData().getHp() > 0 && characterSystem.calculatePathToCharacter(playerNode, enemyAtNode, plannedPath))
				|| getSystem(PickUpSystem.class).getCurrentHighLightedPickup() != null && characterSystem.calculatePath(playerNode, cursorNode, plannedPath)
				|| characterSystem.calculatePath(playerNode, cursorNode, plannedPath));
	}


	private void applyPlayerCommandAccordingToPlan(final MapGraphNode cursorNode, final AttackNodesHandler attackNodesHandler) {
		pathPlanHandler.hideAllArrows();
		CharacterDecalComponent charDecalComp = ComponentsMapper.characterDecal.get(player);
		Vector2 cellPosition = charDecalComp.getNodePosition(auxVector2_1);
		MapGraphNode playerNode = services.getMap().getNode(cellPosition);
		if (attackNodesHandler.getSelectedAttackNode() == null) {
			applyCommandWhenNoAttackNodeSelected();
		} else {
			applyPlayerAttackCommand(cursorNode, playerNode, attackNodesHandler);
		}
	}

	private void applyPlayerAttackCommand(final MapGraphNode targetNode, final MapGraphNode playerNode, final AttackNodesHandler attackNodesHandler) {
		MapGraphNode attackNode = attackNodesHandler.getSelectedAttackNode();
		boolean result = targetNode.equals(attackNode);
		result |= isNodeInAvailableNodes(targetNode, services.getMap().getAvailableNodesAroundNode(enemies, attackNode));
		result |= targetNode.equals(attackNode) && playerNode.isConnectedNeighbour(attackNode);
		if (result) {
			if (services.getMap().getAliveEnemyFromNode(enemies, targetNode) != null) {
				Array<MapGraphNode> nodes = pathPlanHandler.getPath().nodes;
				nodes.removeIndex(nodes.size - 1);
			}
			applyGoToMeleeCommand(pathPlanHandler.getPath());
		}
		attackNodesHandler.setSelectedAttackNode(null);
		deactivateAttackMode();
	}

	private boolean isNodeInAvailableNodes(final MapGraphNode node, final List<MapGraphNode> availableNodes) {
		boolean result = false;
		for (MapGraphNode availableNode : availableNodes) {
			if (availableNode.equals(node)) {
				result = true;
				break;
			}
		}
		return result;
	}

	private void applyCommandWhenNoAttackNodeSelected() {
		MapGraphPath plannedPath = pathPlanHandler.getPath();
		Entity itemToPickup = getSystem(PickUpSystem.class).getItemToPickup();
		if (itemToPickup != null) {
			applyGoToPickupCommand(plannedPath, itemToPickup);
		} else {
			applyGoToCommand(plannedPath);
		}
	}

	private void enemySelected(final MapGraphNode node, final Entity enemyAtNode, final AttackNodesHandler attackNodesHandler) {
		PlayerStorage storage = ComponentsMapper.player.get(player).getStorage();
		Weapon selectedWeapon = storage.getSelectedWeapon();
		if (selectedWeapon.isMelee()) {
			List<MapGraphNode> availableNodes = services.getMap().getAvailableNodesAroundNode(enemies, node);
			attackNodesHandler.setSelectedAttackNode(node);
			activateAttackMode(enemyAtNode, availableNodes);
		} else {
			pathPlanHandler.resetPlan();
			enemySelectedWithRangeWeapon(node);
		}
	}

	private void enemySelectedWithRangeWeapon(final MapGraphNode node) {
		CharacterComponent characterComponent = ComponentsMapper.character.get(player);
		Weapon selectedWeapon = ComponentsMapper.player.get(player).getStorage().getSelectedWeapon();
		WeaponsDefinitions definition = (WeaponsDefinitions) selectedWeapon.getDefinition();
		characterComponent.getCharacterSpriteData().setHitFrameIndex(definition.getHitFrameIndex());
		characterComponent.setTarget(services.getMap().getAliveEnemyFromNode(enemies, node));
		applyShootCommand(node);
		for (PlayerSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onEnemySelectedWithRangeWeapon(node);
		}
	}

	@Override
	public void dispose() {

	}

	@Override
	public void addedToEngine(final Engine engine) {
		super.addedToEngine(engine);
		player = getEngine().getEntitiesFor(Family.all(PlayerComponent.class).get()).first();
		ComponentsMapper.player.get(player).getStorage().subscribeForEvents(this);
		enemies = engine.getEntitiesFor(Family.all(EnemyComponent.class).get());
		walls = engine.getEntitiesFor(Family.all(WallComponent.class).get());
	}

	@Override
	public void mouseMoved(final int screenX, final int screenY) {

	}

	@Override
	public void touchDown(final int screenX, final int screenY, final int button) {
	}


	@Override
	public void touchUp(final int screenX, final int screenY, final int button) {

	}

	@Override
	public void touchDragged(final int screenX, final int screenY) {

	}

	@Override
	public void inputSystemReady(final InputSystem inputSystem) {

	}

	@Override
	public void onDestinationReached(final Entity character) {

	}

	@Override
	public void onCharacterCommandDone(final Entity character) {
		if (ComponentsMapper.player.has(character)) {
			for (PlayerSystemEventsSubscriber subscriber : subscribers) {
				subscriber.onPlayerFinishedTurn();
			}
		}
	}

	@Override
	public void onNewCharacterCommandSet(final CharacterCommand command) {

	}

	@Override
	public void onCharacterSystemReady(final CharacterSystem characterSystem) {
		addSystem(CharacterSystem.class, characterSystem);
	}

	@Override
	public void onCharacterGotDamage(final Entity target) {

	}

	@Override
	public void onItemPickedUp(final Entity itemPickedUp) {
		PlayerStorage storage = ComponentsMapper.player.get(player).getStorage();
		storage.addItem(ComponentsMapper.pickup.get(itemPickedUp).getItem());
		services.getSoundPlayer().playSound(Assets.Sounds.PICKUP);
	}

	@Override
	public void onCharacterDies(final Entity character) {

	}

	@Override
	public void onCharacterNodeChanged(final Entity entity, final MapGraphNode oldNode, final MapGraphNode newNode) {
		if (ComponentsMapper.player.has(entity)) {
			revealRadius(PLAYER_VISION_RAD, ComponentsMapper.characterDecal.get(player).getNodePosition(auxVector2_1));
		}
	}

	@Override
	public void onHudSystemReady(final HudSystem hudSystem) {

	}


	@Override
	public void onUserSelectedNodeToApplyTurn(final MapGraphNode selectedNode, final AttackNodesHandler attackNodesHandler) {
		applyPlayerTurn(selectedNode, attackNodesHandler);
	}

	@Override
	public void onEnemyTurn(final long currentTurnId) {

	}

	@Override
	public void onPlayerTurn(final long currentTurnId) {

	}

	@Override
	public void onTurnsSystemReady(final TurnsSystem turnsSystem) {
	}

	@Override
	public Entity getPlayer() {
		return player;
	}

	@Override
	public void activateAttackMode(final Entity enemyAtNode, final List<MapGraphNode> availableNodes) {
		ComponentsMapper.character.get(player).setTarget(enemyAtNode);
		for (PlayerSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onAttackModeActivated(availableNodes);
		}
	}

	@Override
	public void applyGoToCommand(final MapGraphPath path) {
		MapGraphNode playerNode = services.getMap().getNode(ComponentsMapper.characterDecal.get(player).getDecal().getPosition());
		if (path.getCount() > 0 && !playerNode.equals(path.get(path.getCount() - 1))) {
			getSystem(CharacterSystem.class).applyCommand(auxCommand.init(CharacterCommands.GO_TO, path, player), player);
		}
	}

	@Override
	public void deactivateAttackMode() {
		for (PlayerSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onAttackModeDeactivated();
		}
	}

	@Override
	public void applyGoToMeleeCommand(final MapGraphPath path) {
		getSystem(CharacterSystem.class).applyCommand(auxCommand.init(CharacterCommands.GO_TO_MELEE, path, player), player);
	}

	public void applyShootCommand(final MapGraphNode target) {
		getSystem(CharacterSystem.class).applyCommand(auxCommand.init(CharacterCommands.SHOOT, null, player, target), player);
	}

	@Override
	public void applyGoToPickupCommand(final MapGraphPath path, final Entity itemToPickup) {
		getSystem(CharacterSystem.class).applyCommand(auxCommand.init(CharacterCommands.GO_TO_PICKUP, path, player, itemToPickup), player);
	}

	@Override
	public void activate() {
		pathPlanHandler = new PathPlanHandler(services.getAssetManager());
		pathPlanHandler.init((PooledEngine) getEngine());
		revealRadius(PLAYER_VISION_RAD, ComponentsMapper.characterDecal.get(player).getNodePosition(auxVector2_1));
		for (PlayerSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onPlayerSystemReady(this);
		}
	}

	private void revealRadius(final int radius, final Vector2 srcPosition) {
//		if (DefaultGameSettings.DISABLE_FOW) return;
		srcPosition.add(0.5f, 0.5f);
		for (int row = (int) (srcPosition.y - radius); row < srcPosition.y + radius; row++) {
			for (int col = (int) (srcPosition.x - radius); col < srcPosition.x + radius; col++) {
				tryToRevealNode(srcPosition, row, col, radius);
			}
		}
	}

	private void tryToRevealNode(final Vector2 srcNodePosition, final int row, final int col, final int radius) {
		int[][] fow = services.getMap().getFowMap();
		int currentCellRow = Math.min(Math.max(row, 0), fow.length);
		int currentCellCol = Math.min(Math.max(col, 0), services.getMap().getFowMap()[0].length);
		Vector2 nodeToReveal = auxVector2_2.set(currentCellCol + 0.5f, currentCellRow + 0.5f);
		if (srcNodePosition.dst(nodeToReveal) <= radius) {
			if (!isWallBlocksReveal(srcNodePosition, nodeToReveal)) {
				fow[currentCellRow][currentCellCol] = 1;
			}
		}
	}

	private boolean isWallBlocksReveal(final Vector2 srcNodePosition, final Vector2 nodeToReveal) {
		for (Entity wall : walls) {
			WallComponent wallComp = ComponentsMapper.wall.get(wall);
			int width = Math.abs(wallComp.getBottomRightX() - wallComp.getTopLeftX()) + 1;
			int height = Math.abs(wallComp.getBottomRightY() - wallComp.getTopLeftY()) + 1;
			Rectangle wallRect = auxRect.set(wallComp.getTopLeftX(), wallComp.getTopLeftY(), width, height);
			if (Intersector.intersectSegmentRectangle(srcNodePosition, nodeToReveal, wallRect)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void itemAddedToStorage(final Item item) {

	}

	@Override
	public void onSelectedWeaponChanged(final Weapon selectedWeapon) {
		WeaponsDefinitions definition = (WeaponsDefinitions) selectedWeapon.getDefinition();
		CharacterDecalComponent cdc = ComponentsMapper.characterDecal.get(player);
		CharacterAnimations animations = services.getAssetManager().get(Assets.Atlases.findByRelatedWeapon(definition).name());
		CharacterComponent.Direction direction = cdc.getDirection();
		cdc.init(animations, cdc.getSpriteType(), direction, auxVector3.set(cdc.getDecal().getPosition()));
		CharacterAnimation animation = animations.get(cdc.getSpriteType(), direction);
		ComponentsMapper.animation.get(player).init(cdc.getSpriteType().getAnimationDuration(), animation);
	}

	@Override
	public void onFrameChanged(final Entity entity, final float deltaTime, final TextureAtlas.AtlasRegion newFrame) {
		if (ComponentsMapper.player.has(entity)) {
			if (ComponentsMapper.character.get(entity).getCharacterSpriteData().getSpriteType() == SpriteType.ATTACK) {
				if (newFrame.index == ComponentsMapper.character.get(entity).getCharacterSpriteData().getHitFrameIndex()) {
					PlayerStorage storage = ComponentsMapper.player.get(entity).getStorage();
					WeaponsDefinitions definition = (WeaponsDefinitions) storage.getSelectedWeapon().getDefinition();
					services.getSoundPlayer().playSound(definition.getAttackSound());
				}
			}
		}
	}

	@Override
	public void onRenderSystemReady(final RenderSystem renderSystem) {

	}

	@Override
	public void onPickUpSystemReady(final PickUpSystem pickUpSystem) {
		addSystem(PickUpSystem.class, pickUpSystem);
	}

	@Override
	public void onEnemyFinishedTurn() {

	}

	@Override
	public void onEnemyAwaken(final Entity enemy) {
		revealRadius(ENEMY_AWAKEN_RADIUS, ComponentsMapper.characterDecal.get(enemy).getNodePosition(auxVector2_1));
	}
}
