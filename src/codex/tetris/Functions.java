/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codex.tetris;

import com.jme3.input.KeyInput;
import com.simsilica.lemur.input.FunctionId;
import com.simsilica.lemur.input.InputMapper;
import com.simsilica.lemur.input.InputState;

/**
 *
 * @author gary
 */
public class Functions {
	
	public static final String
			MANIPULATION_GROUP = "manipulation-group";
	
	public static final FunctionId
			F_MOVE = new FunctionId(MANIPULATION_GROUP, "move"),
			F_ROTATE = new FunctionId(MANIPULATION_GROUP, "rotate"),
			F_DROP = new FunctionId(MANIPULATION_GROUP, "drop");
	
	public static void initialize(InputMapper im) {
		im.map(F_MOVE, InputState.Negative, KeyInput.KEY_RIGHT);
		im.map(F_MOVE, InputState.Positive, KeyInput.KEY_LEFT);
		im.map(F_ROTATE, InputState.Negative, KeyInput.KEY_D);
		im.map(F_ROTATE, InputState.Positive, KeyInput.KEY_A);
		im.map(F_DROP, KeyInput.KEY_DOWN);
		im.map(F_DROP, KeyInput.KEY_S);
	}
	
}
