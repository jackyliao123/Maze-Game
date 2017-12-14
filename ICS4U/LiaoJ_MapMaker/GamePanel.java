// Jacky Liao
// December 12, 2017
// Maze Game
// ICS4U Ms.Strelkovska

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Stack;

// Main panel to run game
public class GamePanel extends JPanel implements ActionListener, MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {

	public MapMaker mapMaker;

	// Panning and scaling
	public double offsetX, offsetY, scale = 1;

	// The target panning and scaling, for smooth movement
	public double targetOffsetX, targetOffsetY, targetScale = 1;

	// Moues scroll
	public int wheelMoveX, wheelMoveY;

	public int mouseX, mouseY;

	// Is creating a new line?
	public boolean isDrawing = false;
	// The line being created
	public WorldObject creating;

	// Is dragging an object around?
	public boolean isDragging = false;
	public double dragStartX, dragStartY;
	public boolean dragged;

	// Is panning the screen around?
	public boolean isPanning = false;
	public int panStartX, panStartY;

	public World world = new World();

	// Remembers undo
	public Stack<World> undoStack = new Stack<>();
	// Remembers redo
	public Stack<World> redoStack = new Stack<>();

	// Editing mode
	public int editMode = 0;

	// Highlighted object
	public WorldObject hlobj = null;

	public static final int GRID_SIZE = 128;

	// Is playing?
	public boolean play = false;
	public int lastEditMode;

	// Keep track of pressed down keys
	public boolean keyLeft, keyRight, keyUp, keyDown;

	// Pan x, y when in edit omde
	public double preX, preY;

	// Current colour of the player
	public int currColor = 0;

	// Player position
	public double x, y;
	// Player velocity
	public double vx, vy;
	// Player angular position
	public double r;
	// Player angular velocity
	public double vr;

	// For inventory selection
	public double invLocation = 1e9;
	public int invSelected = -1;
	public double invSelectedShown;
	public double invTargetScrollOffset;
	public double invScrollOffset;

	// Show the inventory?
	public boolean showInventory;

	// List of keys the layer collected
	public ArrayList<UnlockedEntries> collectedKeys = new ArrayList<>();
	public static class UnlockedEntries {
		public int color;
		public String unlockName;
		public UnlockedEntries(int color, String unlockName) {
			this.color = color;
			this.unlockName = unlockName;
		}
	}

	// File to save and load from
	public File file;

	// The list of global properties
	public DummyProperties properties = new DummyProperties();

	// The list of objects that can be added into the world, except DummySelect
	public static Class[] classes = new Class[]{
			DummySelect.class,
			ObjStart.class,
			ObjWall.class,
			ObjOrb.class,
			ObjGoal.class,
	};

	// The default version of all these objects
	public static Object[] defaultObject = new Object[] {
			new DummySelect(),
			new ObjStart(0, 0, 0, ""),
			new ObjWall(0, 0, 0, 0, 0, 0, ""),
			new ObjOrb(0, 0, 0, ""),
			new ObjGoal(0, 0, "", false),
	};

	// The colours to use
	public static Color[] colors = {
			new Color(0xFF3535),
			new Color(0xFD751C),
			new Color(0xFFFF00),
			new Color(0x60FF00),
			new Color(0x0066FF),
			new Color(0x9000FF),
			new Color(0xFFFFFF),
	};

	public GamePanel(MapMaker mapMaker) {
		this.mapMaker = mapMaker;

		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		addKeyListener(this);
		setBackground(Color.black);
		setFocusable(true);
		new Timer(15, this).start();
	}

	public void enterPlay() {
		// Start playing
		lastEditMode = editMode;
		editMode = -1;
		boolean found = false;

		// Reset all the world items
		for(WorldObject obj : world.objs) {
			if(obj instanceof ObjStart) {
				// See if we can find a starting point
				ObjStart start = (ObjStart) obj;
				if(start.label.length() == 0) {
					// If found, reset all world properties, and start playing
					if(found) {
						JOptionPane.showMessageDialog(null, "There can be at most 1 starting point with empty label", "Error", JOptionPane.ERROR_MESSAGE);
						enterEdit();
						return;
					}
					x = start.x;
					y = start.y;
					r = 0;
					vx = 0;
					vy = 0;
					vr = 0;
					showInventory = false;
					currColor = start.color;
					found = true;
					preX = targetOffsetX;
					preY = targetOffsetY;
					invSelected = -1;
					invTargetScrollOffset = 0;
					collectedKeys.clear();
				}
			} else if(obj instanceof ObjOrb) {
				ObjOrb orb = (ObjOrb) obj;
				orb.used = false;
			} else if(obj instanceof ObjWall) {
				ObjWall wall = (ObjWall) obj;
				wall.unlocked = false;
			}
		}
		if(!found) {
			JOptionPane.showMessageDialog(null, "No starting point with empty label defined", "Error", JOptionPane.ERROR_MESSAGE);
			enterEdit();
			return;
		}
		play = true;
		mapMaker.playButton.setText("Edit");
		requestFocus();
	}


