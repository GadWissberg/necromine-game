package com.gadarts.isometric.systems.hud;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.gadarts.isometric.IsometricGame;
import com.gadarts.isometric.components.*;
import com.gadarts.isometric.systems.GameEntitySystem;
import com.gadarts.isometric.systems.camera.CameraSystem;
import com.gadarts.isometric.systems.camera.CameraSystemEventsSubscriber;
import com.gadarts.isometric.systems.character.CharacterCommand;
import com.gadarts.isometric.systems.character.CharacterSystem;
import com.gadarts.isometric.systems.character.CharacterSystemEventsSubscriber;
import com.gadarts.isometric.systems.input.InputSystemEventsSubscriber;
import com.gadarts.isometric.systems.player.PlayerSystem;
import com.gadarts.isometric.systems.player.PlayerSystemEventsSubscriber;
import com.gadarts.isometric.systems.turns.Turns;
import com.gadarts.isometric.systems.turns.TurnsSystem;
import com.gadarts.isometric.systems.turns.TurnsSystemEventsSubscriber;
import com.gadarts.isometric.utils.assets.GameAssetsManager;
import com.gadarts.isometric.utils.map.MapGraph;
import com.gadarts.isometric.utils.map.MapGraphNode;
import com.gadarts.isometric.utils.map.MapGraphPath;

import java.util.List;

