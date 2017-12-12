// Jacky Liao
// December 4, 2017
// Map maker
// ICS4U Ms.Strelkovska

import java.awt.*;
import java.awt.geom.Ellipse2D;

@ObjProperty(name = "Goal", type = ObjProperty.Type.POINT)
public class ObjGoal extends WorldObject {
	private static final long serialVersionUID = -5868188964991908566L;

	public static final double RADIUS = GamePanel.GRID_SIZE / 2.0;
	public static final double RADIUS_SEL = RADIUS + 20;

	public double x, y;

	@GUIProperty(name = "Goto start with label", type = GUIProperty.Type.STRING)
	public String gotoLabel;

	@GUIProperty(name = "Preserve colour", type = GUIProperty.Type.CHECK)
	public boolean preserveColour;

	public ObjGoal(double x, double y, String gotoLabel, boolean preserveColour) {
		this.x = x;
		this.y = y;
		this.gotoLabel = gotoLabel;
		this.preserveColour = preserveColour;
	}

	public boolean test(double nx, double ny) {
		return (x - nx) * (x - nx) + (y - ny) * (y - ny) < RADIUS * RADIUS;
	}

	public void applyDelta(double dx, double dy) {
		x += dx;
		y += dy;
	}

	public WorldObject duplicate() {
		return new ObjGoal(x, y, gotoLabel, preserveColour);
	}

	public void render(Graphics2D g2d, GamePanel panel) {
		float length = (float)(Math.PI * RADIUS / 8);
		g2d.setStroke(new BasicStroke(10.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[]{length, length}, (float)((System.nanoTime() / (gotoLabel.length() == 0 ? 1e9 : 4e9)) % 1 * Math.PI * RADIUS)));
		Ellipse2D ellipse = new Ellipse2D.Double(x - RADIUS, y - RADIUS, RADIUS * 2, RADIUS * 2);
		if(gotoLabel.length() != 0 && !preserveColour) {
			g2d.setColor(new Color(255, 255, 255, 128));
		} else {
			Color clr = GamePanel.colors[(int) (System.nanoTime() / ((preserveColour && gotoLabel.length() != 0) ? 4e9 : 1e9) % 1 * 7)];
			g2d.setColor(new Color(clr.getRed(), clr.getGreen(), clr.getBlue(), 128));
		}
		g2d.draw(ellipse);
		if(gotoLabel != null) {
			FontMetrics fm = g2d.getFontMetrics();
			g2d.setColor(Color.white);
			g2d.drawString(gotoLabel, (int) x - fm.stringWidth(gotoLabel) / 2, (int) y + (int) RADIUS + 20);
		}
	}

	public void renderSelection(Graphics2D g2d, GamePanel panel) {
		Ellipse2D ellipse = new Ellipse2D.Double(x - RADIUS_SEL, y - RADIUS_SEL, RADIUS_SEL * 2, RADIUS_SEL * 2);
		g2d.setColor(new Color(255, 255, 255, 128));
		g2d.fill(ellipse);
	}
}
