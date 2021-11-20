package com.gadarts.isometric.systems.enemy;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.character.CharacterComponent;
import com.gadarts.isometric.components.character.data.CharacterHealthData;
import com.gadarts.isometric.components.decal.RelatedDecal;
import com.gadarts.isometric.components.decal.SimpleDecalComponent;
import com.gadarts.isometric.components.enemy.EnemyComponent;
import com.gadarts.isometric.components.enemy.EnemyStatus;
import com.gadarts.isometric.systems.GameEntitySystem;
import com.gadarts.isometric.systems.character.CharacterSystem;
import com.gadarts.isometric.systems.character.CharacterSystemEventsSubscriber;
import com.gadarts.isometric.systems.character.commands.CharacterCommand;
import com.gadarts.isometric.systems.character.commands.CharacterCommands;
import com.gadarts.isometric.systems.render.RenderSystemEventsSubscriber;
import com.gadarts.isometric.systems.turns.TurnsSystem;
import com.gadarts.isometric.systems.turns.TurnsSystemEventsSubscriber;
import com.gadarts.isometric.utils.SoundPlayer;
import com.gadarts.isometric.utils.map.MapGraph;
import com.gadarts.isometric.utils.map.MapGraphNode;
import com.gadarts.isometric.utils.map.MapGraphPath;
import com.gadarts.necromine.assets.Assets.UiTextures;
import com.gadarts.necromine.model.characters.SpriteType;
import com.gadarts.necromine.model.characters.attributes.Accuracy;
import com.gadarts.necromine.model.characters.attributes.Range;
import com.gadarts.necromine.model.characters.enemies.Enemies;

import java.util.ArrayList;
import java.util.List;

import static com.gadarts.isometric.components.enemy.EnemyStatus.ATTACKING;
import static com.gadarts.isometric.components.enemy.EnemyStatus.IDLE;
import static com.gadarts.necromine.model.characters.attributes.Accuracy.NONE;

/**
 * Handles enemy AI.
 */
