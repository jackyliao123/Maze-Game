// Jacky Liao
// December 12, 2017
// Maze Game
// ICS4U Ms.Strelkovska

import java.awt.*;
import java.awt.geom.Ellipse2D;

// The orb object, to change player colour and unlock walls
@ObjProperty(name = "Orb", type = ObjProperty.Type.POINT)
public class ObjOrb extends WorldObject {

	private static final long serialVersionUID = 8648695136856265463L;

	public static final double RADIUS = GamePanel.GRID_SIZE / 8.0;
	public static final double GLOW_RADIUS = RADIUS + 5;
	public static final double RADIUS_SEL = RADIUS + 20;

	public double x, y;

	@GUIProperty(type = GUIProperty.Type.COLOR, name = "Colour")
	public int color;

	@GUIProperty(type = GUIProperty.Type.STRING, name = "Unlock key")
	public String unlock;

	// Whether the orb has been used or not (don't serialize)
	public transient boolean used;

	public ObjOrb(double x, double y, int color, String unlock) {
		this.x = x;
		this.y = y;
		this.color = color;
		this.unlock = unlock;
	}

	// Point in object?
	public boolean test(double nx, double ny) {
		return (x - nx) * (x - nx) + (y - ny) * (y - ny) < GLOW_RADIUS * GLOW_RADIUS;
	}

	public void applyDelta(double dx, double dy) {
		x += dx;
		y += dy;
	}

	public WorldObject duplicate() {
		return new ObjOrb(x, y, color, unlock);
	}

	public void render(Graphics2D g2d, GamePanel panel) {
		if(!panel.play || !used) {
			// Render the orb differently based on different configurations
			Color clr = GamePanel.colors[color];
			g2d.setColor(new Color(clr.getRed(), clr.getGreen(), clr.getBlue(), 0x80));
			g2d.setStroke(new BasicStroke(10));
			g2d.draw(new Ellipse2D.Double(x - GLOW_RADIUS, y - GLOW_RADIUS, GLOW_RADIUS * 2, GLOW_RADIUS * 2));
			if(unlock.length() == 0) {
				g2d.setColor(clr);
				g2d.fill(new Ellipse2D.Double(x - RADIUS, y - RADIUS, RADIUS * 2, RADIUS * 2));
			}
		}
		if(!panel.play && unlock.length() != 0) {
			// Render text if in edit mode
			FontMetrics fm = g2d.getFontMetrics();
			g2d.setColor(Color.white);
			g2d.drawString(unlock, (int) x - fm.stringWidth(unlock) / 2, (int) y + 40);
		}
	}

	public void renderSelection(Graphics2D g2d, GamePanel panel) {
		// Render selection highlighting box
		Ellipse2D ellipse = new Ellipse2D.Double(x - RADIUS_SEL, y - RADIUS_SEL, RADIUS_SEL * 2, RADIUS_SEL * 2);
		g2d.setColor(new Color(255, 255, 255, 128));
		g2d.fill(ellipse);
	}
}
