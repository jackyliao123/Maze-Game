// Jacky Liao
// December 12, 2017
// Maze Game
// ICS4U Ms.Strelkovska

// A class to represent a collision
public class Collision {

	// Time of collision
	public double time;

	// Vector parallel to collision surface
	public double px;
	public double py;

	// New location after collision
	public double rx;
	public double ry;

	public Collision(double time, double px, double py, double rx, double ry) {
		this.time = time;
		this.px = px;
		this.py = py;
		this.rx = rx;
		this.ry = ry;
	}

	// Normalize the parallel vector
	public Collision normalize() {
		double len = Math.sqrt(px * px + py * py);
		px /= len;
		py /= len;
		return this;
	}
}
