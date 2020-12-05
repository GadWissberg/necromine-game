package com.gadarts.isometric.systems.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalMaterial;
import com.badlogic.gdx.graphics.g3d.decals.GroupStrategy;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;

import java.util.Comparator;

public class GameCameraGroupStrategy implements GroupStrategy, Disposable {
	private static final int GROUP_OPAQUE = 0;
	private static final int GROUP_BLEND = 1;
	private final Comparator<Decal> cameraSorter;
	Array<Array<Decal>> usedArrays = new Array<>();

	ObjectMap<DecalMaterial, Array<Decal>> materialGroups = new ObjectMap<>();
	Pool<Array<Decal>> arrayPool = new Pool<>(16) {
		@Override
		protected Array<Decal> newObject() {
			return new Array<>();
		}
	};
	Camera camera;
	ShaderProgram shader;

	public GameCameraGroupStrategy(final Camera camera) {
		this.camera = camera;
		this.cameraSorter = (o1, o2) -> {
			float dist1 = camera.position.dst(o1.getPosition());
			float dist2 = camera.position.dst(o2.getPosition());
			return (int) Math.signum(dist2 - dist1);
		};
		String vertexShader = Gdx.files.internal("shaders/decal_vertex.glsl").readString();
		String fragmentShader = Gdx.files.internal("shaders/decal_fragment.glsl").readString();
		shader = new ShaderProgram(vertexShader, fragmentShader);
		if (!shader.isCompiled()) throw new IllegalArgumentException("couldn't compile shader: " + shader.getLog());
	}

	@Override
	public ShaderProgram getGroupShader(final int group) {
		return shader;
	}

	@Override
	public int decideGroup(final Decal decal) {
		return decal.getMaterial().isOpaque() ? GROUP_OPAQUE : GROUP_BLEND;
	}

	@Override
	public void beforeGroup(final int group, final Array<Decal> contents) {
		if (group == GROUP_BLEND) {
			Gdx.gl.glEnable(GL20.GL_BLEND);
			contents.sort(cameraSorter);
		} else {
			for (int i = 0, n = contents.size; i < n; i++) {
				Decal decal = contents.get(i);
				Array<Decal> materialGroup = materialGroups.get(decal.getMaterial());
				if (materialGroup == null) {
					materialGroup = arrayPool.obtain();
					materialGroup.clear();
					usedArrays.add(materialGroup);
					materialGroups.put(decal.getMaterial(), materialGroup);
				}
				materialGroup.add(decal);
			}

			contents.clear();
			for (Array<Decal> materialGroup : materialGroups.values()) {
				contents.addAll(materialGroup);
			}

			materialGroups.clear();
			arrayPool.freeAll(usedArrays);
			usedArrays.clear();
		}
	}

	@Override
	public void afterGroup(final int group) {
		if (group == GROUP_BLEND) {
			Gdx.gl.glDisable(GL20.GL_BLEND);
		}
	}

	@Override
	public void beforeGroups() {
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		shader.bind();
		shader.setUniformMatrix("u_projectionViewMatrix", camera.combined);
		shader.setUniformi("u_texture", 0);
	}

	@Override
	public void afterGroups() {
		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
	}

	@Override
	public void dispose() {
		if (shader != null) shader.dispose();
	}
}