public class EnemySystemImpl extends GameEntitySystem<EnemySystemEventsSubscriber> implements
		TurnsSystemEventsSubscriber,
		RenderSystemEventsSubscriber,
		CharacterSystemEventsSubscriber {

	public static final float SKILL_FLOWER_HEIGHT_RELATIVE = 1F;
	private static final Vector2 auxVector2_1 = new Vector2();
	private static final Vector2 auxVector2_2 = new Vector2();
	private static final MapGraphPath auxPath = new MapGraphPath();
	private static final CharacterCommand auxCommand = new CharacterCommand();
	private static final float MAX_SIGHT = 11;
	private static final float ENEMY_HALF_FOV_ANGLE = 75F;
	private static final int NUMBER_OF_SKILL_FLOWER_LEAF = 8;
	private static final Bresenham2 bresenham = new Bresenham2();
	private static final float RANGE_ATTACK_MIN_RADIUS = 1.7F;
	private static final List<MapGraphNode> auxNodesList = new ArrayList<>();
	private ImmutableArray<Entity> enemies;
	private CharacterSystem characterSystem;
	private TurnsSystem turnsSystem;
	private TextureRegion skillFlowerTexture;

	@Override
	public void update(final float deltaTime) {
		super.update(deltaTime);
		for (Entity enemy : enemies) {
			EnemyComponent enemyComponent = ComponentsMapper.enemy.get(enemy);
			if (enemyComponent.getStatus() != IDLE) {
				if (TimeUtils.timeSinceMillis(enemyComponent.getNextRoamSound()) >= 0) {
					if (enemyComponent.getNextRoamSound() != 0) {
						SoundPlayer soundPlayer = services.getSoundPlayer();
						soundPlayer.playSound(enemyComponent.getEnemyDefinition().getRoamSound());
					}
					enemyComponent.calculateNextRoamSound();
				}
			}
		}
	}


	@Override
	public void dispose( ) {

	}

	@Override
	public void addedToEngine(final Engine engine) {
		super.addedToEngine(engine);
		enemies = engine.getEntitiesFor(Family.all(EnemyComponent.class).get());
	}


	@Override
	public void onEnemyTurn(final long currentTurnId) {
		if (invokeTurnForUnplayedEnemy(currentTurnId)) return;
		enemyFinishedTurn();
	}

	private boolean invokeTurnForUnplayedEnemy(final long currentTurnId) {
		for (Entity enemy : enemies) {
			int hp = ComponentsMapper.character.get(enemy).getSkills().getHealthData().getHp();
			EnemyComponent enemyComponent = ComponentsMapper.enemy.get(enemy);
			if (enemyComponent.getTimeStamps().getLastTurn() < currentTurnId) {
				if (hp > 0 && enemyComponent.getStatus() != IDLE) {
					invokeEnemyTurn(enemy);
					return true;
				}
			}
		}
		return false;
	}

	private void invokeEnemyTurn(final Entity enemy) {
		EnemyComponent enemyComponent = ComponentsMapper.enemy.get(enemy);
		if (enemyComponent.getStatus() == ATTACKING) {
			invokeEnemyAttackBehaviour(enemy, enemyComponent);
		} else {
			Vector2 nodePosition = ComponentsMapper.characterDecal.get(enemy).getNodePosition(auxVector2_1);
			MapGraph map = services.getMapService().getMap();
			MapGraphNode enemyNode = map.getNode(nodePosition);
			MapGraphNode targetLastVisibleNode = enemyComponent.getTargetLastVisibleNode();
			if (targetLastVisibleNode != null) {
				if (enemyNode.equals(targetLastVisibleNode)) {
					auxNodesList.clear();
					int col = enemyNode.getCol();
					int row = enemyNode.getRow();
					int left = Math.max(col - 1, 0);
					int top = Math.max(row - 1, 0);
					int bottom = Math.min(row + 1, map.getDepth());
					int right = Math.min(col + 1, map.getWidth() - 1);
					addAsPossibleNodeToLookIn(enemyNode, map.getNode(left, top));
					addAsPossibleNodeToLookIn(enemyNode, map.getNode(col, top));
					addAsPossibleNodeToLookIn(enemyNode, map.getNode(right, top));
					addAsPossibleNodeToLookIn(enemyNode, map.getNode(left, row));
					addAsPossibleNodeToLookIn(enemyNode, map.getNode(right, row));
					addAsPossibleNodeToLookIn(enemyNode, map.getNode(left, bottom));
					addAsPossibleNodeToLookIn(enemyNode, map.getNode(col, bottom));
					addAsPossibleNodeToLookIn(enemyNode, map.getNode(right, bottom));
					if (!auxNodesList.isEmpty()) {
						enemyComponent.setTargetLastVisibleNode(auxNodesList.get(MathUtils.random(auxNodesList.size() - 1)));
					}
				}
				if (characterSystem.calculatePath(enemyNode, enemyComponent.getTargetLastVisibleNode(), auxPath, true)) {
					applyCommand(enemy, CharacterCommands.GO_TO_MELEE);
				}
			}
			enemyFinishedTurn();
		}
	}

	private void addAsPossibleNodeToLookIn(final MapGraphNode enemyNode, final MapGraphNode node) {
		if (characterSystem.calculatePath(enemyNode, node, auxPath, true)) {
			if (!auxNodesList.contains(node)) {
				auxNodesList.add(node);
			}
		}
	}

	private void invokeEnemyAttackBehaviour(final Entity enemy, final EnemyComponent enemyComponent) {
		Vector2 enemyPosition = ComponentsMapper.characterDecal.get(enemy).getNodePosition(auxVector2_1);
		Entity target = ComponentsMapper.character.get(enemy).getTarget();
		MapGraphNode enemyNode = services.getMapService().getMap().getNode(enemyPosition);
		Enemies enemyDefinition = enemyComponent.getEnemyDefinition();
		if (!considerPrimaryAttack(enemy, enemyComponent, enemyDefinition, enemyComponent.getSkill() - 1)) {
			applyGoToMelee(enemy, enemyNode, target);
		}
	}

	private boolean considerPrimaryAttack(final Entity enemy,
										  final EnemyComponent enemyCom,
										  final Enemies def,
										  final int skillIndex) {
		Accuracy[] accuracy = def.getAccuracy();
		if (accuracy != null && accuracy[skillIndex] != NONE && def.getRange().get(skillIndex) != Range.NONE) {
			float disToTarget = calculateDistanceToTarget(enemy);
			if (disToTarget <= def.getRange().get(skillIndex).getMaxDistance() && disToTarget > RANGE_ATTACK_MIN_RADIUS) {
				int turnsDiff = def.getReloadTime().get(skillIndex).getNumberOfTurns();
				if (checkIfPrimaryAttackIsReady(enemyCom, turnsDiff) && checkLineOfSightForEnemy(enemy, false)) {
					applyCommand(enemy, CharacterCommands.ATTACK_PRIMARY);
					return true;
				}
			}
		}
		return false;
	}

	private boolean checkIfPrimaryAttackIsReady(final EnemyComponent enemyComponent, final int turnsDiff) {
		return turnsSystem.getCurrentTurnId() - enemyComponent.getTimeStamps().getLastPrimaryAttack() > turnsDiff;
	}

	private void applyCommand(final Entity enemy, final CharacterCommands attackPrimary) {
		auxCommand.init(attackPrimary, auxPath, enemy);
		characterSystem.applyCommand(auxCommand, enemy);
	}

	private float calculateDistanceToTarget(final Entity enemy) {
		Entity target = ComponentsMapper.character.get(enemy).getTarget();
		Vector3 targetPosition = ComponentsMapper.characterDecal.get(target).getDecal().getPosition();
		Vector3 position = ComponentsMapper.characterDecal.get(enemy).getDecal().getPosition();
		return position.dst(targetPosition);
	}

	private void applyGoToMelee(final Entity enemy,
								final MapGraphNode enemyNode,
								final Entity target) {
		if (characterSystem.calculatePathToCharacter(enemyNode, target, auxPath, false)) {
			auxPath.nodes.removeIndex(auxPath.getCount() - 1);
			applyCommand(enemy, CharacterCommands.GO_TO_MELEE);
		} else {
			onCharacterCommandDone(enemy, null);
		}
	}


	@Override
	public void onTurnsSystemReady(final TurnsSystem turnsSystem) {
		this.turnsSystem = turnsSystem;
	}


	@Override
	public void onCharacterCommandDone(final Entity character, final CharacterCommand executedCommand) {
		if (ComponentsMapper.enemy.has(character)) {
			long currentTurnId = turnsSystem.getCurrentTurnId();
			EnemyComponent enemyComponent = ComponentsMapper.enemy.get(character);
			if (executedCommand != null && executedCommand.getType() == CharacterCommands.ATTACK_PRIMARY) {
				enemyComponent.getTimeStamps().setLastPrimaryAttack(currentTurnId);
			}
			enemyComponent.getTimeStamps().setLastTurn(currentTurnId);
			onEnemyTurn(currentTurnId);
		}
	}

	private void enemyFinishedTurn( ) {
		for (EnemySystemEventsSubscriber subscriber : subscribers) {
			subscriber.onEnemyFinishedTurn();
		}
	}


	@Override
	public void onCharacterSystemReady(final CharacterSystem characterSystem) {
		this.characterSystem = characterSystem;
	}

	@Override
	public void onCharacterGotDamage(final Entity entity) {
		if (ComponentsMapper.enemy.has(entity)) {
			awakeEnemy(entity);
			refreshSkillFlower(entity);
		}
	}

	private void refreshSkillFlower(final Entity entity) {
		List<RelatedDecal> relatedDecals = ComponentsMapper.simpleDecal.get(entity).getRelatedDecals();
		CharacterHealthData healthData = ComponentsMapper.character.get(entity).getSkills().getHealthData();
		float div = (((float) healthData.getHp()) / ((float) healthData.getInitialHp()));
		int numberOfVisibleLeaf = (int) (div * NUMBER_OF_SKILL_FLOWER_LEAF);
		for (int i = 0; i < relatedDecals.size(); i++) {
			relatedDecals.get(i).setVisible(i < numberOfVisibleLeaf);
		}
	}


	@Override
	public void onCharacterDies(final Entity character) {
		if (ComponentsMapper.enemy.has(character)) {
			EnemyComponent enemyComponent = ComponentsMapper.enemy.get(character);
			if (enemyComponent.getStatus() != IDLE) {
				enemyComponent.setStatus(IDLE);
			}
			character.remove(SimpleDecalComponent.class);
		}
	}

	@Override
	public void onFrameChanged(final Entity entity, final float deltaTime, final TextureAtlas.AtlasRegion newFrame) {
		SpriteType spriteType = ComponentsMapper.character.get(entity).getCharacterSpriteData().getSpriteType();
		if (ComponentsMapper.enemy.has(entity)) {
			if (spriteType == SpriteType.ATTACK) {
				onFrameChangedOfAttack(entity, newFrame);
			} else if (spriteType == SpriteType.RUN) {
				onFrameChangedOfRun(entity);
			}
		}
	}

	private void onFrameChangedOfRun(final Entity entity) {
		Vector3 position = ComponentsMapper.characterDecal.get(entity).getDecal().getPosition();
		SimpleDecalComponent simpleDecalComponent = ComponentsMapper.simpleDecal.get(entity);
		float height = ComponentsMapper.enemy.get(entity).getEnemyDefinition().getHeight();
		simpleDecalComponent.getDecal().setPosition(position.x, height + SKILL_FLOWER_HEIGHT_RELATIVE, position.z);
		List<RelatedDecal> relatedDecals = simpleDecalComponent.getRelatedDecals();
		for (RelatedDecal decal : relatedDecals) {
			if (decal.isVisible()) {
				decal.setPosition(position.x, height + SKILL_FLOWER_HEIGHT_RELATIVE, position.z);
			}
		}
	}

	private void onFrameChangedOfAttack(final Entity entity, final TextureAtlas.AtlasRegion newFrame) {
		if (newFrame.index == ComponentsMapper.character.get(entity).getCharacterSpriteData().getMeleeHitFrameIndex()) {
			SoundPlayer soundPlayer = services.getSoundPlayer();
			soundPlayer.playSound(ComponentsMapper.enemy.get(entity).getEnemyDefinition().getAttackSound());
		}
	}

	private boolean checkLineOfSightForEnemy(final Entity enemy, final boolean checkFov) {
		if (checkFov && !isTargetInFov(enemy)) return false;
		return !checkIfFloorNodesBlockSightToTarget(enemy);
	}

	private boolean checkIfFloorNodesBlockSightToTarget(final Entity enemy) {
		Vector2 pos = ComponentsMapper.characterDecal.get(enemy).getNodePosition(auxVector2_1);
		Entity target = ComponentsMapper.character.get(enemy).getTarget();
		Vector2 targetPos = ComponentsMapper.characterDecal.get(target).getNodePosition(auxVector2_2);
		Array<GridPoint2> nodes = bresenham.line((int) pos.x, (int) pos.y, (int) targetPos.x, (int) targetPos.y);
		MapGraph map = services.getMapService().getMap();
		for (GridPoint2 n : nodes) {
			if (map.getNode(n.x, n.y).getHeight() > map.getNode((int) pos.x, (int) pos.y).getHeight() + 1) {
				return true;
			}
		}
		return false;
	}

	private void awakeEnemy(final Entity enemy) {
		if (ComponentsMapper.character.get(enemy).getSkills().getHealthData().getHp() <= 0) return;
		ComponentsMapper.enemy.get(enemy).setStatus(ATTACKING);
		ComponentsMapper.simpleDecal.get(enemy).getDecal().setTextureRegion(skillFlowerTexture);
		for (EnemySystemEventsSubscriber subscriber : subscribers) {
			subscriber.onEnemyAwaken(enemy);
		}
	}

	private boolean isTargetInFov(final Entity enemy) {
		Vector3 enemyPos = ComponentsMapper.characterDecal.get(enemy).getDecal().getPosition();
		CharacterComponent charComponent = ComponentsMapper.character.get(enemy);
		Vector3 targetPosition = ComponentsMapper.characterDecal.get(charComponent.getTarget()).getDecal().getPosition();
		Vector2 directionToTarget = auxVector2_2.set(targetPosition.x, targetPosition.z).sub(enemyPos.x, enemyPos.z).nor();
		Vector2 enemyDirection = charComponent.getCharacterSpriteData().getFacingDirection().getDirection(auxVector2_1);
		float toDegrees = enemyDirection.angleDeg() - ENEMY_HALF_FOV_ANGLE;
		float fromDegrees = enemyDirection.angleDeg() + ENEMY_HALF_FOV_ANGLE;
		float dirToTarget = directionToTarget.angleDeg();
		float delta0 = (dirToTarget - fromDegrees + 360 + 180) % 360 - 180;
		float delta1 = (toDegrees - dirToTarget + 360 + 180) % 360 - 180;
		return delta0 + delta1 < 180;
	}

	@Override
	public void onCharacterNodeChanged(final Entity entity, final MapGraphNode oldNode, final MapGraphNode newNode) {
		if (ComponentsMapper.player.has(entity)) {
			for (Entity enemy : enemies) {
				EnemyComponent enemyComponent = ComponentsMapper.enemy.get(enemy);
				EnemyStatus status = enemyComponent.getStatus();
				if (status != ATTACKING) {
					if (isTargetInFov(enemy) && !checkIfFloorNodesBlockSightToTarget(enemy)) {
						Vector2 enemyPos = ComponentsMapper.characterDecal.get(enemy).getNodePosition(auxVector2_1);
						Entity target = ComponentsMapper.character.get(enemy).getTarget();
						Vector2 targetPos = ComponentsMapper.characterDecal.get(target).getNodePosition(auxVector2_2);
						if (enemyPos.dst2(targetPos) <= Math.pow(MAX_SIGHT, 2)) {
							awakeEnemy(enemy);
						}
					}
				} else {
					if (!isTargetInFov(enemy) || checkIfFloorNodesBlockSightToTarget(enemy)) {
						enemyComponent.setStatus(EnemyStatus.SEARCHING);
						updateEnemyTargetLastVisibleNode(enemy, enemyComponent);
					}
				}
			}
		}
	}

	private void updateEnemyTargetLastVisibleNode(final Entity enemy, final EnemyComponent enemyComponent) {
		Entity target = ComponentsMapper.character.get(enemy).getTarget();
		Vector2 nodePosition = ComponentsMapper.characterDecal.get(target).getNodePosition(auxVector2_1);
		MapGraphNode node = services.getMapService().getMap().getNode(nodePosition);
		enemyComponent.setTargetLastVisibleNode(node);
	}

	@Override
	public void activate( ) {
		skillFlowerTexture = new TextureRegion(services.getAssetManager().getTexture(UiTextures.SKILL_FLOWER_CENTER));
	}
}
