package com.gadarts.isometric.systems;

import com.gadarts.isometric.systems.bullets.BulletsSystemEventsSubscriber;
import com.gadarts.isometric.systems.bullets.BulletsSystemImpl;
import com.gadarts.isometric.systems.camera.CameraSystemEventsSubscriber;
import com.gadarts.isometric.systems.camera.CameraSystemImpl;
import com.gadarts.isometric.systems.character.CharacterSystemEventsSubscriber;
import com.gadarts.isometric.systems.character.CharacterSystemImpl;
import com.gadarts.isometric.systems.enemy.EnemySystemEventsSubscriber;
import com.gadarts.isometric.systems.enemy.EnemySystemImpl;
import com.gadarts.isometric.systems.hud.UserInterfaceSystemEventsSubscriber;
import com.gadarts.isometric.systems.hud.UserInterfaceSystemImpl;
import com.gadarts.isometric.systems.input.InputSystemEventsSubscriber;
import com.gadarts.isometric.systems.input.InputSystemImpl;
import com.gadarts.isometric.systems.particles.ParticleEffectsSystemEventsSubscriber;
import com.gadarts.isometric.systems.particles.ParticleEffectsSystemImpl;
import com.gadarts.isometric.systems.pickup.PickUpSystemImplUser;
import com.gadarts.isometric.systems.pickup.PickupSystemEventsSubscriber;
import com.gadarts.isometric.systems.player.PlayerSystemEventsSubscriber;
import com.gadarts.isometric.systems.player.PlayerSystemImpl;
import com.gadarts.isometric.systems.render.RenderSystemEventsSubscriber;
import com.gadarts.isometric.systems.render.RenderSystemImpl;
import com.gadarts.isometric.systems.turns.TurnsSystemEventsSubscriber;
import com.gadarts.isometric.systems.turns.TurnsSystemImpl;
import lombok.Getter;

@Getter
public enum Systems {
	CAMERA(new CameraSystemImpl(), CameraSystemEventsSubscriber.class),
	CHARACTER(new CharacterSystemImpl(), CharacterSystemEventsSubscriber.class),
	ENEMY(new EnemySystemImpl(), EnemySystemEventsSubscriber.class),
	USER_INTERFACE(new UserInterfaceSystemImpl(), UserInterfaceSystemEventsSubscriber.class),
	INPUT(new InputSystemImpl(), InputSystemEventsSubscriber.class),
	PICKUP(new PickUpSystemImplUser(), PickupSystemEventsSubscriber.class),
	PLAYER(new PlayerSystemImpl(), PlayerSystemEventsSubscriber.class),
	RENDER(new RenderSystemImpl(), RenderSystemEventsSubscriber.class),
	TURNS(new TurnsSystemImpl(), TurnsSystemEventsSubscriber.class),
	PICKUPS(new PickUpSystemImplUser(), PickupSystemEventsSubscriber.class),
	BULLETS(new BulletsSystemImpl(), BulletsSystemEventsSubscriber.class),
	PARTICLES(new ParticleEffectsSystemImpl(), ParticleEffectsSystemEventsSubscriber.class),
	PROFILER(new ProfilerSystemUser());

	private final GameSystem implementation;
	private final Class<? extends SystemEventsSubscriber> eventsSubscriberClass;

	Systems(final GameSystem implementation, final Class<? extends SystemEventsSubscriber> eventsSubscriberClass) {
		this.implementation = implementation;
		this.eventsSubscriberClass = eventsSubscriberClass;
	}

	Systems(final GameSystem system) {
		this(system, SystemEventsSubscriber.class);
	}
}
