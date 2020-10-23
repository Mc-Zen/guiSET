package guiSET.core;


/**
 * Mostly a copy of processings color stuff. Some time ago we needed this to
 * process colors internally but we could also use PApplets static color methods
 * now. Convenience keeps this thing around and also some basic colors are defined here. 
 * 
 * @author Mc-Zen
 *
 */
public class GuisetColor {

	public static final int BLACK = -16777216;
	public static final int WHITE = -1;
	public static final int TRANSPARENT = 0;
	public static final int SELECTION_BLUE = -13395457;
	public static final int TEXT_CURSOR_COLOR = create(70);
	

	
	
	public static final int create(int gray, int alpha) {
		if (alpha > 255)
			alpha = 255;
		else if (alpha < 0)
			alpha = 0;
		if (gray > 255) {
			// then assume this is actually a #FF8800
			return (alpha << 24) | (gray & 0xFFFFFF);
		} else {
			// if (gray > 255) gray = 255; else if (gray < 0) gray = 0;
			return (alpha << 24) | (gray << 16) | (gray << 8) | gray;
		}
	}

	public static final int create(int r, int g, int b, int alpha) {
		if (alpha > 255)
			alpha = 255;
		else if (alpha < 0)
			alpha = 0;
		if (r > 255)
			r = 255;
		else if (r < 0)
			r = 0;
		if (g > 255)
			g = 255;
		else if (g < 0)
			g = 0;
		if (b > 255)
			b = 255;
		else if (b < 0)
			b = 0;

		return (alpha << 24) | (r << 16) | (g << 8) | b;
	}

	public static final int create(int gray) {
		return create(gray, 255);
	}

	public static final int create(float fgray) {
		return ((int) fgray);
	}

	public static final int create(float gray, float alpha) {
		return create((int) gray, (int) alpha);
	}

	public static final int create(int r, int g, int b) {
		return create(r, g, b, 255);
	}

	public static final int create(float r, float g, float b) {
		return create((int) r, (int) g, (int) b, 255);
	}

	public static final int create(float r, float g, float b, float alpha) {
		return create((int) r, (int) g, (int) b, (int) alpha);
	}
}

