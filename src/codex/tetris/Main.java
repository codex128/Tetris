package codex.tetris;

import codex.j3map.J3map;
import codex.j3map.J3mapFactory;
import codex.j3map.processors.IntegerProcessor;
import codex.j3map.processors.StringProcessor;
import codex.jmeutil.ColorHSBA;
import codex.jmeutil.StringIterator;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingVolume;
import com.jme3.collision.CollisionResults;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.input.FunctionId;
import com.simsilica.lemur.input.InputMapper;
import com.simsilica.lemur.input.InputState;
import com.simsilica.lemur.input.StateFunctionListener;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * This is the Main Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls
 * @author normenhansen
 */
public class Main extends SimpleApplication implements StateFunctionListener {
	
	public static final float UNIT = 1f;
	
	Node tileRoot = new Node();
	LinkedList<TileNode> tiles = new LinkedList<>();
	LinkedList<TileNode> playing = new LinkedList<>();
	J3map generator;
	int boardwidth = 12;
	Geometry bounds;
	Vector3f spawnlocation = new Vector3f(0f, 20f, 0f);
	float regspeed = .05f;
	float fastspeed = .2f;
	boolean dropping = false;
	boolean gameover = false;
	
	public Main() {
		super(new StatsAppState());
	}
	
    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
		
		GuiGlobals.initialize(this);
		GuiGlobals.getInstance().setCursorEventsEnabled(false);
		InputMapper im = GuiGlobals.getInstance().getInputMapper();
		Functions.initialize(im);
		
		rootNode.attachChild(tileRoot);
		
		assetManager.registerLoader(J3mapFactory.class, "j3map");
		J3mapFactory.registerAllProcessors(StringProcessor.class, IntegerProcessor.class);
		generator = (J3map)assetManager.loadAsset("Models/TileGenerator.j3map");
		
		float w = ((float)boardwidth/2f)*UNIT;
		bounds = new Geometry("bounds-mesh", new Box(w, 50f, .75f));
		bounds.setLocalTranslation(0f, 50f, 0f);
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat.setColor("Color", ColorRGBA.Blue);
		mat.getAdditionalRenderState().setWireframe(true);
		bounds.setMaterial(mat);
		rootNode.attachChild(bounds);
		
		cam.setLocation(new Vector3f(0f, 10f, -30f));
		cam.lookAtDirection(Vector3f.UNIT_Z, Vector3f.UNIT_Y);
		inputManager.setCursorVisible(true);
		
		im.addStateListener(this, Functions.F_MOVE, Functions.F_ROTATE, Functions.F_DROP);
		im.activateGroup(Functions.MANIPULATION_GROUP);

    }
    @Override
    public void simpleUpdate(float tpf) {
		
		for (Iterator<TileNode> i = tiles.iterator(); i.hasNext();) {
			TileNode t = i.next();
			t.moveDown(bounds, regspeed);
			if (t.getChildren().isEmpty()) {
				i.remove();
				t.removeFromParent();
			}
		}
		if (!playing.isEmpty()) {
			playing.getFirst().moveDown(bounds, (!dropping ? regspeed : fastspeed));
			if (playing.getFirst().isAtRest() && !gameover) {
				spawnNextTile();
			}
		}
		else {
			spawnNextTile();
		}
		if (!tiles.isEmpty() && bottomRowFilled()) {
			Ray ray = new Ray(new Vector3f(-boardwidth, 0f, 0f), Vector3f.UNIT_X);
			for (TileNode t : tiles) {
				t.removeCollisionUnits(ray);
			}
		}
		
    }
    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
	
	private TileNode generateTile() {
		// generate tile
		int index = FastMath.rand.nextInt(generator.getInteger("limit")+1);
		String data = generator.getString("t"+index);
		if (data == null) {
			throw new NullPointerException("Tile data does not exist at \"t"+index+"\"!");
		}
		Vector3f i = new Vector3f();
		Vector3f center = null;
		TileNode tile = new TileNode();
		LinkedList<Spatial> units = new LinkedList<>();
		for (Character c : new StringIterator(data)) {
			if (c == '+') {
				i.x = 0;
				i.y++;
				continue;
			}
			if (c == 'O' || c == '#') {
				if (center == null && c == 'O') {
					center = i.clone().multLocal(UNIT);
				}
				Spatial unit = assetManager.loadModel("Models/unit.j3o");
				unit.setLocalTranslation(i.mult(UNIT));
				unit.setLocalScale(UNIT);
				units.add(unit);
			}
			i.x++;
		}
		if (center == null) {
			center = averageLocation(units);
		}
		center.negateLocal();
		for (Spatial u : units) {
			u.move(center);
			tile.attachChild(u);
		}
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat.setColor("Color", new ColorHSBA(FastMath.rand.nextFloat(), 1f, 1f, 1f).toRGBA());
//		mat.setColor("Color", ColorRGBA.Blue);
		tile.setMaterial(mat);
		return tile;
	}
	private void spawnNextTile() {
		if (!playing.isEmpty()) {
			tiles.addLast(playing.removeFirst());
		}
		playing.addLast(generateTile());
		tileRoot.attachChild(playing.getFirst());
		playing.getFirst().setLocalTranslation(spawnlocation);
		if (playing.getFirst().testCollision()) {
			System.out.println("you lose!");
			gameover = true;
		}
	}
	private Vector3f averageLocation(Collection<Spatial> spatials) {
		Vector3f total = new Vector3f();
		for (Spatial s : spatials) {
			total.addLocal(s.getLocalTranslation());
		}
		return total.divideLocal(spatials.size());
	}
	private boolean bottomRowFilled() {
		Transform t = new Transform(new Vector3f(UNIT, 0f, 0f), Quaternion.ZERO, Vector3f.ZERO);
		BoundingVolume test = new BoundingBox(new Vector3f(-boardwidth/2f, -UNIT/2f, 0f), UNIT/4f, UNIT/4f, UNIT/4f);
		CollisionResults res = new CollisionResults();
		for (float i = 0; i < boardwidth/UNIT; i++) {
			tileRoot.collideWith(test, res);
			if (res.size() == 0) {
				return false;
			}
			test = test.transform(t);
			res.clear();
		}
		return true;
	}

	@Override
	public void valueChanged(FunctionId func, InputState value, double tpf) {
		if (playing.isEmpty()) return;
		if (func == Functions.F_MOVE && value != InputState.Off) {
			playing.getFirst().moveOver(bounds, value.asNumber());
		}
		else if (func == Functions.F_ROTATE && value != InputState.Off) {
			playing.getFirst().rotate(bounds, value.asNumber());
		}
		else if (func == Functions.F_DROP) {
			dropping = value == InputState.Positive;
		}
	}
	
}
