// Jacky Liao
// December 4, 2017
// Map maker
// ICS4U Ms.Strelkovska

import java.awt.*;
import java.awt.geom.Line2D;

@ObjProperty(name = "Wall", type = ObjProperty.Type.LINE)
public class ObjWall extends WorldObject {
	private static final long serialVersionUID = 5710860197852031437L;

	public static final float THICKNESS = 30;

	public double x1, y1, x2, y2;

	@GUIProperty(type = GUIProperty.Type.COLOR, name = "Colour")
	public int color;

	@GUIProperty(type = GUIProperty.Type.STRING, name = "Unlock key")
	public String unlockName;

	@GUIProperty(type = GUIProperty.Type.NUMERIC, name = "Coefficient of restitution", min = 0, max = 1)
	public double coeffRes;

	public transient boolean unlocked;

	public static final Stroke lineStroke = new BasicStroke(ObjWall.THICKNESS, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	public static final Stroke lineStrokeAround = new BasicStroke(ObjWall.THICKNESS * 1.25f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	public static final Stroke selectStroke = new BasicStroke(ObjWall.THICKNESS * 1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

	public boolean test(double nx, double ny) {
		return distSq(nx, ny) < THICKNESS * THICKNESS / 4;
	}

	public double distSq(double x, double y) {
		double dx = x2 - x1, dy = y2 - y1;
		double vx = x - x1, vy = y - y1;
		double lenSq = dx * dx + dy * dy;
		double nx, ny;
		if(lenSq > 1e-6) {
			double scale = (dx * vx + dy * vy) / lenSq;
			scale = Math.min(Math.max(0, scale), 1);
			nx = x1 + dx * scale;
			ny = y1 + dy * scale;
		} else {
			nx = x1;
			ny = y1;
		}
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
		if(panel.play && unlocked) {
			return;
		}
		if(unlockName.length() != 0) {
			g2d.setStroke(lineStrokeAround);
			g2d.setColor(new Color(255, 255, 255, 128));
			g2d.draw(new Line2D.Double(x1, y1, x2, y2));
		}
		Color clr = GamePanel.colors[color];
		if(panel.currColor == color && unlockName.length() == 0 && panel.play) {
			clr = new Color(clr.getRed(), clr.getGreen(), clr.getBlue(), 60);
		}
		g2d.setStroke(lineStroke);
		g2d.setColor(clr);
		g2d.draw(new Line2D.Double(x1, y1, x2, y2));
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
