import java.io.Serializable;

@ObjProperty(type = ObjProperty.Type.NONE, name = "Select")
public class DummyProperties implements Serializable {

	private static final long serialVersionUID = 3578063490473415080L;

	@GUIProperty(type = GUIProperty.Type.ANGLE, name = "Gravity angle")
	public double ang;
	@GUIProperty(type = GUIProperty.Type.NUMERIC, name = "Gravity strength", min = 0, rangeH = 10)
	public double str;
	@GUIProperty(type = GUIProperty.Type.NUMERIC, name = "Friction strength", min = 0, rangeH = 0.5)
	public double friction = 0.01;
	@GUIProperty(type = GUIProperty.Type.NUMERIC, name = "Movement strength", min = 0, rangeH = 10)
	public double movement = 1;
	@GUIProperty(type = GUIProperty.Type.NUMERIC, name = "Iterations", min = 1, rangeL = 1, rangeH = 64)
	public int iterations = 8;

}

