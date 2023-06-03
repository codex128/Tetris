package codex.tetris;

import codex.j3map.J3map;
import codex.j3map.J3mapFactory;
import codex.j3map.processors.IntegerProcessor;
import codex.j3map.processors.StringProcessor;
import codex.jmeutil.ColorHSBA;
import codex.jmeutil.StringIterator;
import com.jme3.app.SimpleApplication;
import com.jme3.bounding.BoundingBox;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.Collection;
import java.util.LinkedList;

/**
 * This is the Main Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls
 * @author normenhansen
 */
public class Main extends SimpleApplication {
	
	public static final float UNIT = 1f;
	public static final BoundingBox BOUNDS = new BoundingBox(
			new Vector3f(-6f, 0f, -1.5f), new Vector3f(6f, 50f, 1.5f));
	
	Node pieces = new Node();
	J3map generator;
	
    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        
		assetManager.registerLoader(J3mapFactory.class, "j3map");
		J3mapFactory.registerAllProcessors(StringProcessor.class, IntegerProcessor.class);
		generator = (J3map)assetManager.loadAsset("Models/PieceGenerator.j3map");
		
    }
    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
    }
    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
	
	private PieceNode generatePiece() {
		int index = FastMath.rand.nextInt(generator.getInteger("limit")+1);
		String data = generator.getString("p"+index);
		if (data == null) {
			throw new NullPointerException("Piece data does not exist at \"p"+index+"\"!");
		}
		Vector3f i = new Vector3f();
		Vector3f center = null;
		PieceNode piece = new PieceNode();
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
			}
			i.x++;
		}
		if (center == null) {
			center = averageLocation(units);
		}
		center.negateLocal();
		for (Spatial u : units) {
			u.move(center);
			piece.attachChild(u);
		}
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat.setColor("Color", new ColorHSBA(FastMath.rand.nextFloat(), 1f, 1f, 1f).toRGBA());
		piece.setMaterial(mat);
		return piece;
	}
	private Vector3f averageLocation(Collection<Spatial> spatials) {
		Vector3f total = new Vector3f();
		for (Spatial s : spatials) {
			total.addLocal(s.getLocalTranslation());
		}
		return total.divideLocal(spatials.size());
	}
	
}
