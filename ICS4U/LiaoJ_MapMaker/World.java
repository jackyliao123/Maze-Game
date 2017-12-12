// Jacky Liao
// December 4, 2017
// Map maker
// ICS4U Ms.Strelkovska

import java.io.Serializable;
import java.util.ArrayList;

public class World implements Serializable {

	private static final long serialVersionUID = 449265521547170087L;

	public ArrayList<WorldObject> objs = new ArrayList<>();

	public World duplicate() {
		World world = new World();
		for(WorldObject obj : objs) {
			world.objs.add(obj.duplicate());
		}
		return world;
	}

}
