package com.gadarts.isometric.systems.enemy;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.TimeUtils;
import com.gadarts.isometric.components.ComponentsMapper;
import com.gadarts.isometric.components.EnemyComponent;
import com.gadarts.isometric.systems.GameEntitySystem;
import com.gadarts.isometric.systems.character.CharacterCommand;
import com.gadarts.isometric.systems.character.CharacterSystem;
import com.gadarts.isometric.systems.character.CharacterSystemEventsSubscriber;
import com.gadarts.isometric.systems.character.Commands;
import com.gadarts.isometric.systems.turns.TurnsSystem;
import com.gadarts.isometric.systems.turns.TurnsSystemEventsSubscriber;
import com.gadarts.isometric.utils.assets.Assets;
import com.gadarts.isometric.utils.map.MapGraphNode;
import com.gadarts.isometric.utils.map.MapGraphPath;

/**
 * Handles enemy AI.
 */
public class EnemySystem extends GameEntitySystem<EnemySystemEventsSubscriber> implements
		TurnsSystemEventsSubscriber,
		CharacterSystemEventsSubscriber {

	private static final Vector3 auxVector3_1 = new Vector3();
	private static final MapGraphPath auxPath = new MapGraphPath();
	private static final CharacterCommand auxCommand = new CharacterCommand();

	private ImmutableArray<Entity> enemies;
	private CharacterSystem characterSystem;



	@Override
	public void update(final float deltaTime) {
		super.update(deltaTime);
		for (Entity enemy : enemies) {
			EnemyComponent enemyComponent = ComponentsMapper.enemy.get(enemy);
			if (TimeUtils.timeSinceMillis(enemyComponent.getNextRoamSound()) >= 0) {
				enemyComponent.calculateNextRoamSound();
				soundPlayer.playSound(Assets.Sounds.ENEMY_ROAM);
			}
		}
	}

	@Override
	public void dispose() {

	}

	@Override
	public void addedToEngine(final Engine engine) {
		super.addedToEngine(engine);
		enemies = engine.getEntitiesFor(Family.all(EnemyComponent.class).get());
	}


	@Override
	public void onEnemyTurn() {
		for (Entity enemy : enemies) {
			if (ComponentsMapper.character.get(enemy).getHp() > 0) {
				invokeEnemyTurn(enemy);
			} else {
				onCommandDone(enemy);
			}
		}
	}

	private void invokeEnemyTurn(final Entity enemy) {
		Vector3 enemyPosition = ComponentsMapper.characterDecal.get(enemy).getCellPosition(auxVector3_1);
		Entity target = ComponentsMapper.character.get(enemy).getTarget();
		MapGraphNode enemyNode = map.getNode((int) enemyPosition.x, (int) enemyPosition.z);
		applyGoToMelee(enemy, enemyNode, target);
	}

	private void applyGoToMelee(final Entity enemy,
								final MapGraphNode enemyNode,
								final Entity target) {
		if (characterSystem.calculatePathToCharacter(enemyNode, target, auxPath)) {
			auxPath.nodes.removeIndex(auxPath.getCount() - 1);
			auxCommand.init(Commands.GO_TO_MELEE, auxPath, enemy);
			characterSystem.applyCommand(auxCommand, enemy);
		}
	}

	@Override
	public void onPlayerTurn() {

	}

	@Override
	public void onTurnsSystemReady(final TurnsSystem turnsSystem) {

	}

	@Override
	public void onDestinationReached(final Entity character) {
	}

	@Override
	public void onCommandDone(final Entity character) {
		if (ComponentsMapper.enemy.has(character)) {
			for (EnemySystemEventsSubscriber subscriber : subscribers) {
				subscriber.onEnemyFinishedTurn(character);
			}
		}
	}

	@Override
	public void onNewCommandSet(final CharacterCommand command) {

	}

	@Override
	public void onCharacterSystemReady(final CharacterSystem characterSystem) {
		this.characterSystem = characterSystem;
	}

	@Override
	public void onCharacterGotDamage(final Entity target) {

	}

	@Override
	public void onItemPickedUp(final Entity itemPickedUp) {

	}

	@Override
	public void activate() {

	}
}
