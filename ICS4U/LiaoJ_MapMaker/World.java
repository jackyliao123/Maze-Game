// Jacky Liao
// December 12, 2017
// Maze Game
// ICS4U Ms.Strelkovska

import java.io.Serializable;
import java.util.ArrayList;

// The world object
public class World implements Serializable {

	private static final long serialVersionUID = 449265521547170087L;

	// A list of WorldObjects
	public ArrayList<WorldObject> objs = new ArrayList<>();

	public World duplicate() {
		World world = new World();
		for(WorldObject obj : objs) {
			world.objs.add(obj.duplicate());
		}
		return world;
	}

}
