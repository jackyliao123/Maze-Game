// Jacky Liao
// December 4, 2017
// Map maker
// ICS4U Ms.Strelkovska

import java.awt.*;
import java.io.Serializable;

public abstract class WorldObject implements Serializable {
	public abstract boolean test(double nx, double ny);
	public abstract void applyDelta(double dx, double dy);
	public abstract WorldObject duplicate();
	public abstract void render(Graphics2D g2d, GamePanel panel);
	public abstract void renderSelection(Graphics2D g2d, GamePanel panel);
}