	// Saves the world
	public void save() throws IOException, ClassNotFoundException {
		ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(file));
		output.writeObject(properties);
		output.writeObject(world);
		output.close();
	}

	// Load the world
	public void load() throws IOException, ClassNotFoundException {
		ObjectInputStream input = new ObjectInputStream(new FileInputStream(file));
		properties = (DummyProperties) input.readObject();
		world = (World) input.readObject();
		input.close();
	}

	// Enter edit mode
	public void enterEdit() {
		editMode = lastEditMode;
		targetOffsetX = preX;
		targetOffsetY = preY;
		play = false;
		mapMaker.playButton.setText("Play");
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;

		Font f = g2d.getFont();
		g2d.setFont(f.deriveFont(24.0f));

		// Draw dots if not playing
		if(!play) {
			g2d.setColor(Color.white);
			if(scale > 0.25) {
				for(double i = 0; i < getWidth() + scale * GRID_SIZE; i += scale * GRID_SIZE) {
					for(double j = 0; j < getHeight() + scale * GRID_SIZE; j += scale * GRID_SIZE) {
						g2d.fill(new Rectangle2D.Double(i + ((offsetX + getWidth() / 2 / scale) % GRID_SIZE) * scale - 1, j + ((offsetY + getHeight() / 2 / scale) % GRID_SIZE) * scale - 1, 2, 2));
					}
				}
			}
		}

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// Remember transform, to be restored when rendering inventory
		AffineTransform transform = g2d.getTransform();

		// Let Graphics translate world coords to screen coords
		g2d.translate(getWidth() / 2, getHeight() / 2);
		g2d.scale(scale, scale);
		g2d.translate(offsetX, offsetY);

		// Draw all objects
		for(WorldObject obj : world.objs) {
			if(play && obj instanceof ObjStart)
				continue;
			obj.render(g2d, this);
		}

		if(!play) {
			// Not playing, show the line being created and a cursor
			if(hlobj != null) {
				hlobj.renderSelection(g2d, this);
			}
			g2d.setStroke(ObjWall.lineStroke);
			if(isDrawing && creating != null) {
				creating.render(g2d, this);
			}

			if(editMode > 0) {
				Object obj = defaultObject[editMode];
				try {
					Field field = obj.getClass().getDeclaredField("color");
					g2d.setColor(colors[(int) field.get(obj)]);
					double t = 20;
					g2d.fill(new Ellipse2D.Double(snap(toWorldX(mouseX)) - t / 2.0, snap(toWorldY(mouseY)) - t / 2.0, t, t));
				} catch(ReflectiveOperationException e) {
				}
			}
		} else {
			// Playing, run logic and render world
			logic();
			g2d.setColor(colors[currColor]);
			int sections = 4;
			double ws = 360.0 / sections;
			double ang1 = r / (2 * Math.PI * ObjStart.RADIUS) * 360;

			// Draw the player, to show the rotation
			for(int i = 0; i < sections; ++i) {
				Color color = colors[currColor];
				if(i % 2 == 0) {
					color = new Color(color.getRed() / 2, color.getGreen() / 2, color.getBlue() / 2);
				}

				g2d.setColor(color);
				g2d.fill(new Arc2D.Double(x - ObjStart.RADIUS, y - ObjStart.RADIUS, ObjStart.RADIUS * 2, ObjStart.RADIUS * 2, ang1 + ws * i, ws, Arc2D.PIE));
			}
		}

		// Compute zoom around mouse cursor
		double dScale = Math.pow(targetScale / scale, 0.2);
		double offsetFact = 1 / scale / dScale - 1 / scale;

		// Reset scrolling mouse position
		if(play) {
			wheelMoveX = 0;
			wheelMoveY = 0;
		}

		// Apply zoom and panning
		offsetX += wheelMoveX * offsetFact;
		offsetY += wheelMoveY * offsetFact;
		targetOffsetX += wheelMoveX * offsetFact;
		targetOffsetY += wheelMoveY * offsetFact;
		scale *= dScale;

		offsetX += (targetOffsetX - offsetX) * 0.2;
		offsetY += (targetOffsetY - offsetY) * 0.2;

		// Reset the transform back to screen coordinates
		g2d.setTransform(transform);

		if(play) {
			// Is playing, center on player
			targetOffsetX = -x;
			targetOffsetY = -y;

			// Render inventory
			double target = getHeight() + 5;
			if(showInventory) {
				target = getHeight() - 200;
			}

			// Smoothly move inventory
			invLocation += (target - invLocation) * 0.2;
			invSelectedShown += (invSelected - invSelectedShown) * 0.3;
			invScrollOffset += (invTargetScrollOffset - invScrollOffset) * 0.3;

			// Move inventory to bottom
			g2d.translate(0, invLocation);

			FontMetrics fm = g2d.getFontMetrics();
			int height = fm.getHeight();

			g2d.setColor(new Color(128, 128, 128, 128));
			g2d.fill(new Rectangle2D.Double(0, 0, getWidth(), 200));

			// Don't draw outside of this region
			g2d.setClip(0, fm.getHeight() - fm.getAscent(), getWidth(), 200 - (fm.getHeight() - fm.getAscent()) * 2);

			// Draw all the collected orbs unlock keys
			for(int i = 0; i < collectedKeys.size(); ++i) {
				double x = 50;
				double y = (i - invScrollOffset) * height + fm.getAscent();
				UnlockedEntries entries = collectedKeys.get(collectedKeys.size() - 1 - i);
				g2d.setStroke(new BasicStroke(5));
				g2d.setColor(GamePanel.colors[entries.color]);
				g2d.draw(new Ellipse2D.Double(x - ObjOrb.GLOW_RADIUS / 2, y - ObjOrb.GLOW_RADIUS / 2, ObjOrb.GLOW_RADIUS, ObjOrb.GLOW_RADIUS));
				g2d.setColor(Color.white);
				g2d.drawString(entries.unlockName, 70, (int) ((i + 1 - invScrollOffset) * height));
			}

			// If something is selected, draw the selecting box
			if(invSelected != -1) {
				if((collectedKeys.size() - 1 - invSelected) >= invTargetScrollOffset + 200 / height) {
					invTargetScrollOffset ++;
				}
				if((collectedKeys.size() - 1 - invSelected) < invTargetScrollOffset) {
					invTargetScrollOffset --;
				}
				g2d.setStroke(new BasicStroke(2));
				g2d.setColor(Color.gray);
				g2d.draw(new Rectangle2D.Double(25, (collectedKeys.size() - 1 - invSelectedShown - invScrollOffset) * height + fm.getAscent() - fm.getHeight() / 2 - 1, getWidth() - 50, fm.getHeight()));
			}
		}

	}

	// Run game logic
	public void logic() {

		// Apply movement
		double kx = 0, ky = 0;
		if(keyLeft)
			kx -= properties.movement;
		if(keyRight)
			kx += properties.movement;
		if(keyUp)
			ky -= properties.movement;
		if(keyDown)
			ky += properties.movement;
		vx += kx;
		vy += ky;

		// Apply gravity
		vx += Math.cos(properties.ang) * properties.str;
		vy += Math.sin(properties.ang) * properties.str;

		double vel = Math.sqrt(vx * vx + vy * vy);

		// Apply linear friction
		if(vel > 0.005) {
			double nx = vx / vel;
			double ny = vy / vel;
			vx -= nx * vel * properties.friction;
			vy -= ny * vel * properties.friction;
		}

		// Apply angular friction
		if(Math.abs(vr) > 0.005) {
			vr -= vr * properties.friction;
		}

		// Start collision
		double timeLeft = 1;

		// Run collision with iterations
		for(int i = 0; i < properties.iterations; ++i) {

			Collision minC = null;
			double coeffRes = 0;

			// If collision response should be applied immediately
			boolean instantlyCollided = false;

			// For every object
			for(WorldObject obj : world.objs) {
				if(obj instanceof ObjOrb) {
					ObjOrb orb = (ObjOrb) obj;
					Collision t = timeOfCollisionCirc(x, y, vx * timeLeft, vy * timeLeft, orb.x, orb.y, ObjStart.RADIUS + ObjOrb.RADIUS);
					if(t != null) {
						if(orb.unlock.length() != 0) {
							// Not colour setting orb, unlock walls
							if(orb.color == currColor && !orb.used) {
								for(WorldObject o : world.objs) {
									if(o instanceof ObjWall) {
										ObjWall wall = (ObjWall) o;
										if(orb.unlock.equals(wall.unlockName)) {
											wall.unlocked = true;
										}
									}
								}
								collectedKeys.add(new UnlockedEntries(orb.color, orb.unlock));
								++invScrollOffset;
								orb.used = true;
							}
						} else {
							// Colour setting orb, set colour
							currColor = orb.color;
						}
					}
				} else if(obj instanceof ObjGoal) {
					ObjGoal goal = (ObjGoal) obj;
					Collision t = timeOfCollisionCirc(x, y, vx * timeLeft, vy * timeLeft, goal.x, goal.y, ObjStart.RADIUS + ObjGoal.RADIUS);
					if(t != null) {
						if(goal.gotoLabel.length() != 0) {
							// A teleporting goal
							for(WorldObject o : world.objs) {
								if(o instanceof ObjStart) {
									ObjStart start = (ObjStart) o;
									if(goal.gotoLabel.equals(start.label)) {
										x = start.x;
										y = start.y;
										if(!goal.preserveColour) {
											currColor = start.color;
										}
										break;
									}
								}
							}
						} else {
							// A game-win orb
							new Thread(() -> {
								JOptionPane.showMessageDialog(null, "You won!", "Congratulations!", JOptionPane.INFORMATION_MESSAGE);
							}).start();
							enterEdit();
							return;
						}
					}
				} else if(obj instanceof ObjWall) {
					ObjWall wall = (ObjWall) obj;
					if(wall.color == currColor && wall.unlockName.length() == 0 || wall.unlocked)
						continue;
					Collision t = timeOfCollision(x, y, vx * timeLeft, vy * timeLeft, wall.x1, wall.y1, wall.x2, wall.y2, ObjStart.RADIUS + ObjWall.THICKNESS / 2);
					if(t != null) {
						if(t.time == 0) {
							// Already inside the wall, needs to be immediately collided with
							instantlyCollided = true;
							reactTo(t, wall.coeffRes);
						} else if(!instantlyCollided && (minC == null || (t.rx - x) * (t.rx - y) + (t.ry - y) * (t.ry - y) < (minC.rx - x) * (minC.rx - x) + (minC.ry - y) * (minC.ry - y))) {
							// Not inside the wall yet, can still move to right next to the wall
							minC = t;
							coeffRes = wall.coeffRes;
						}
					}
				}
			}

			// Already collided, or can't collide
			if(minC == null || instantlyCollided) {
				continue;
			}

			// Apply collision response
			timeLeft -= minC.time;
			reactTo(minC, coeffRes);
		}

		// Apply velocity
		x += vx;
		y += vy;
		r += vr;
	}

	// Collision response
	public void reactTo(Collision c, double coeffRes) {
		double dot = c.px * vx + c.py * vy;
		double cross = c.px * vy - c.py * vx;
		// Place to response location
		x = c.rx;
		y = c.ry;
		// Remove perpendicular velocity
		vx = dot * c.px;
		vy = dot * c.py;
		// Compute angular velocity
		vr = -Math.sqrt(vx * vx + vy * vy) * Math.signum(dot);
		// Add perpendicular velocity back in, depending on the coefficient of restitution
		vx += cross * c.py * coeffRes;
		vy += -cross * c.px * coeffRes;
	}

	// Determine time circle-circle sweep collision
	public Collision timeOfCollisionCirc(double x0, double y0, double vx, double vy, double x, double y, double dist) {
		double a = vx * vx + vy * vy;
		double b = 2 * ((x0 - x) * vx + (y0 - y) * vy);
		double c = (x0 - x) * (x0 - x) + (y0 - y) * (y0 - y) - dist * dist;
		double acc = b * b - 4 * a * c;
		if(acc < 0)
			return null;
		double sqrt = Math.sqrt(acc);
		// Use quadratic formula to determine minimum time of intersection
		double rt1 = (-b - sqrt) / 2.0 / a;
		if(c < 0) {
			// Circles are already intersecting, bounce back to closest non-intersecting point
			double dd = Math.sqrt(c + dist * dist);
			if(dd < 0.00001) {
				return null;
			}
			return new Collision(0, -(y0 - y), x0 - x, x + (x0 - x) / dd * dist, y + (y0 - y) / dd * dist).normalize();
		}
		if(0 <= rt1 && rt1 <= 1) {
			// Circles are not yet intersecting, return the time when it would collide
			return new Collision(rt1, -(y0 + vy * rt1 - y), x0 + vx * rt1 - x, x0 + vx * rt1, y0 + vy * rt1).normalize();
		}
		return null;
	}

	// Line-line intersection
	public Collision intersectLineSegment(double x0, double y0, double vx, double vy, double x1, double y1, double dx, double dy) {
		double cross = vx * dy - vy * dx;
		double t1 = (vx * (y0 - y1) - vy * (x0 - x1)) / cross;
		double t2 = (dx * (y0 - y1) - dy * (x0 - x1)) / cross;
		// Is the intersection point on both line segments?
		if(cross > 0 && 0 <= t1 && t1 <= 1 && 0 <= t2 && t2 <= 1) {
			return new Collision(t2, -dx, -dy, x0 + vx * t2, y0 + vy * t2).normalize();
		}
		return null;
	}

	// Determine line-circle sweep collision
	public Collision timeOfCollision(double x0, double y0, double vx, double vy, double x1, double y1, double x2, double y2, double dist) {
		double dx = x2 - x1;
		double dy = y2 - y1;
		double ld = Math.sqrt(dx * dx + dy * dy);

		// Line too small, treat as circle
		if(ld <= 0.01) {
			return timeOfCollisionCirc(x0, y0, vx, vy, x1, y1, dist);
		}

		double scross = Math.signum((x1 - x0) * dy - (y1 - y0) * dx);
		double ndx = -dx / ld;
		double ndy = -dy / ld;
		double dot = (x1 - x0) * ndx + (y1 - y0) * ndy;

		// Already inside the line, return the closest point to rebound to
		if(0 <= dot && dot <= ld) {
			double dd = (x1 - x0) * (x1 - x0) + (y1 - y0) * (y1 - y0) - dot * dot;
			if(dd < dist * dist) {
				return new Collision(0, scross * ndx, scross * ndy, x1 - ndx * dot + scross * ndy * (dist + 0.1), y1 - ndy * dot - scross * ndx * (dist + 0.1));
			}
		}

		double xi1 = x1 - dy / ld * dist;
		double yi1 = y1 + dx / ld * dist;
		double xi2 = x2 + dy / ld * dist;
		double yi2 = y2 - dx / ld * dist;

		// Try intersecting both end points, and both sides of the line.
		Collision i1 = intersectLineSegment(x0, y0, vx, vy, xi1, yi1, dx, dy);
		Collision i2 = intersectLineSegment(x0, y0, vx, vy, xi2, yi2, -dx, -dy);
		Collision c1 = timeOfCollisionCirc(x0, y0, vx, vy, x1, y1, dist);
		Collision c2 = timeOfCollisionCirc(x0, y0, vx, vy, x2, y2, dist);

		Collision time = new Collision(Double.POSITIVE_INFINITY, 0, 0, 0, 0);

		// Find the minimum time of collision
		if(i1 != null && i1.time < time.time) {
			time = i1;
		}
		if(i2 != null && i2.time < time.time) {
			time = i2;
		}
		if(c1 != null && c1.time < time.time) {
			time = c1;
		}
		if(c2 != null && c2.time < time.time) {
			time = c2;
		}

		// If not found, no collision
		if(time.time == Double.POSITIVE_INFINITY) {
			return null;
		}

		// Found, return collision properties
		return time.normalize();
	}

	// Push undo action onto stack
	public void pushUndo() {
		undoStack.push(world.duplicate());
		redoStack.clear();
	}

	// Undo operation
	public void undo() {
		if(play)
			return;
		if(!undoStack.isEmpty()) {
			redoStack.push(world);
			world = undoStack.pop();
			hlobj = null;
		}
	}

	// Redo operation
	public void redo() {
		if(play)
			return;
		if(!redoStack.isEmpty()) {
			undoStack.push(world);
			world = redoStack.pop();
			hlobj = null;
		}
	}

	// Convert screen to world coordinates
	public double toWorldX(double x) {
		return (x - getWidth() / 2) / scale - offsetX;
	}

	// Convert screen to world coordinates
	public double toWorldY(double y) {
		return (y - getHeight() / 2) / scale - offsetY;
	}

	// Snap to grid
	public int snap(double v) {
		return (int) Math.round(v / GRID_SIZE) * GRID_SIZE;
	}

	public void actionPerformed(ActionEvent e) {
		repaint();
	}

	public void mousePressed(MouseEvent e) {
		requestFocus();
		if(e.getButton() == MouseEvent.BUTTON1) {
			double worldX = snap(toWorldX(e.getX()));
			double worldY = snap(toWorldY(e.getY()));
			if(editMode > 0) {
				// Not selecting
				Class clazz = classes[editMode];
				ObjProperty property = (ObjProperty) clazz.getAnnotation(ObjProperty.class);
				if(property.type() == ObjProperty.Type.POINT) {
					// The object to add is a point, add immediately
					pushUndo();
					WorldObject obj = ((WorldObject) defaultObject[editMode]).duplicate();
					try {
						// Set x and y
						obj.getClass().getDeclaredField("x").set(obj, worldX);
						obj.getClass().getDeclaredField("y").set(obj, worldY);
						world.objs.add(obj);
					} catch(Exception ex) {
						ex.printStackTrace();
					}
				} else if(property.type() == ObjProperty.Type.LINE) {
					// The object to add is a line, add later
					isDrawing = true;
					creating = ((WorldObject) defaultObject[editMode]).duplicate();
					try {
						// Set x1 y1 x2 y2
						creating.getClass().getDeclaredField("x1").set(creating, worldX);
						creating.getClass().getDeclaredField("y1").set(creating, worldY);
						creating.getClass().getDeclaredField("x2").set(creating, worldX);
						creating.getClass().getDeclaredField("y2").set(creating, worldY);
					} catch(Exception ex) {
						ex.printStackTrace();
					}
				}
			} else if(editMode == 0) {
				// Selecting
				isDragging = true;
				dragged = false;
				dragStartX = worldX;
				dragStartY = worldY;
			}
		}
		if(e.getButton() == MouseEvent.BUTTON2) {
			// Middle click, pan around
			isPanning = true;
			dragged = false;
			panStartX = e.getX();
			panStartY = e.getY();
		}
	}

	public void mouseReleased(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON1) {
			if(isDrawing) {
				// Drawing line, add line to world
				pushUndo();
				isDrawing = false;
				world.objs.add(creating);
				creating = null;
			}
			if(isDragging) {
				isDragging = false;
			}
		} else if(e.getButton() == MouseEvent.BUTTON2) {
			// Stop panning
			isPanning = false;
		}
	}

	public void mouseDragged(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
		double worldX = snap(toWorldX(mouseX));
		double worldY = snap(toWorldY(mouseY));
		if(isPanning) {
			// Pan around
			targetOffsetX += (mouseX - panStartX) / scale;
			targetOffsetY += (mouseY - panStartY) / scale;
			panStartX = mouseX;
			panStartY = mouseY;
		}
		if(isDragging) {
			if(hlobj != null) {
				// Dragging
				if(worldX != dragStartX || worldY != dragStartY) {
					if(!dragged) {
						pushUndo();
						dragged = true;
					}
					hlobj.applyDelta(worldX - dragStartX, worldY - dragStartY);
					dragStartX = worldX;
					dragStartY = worldY;
				}
			}
		}
		if(creating != null) {
			// Creating a line
			try {
				creating.getClass().getDeclaredField("x2").set(creating, snap(toWorldX(mouseX)));
				creating.getClass().getDeclaredField("y2").set(creating, snap(toWorldY(mouseY)));
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public void mouseMoved(MouseEvent e) {
		// Move objects around while dragging
		mouseX = e.getX();
		mouseY = e.getY();
		hlobj = null;
		WorldObject obj = getUnder(world.objs, toWorldX(mouseX), toWorldY(mouseY));
		hlobj = obj;
	}

	// Find which item is under the mouse cursor
	public WorldObject getUnder(ArrayList<WorldObject> objs, double x, double y) {
		for(int i = objs.size() - 1; i >= 0; --i) {
			WorldObject obj = objs.get(i);
			if(obj.test(x, y)) {
				return obj;
			}
		}
		return null;
	}

	// Zoom
	public void mouseWheelMoved(MouseWheelEvent e) {
		double dScale = Math.pow(0.9, e.getPreciseWheelRotation());
		wheelMoveX = e.getX() - getWidth() / 2;
		wheelMoveY = e.getY() - getHeight() / 2;
		targetScale *= dScale;
		if(targetScale < 0.05) {
			targetScale = 0.05;
		} else if(targetScale > 4) {
			targetScale = 4;
		}
	}

	// Add object to world
	public void insertObj(int ind, WorldObject obj) {
		if(ind == world.objs.size()) {
			world.objs.add(obj);
		} else {
			world.objs.add(ind, obj);
		}
	}

	public void keyPressed(KeyEvent e) {
		switch(e.getKeyCode()) {
			case KeyEvent.VK_ESCAPE:
				isDrawing = false;
				break;
			case KeyEvent.VK_DELETE:
				// Delete object
				if(hlobj != null) {
					pushUndo();
					world.objs.remove(hlobj);
					hlobj = null;
				}
				break;
			case KeyEvent.VK_PAGE_UP:
				// Bring up
				if(hlobj != null) {
					pushUndo();
					int ind = world.objs.indexOf(hlobj);
					world.objs.remove(ind);
					ind = Math.min(world.objs.size(), ind + 1);
					insertObj(ind, hlobj);
				}
				break;
			case KeyEvent.VK_PAGE_DOWN:
				// Send down
				if(hlobj != null) {
					pushUndo();
					int ind = world.objs.indexOf(hlobj);
					world.objs.remove(ind);
					ind = Math.max(0, ind - 1);
					insertObj(ind, hlobj);
				}
				break;
			case KeyEvent.VK_HOME:
				// Bring to front
				if(hlobj != null) {
					pushUndo();
					world.objs.remove(hlobj);
					world.objs.add(hlobj);
				}
				break;
			case KeyEvent.VK_END:
				// Send to back
				if(hlobj != null) {
					pushUndo();
					world.objs.remove(hlobj);
					insertObj(0, hlobj);
				}
				break;
			case KeyEvent.VK_I:
				// Show inventory
				showInventory = !showInventory;
				break;
			case KeyEvent.VK_J:
			case KeyEvent.VK_DOWN:
				// Scroll around in inventory
				if(collectedKeys.size() != 0) {
					if(invSelected == -1) {
						invSelected = collectedKeys.size() - 1;
					} else {
						invSelected = Math.max(invSelected - 1, 0);
					}
				}
				break;
			case KeyEvent.VK_K:
			case KeyEvent.VK_UP:
				// Scroll around in inventory
				if(collectedKeys.size() != 0) {
					if(invSelected == -1) {
						invSelected = collectedKeys.size() - 1;
					} else {
						invSelected = Math.min(invSelected + 1, collectedKeys.size() - 1);
					}
				}
				break;
		}
		// Movement
		switch(e.getKeyCode()) {
			case KeyEvent.VK_W:
				keyUp = true;
				break;
			case KeyEvent.VK_A:
				keyLeft = true;
				break;
			case KeyEvent.VK_S:
				keyDown = true;
				break;
			case KeyEvent.VK_D:
				keyRight = true;
				break;
		}
	}
	public void keyReleased(KeyEvent e) {
		// Movement
		switch(e.getKeyCode()) {
			case KeyEvent.VK_W:
				keyUp = false;
				break;
			case KeyEvent.VK_A:
				keyLeft = false;
				break;
			case KeyEvent.VK_S:
				keyDown = false;
				break;
			case KeyEvent.VK_D:
				keyRight = false;
				break;
		}
	}

	public void keyTyped(KeyEvent e) { }
	public void mouseClicked(MouseEvent mouseEvent) { }
	public void mouseEntered(MouseEvent mouseEvent) { }
	public void mouseExited(MouseEvent mouseEvent) { }
}
