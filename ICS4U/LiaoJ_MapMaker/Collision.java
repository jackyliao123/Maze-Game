// Jacky Liao
// December 4, 2017
// Map maker
// ICS4U Ms.Strelkovska

public class Collision {
	public double time;
	public double px;
	public double py;
	public double rx;
	public double ry;

	public Collision(double time, double px, double py, double rx, double ry) {
		this.time = time;
		this.px = px;
		this.py = py;
		this.rx = rx;
		this.ry = ry;
	}

	public Collision normalize() {
		double len = Math.sqrt(px * px + py * py);
		px /= len;
		py /= len;
		return this;
	}
}
