// Jacky Liao
// December 12, 2017
// Maze Game
// ICS4U Ms.Strelkovska

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

// A widget to choose colour
public class ColorChooser extends JPanel implements MouseListener, MouseMotionListener, KeyListener {

	// The colour
	public int clr;

	// The listener when colour changes
	public ActionListener listener;

	public ColorChooser() {
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
		setBackground(Color.black);
		setFocusable(true);
	}

	public void setActionListener(ActionListener listener) {
		this.listener = listener;
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;

		// Draw colours
		for(int i = 0; i < GamePanel.colors.length; ++i) {
			Color color = GamePanel.colors[i];
			g.setColor(color);
			g.fillRect(5 + i * 30, 5, 30, 30);
		}

		// Draw colour selecting box
		g2d.setStroke(new BasicStroke(8));
		Color color = GamePanel.colors[clr];
		g2d.setColor(new Color(color.getRed() / 2, color.getGreen() / 2, color.getBlue() / 2));
		g.drawRect(5 + clr * 30, 5, 30, 30);
	}
	public Dimension getPreferredSize() {
		return new Dimension(30 * GamePanel.colors.length + 10, 40);
	}

	public void mousePressed(MouseEvent e) {
		// Update colours
		requestFocus();
		int x = e.getX();
		int newClr = Math.max(Math.min((x - 5) / 30, GamePanel.colors.length - 1), 0);
		if(newClr != clr) {
			clr = newClr;
			callListener();
		}
		repaint();
	}
	public void keyPressed(KeyEvent e) {
		// Update colours
		if(e.getKeyCode() == KeyEvent.VK_LEFT && clr > 0) {
			--clr;
			callListener();
			repaint();
		} else if(e.getKeyCode() == KeyEvent.VK_RIGHT && clr < GamePanel.colors.length - 1) {
			++clr;
			callListener();
			repaint();
		}
	}

	public void callListener() {
		// Notify colour update
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