public class HudSystemImpl extends GameEntitySystem<HudSystemEventsSubscriber> implements
        HudSystem,
        InputSystemEventsSubscriber,
        PlayerSystemEventsSubscriber,
        CameraSystemEventsSubscriber,
        TurnsSystemEventsSubscriber,
        CharacterSystemEventsSubscriber {

    public static final Color CURSOR_REGULAR = Color.YELLOW;
    static final Color CURSOR_ATTACK = Color.RED;
    private static final Vector3 auxVector3_1 = new Vector3();
    private static final Vector3 auxVector3_2 = new Vector3();

    private final PathPlanHandler pathPlanHandler;
    private final AttackNodesHandler attackNodesHandler = new AttackNodesHandler();
    private ImmutableArray<Entity> enemiesEntities;
    private ImmutableArray<Entity> pickupEntities;
    private ModelInstance cursorModelInstance;
    private Stage stage;
    private Entity currentHighLightedPickup;
    private Entity itemToPickup;

    public HudSystemImpl(final MapGraph map, final GameAssetsManager assetManager) {
        super(map);
        pathPlanHandler = new PathPlanHandler(assetManager);
    }

    @Override
    public void dispose() {
        attackNodesHandler.dispose();
    }

    @Override
    public void addedToEngine(final Engine engine) {
        super.addedToEngine(engine);
        pathPlanHandler.init((PooledEngine) engine);
        stage = new Stage(new FitViewport(IsometricGame.RESOLUTION_WIDTH, IsometricGame.RESOLUTION_HEIGHT));
        Entity cursorEntity = engine.getEntitiesFor(Family.all(CursorComponent.class).get()).first();
        cursorModelInstance = ComponentsMapper.modelInstance.get(cursorEntity).getModelInstance();
        enemiesEntities = engine.getEntitiesFor(Family.all(EnemyComponent.class).get());
        pickupEntities = engine.getEntitiesFor(Family.all(PickUpComponent.class).get());
        attackNodesHandler.init(getEngine());
    }


    @Override
    public void mouseMoved(final int screenX, final int screenY) {
        MapGraph map = getMap();
        MapGraphNode newNode = map.getRayNode(screenX, screenY, getSystem(CameraSystem.class).getCamera());
        MapGraphNode oldNode = map.getNode(cursorModelInstance.transform.getTranslation(auxVector3_2));
        if (!newNode.equals(oldNode)) {
            cursorModelInstance.transform.setTranslation(newNode.getX(), 0, newNode.getY());
            colorizeCursor(newNode);
        }
        boolean foundPickup = highlightPickupUnderMouse(screenX, screenY);
        currentHighLightedPickup = foundPickup ? currentHighLightedPickup : null;
    }

    private boolean highlightPickupUnderMouse(final int screenX, final int screenY) {
        Ray ray = getSystem(CameraSystem.class).getCamera().getPickRay(screenX, screenY);
        boolean result = false;
        for (Entity pickup : pickupEntities) {
            result = handlePickupHighlight(ray, pickup);
            if (result) {
                currentHighLightedPickup = pickup;
                break;
            }
        }
        return result;
    }

    private boolean handlePickupHighlight(final Ray ray, final Entity pickup) {
        ModelInstance mic = ComponentsMapper.modelInstance.get(pickup).getModelInstance();
        Vector3 cen = mic.transform.getTranslation(auxVector3_1);
        ColorAttribute attr = (ColorAttribute) mic.materials.get(0).get(ColorAttribute.Emissive);
        if (Intersector.intersectRayBoundsFast(ray, cen, auxVector3_2.set(0.5f, 0.5f, 0.5f))) {
            attr.color.set(1, 1, 1, 1);
            return true;
        } else {
            attr.color.set(0, 0, 0, 0);
            return false;
        }
    }

    private void colorizeCursor(final MapGraphNode newNode) {
        if (getMap().getEnemyFromNode(enemiesEntities, newNode) != null) {
            setCursorColor(CURSOR_ATTACK);
        } else {
            setCursorColor(CURSOR_REGULAR);
        }
    }

    private void setCursorColor(final Color color) {
        Material material = cursorModelInstance.materials.get(0);
        ColorAttribute colorAttribute = (ColorAttribute) material.get(ColorAttribute.Diffuse);
        colorAttribute.color.set(color);
    }

    private MapGraphNode getCursorNode() {
        Vector3 dest = getCursorModelInstance().transform.getTranslation(auxVector3_1);
        return getMap().getNode((int) dest.x, (int) dest.z);
    }

    @Override
    public void touchDown(final int screenX, final int screenY, final int button) {
        if (getSystem(CameraSystem.class).isCameraRotating()) return;
        Entity player = getSystem(PlayerSystem.class).getPlayer();
        Turns currentTurn = getSystem(TurnsSystem.class).getCurrentTurn();
        if (currentTurn == Turns.PLAYER && ComponentsMapper.character.get(player).getHp() > 0) {
            if (button == Input.Buttons.LEFT && !getSystem(CharacterSystem.class).isProcessingCommand()) {
                applyPlayerTurn();
            }
        }
    }


    private void applyPlayerTurn() {
        MapGraphNode cursorNode = getCursorNode();
        MapGraphPath plannedPath = pathPlanHandler.getPlannedPath();
        if (plannedPath.getCount() > 0 && plannedPath.get(plannedPath.getCount() - 1).equals(cursorNode)) {
            applyPlayerCommand(cursorNode);
        } else {
            Entity enemyAtNode = getMap().getEnemyFromNode(enemiesEntities, cursorNode);
            if (calculatePathAccordingToSelection(cursorNode, enemyAtNode)) {
                pathHasCreated(cursorNode, enemyAtNode);
            }
        }
    }

    private void pathHasCreated(final MapGraphNode cursorNode, final Entity enemyAtNode) {
        if (enemyAtNode != null) {
            enemySelected(cursorNode, enemyAtNode);
        } else if (currentHighLightedPickup != null) {
            itemToPickup = currentHighLightedPickup;
        }
        pathPlanHandler.displayPathPlan();
    }

    private boolean calculatePathAccordingToSelection(final MapGraphNode cursorNode, final Entity enemyAtNode) {
        PlayerSystem system = getSystem(PlayerSystem.class);
        CharacterDecalComponent characterDecalComponent = ComponentsMapper.characterDecal.get(system.getPlayer());
        MapGraphNode playerNode = getMap().getNode(characterDecalComponent.getCellPosition(auxVector3_1));
        CharacterSystem characterSystem = getSystem(CharacterSystem.class);
        MapGraphPath plannedPath = pathPlanHandler.getPlannedPath();
        return (enemyAtNode != null && characterSystem.calculatePathToCharacter(playerNode, enemyAtNode, plannedPath))
                || currentHighLightedPickup != null && characterSystem.calculatePath(playerNode, cursorNode, plannedPath)
                || characterSystem.calculatePath(playerNode, cursorNode, plannedPath);
    }


    private void applyPlayerCommand(final MapGraphNode cursorNode) {
        pathPlanHandler.hideAllArrows();
        PlayerSystem playerSystem = getSystem(PlayerSystem.class);
        CharacterDecalComponent characterDecalComponent = ComponentsMapper.characterDecal.get(playerSystem.getPlayer());
        MapGraphNode playerNode = getMap().getNode(characterDecalComponent.getCellPosition(auxVector3_1));
        if (attackNodesHandler.getSelectedAttackNode() == null) {
            applyCommandWhenNoAttackNodeSelected();
        } else {
            applyPlayerAttackCommand(cursorNode, playerNode);
        }
    }

    private void applyPlayerAttackCommand(final MapGraphNode cursorNode, final MapGraphNode playerNode) {
        MapGraphNode attackNode = attackNodesHandler.getSelectedAttackNode();
        boolean result;
        result = isNodeInAvailableNodes(cursorNode, getMap().getAvailableNodesAroundNode(enemiesEntities, attackNode));
        result |= cursorNode.equals(attackNode) && playerNode.isConnectedNeighbour(attackNode);
        if (result) {
            getSystem(PlayerSystem.class).applyGoToMeleeCommand(pathPlanHandler.getPlannedPath());
        }
        attackNodesHandler.setSelectedAttackNode(null);
        getSystem(PlayerSystem.class).deactivateAttackMode();
    }

    private boolean isNodeInAvailableNodes(final MapGraphNode cursorNode, final List<MapGraphNode> availableNodes) {
        boolean result = false;
        for (MapGraphNode availableNode : availableNodes) {
            if (availableNode.equals(cursorNode)) {
                result = true;
                break;
            }
        }
        return result;
    }

    private void applyCommandWhenNoAttackNodeSelected() {
        MapGraphPath plannedPath = pathPlanHandler.getPlannedPath();
        if (itemToPickup != null) {
            getSystem(PlayerSystem.class).applyGoToPickupCommand(plannedPath);
        } else {
            getSystem(PlayerSystem.class).applyGoToCommand(plannedPath);
        }
    }

    private void enemySelected(final MapGraphNode node, final Entity enemyAtNode) {
        List<MapGraphNode> availableNodes = getMap().getAvailableNodesAroundNode(enemiesEntities, node);
        if (!availableNodes.isEmpty()) {
            attackNodesHandler.setSelectedAttackNode(node);
            getSystem(PlayerSystem.class).activateAttackMode(enemyAtNode, availableNodes);
        }
    }

    @Override
    public void update(final float deltaTime) {
        super.update(deltaTime);
        stage.act();
    }


    @Override
    public void touchUp(final int screenX, final int screenY, final int button) {

    }

    @Override
    public void touchDragged(final int screenX, final int screenY) {

    }

    @Override
    public void onPlayerFinishedTurn() {

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
    public void init() {
        for (HudSystemEventsSubscriber subscriber : subscribers) {
            subscriber.onHudSystemReady(this);
        }
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
    public void onEnemyTurn() {

    }

    @Override
    public void onPlayerTurn() {

    }

    @Override
    public void onTurnsSystemReady(final TurnsSystem turnsSystem) {
        super.addSystem(TurnsSystem.class, turnsSystem);
    }

    @Override
    public void onDestinationReached(final Entity character) {

    }

    @Override
    public void onCommandDone(final Entity character) {

    }

    @Override
    public void onNewCommandSet(final CharacterCommand command) {

    }

    @Override
    public void onCharacterSystemReady(final CharacterSystem characterSystem) {
        addSystem(CharacterSystem.class, characterSystem);
    }

    @Override
    public void onCharacterGotDamage(final Entity target) {

    }

}
