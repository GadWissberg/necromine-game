package com.gadarts.isometric.components.character.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CharacterRotationData {
    private boolean rotating;
    private long lastRotation;

    public void reset() {
        rotating = false;
        lastRotation = 0;
    }
}
