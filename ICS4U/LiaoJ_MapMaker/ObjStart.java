// Jacky Liao
// December 12, 2017
// Maze Game
// ICS4U Ms.Strelkovska

import java.awt.*;
import java.awt.geom.Ellipse2D;

// The starting point of the game
@ObjProperty(name = "Start", type = ObjProperty.Type.POINT)
public class ObjStart extends WorldObject {
	private static final long serialVersionUID = 4336170146506640705L;

	public static final double RADIUS = GamePanel.GRID_SIZE / 2.0;
	public static final double RADIUS_SEL = RADIUS + 20;

	public double x, y;

	@GUIProperty(type = GUIProperty.Type.COLOR, name = "Colour")
	public int color;

	@GUIProperty(type = GUIProperty.Type.STRING, name = "Label")
	public String label;

	public ObjStart(double x, double y, int color, String label) {
		this.x = x;
		this.y = y;
		this.color = color;
		this.label = label;
	}

	public boolean test(double nx, double ny) {
		double dist = (x - nx) * (x - nx) + (y - ny) * (y - ny);
		return dist < (RADIUS + 10) * (RADIUS + 10);
	}

	public void applyDelta(double x, double y) {
		this.x += x;
		this.y += y;
	}

	public WorldObject duplicate() {
		return new ObjStart(x, y, color, label);
	}

	public void render(Graphics2D g2d, GamePanel panel) {
		Ellipse2D ellipse = new Ellipse2D.Double(x - RADIUS, y - RADIUS, 2 * RADIUS, 2 * RADIUS);
		g2d.setStroke(new BasicStroke(20.0f));
		g2d.setColor(new Color(GamePanel.colors[color].getRGB() + 0x80000000, true));
		g2d.draw(ellipse);
		if(label != null) {
			FontMetrics fm = g2d.getFontMetrics();
			g2d.setColor(Color.white);
			g2d.drawString(label, (int) x - fm.stringWidth(label) / 2, (int) y + (int) RADIUS + 25);
		}
	}

	public void renderSelection(Graphics2D g2d, GamePanel panel) {
		Ellipse2D ellipse = new Ellipse2D.Double(x - RADIUS_SEL, y - RADIUS_SEL, 2 * RADIUS_SEL, 2 * RADIUS_SEL);
		g2d.setColor(new Color(128, 128, 128, 128));
		g2d.fill(ellipse);
	}
}
