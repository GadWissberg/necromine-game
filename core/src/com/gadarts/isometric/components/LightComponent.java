package com.gadarts.isometric.components;

import com.badlogic.gdx.math.Vector3;
import lombok.Getter;

public class LightComponent implements GameComponent {
    private final Vector3 position = new Vector3();

    @Getter
    private float radius;

    public Vector3 getPosition(final Vector3 output) {
        return output.set(position);
    }

    @Override
    public void reset() {

    }

    public void init(final float x, final float y, final float z, final float radius) {
        this.position.set(x, y, z);
        this.radius = radius;
    }
}
