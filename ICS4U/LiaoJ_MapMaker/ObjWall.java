// Jacky Liao
// December 12, 2017
// Maze Game
// ICS4U Ms.Strelkovska

import java.awt.*;
import java.awt.geom.Line2D;

// The wall in the game
@ObjProperty(name = "Wall", type = ObjProperty.Type.LINE)
public class ObjWall extends WorldObject {
	private static final long serialVersionUID = 5710860197852031437L;

	public static final float THICKNESS = 30;

	// 2 endpoints
	public double x1, y1, x2, y2;

	@GUIProperty(type = GUIProperty.Type.COLOR, name = "Colour")
	public int color;

	@GUIProperty(type = GUIProperty.Type.STRING, name = "Unlock key")
	public String unlockName;

	@GUIProperty(type = GUIProperty.Type.NUMERIC, name = "Coefficient of restitution", min = 0, max = 1)
	public double coeffRes;

	// Is it unlocked? (Don't serialize)
	public transient boolean unlocked;

	// Different stroke thicknesses
	public static final Stroke lineStroke = new BasicStroke(ObjWall.THICKNESS, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	public static final Stroke lineStrokeIn = new BasicStroke(ObjWall.THICKNESS * 0.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	public static final Stroke lineStrokeAround = new BasicStroke(ObjWall.THICKNESS * 1.25f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	public static final Stroke selectStroke = new BasicStroke(ObjWall.THICKNESS * 1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

	// Is the point in the line?
	public boolean test(double nx, double ny) {
		return distSq(nx, ny) < THICKNESS * THICKNESS / 4;
	}

	// Compute distance squared
	public double distSq(double x, double y) {
		double dx = x2 - x1, dy = y2 - y1;
		double vx = x - x1, vy = y - y1;
		double lenSq = dx * dx + dy * dy;
		double nx, ny;
		if(lenSq > 1e-6) {
			// Non-zero length
			double scale = (dx * vx + dy * vy) / lenSq;
			// Clamp point to on the line
			scale = Math.min(Math.max(0, scale), 1);
			nx = x1 + dx * scale;
			ny = y1 + dy * scale;
		} else {
			// Zero length
			nx = x1;
			ny = y1;
		}
		// Distance between 2 points
		return (x - nx) * (x - nx) + (y - ny) * (y - ny);
	}

	public ObjWall(double x1, double y1, double x2, double y2, int color, double coeffRes, String unlockName) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		this.color = color;
		this.coeffRes = coeffRes;
		this.unlockName = unlockName;
	}

	public ObjWall duplicate() {
		return new ObjWall(x1, y1, x2, y2, color, coeffRes, unlockName);
	}

	public void render(Graphics2D g2d, GamePanel panel) {

		// Render the wall differently in different cases (Flash if selected in inventory)
		if(panel.play && (unlocked != (System.nanoTime() / 1e9 % 1 < 0.5 && panel.showInventory && panel.invSelected != -1 && panel.collectedKeys.get(panel.invSelected).unlockName.equals(unlockName)))) {
			return;
		}
		if(unlockName.length() != 0) {
			g2d.setStroke(lineStrokeAround);
			g2d.setColor(new Color(255, 255, 255, 128));
			g2d.draw(new Line2D.Double(x1, y1, x2, y2));
		}
		Color clr = GamePanel.colors[color];
		if(panel.currColor == color && unlockName.length() == 0 && panel.play) {
			clr = new Color(clr.getRed(), clr.getGreen(), clr.getBlue(), 100);
		}
		g2d.setStroke(lineStroke);
		g2d.setColor(clr);
		g2d.draw(new Line2D.Double(x1, y1, x2, y2));

		g2d.setStroke(lineStrokeIn);
		int r = clr.getRed();
		int g = clr.getGreen();
		int b = clr.getBlue();
		g2d.setColor(new Color((int) (r + (255 - r) * coeffRes), (int) (g + (255 - g) * coeffRes), (int) (b + (255 - b) * coeffRes), (int) (255 * coeffRes)));
		g2d.draw(new Line2D.Double(x1, y1, x2, y2));

		// Draw the text
		if(unlockName.length() != 0 && !panel.play) {
			double x = (x1 + x2) / 2;
			double y = (y1 + y2) / 2;
			FontMetrics fm = g2d.getFontMetrics();
			if(color == 6) {
				g2d.setColor(new Color(128, 128, 128, 255));
			} else {
				g2d.setColor(Color.white);
			}
			g2d.drawString(unlockName, (int) x - fm.stringWidth(unlockName) / 2, (int) y);
		}
	}

	public void renderSelection(Graphics2D g2d, GamePanel panel) {
		// Draw the selection around the line
		g2d.setStroke(selectStroke);
		g2d.setColor(new Color(128, 128, 128, 128));
		g2d.draw(new Line2D.Double(x1, y1, x2, y2));
	}

	public void applyDelta(double dx, double dy) {
		x1 += dx;
		x2 += dx;
		y1 += dy;
		y2 += dy;
	}
}
