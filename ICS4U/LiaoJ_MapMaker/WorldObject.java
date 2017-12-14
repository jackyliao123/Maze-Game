// Jacky Liao
// December 12, 2017
// Maze Game
// ICS4U Ms.Strelkovska

import java.awt.*;
import java.io.Serializable;

// A world object
public abstract class WorldObject implements Serializable {
	// Test whether a point is inside object
	public abstract boolean test(double nx, double ny);
	// Move object by (+dx, +dy)
	public abstract void applyDelta(double dx, double dy);
	// Duplicate the object
	public abstract WorldObject duplicate();
	// Render the object
	public abstract void render(Graphics2D g2d, GamePanel panel);
	// Render the selection box
	public abstract void renderSelection(Graphics2D g2d, GamePanel panel);
}
