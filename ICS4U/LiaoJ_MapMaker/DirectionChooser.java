import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

public class DirectionChooser extends JPanel implements MouseListener, MouseMotionListener, KeyListener {

	public double ang;
	public ActionListener listener;

	public static final double PX = 110;
	public static final double PY = 50;
	public static final double R = 50;
	public static final double R2 = 100;

	public DirectionChooser() {
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
		setBackground(Color.white);
		setFocusable(true);
	}

	public void setActionListener(ActionListener listener) {
		this.listener = listener;
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setStroke(new BasicStroke(10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g2d.setColor(Color.black);
		g2d.fill(new Ellipse2D.Double(PX - R, PY - R, R2, R2));
		g2d.setColor(Color.gray);
		g2d.fill(new Ellipse2D.Double(PX - 10, PY - 10, 20, 20));
		g2d.draw(new Line2D.Double(PX, PY, Math.cos(ang) * R + PX, Math.sin(ang) * R + PY));
		g2d.setColor(Color.white);
		FontMetrics fm = g2d.getFontMetrics();
		String txt = String.format("%.2f", ang / Math.PI * 180);
		int wid = fm.stringWidth(txt);
		g2d.drawString(txt, (int) PX - wid / 2, (int) PY - 20);
	}
	public Dimension getPreferredSize() {
		return new Dimension((int) R2, (int) R2);
	}

	public void mousePressed(MouseEvent e) {
		requestFocus();
		double x = e.getX() - PX;
		double y = e.getY() - PY;
		ang = Math.atan2(y, x);
		callListener();
		repaint();
	}
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_LEFT) {
			ang += Math.PI / 180;
			callListener();
			repaint();
		} else if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
			ang -= Math.PI / 180;
			callListener();
			repaint();
		}
	}

	public void callListener() {
		if(listener != null) {
			listener.actionPerformed(null);
		}
	}

	public void mouseDragged(MouseEvent e) {
		mousePressed(e);
	}
	public void mouseClicked(MouseEvent e) {
	}
	public void mouseReleased(MouseEvent e) {
	}
	public void mouseEntered(MouseEvent e) {
	}
	public void mouseExited(MouseEvent e) {
	}
	public void mouseMoved(MouseEvent e) {
	}
	public void keyTyped(KeyEvent e) {
	}
	public void keyReleased(KeyEvent e) {
	}
}
