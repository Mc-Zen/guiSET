package guiSET.classes;

/**
 * A class for storing absolute position values. Stores x start and end as well
 * as y start and end values. Used by the library to figure out where which
 * elements are placed and so on.
 */
public class Bounds {
	public int X0;
	public int X;
	public int Y;
	public int Y0;

	public Bounds(int X0, int Y0, int X, int Y) {
		this.X0 = X0;
		this.Y0 = Y0;
		this.X = X;
		this.Y = Y;
	}

	public void print() {
		System.out.println(X0 + " " + Y0 + " " + X + " " + Y);
	}

	public boolean isWithin(int x, int y) {
		return (x > X0 && x < X && y > Y0 && y < Y);
	}
}
