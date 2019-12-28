package pGUI.classes;

/*
 * Not much to say - just a class for storing absolute position values. 
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

	void print() {
		System.out.println(X0 + " " + Y0 + " " + X + " " + Y);
	}
}
