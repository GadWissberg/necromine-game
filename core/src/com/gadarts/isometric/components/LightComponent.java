package com.gadarts.isometric.components;

import com.badlogic.gdx.math.Vector3;
import lombok.AccessLevel;
import lombok.Getter;

@Getter
public class LightComponent implements GameComponent {
    @Getter(AccessLevel.NONE)
    private final Vector3 position = new Vector3();

    private float radius;
    private float intensity;

    public Vector3 getPosition(final Vector3 output) {
        return output.set(position);
    }

    @Override
    public void reset() {

    }


    public void init(final Vector3 position, final float radius, final float intensity) {
        this.position.set(position);
        this.radius = radius;
        this.intensity = intensity;
    }
}
