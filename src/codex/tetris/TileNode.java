/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codex.tetris;

import com.jme3.bounding.BoundingVolume;
import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.LinkedList;

/**
 *
 * @author gary
 */
public class TileNode extends Node {
	
	boolean atRest = false;
	
	public void moveDown(Geometry bounds, float speed) {
		move(0f, -speed, 0f);
		downCollision(bounds);
	}
	public boolean moveOver(Geometry bounds, int direction) {
		direction = FastMath.sign(direction);
		move(direction, 0f, 0f);
		if (!testInBounds(bounds) || testCollision()) {
			move(-direction, 0f, 0f);
			return false;
		}
		return true;
	}
	public boolean rotate(Geometry bounds, int direction) {
		float angle = FastMath.HALF_PI*FastMath.sign(direction);
		rotate(0f, 0f, angle);
		if (!testInBounds(bounds) || testCollision()) {
			rotate(0f, 0f, -angle);
			return false;
		}
		return true;
	}	
	public void removeCollisionUnits(Collidable c) {
		CollisionResults res = new CollisionResults();
		LinkedList<Spatial> remove = new LinkedList<>();
		for (Spatial unit : getChildren()) {
			unit.collideWith(c, res);
			if (res.size() > 0) {
				remove.add(unit);
				res.clear();
			}
		}
		for (Spatial unit : remove) {
			detachChild(unit);
		}
	}
	
	private void downCollision(Geometry bounds) {
		Ray ray = new Ray(new Vector3f(), new Vector3f(0f, -1f, 0f));
		CollisionResults res = new CollisionResults();
		for (Spatial unit : getChildren()) {
			ray.setOrigin(unit.getWorldTranslation());
			for (Spatial p : parent.getChildren()) {
				if (p == this) continue;
				p.collideWith(ray, res);
			}
			bounds.collideWith(ray, res);
			if (res.size() > 0) {
				CollisionResult closest = res.getClosestCollision();
				if (closest.getDistance() < Main.UNIT/2f) {
					move(0f, Main.UNIT/2f-closest.getDistance(), 0f);
					atRest = true;
					return;
				}
			}
			res.clear();
		}
		atRest = false;
	}
	public boolean testCollision() {
		Transform t = new Transform(new Vector3f(), new Quaternion(), new Vector3f(.5f, .5f, .5f));
		CollisionResults res = new CollisionResults();
		for (Spatial unit : getChildren()) {
			BoundingVolume vol = unit.getWorldBound().transform(t);
			for (Spatial p : parent.getChildren()) {
				if (p == this || !(p instanceof Node)) continue;
				for (Spatial u : ((Node)p).getChildren()) {
					u.getWorldBound().transform(t).collideWith(vol, res);
					if (res.size() > 0) {
						return true;
					}
				}
			}
		}
		return false;
	}
	private boolean testInBounds(Geometry bounds) {
		BoundingVolume vol = bounds.getWorldBound();
		for (Spatial unit : getChildren()) {
			if (!vol.contains(unit.getWorldTranslation())) {
				return false;
			}
		}
		return true;
	}
	
	public boolean isAtRest() {
		return atRest;
	}
	
}
