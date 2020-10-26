package com.gadarts.isometric.systems.pickup;

import com.gadarts.isometric.systems.SystemEventsSubscriber;

public interface PickupSystemEventsSubscriber extends SystemEventsSubscriber {
    void onPickUpSystemReady(PickUpSystem pickUpSystem);
}
