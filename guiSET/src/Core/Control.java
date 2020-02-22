package guiSET.core;

import processing.core.*;
import processing.event.*;
import java.lang.reflect.Method;

import guiSET.classes.*;

import java.lang.reflect.InvocationTargetException;
import java.awt.Font;

/**
 * (Abstract) Base class for all other visual components.
 * 
 * It provides all basic style attributes and methods. It also comes with the
 * essential event handling and some rendering methods.
 * 
 */


public abstract class Control {

	/**
	 * Name for the element can be specified manually, useful for distinguishing.
	 * 
	 * This property has no effect on the looks.
	 */
	public String name = "";


	/**
	 * Parent container of this element. The Frame itself is the master container
	 * and every element that should be visible has to be a child of Frame in some
	 * nested way.
	 */
	protected Control parent;



	/**
	 * Image object that always contains the updated looks of this element. When
	 * parents are rendered they project the PGraphics of their children onto
	 * themselves. In this way only the changed parts have to be re-rendered and not
	 * the entire gui.
	 */
	protected PGraphics pg;




	/*
	 * dirty will be set to true when graphics of this control changed, so parents
	 * know if they need to re-render this object.
	 * 
	 * This will happen when update() has been called for this Control (update()
	 * also calls update() for all parent containers)
	 * 
	 * Don't set this property, only call update() in your classes !
	 */

	protected boolean dirty = true;


	/*
	 * type of control class, i.e. all containers are marked as such (needed for
	 * quick render decisions without casting)
	 */

	protected int cType = DEFAULT;
	protected static final int DEFAULT = 0;
	protected static final int CONTAINER = 1;


	/*
	 * Status properties
	 */
	protected boolean focusable = true; 		// determines if this control can be focused at all
	protected boolean focused = false; 			// Dont'change focused from outside
	protected boolean stickyFocus = false; 		// when true than its focused state can't be overridden by other elements
											 		// requesting focus, only when itself calls blur

	protected boolean enabled = true; 			// setting this to false prevents it from getting any mouse and key events
	protected boolean visible = true;


	/*
	 * Coordinates and size relative to container (won't be changed by
	 * flowContainer!).
	 */
	protected int x;
	protected int y;

	/*
	 * z-index of component, only has an effect when component is in a
	 * panelcontainer (not a flow- or scroll container)
	 */
	protected int z;

	protected int width;
	protected int height;

	protected int minWidth = 1;
	protected int maxWidth = 100000;
	protected int minHeight = 1;
	protected int maxHeight = 100000;

	protected int marginLeft;
	protected int marginTop;
	protected int marginRight;
	protected int marginBottom;

	protected int paddingLeft;
	protected int paddingRight;
	protected int paddingTop;
	protected int paddingBottom;


	/*
	 * EDIT: new mouse event listening does not use bounds anymore
	 * 
	 * Bounds are the key to the mouse event listening process. They describe the
	 * objects position and size relative to the window origin.
	 * 
	 * Containers set the bounds of their children after rendering. Don't change
	 * them manually!
	 */

	protected Bounds bounds = new Bounds(0, 0, 0, 0);


	/*
	 * Visuals
	 */
	protected String text = ""; 			// multi-purpose text to display, e.g. button text, label text, textbox content
	protected float fontSize = 13;
	protected int textAlign = 3; 			// LEFT, CENTER, RIGHT (= 37, 3, 39) from PConstants
	protected int textAlignY = 3; 			// vertical text align (TOP, CENTER, BOTTOM) (= 101, 3, 102)

	protected PImage image;
	protected int imageMode = FILL;


	/*
	 * IMAGE MODES:
	 */

	/**
	 * Image mode: fill Component with given image and if necessary, distort the
	 * image
	 */
	public static final int FILL = 1;

	/**
	 * Image mode: enlarge image so it fills out entire object centered, parts of
	 * the image might be hidden; fits the larger side
	 */
	public static final int FIT = 2;

	/**
	 * Image mode: place image centered in the object without cropping it, so it
	 * fits the smaller side
	 */
	public static final int FIT_INSIDE = 3;

	protected int backgroundColor;
	protected int foregroundColor;
	protected int borderColor;
	protected int hoverColor; 			// first set automatically with backgroundColor, can be changed programatically
	protected int pressedColor; 		// first set automatically with backgroundColor, can be changed programatically

	/*
	 * visual background color is the actually displayed bg-color, while
	 * backgroundColor is only the color in normal state (neither hovered on or
	 * pressed). When entering/pressing the control visualBackgroundColor is set to
	 * hoverColor/pressedColor and back to backgroundColor when exiting/releasing
	 * the control
	 */
	protected int visualBackgroundColor;

	protected int borderWidth;
	protected int borderRadius;

	protected int cursor;				// type of cursor to display when hovering over this control

	protected float opacity = 1; 		// opacity


	/*
	 * Events
	 */

	// previous hovered/pressed state
	protected boolean pHovered = false;
	protected boolean pPressed = false;




	/*
	 * if true then shortcuts registered to frame won't be handled if this element
	 * is focused. I.e. useful for textboxes (ctrl-c, x etc.)
	 */

	protected boolean overridesFrameShortcuts = false;






	public Control() {

		height = 50;
		width = 50;

		backgroundColor = -1;			// color(255)
		visualBackgroundColor = 255;
		foregroundColor = -16777216; 	// color(0)
		borderColor = 20;
		hoverColor = -1;				// color(255)
		pressedColor = -1;				// color(255)

		setupListeners(0);
	}







	protected void initialize() {

	}




	/*
	 * DRAWING AND RENDERING
	 */

	/*
	 * This method has to be implemented by all container classes. Containers have
	 * to set their childrens bounds in respect to their own.
	 * 
	 * Bounds are always relative to the window origin If a child is a container
	 * itself (child.cType == CONTAINER) its calcBounds()-method has to be called
	 * too.
	 */

	protected void calcBounds() {
	};

	/*
	 * Main drawing method that determines the looks of the object.
	 * 
	 * It has to be implemented for each control individually. Just start with
	 * something like:
	 * 
	 * pg.rect(...); pg.line(...) ...
	 * 
	 * , using the standard drawing funcions. It is not necessary to create the
	 * PGraphics object nor to call beginDraw() or endDraw() as you are maybe used
	 * to.
	 * 
	 * You can call the drawDefaultBackgrond() and drawDefaultText() methods which
	 * do the standard drawing of text and background while paying attention to
	 * attributes like backgroundColor, borderColor, borderWidth, image, imageMode,
	 * ..., textAlign, fontColor, fontSize.
	 * 
	 */

	protected abstract void render();

	protected final void containerRenderItem(Control item, int x, int y) {
		// check visiblity in render(), because some containers need to check it in
		// render() anyway


		// set new mousevent style bounds
		/*
		 * if (!(item.y0 > 0 || item.y0 + item.height < bounds.Y0)) { item.x0 = x;
		 * item.y0 = y; }else
		 */ {
		}
		if (item.opacity == 0) {

			// make this thing inaccessible to mouse events
			item.relativeX = width;
			item.relativeY = height;
			return;
		}

		item.relativeX = x;
		item.relativeY = y;


		if (item.dirty) {
			item.dirty = false; // do this before render, so it can use animations by calling update again

			item.preRender();
			item.render();
			item.pg.endDraw();

//			Frame.renderCount++;
//			Frame.renderedObjects += item.getClass();
		}



		// apply opacity (tinting carries out quite a lot of calculations in PGraphics,
		// while noTint() doesn't)
		if (item.opacity < 1) {
			pg.tint(255, (int) (item.opacity * 256));
		} else {
			pg.tint = false;
		}

		pg.image(item.getGraphics(), x, y);
	}

	/*
	 * Dimensions (width, height) from previous frame. Used to check if size
	 * changed. If so the PGraphics needs to be created new
	 */

	protected int pWidth, pHeight = -1;

	/*
	 * Method to executed by the parent container before calling render(). It
	 * prepares the PGraphics for rendering
	 */

	protected final void preRender() {
		// only create graphics when size changed
		if (width != pWidth || height != pHeight) {
			pg = Frame.frame0.papplet.createGraphics(width, height);
			pWidth = width;
			pHeight = height;
			pg.beginDraw();
		} else {
			pg.beginDraw();
			pg.clear();
		}
	}

	// just return the looks of this control, without drawing
	protected PImage getGraphics() {
		return pg;
	}


	/*
	 * Call parent to update and set flag that this control has changed its looks.
	 * In the next frame elements that have changed get the chance to redraw
	 * themselves. (normally the render() method is not called every frame)
	 */

	/**
	 * Method that is called when properties of a Component that influence the
	 * appearance change, i.e. called in most setters.
	 */
	protected void update() {
		if (parent != null) {
			dirty = true;
			parent.update();
		}
	}



	/*
	 * Called by containers when they add this object to themselves
	 */
	protected void addedToParent() {

	}

	/*
	 * Called by animations after a change Some components might need to adjust
	 * stuff then as no setter is called.
	 */
	protected void animated() {
		update();
	}

	/**
	 * Force a re-render of this Component. This shouldn't be needed, but just in
	 * case
	 */
	public void forceRender() {
		update();
	}







	/**
	 * Request the Frame Component to focus this object. The Frame decides upon the
	 * demand and can set this Components focused-state to true. There can only be
	 * one focused element at a time and it is stored in the Frames property
	 * 
	 * @see Frame#focusedElement focusedElement.
	 */

	public void focus() {
		Frame.frame0.requestFocus(this);
	}


	/**
	 * Request the Frame Component to blur this object(set focus to false).
	 * 
	 * @see Frame#focusedElement focusedElement.
	 */
	public void blur() {
		Frame.frame0.requestBlur(this);
	}

	// just a useful debugging function
	protected void drawBounds() {
		Frame.frame0.papplet.rect(bounds.X0, bounds.Y0, bounds.X - bounds.X0, bounds.Y - bounds.Y0);
	}





	/**
	 * Standard text drawing method accounting padding, align, color etc. This
	 * method can be used by any control for drawing its text.
	 */

	protected void drawDefaultText() {
		pg.fill(foregroundColor);
		pg.textSize(fontSize);
		pg.textAlign(textAlign, textAlignY);
		int x = 0;
		int y = 0;

		switch (textAlign) {
		case 37:
			x = paddingLeft;
			break;
		case 3:
			x = (width - paddingRight - paddingLeft) / 2 + paddingLeft;
			break;
		case 39:
			x = width - paddingRight;
			break;
		}
		switch (textAlignY) {
		case 101:
			y = (int) (paddingTop - 0.2 * fontSize);
			break;
		case 3:
			y = (int) (height / 2 - 0.07 * fontSize); // somehow alignY center by processing is not quite exact in middle of fontSize
			break;
		case 102:
			y = height - paddingBottom;
			break;
		}
		pg.text(text.toCharArray(), 0, text.length(), x, y);
	}



	/**
	 * Standard background drawing method - this method can be used by any control
	 * to draw its background. Features backgroundColors, borders, images and
	 * transparency.
	 */
	protected void drawDefaultBackground() {
		if (image == null) {

			if (visualBackgroundColor != 0) {
				pg.fill(visualBackgroundColor);
			} else {
				pg.noFill();
			}
			if (borderWidth > 0) {
				pg.strokeWeight(borderWidth);
				pg.stroke(borderColor);
			} else {
				pg.noStroke();
			}
			pg.rect(borderWidth / 2, borderWidth / 2, width - borderWidth, height - borderWidth, borderRadius);

		} else {

			// All this stuff does not work :( now using mask() instead
			/*
			 * PShape pg, globe; pg = pg.createShape(PApplet.RECT, 0, 0, width, height,
			 * borderRadius); pg.setTexture(image);
			 * 
			 * globe = pg.createShape(PApplet.ELLIPSE, width/2, height/2, width, height); //
			 * Automatically texture the shape with the image globe.setTexture(image);
			 */
			/*
			 * s = pg.createShape(PApplet.RECT, 0, 0, width, height);
			 * s.beginShape(PApplet.TRIANGLE_STRIP); s.vertex(30, 75); s.vertex(40, 20);
			 * s.vertex(50, 75); s.vertex(60, 20); s.vertex(70, 75); s.vertex(80, 20);
			 * s.vertex(90, 75); s.endShape();
			 */
			// a.setTexture(image);
			// pg.setTexture(image);
			/*
			 * pg.beginShape(); // vertex(x1+tl, y1); //pg.fill(30); //pg.texture(image);
			 * //pg.fill(34); if (borderRadius != 0) { pg.vertex(width-borderRadius, 0);
			 * pg.quadraticVertex(width, 0, width, 0+borderRadius); pg.vertex(width,
			 * height-borderRadius); pg.quadraticVertex(width, height, width-borderRadius,
			 * height); pg.vertex(0+borderRadius, height); pg.quadraticVertex(0, height, 0,
			 * height-borderRadius); pg.vertex(0, 0+borderRadius); pg.quadraticVertex(0,0,
			 * 0+borderRadius, 0); } else { pg.vertex(width, 0); pg.vertex(width, height);
			 * pg.vertex(0, height); pg.vertex(0,0); }
			 */

			/*
			 * if (borderRadius != 0) { pg.vertex(width-borderRadius, 0);
			 * pg.quadraticVertex(width, 0, width, 0+borderRadius); } else {
			 * pg.vertex(width, 0); } if (borderRadius != 0) { pg.vertex(width,
			 * height-borderRadius); pg.quadraticVertex(width, height, width-borderRadius,
			 * height); } else { } if (borderRadius != 0) { pg.vertex(0+borderRadius,
			 * height); pg.quadraticVertex(0, height, 0, height-borderRadius); } else { } if
			 * (borderRadius != 0) { pg.vertex(0, 0+borderRadius); pg.quadraticVertex(0,0,
			 * 0+borderRadius, 0); } else { }
			 */
//		    endShape();
			// pg.endShape(PApplet.CLOSE);
			// pg.shape(pg);



			if (imageMode == FILL) {
				pg.image(image, 0, 0, width, height);
			}

			else if (imageMode == FIT) {

				// mode FIT fills the entire background (without distortion) but without leaving
				// any blank parts
				if (image.width / image.height < width / height) {
					int newHeight = (int) (image.height / (float) image.width * width);
					pg.image(image, 0, -(newHeight - height) / 2, width, newHeight);
				} else {
					int newWidth = (int) (image.width / (float) image.height * height);
					pg.image(image, -(newWidth - width) / 2, 0, newWidth, height);
				}

			} else if (imageMode == FIT_INSIDE) {

				// mode FITINSIDE makes sure the entire image is visible without distortion;
				// usually results in blank parts
				if (image.width / image.height > width / height) {
					int newHeight = (int) (image.height / (float) image.width * width);
					pg.image(image, 0, -(newHeight - height) / 2, width, newHeight);
				} else {
					int newWidth = (int) (image.width / (float) image.height * height);
					pg.image(image, -(newWidth - width) / 2, 0, newWidth, height);
				}

			}

			// is there is a border radius we need to mask the image
			if (borderRadius > 0) {
				PGraphics maskImage = Frame.frame0.papplet.createGraphics(width, height);
				maskImage.beginDraw();
				maskImage.noStroke();
				maskImage.rect(borderWidth / 2, borderWidth / 2, width - borderWidth, height - borderWidth, borderRadius);
				maskImage.endDraw();
				pg.mask(maskImage);
			}
			// still draw border

			if (borderWidth > 0) {
				pg.noFill();
				pg.strokeWeight(borderWidth);
				pg.stroke(borderColor);
				pg.rect(borderWidth / 2, borderWidth / 2, width - borderWidth, height - borderWidth, borderRadius);

			}
		}
	}


	/**
	 * Standard disabled-state drawing feature. If called at end of render() it will
	 * draw a transparent grey rectangel upon the control to indicate its diabled
	 * state.
	 */
	protected void standardDisabled() {
		if (!enabled) {
			pg.fill(200, 100);
			pg.noStroke();
			pg.rect(borderWidth / 2, borderWidth / 2, width - borderWidth, height - borderWidth, borderRadius);
		}
	}


	/**
	 * A 1x1 dummy graphics is used for getting textwidth etc. without needing to
	 * create the pgraphics before for each control method for getting width of text
	 * making autoFit calculations easier and more independant.
	 * 
	 * It is intialized when a Frame is created first time and only then ready.
	 */
	private static PGraphics textInfo_graphics;
	private static PFont pfont;

	// called by Frame at constructor
	protected static void init_textinfo_graphics() {

		textInfo_graphics = Frame.frame0.papplet.createGraphics(1, 1);
		textInfo_graphics.beginDraw();
		textInfo_graphics.textSize(12);
		// System.out.println(textInfo_graphics.textFont);
		// textInfo_graphics.textFont = createDefaultFont(12f);
		// textInfo_graphics.endDraw();

		pfont = createDefaultFont(13);
	}

	// copied from PGraphics - need this here but cant access
	private static PFont createDefaultFont(float size) {
		Font baseFont = new Font("Lucida Sans", Font.PLAIN, 1);
		return createFont(baseFont, size, true, null, false);
	}

	// copied from PGraphics - need this here but cant access
	private static PFont createFont(Font baseFont, float size, boolean smooth, char[] charset, boolean stream) {
		return new PFont(baseFont.deriveFont(size * Frame.frame0.papplet.pixelDensity), smooth, charset, stream, Frame.frame0.papplet.pixelDensity);
	}



	/**
	 * Descent of text below baseline for the set fontsize in pixel.
	 */
	protected float textDescent() {
		if (pfont == null)
			throw new RuntimeException("Frame needs to be intialized before any other guiSET Component");
		return pfont.descent() * fontSize;
	}

	/**
	 * Ascent of text for the set fontsize in pixel.
	 */
	protected float textAscent() {
		if (pfont == null)
			throw new RuntimeException("Frame needs to be intialized before any other guiSET Component");
		return pfont.ascent() * fontSize;
	}

	protected float textLeading() {
		return (textAscent() + textDescent()) * 1.5f;
	}

	/**
	 * Width of text in pixel.
	 */
	protected float textWidth(String text) {
		if (pfont == null)
			throw new RuntimeException("Frame needs to be intialized before any other guiSET Component");


//		char[] buffer = text.toCharArray();
//		float wide = 0.0f;
//		for (int i = 0; i < buffer.length; i++) {
//			wide += pfont.width(buffer[i]) * fontSize;
//		}
//
//
//		System.out.println(wide + " " + (textInfo_graphics.textWidth(text) - wide) + " " + textWidthStr(text) + " " + text);
//		return wide;

		return textWidthStr(text);
	}




	static char[] textWidthBuffer = new char[3];

	// copied from PGraphics
	private float textWidthStr(String str) {
		int length = str.length();
		if (length > textWidthBuffer.length) {
			textWidthBuffer = new char[length + 10];
		}
		str.getChars(0, length, textWidthBuffer, 0);

		float wide = 0;
		int index = 0;
		int start = 0;

		while (index < length) {
			if (textWidthBuffer[index] == '\n') {
				wide = Math.max(wide, textWidthImpl(textWidthBuffer, start, index));
				start = index + 1;
			}
			index++;
		}
		if (start < length) {
			wide = Math.max(wide, textWidthImpl(textWidthBuffer, start, index));
		}
		return wide;
	}

	// copied from PGraphics
	private float textWidthImpl(char buffer[], int start, int stop) {
		float wide = 0;
		for (int i = start; i < stop; i++) {
			// could add kerning here, but it just ain't implemented
			wide += pfont.width(buffer[i]) * fontSize;
		}
		return wide;
	}







	/*
	 * _______________________________________________________________________________________________________________
	 * ANCHORS / AUTOMATIC RESIZING
	 * _______________________________________________________________________________________________________________
	 * 
	 *
	 *
	 * 
	 * The usage of anchors allows to adapt size or keep fixed positions when a
	 * parent container changes in size. I.e. When the RIGHT anchor is set the
	 * control will keep the distance between its right edge and the right edge of
	 * the container (like a right align). When RIGHT and LEFT anchors are set both
	 * right and left edges will keep their distance to the container Bounds which
	 * results in a new size of this control.
	 * 
	 * The anchors-array is set to all MIN_VALUE per default. When an anchor is
	 * added it stores the distance from the top, bottom, left or right edge of this
	 * control to the according edge of the container. When the resize() function is
	 * called the anchors will be checked and applied.
	 *
	 * 
	 * The new size of the control will be constrained by minimal and maximal
	 * width/height obviously. If the new size cannot be fully attained the control
	 * will orient at the top left of the container.
	 * 
	 * 
	 */

	// the anchor array used to store the anchors data.
	// In following order: TOP, RIGHT, BOTTOM, LEFT:
	protected int[] anchors = { Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE };

	public static final int TOP_ANCHOR = 0;
	public static final int RIGHT_ANCHOR = 1;
	public static final int BOTTOM_ANCHOR = 2;
	public static final int LEFT_ANCHOR = 3;

	// used for quickly checking if anchors are set up at all.
	protected boolean usingAnchors = false;


	/**
	 * Add an anchor (types are TOP, RIGHT, BOTTOM, LEFT). Anchors ensure the
	 * Component stays at fixed relative positions when the parent is resized. If
	 * opposite anchors are set, the Component will be resized as well. When this
	 * function is called the Component will remember the CURRENT(!) distances to
	 * the parents bounds. Multiple anchors can be set with this method.
	 * 
	 * @param newAnchors accepts TOP, RIGHT, LEFT or BOTTOM. Pass up to four anchor
	 *                   types (order doesn't matter)
	 */
	public void addAutoAnchor(int... newAnchors) {
		if (parent == null) {
			System.err.println("addAutoAnchor() ignored: element " + this + " has no parent");
		}

		for (int anchor : newAnchors) {
			switch (anchor) {
			case PApplet.TOP:
				anchors[TOP_ANCHOR] = y;
				break;
			case PApplet.RIGHT:
				anchors[RIGHT_ANCHOR] = parent.width - width - x;
				break;
			case PApplet.BOTTOM:
				anchors[BOTTOM_ANCHOR] = parent.height - height - y;
				break;
			case PApplet.LEFT:
				anchors[LEFT_ANCHOR] = x;
				break;
			default:
				return;
			}
		}

		usingAnchors = true;
	}




	/*
	 * Not yet ready for users.
	 * 
	 * resize() needs to be called, so the changes that should be applied so the
	 * anchor is executed correctly need to be updated. On the other hand this needs
	 * the parent to be set. Unlike
	 */

	/**
	 * Set an Anchor of type TOP BOTTOM, LEFT or RIGHT to a certain value. This also
	 * immediately sets the position or size of this element if necessary according
	 * to the anchors. Unlike with {@link #addAutoAnchor(int...)} only one anchor
	 * can be set with one call of this function so you might want to call it
	 * several times.
	 * 
	 * @param anchorType anchor type
	 * @param value      value
	 */
	public void setAnchor(int anchorType, int value) {
		if (parent == null) {
			System.err.println("addAutoAnchor() ignored: element has no parent");
		}

		switch (anchorType) {
		case PApplet.TOP:
			anchors[TOP_ANCHOR] = value;
			usingAnchors = true;

			// set y now, because resize only changes y if BOTTOM is set. (elements are
			// always "anchored" to TOP). This way everything is alright. Still call resize
			// because maybe BOTTOM_ANCHOR is set too.
			this.y = value;
			resize();
			break;
		case PApplet.RIGHT:
			anchors[RIGHT_ANCHOR] = value;
			usingAnchors = true;
			resize();
			break;
		case PApplet.BOTTOM:
			anchors[BOTTOM_ANCHOR] = value;
			usingAnchors = true;
			resize();
			break;
		case PApplet.LEFT:
			anchors[LEFT_ANCHOR] = value;
			usingAnchors = true;

			// set x now, because resize only changes x if RIGHT is set. This way everything
			// is alright
			this.x = value;
			resize();
			break;
		default:
			return;
		}

	}


	/**
	 * Calculate all set anchors new. Needed when position of this item has been
	 * changed through the setter.
	 */
	protected void updateAnchors() {
		if (usingAnchors) {
			if (anchors[TOP_ANCHOR] != Integer.MIN_VALUE) {
				anchors[TOP_ANCHOR] = y;
			}
			if (anchors[RIGHT_ANCHOR] != Integer.MIN_VALUE) {
				anchors[RIGHT_ANCHOR] = parent.width - width - x;
			}
			if (anchors[BOTTOM_ANCHOR] != Integer.MIN_VALUE) {
				anchors[BOTTOM_ANCHOR] = parent.height - height - y;
			}
			if (anchors[LEFT_ANCHOR] != Integer.MIN_VALUE) {
				anchors[LEFT_ANCHOR] = x;
			}

		}
	}

	/**
	 * Remove a set anchor.
	 * 
	 * @param anchorType accepts TOP, RIGHT, LEFT or BOTTOM
	 */
	public void removeAnchor(int anchorType) {
		switch (anchorType) {
		case PApplet.TOP:
			anchors[TOP_ANCHOR] = Integer.MIN_VALUE;
			break;
		case PApplet.RIGHT:
			anchors[RIGHT_ANCHOR] = Integer.MIN_VALUE;
			break;
		case PApplet.BOTTOM:
			anchors[BOTTOM_ANCHOR] = Integer.MIN_VALUE;
			break;
		case PApplet.LEFT:
			anchors[LEFT_ANCHOR] = Integer.MIN_VALUE;
			break;
		}
		if (anchors[TOP_ANCHOR] == Integer.MIN_VALUE && anchors[RIGHT_ANCHOR] == Integer.MIN_VALUE && anchors[BOTTOM_ANCHOR] == Integer.MIN_VALUE
				&& anchors[LEFT_ANCHOR] == Integer.MIN_VALUE) {
			usingAnchors = false;
		}
	}



	/**
	 * Intenal resize Event: if any anchors are set, either size or position of this
	 * control might need adjusting.
	 */
	protected void resize() {
		if (usingAnchors) {
			if (anchors[RIGHT_ANCHOR] != Integer.MIN_VALUE) {
				if (anchors[LEFT_ANCHOR] != Integer.MIN_VALUE) { // also left anchor

					// don't use setter here, so not to call this method over and over
					width = Math.max(Math.min(parent.width - x - anchors[RIGHT_ANCHOR], maxWidth), minWidth);
				} else { // only right anchor
					x = parent.width - width - anchors[RIGHT_ANCHOR];
				}
			}
			if (anchors[BOTTOM_ANCHOR] != Integer.MIN_VALUE) {
				if (anchors[TOP_ANCHOR] != Integer.MIN_VALUE) { // also top anchor

					// don't use setter here, so not to call this method over and over
					height = Math.max(Math.min(parent.height - y - anchors[BOTTOM_ANCHOR], maxHeight), minHeight);
				} else { // only bottom anchor
					y = parent.height - height - anchors[BOTTOM_ANCHOR];
				}
			}
			update();
			handleRegisteredEventMethod(RESIZE_EVENT, this);
		}
	}




	/**
	 * Autosize specifies actions that will set width/height new when i.e. padding,
	 * text or fontSize is changed. Each class can override it to specify custom
	 * calculations.
	 */
	protected void autosize() {
	}





	/*
	 * _______________________________________________________________________________________________________________
	 * STYLE SETTER
	 * _______________________________________________________________________________________________________________
	 * 
	 */

	/**
	 * Set x-coordinate of Component relative to parent. Does not apply if Component
	 * is added to a FlowContainer.
	 * 
	 * @param x pixel integer
	 */
	public void setX(int x) {
		this.x = x;
		updateAnchors();
		update();
	}

	/**
	 * Set y-coordinate of Component relative to parent. Does not apply if Component
	 * is added to a FlowContainer.
	 * 
	 * @param y pixel integer
	 */
	public void setY(int y) {
		this.y = y;
		updateAnchors();
		update();
	}

	/**
	 * Set z-coordinate of Component. Components are sorted by z-index when
	 * overlapping on the screen.
	 * 
	 * @param z z-index
	 */
	public void setZ(int z) {
		this.z = z;
		if (parent != null)
			try {
				// no use to sort containers with autolayout (its bad actually because it could
				// chang order
				if (!((Container) parent).containerMakesAutoLayout)
					((Container) parent).sortContent();
			} catch (ClassCastException cce) {

			}
		update();
	}

	/**
	 * Set Location relative to parent.
	 * 
	 * @param x x-coordinate
	 * @param y y-coordinate
	 */
	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
		updateAnchors();
		update();
	}

	/**
	 * Set width of Component in pixel.
	 * 
	 * @param width width in pixel
	 */
	public void setWidth(int width) {
		// constrain width immediately
		this.width = Math.max(Math.min(width, maxWidth), minWidth);
		// call the resize event.
		updateAnchors();
		resize();
		update();
	}

	/**
	 * Set minimum width of Component in pixel. Width of Component will never be
	 * less than minWidth. minWidth cannot be smaller than 1.
	 * 
	 * @param minWidth minimum width in pixel
	 */
	public void setMinWidth(int minWidth) {
		// don't allow width ever to go below 1 (that produces errors when creating
		// graphics)
		this.minWidth = Math.max(1, minWidth);
		autosize();
		update();
	}

	/**
	 * Set maximum width of Component in pixel. Width of Component will never be
	 * greater than maxWidth. Default is 100000.
	 * 
	 * @param maxWidth maximum width in pixel
	 */
	public void setMaxWidth(int maxWidth) {
		this.maxWidth = Math.max(minWidth, maxWidth);
		autosize();
		update();
	}


	/**
	 * Set height of Component in pixel.
	 * 
	 * @param height height in pixel
	 */
	public void setHeight(int height) {
		// constrain height immeditaly
		this.height = Math.max(Math.min(height, maxHeight), minHeight);
		updateAnchors();
		resize();
		update();
	}

	/**
	 * Set minimum height of Component in pixel. Width of Component will never be
	 * less than minHeight. minHeight cannot be smaller than 1.
	 * 
	 * @param minHeight minimum height in pixel
	 */
	public void setMinHeight(int minHeight) {
		// don't allow height ever to go below 1 (that produces errors when creating
		// graphics)
		this.minHeight = Math.max(1, minHeight);
		autosize();
		update();
	}

	/**
	 * Set maximum height of Component in pixel. Width of Component will never be
	 * greater than maxHeight. Default is 100000.
	 * 
	 * @param maxHeight maximum height in pixel
	 */
	public void setMaxHeight(int maxHeight) {
		this.maxHeight = Math.max(minHeight, maxHeight);
		autosize();
		update();
	}

	public void setSize(int width, int height) {
		// no setter used here so not to call resize twice
		this.width = Math.max(Math.min(width, maxWidth), minWidth);
		this.height = Math.max(Math.min(height, maxHeight), minHeight);
		updateAnchors();
		resize();
		update();
	}

	/**
	 * Set the plain background color of the Component. Actual displayed color can
	 * vary if the Component is i.e. hovered over.
	 * 
	 * @param clr integer rgb color
	 */
	public void setBackgroundColor(int clr) {
		if (hoverColor == backgroundColor)
			hoverColor = clr;
		if (pressedColor == backgroundColor)
			pressedColor = clr;
		visualBackgroundColor = clr;
		image = null;
		backgroundColor = clr;
		update();
	}

	/**
	 * Automatically generates hover and pressed color of backgroundColor
	 * 
	 * @param clr integer rgb color
	 */
	protected void setStatusBackgroundColorsAutomatically(int clr) {
		backgroundColor = clr;
		visualBackgroundColor = clr;

		int r = (int) Frame.frame0.papplet.red(clr);
		int g = (int) Frame.frame0.papplet.green(clr);
		int b = (int) Frame.frame0.papplet.blue(clr);
		// int a = (int) papplet.alpha(clr);

		if (Frame.frame0.papplet.brightness(clr) > 40) { // darken color for HoverColor and PressedColor when color is
														 // bright enough
			hoverColor = Color.create(r - 20, g - 20, b - 20);
			pressedColor = Color.create(r - 40, g - 40, b - 40);
		} else { // lighten color for HoverColor and PressedColor when color too dark
			hoverColor = Color.create(r + 20, g + 20, b + 20);
			pressedColor = Color.create(r + 40, g + 40, b + 40);
		}
		image = null; // just in case
		update();
	}

	/**
	 * Set the foreground color (usually the text color)
	 * 
	 * @param clr integer rgb color
	 */
	public void setForegroundColor(int clr) {
		foregroundColor = clr;
		update();
	}

	/**
	 * Background color when mouse hovers over this component.
	 * 
	 * @param clr integer rgb color
	 */
	public void setHoverColor(int clr) {
		this.hoverColor = clr;
		update();
	}

	/**
	 * Background color when mouse is pressed down on this component.
	 * 
	 * @param clr integer rgb color
	 */
	public void setPressedColor(int clr) {
		this.pressedColor = clr;
		update();
	}

	/**
	 * Color of Components border.
	 * 
	 * @param clr integer rgb color
	 */
	public void setBorderColor(int clr) {
		borderColor = clr;
		update();
	}

	/**
	 * Stroke width of the Components border.
	 * 
	 * @param borderWidth border with in pixel
	 */
	public void setBorderWidth(int borderWidth) {
		this.borderWidth = Math.max(0, borderWidth);
		update();
	}

	/**
	 * Rounds the corners of the Component. Negative values will be ignored.
	 * 
	 * @param borderRadius border radius
	 */
	public void setBorderRadius(int borderRadius) {
		this.borderRadius = borderRadius;
		update();
	}


	/**
	 * Set the text content. Some Components do not display any text (i.e. most
	 * Containers).
	 * 
	 * @param text text
	 */
	public void setText(String text) {
		this.text = text;
		autosize();
		update();
	}

	public void setFontSize(float fontSize) {
		this.fontSize = Math.max(0, fontSize);
		autosize();
		update();
	}

	/**
	 * Set the text align (works for most Components).
	 * 
	 * @param align LEFT, CENTER, RIGHT
	 */
	public void setTextAlign(int align) {
		if (align == 3 || align == 37 || align == 39)
			this.textAlign = align;
		update();
	}

	/**
	 * Vertical align (not implemented in all Components).
	 * 
	 * @param align TOP, MIDDLE, BOTTOM
	 */
	public void setTextAlignY(int align) {
		if (align == PApplet.CENTER || align == PApplet.TOP || align == PApplet.BOTTOM)
			this.textAlignY = align;
		update();
	}

	/**
	 * Set the cursor displayed when mouse is over this Component.
	 * 
	 * @param cursor Integer between 0 and 11. Can use constants like ARROW, CROSS,
	 *               HAND, MOVE, TEXT, or WAIT.
	 */
	public void setCursor(int cursor) {
		if (cursor >= 0 && cursor < 12) {
			this.cursor = cursor;
		}
	}

	/**
	 * Set the (background) image of the Component. This is not implemented in all
	 * but in most Components. The image is copied!
	 * 
	 * @param image PImage or PGraphics object.
	 */
	public void setImage(PImage image) {
		try {
			this.image = (PImage) image.clone();
			update();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Set the image filling mode. @see FILL @see FIT @see FIT_INSIDE
	 * 
	 * @param imageMode. Use Control.FILL, Control.FIT or Control.FIT_INSIDE
	 */
	public void setImageMode(int imageMode) {
		this.imageMode = imageMode;
		update();
	}

	/**
	 * Set background to a vertical gradient between the two given colros. This
	 * removes the background image if set.
	 * 
	 * @param clr1 top color
	 * @param clr2 bottom color
	 */
	public void setGradient(int clr1, int clr2) {
		PGraphics gradient = Frame.frame0.papplet.createGraphics(width, height);
		gradient.beginDraw();
		for (int i = 0; i < height; i++) {
			float inter = PApplet.map(i, 0, height, 0, 1);
			int c = Frame.frame0.papplet.lerpColor(clr1, clr2, inter);
			gradient.stroke(c);
			gradient.line(0, i, width, i);
		}
		setImage(gradient);
	}

	/**
	 * Set the enabled state of this Component. If false it will not receive any
	 * events and be displayed in a different (mostly greyish) way.
	 * 
	 * @param enabled enabled state
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		update();
	}

	/**
	 * Set the visibility. Invisible Components will not be rendered and receive not
	 * events.
	 * 
	 * @param visible visibility state
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
		update();
	}

	/**
	 * Set opacity (opposite of transparency).
	 * 
	 * @param opacity opacity from 0 (transparent) to 1 (opaque).
	 */
	public void setOpacity(float opacity) {
		this.opacity = Math.max(0, Math.min(1, opacity));
		update();
	}

	/**
	 * Apply margins to all sides of the Component. In FlowContainers and similar
	 * containers neighboring Components this determines the distance to neighbor
	 * Components and the parents bounds.
	 * 
	 * @param all margin in pixel
	 */
	public void setMargin(int all) {
		marginTop = all;
		marginRight = all;
		marginBottom = all;
		marginLeft = all;
		update();
	}

	/**
	 * Apply same margins to both top/bottom and left/right.
	 * 
	 * @param top_bottom top and bottom margin
	 * @param left_right left and right margin
	 */
	public void setMargin(int top_bottom, int left_right) {
		marginTop = top_bottom;
		marginRight = left_right;
		marginBottom = top_bottom;
		marginLeft = left_right;
		update();
	}

	/**
	 * Set individual top, right, bottom and left margins.
	 * 
	 * @param top    top margin in pixel
	 * @param right  right margin in pixel
	 * @param bottom bottom margin in pixel
	 * @param left   left margin in pixel
	 */
	public void setMargin(int top, int right, int bottom, int left) {
		marginTop = top;
		marginRight = right;
		marginBottom = bottom;
		marginLeft = left;
		update();
	}

	public void setMarginTop(int top) {
		marginTop = top;
		update();
	}

	public void setMarginRight(int right) {
		marginRight = right;
		update();
	}

	public void setMarginBottom(int bottom) {
		marginBottom = bottom;
		update();
	}

	public void setMarginLeft(int left) {
		marginLeft = left;
		update();
	}


	/**
	 * Apply same padding to all sides
	 * 
	 * @param all padding in pixel
	 */
	public void setPadding(int all) {
		paddingTop = all;
		paddingRight = all;
		paddingBottom = all;
		paddingLeft = all;
		autosize();
		update();
	}

	public void setPadding(int top_bottom, int left_right) {
		paddingTop = top_bottom;
		paddingRight = left_right;
		paddingBottom = top_bottom;
		paddingLeft = left_right;
		autosize();
		update();
	}

	/**
	 * Apply individual padding to all sides of the Component. Padding will create
	 * space inside the Component between borders and content.
	 * 
	 * @param top    top padding in pixel
	 * @param right  right padding in pixel
	 * @param bottom bottom padding in pixel
	 * @param left   left padding in pixel
	 */
	public void setPadding(int top, int right, int bottom, int left) {
		paddingTop = top;
		paddingRight = right;
		paddingBottom = bottom;
		paddingLeft = left;
		autosize();
		update();
	}

	public void setPaddingTop(int top) {
		paddingTop = top;
		autosize();
		update();
	}

	public void setPaddingRight(int right) {
		paddingRight = right;
		autosize();
		update();
	}

	public void setPaddingBottom(int bottom) {
		paddingBottom = bottom;
		autosize();
		update();
	}

	public void setPaddingLeft(int left) {
		paddingLeft = left;
		autosize();
		update();
	}












	/*
	 * _______________________________________________________________________________________________________________
	 * STYLE GETTER
	 * _______________________________________________________________________________________________________________
	 * 
	 */

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getWidth() {
		return width;
	}

	public int getMinWidth() {
		return minWidth;
	}

	public int getMaxWidth() {
		return maxWidth;
	}

	public int getHeight() {
		return height;
	}

	public int getMinHeight() {
		return minHeight;
	}

	public int getMaxHeight() {
		return maxHeight;
	}

	public int getBackgroundColor() {
		return backgroundColor;
	}

	public int getForegroundColor() {
		return foregroundColor;
	}

	public int getHoverColor() {
		return hoverColor;
	}

	public int getPressedColor() {
		return pressedColor;
	}

	public int getBorderColor() {
		return borderColor;
	}

	public int getBorderWidth() {
		return borderWidth;
	}

	public int getBorderRadius() {
		return borderRadius;
	}

	public String getText() {
		return text;
	}

	public float getFontSize() {
		return fontSize;
	}

	public int getTextAlign() {
		return textAlign;
	}

	public int getTextAlignY() {
		return textAlignY;
	}

	public int getCursor() {
		return cursor;
	}

	public PImage getImage() {
		return image;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean isVisible() {
		return visible;
	}

	public float getOpacity() {
		return opacity;
	}

	public boolean isFocusable() {
		return focusable;
	}

	public boolean isFocused() {
		return focused;
	}

	public int getMarginTop() {
		return marginTop;
	}

	public int getMarginRight() {
		return marginRight;
	}

	public int getMarginBottom() {
		return marginBottom;
	}

	public int getMarginLeft() {
		return marginLeft;
	}

	public int getPaddingTop() {
		return paddingTop;
	}

	public int getPaddingRight() {
		return paddingRight;
	}

	public int getPaddingBottom() {
		return paddingBottom;
	}

	public int getPaddingLeft() {
		return paddingLeft;
	}



	/**
	 * Create a transition animation for a property of this Component. The @link
	 * Frame will deal with changing this property in appropriate steps to create
	 * the effect.
	 * 
	 * @param attribute    name of attribute to animate
	 * @param aimedValue   final value for the attribute
	 * @param milliseconds time for animation in milliseconds
	 */
	public void animate(String attribute, float aimedValue, double milliseconds) {
		Frame.frame0.animateImpl(attribute, this, aimedValue, milliseconds);
	}








	/*
	 * _______________________________________________________________________________________________________________
	 * EVENT METHODS
	 * _______________________________________________________________________________________________________________
	 * 
	 */

	class RMethod {
		Method method;
		Object target;
		Class<?> args;

		RMethod(Method m, Object t, Class<?> args) {
			method = m;
			target = t;
			this.args = args;
		}
	}

	protected RMethod[] registeredRMethods;

	protected static final int PRESS_EVENT = 0;
	protected static final int RELEASE_EVENT = 1;
	protected static final int ENTER_EVENT = 2;
	protected static final int EXIT_EVENT = 3;
	protected static final int MOVE_EVENT = 4;
	protected static final int DRAG_EVENT = 5;
	protected static final int WHEEL_EVENT = 6;
	protected static final int RESIZE_EVENT = 7;
	protected static final int FOCUS_EVENT = 8;

	static final int numberMouseListeners = 9;

	protected int totalNumberListeners = numberMouseListeners;



	protected void setupListeners(int number) {
		totalNumberListeners = totalNumberListeners + number;
		registeredRMethods = new RMethod[totalNumberListeners];
	}

	/*
	 * WORKING WITH EVENTS IN ProcessingGUI :
	 * 
	 *
	 *
	 *
	 * The programmer can add certain listeners to his objects. Mouse listeners are
	 * available for all Controls. They can be assigned and given a registered
	 * method with the addMouseListener() method, specifying the type with a string
	 * ("press", "release"...) If no target is specified with the overloaded
	 * addMouseListener() method, papplet will be assumed.
	 * 
	 * How MouseListener works: Frame registers a mouseEvent() method at papplet.
	 * Every Container (also Frame) calls the mouseEvent of all its items, but only
	 * if have the activatedInternalMouseListener property set true. Containers do
	 * that by default and so do buttons, textboxes, sliders etc. (almost
	 * everything) but once the programmer adds a mouseListener it's set to true
	 * anyway.
	 * 
	 * When a Control gets a mouseEvent it Control can decide to stop the
	 * propagation by calling the Frames stopPropagation() method to true (it will
	 * be set to false automatically at the beginning of each frame). This way
	 * objects with high z-index, which will be checked fist can prevent lower
	 * objects from getting the mouseEvent. This is needed for example with menus,
	 * spinners, popups etc.
	 *
	 *
	 * Moreover Controls like frame, textbox feature a keylistener or an
	 * itemchanged-listener (listview, menustrip). These listeners are usually
	 * assigned with extra methods like "addItemChangedListener()" etc.
	 * 
	 *
	 * All listener-adding-methods call registerEventRMethod(int number, String
	 * methodName, Object target, Class<?> args) If a listener needs no arguments it
	 * can pass null for args. registerEventRMethod() will try to find a method in
	 * the target that has no args first, even if it should have one, so it doesn't
	 * break down if the programmer forgets to add i.e. "MouseEvent e" or doesn't
	 * need it at all. Then it checks if args are provided at all and if so tries to
	 * find a method in the target. If none is found error will be thrown.
	 *
	 *
	 *
	 *
	 *
	 * CREATING OWN CONTROLS WITH CUSTOM LISTENERS
	 *
	 * If you create a custom control with custom listeners you have to provide
	 * methods to add them to the object.
	 *
	 *
	 *
	 * You have to honor some essential rules: - all registered methods are stored
	 * in the "registeredRMethods" array. The first 7 (from 0 to 6) methods are
	 * reserved for the mouselisteners: press/release/enter/exit/move/drag/wheel,
	 * DON'T TOUCH THEM!
	 *
	 * - by default this array is 9 (for mouse/resize/focus listeners) in size - a
	 * custom control needs to recreate this array in the constructor with needed
	 * size. This is done by calling setupListeners(int) in the constructor and
	 * passing the number of ADDITIONAL listeners for this class
	 *
	 * - If your object inherits from any other that "Control" beware that the
	 * parent might already use a listener for an index
	 *
	 * - Create a static final int variable that specifies the index for your
	 * listener (starting at the static Frame.numberMouseListeners) and always use
	 * that one also when handling the method. Default number might change in future
	 * and this should be an easy update.
	 *
	 * - Provide new methods to add AND remove those listeners (best provide an
	 * option with target object, and without making the default target the papplet)
	 *
	 *
	 */

	/*
	 * protected boolean activatedInternalMouseListener = false;
	 * 
	 * protected void activateInternalMouseListener() {
	 * activatedInternalMouseListener = true;
	 * 
	 * }
	 * 
	 * protected void deactivateInternalMouseListener() {
	 * activatedInternalMouseListener = false; }
	 */



	// if zero do not process move events.
	private static int moveListenersCount = 0;

	protected static void incrementMoveListenersCount() {
		moveListenersCount++;
	}

	protected static void decrementMoveListenersCount() {
		moveListenersCount = Math.max(0, --moveListenersCount);
	}

	protected int moveListenersCount() {
		return moveListenersCount;
	}

	/**
	 * Adds a mouse listener. The type can be "press", "release", "enter", "exit,
	 * "move", "drag" and "wheel". Each Component allows one listener per type. The
	 * given method can be implemented without arguments or with {@link MouseEvent}
	 * as parameter. If it is defined in another class than in the main PApplet
	 * scope use the overloaded method
	 * {@link #addMouseListener(String, String, Object)} that allows passing a
	 * target object.
	 * 
	 * @param type       String for event type
	 * @param methodName Name of method to invoke
	 * @return false if type is invalid, method is not accessible or a listener has
	 *         already been registered for this type. Returns true if successful.
	 */
	public boolean addMouseListener(String type, String methodName) {
		return addMouseListener(type, methodName, Frame.frame0.papplet);
	}

	/**
	 * @see #addMouseListener(String, String) Allows to pass a target object where
	 *      the given method is declared.
	 * 
	 * @param type       String for event type
	 * @param methodName name of callback method
	 * @param target     Object where the callback method is declared.
	 * @return false if type is invalid, method is not accessible or a listener has
	 *         already been registered for this type. Returns true if successful.
	 */
	public boolean addMouseListener(String type, String methodName, Object target) {
		switch (type) {
		case "press":
			return registerEventRMethod(PRESS_EVENT, methodName, target, MouseEvent.class);
		case "release":
			return registerEventRMethod(RELEASE_EVENT, methodName, target, MouseEvent.class);
		case "enter":
			return registerEventRMethod(ENTER_EVENT, methodName, target, MouseEvent.class);
		case "exit":
			return registerEventRMethod(EXIT_EVENT, methodName, target, MouseEvent.class);
		case "move":
			incrementMoveListenersCount();
			return registerEventRMethod(MOVE_EVENT, methodName, target, MouseEvent.class);
		case "drag":
			return registerEventRMethod(DRAG_EVENT, methodName, target, MouseEvent.class);
		case "wheel":
			return registerEventRMethod(WHEEL_EVENT, methodName, target, MouseEvent.class);
		default:
			return false;
		}
	}

	/**
	 * Remove a mouse listener for given type if one has already been set up.
	 * 
	 * @param type event type.
	 */
	public void removeMouseListener(String type) {
		switch (type) {
		case "press":
			deregisterEventRMethod(PRESS_EVENT);
			break;
		case "release":
			deregisterEventRMethod(RELEASE_EVENT);
			break;
		case "enter":
			deregisterEventRMethod(ENTER_EVENT);
			break;
		case "exit":
			deregisterEventRMethod(EXIT_EVENT);
			break;
		case "move":
			if (registeredRMethods[MOVE_EVENT] != null) // tell Frame there is one move listener less
				decrementMoveListenersCount();
			deregisterEventRMethod(MOVE_EVENT);
			break;
		case "drag":
			deregisterEventRMethod(DRAG_EVENT);
			break;
		case "wheel":
			deregisterEventRMethod(WHEEL_EVENT);
			break;
		}
	}


	// resize and focus listener

	/**
	 * Add a resize listener that fires each time the Component is resized by anchor
	 * resizing. Event arguments: null
	 * 
	 * @param methodName name of callback method
	 * @param target     Object where the callback method is declared.
	 */
	public void addResizeListener(String methodName, Object target) {
		registerEventRMethod(RESIZE_EVENT, methodName, target, Control.class);
	}

	/**
	 * Add a focus listener that fires when the Component gets focus (through mouse
	 * click, programmatically, ...). Event arguments: null
	 * 
	 * @param methodName name of callback method
	 * @param target     Object where the callback method is declared.
	 */
	public void addFocusListener(String methodName, Object target) {
		registerEventRMethod(FOCUS_EVENT, methodName, target, Control.class);
	}

	/**
	 * @see #addResizeListener(String) , target is the PApplet sketch
	 * 
	 * @param methodName method to call.
	 */
	public void addResizeListener(String methodName) {
		addResizeListener(methodName, Frame.frame0.papplet);
	}

	/**
	 * @see #addFocusListener(String) , target is the PApplet sketch
	 * @param methodName method to call.
	 */
	public void addFocusListener(String methodName) {
		addFocusListener(methodName, Frame.frame0.papplet);
	}



	public void removeResizeListener() {
		deregisterEventRMethod(RESIZE_EVENT);
	}

	public void removeFocusListener() {
		deregisterEventRMethod(FOCUS_EVENT);
	}




	protected boolean registerEventRMethod(int number, String methodName, Object target, Class<?> args) {
		if (registeredRMethods[number] == null) { // sure?
			Class<?> c = target.getClass();
			try { // try with no args
				Method m = c.getMethod(methodName);
				registeredRMethods[number] = new RMethod(m, target, null);
				return true;
			} catch (NoSuchMethodException nsme) {
				try { // try with args
					if (args != null) {
						Method m = c.getMethod(methodName, args);
						registeredRMethods[number] = new RMethod(m, target, args);
						return true;
					} else {
						Frame.frame0.papplet.die("There is no public " + methodName + "() method with the right arguments.");
						registeredRMethods[number] = null;
					}
				} catch (NoSuchMethodException nsme2) {
					Frame.frame0.papplet.die("There is no public " + methodName + "() method with the right arguments.");
					registeredRMethods[number] = null;
				}
			}
		}
		return false;
	}

	protected void deregisterEventRMethod(int number) {
		registeredRMethods[number] = null;
	}

	protected void handleRegisteredEventMethod(int number, Object args) {
		if (registeredRMethods[number] != null) {

			try {
				if (registeredRMethods[number].args != null) {
					registeredRMethods[number].method.invoke(registeredRMethods[number].target, args);
				} else
					registeredRMethods[number].method.invoke(registeredRMethods[number].target);
			} catch (IllegalAccessException ie) {
				ie.printStackTrace();
			} catch (InvocationTargetException te) {
				te.printStackTrace();
			}
		}
	}





	// new mouse event stuff
	/*
	 * replace bounds with x0, y0 Right and bottom bounds are not needed for an item
	 * gets the mouse event only if mouse is over parent. This way no constraining
	 * like in calcbounds is needed. instead of passing the mouseevent, this is
	 * stored globally (in Control.e) and relative x,y coords are passed. Each
	 * container subtracts its own x0,y0 from them so the items only need to know
	 * their relative position to parent and not absolute position in window to
	 * determine if mouse is over the item.
	 * 
	 * x0,y0 store computed position origin of item relative container it is
	 * evaluated and set in containerRenderItem(). Problem: Flowcontainers decide
	 * not to draw items that are out of the visible area and needs to set x0,y0 to
	 * somewhere outside this area.
	 * 
	 * hovering is now dealt with in a different way: the first item to find out
	 * that the mouse is over it sets the Control.first to itself. At then end of
	 * mouseEvent processing Frame checks Control.first and if this items pHovered
	 * is false then Frame calls enter(), handleRegEvent(..) and first.pHovered =
	 * true; Also the cursor is set to the cursor of this item. Similarily exit() is
	 * called if Control.first has changed through the event propagation.
	 * 
	 * 
	 * 
	 * Issues:
	 * 
	 * - MenuItem cannot hide when clicked aynwhere else, because menuitem does not
	 * get these events anymore also menuitem seems to rely on calcbounds in some
	 * point.
	 * 
	 * - only one element can be hovered on (not really an issue)
	 * 
	 * - scrollcontainers still set their fullscrollsize in calcBounds and use
	 * bounds in their event listening (solved?)
	 * 
	 * - scrollarea, scrollcontainers: content in scrollarea is accessible if mouse
	 * over scrollbar
	 * 
	 * 
	 */

	// origin coordinates relative to parent. Set by parent in
	// containerRenderItem(Control, int, int)
	protected int relativeX, relativeY;

	// current mouse event is stored here by Frame, no need to carry it around
	protected static MouseEvent currentMouseEvent;

	// first control that notices that the mouse is over it sets this to itself.
	// This is used for ENTER/EXIT as well as setting the cursor
	protected static Control hoveredElement;

	// currently dragged element
	protected static Control draggedElement;

	// use new mouse event handling or old?
	protected static boolean useNewMouseEvent = true;


	protected void mouseEvent(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		PApplet.println("Old Mouse Event", this);

		if (visible && enabled) {

			if (bounds.isWithin(x, y)) { // if over element
				/*
				 * if(Frame.topmost == null) { System.out.println(this); Frame.topmost = this; }
				 */

				switch (e.getAction()) {
				case MouseEvent.PRESS:
					focus();
					press(e);
					draggedElement = this;
					Frame.stopPropagation();
					handleRegisteredEventMethod(PRESS_EVENT, e);
					break;
				case MouseEvent.RELEASE:
					release(e);
					Frame.stopPropagation();
					handleRegisteredEventMethod(RELEASE_EVENT, e);
					break;
				case MouseEvent.MOVE:
					move(e);
					handleRegisteredEventMethod(MOVE_EVENT, e);
					break;
				case MouseEvent.DRAG:
					// this code wont be reached anymore for every drag event will be caught by
					// frame
					drag(e);
					handleRegisteredEventMethod(DRAG_EVENT, e);
					Frame.stopPropagation();
					break;
				case MouseEvent.WHEEL:
					mouseWheel(e);
					handleRegisteredEventMethod(WHEEL_EVENT, e);
					break;
				}
				if (!pHovered) { // ENTER
					Frame.frame0.papplet.cursor(cursor);
					enter(e);
					handleRegisteredEventMethod(ENTER_EVENT, e);
					pHovered = true;
					Frame.stopPropagation();
				}
			} else {
				if (pHovered) { // EXIT
					Frame.frame0.papplet.cursor(0); // default cursor
					exit(e);
					handleRegisteredEventMethod(EXIT_EVENT, e);
					pHovered = false;
				}
			}
		}
	}

	// check if given coordinates (need to be relative to parent) are within this
	// elements bounds
	protected boolean relCoordsAreWithin(int x, int y) {
		return x > relativeX && y > relativeY && x < relativeX + width && y < relativeY + height;
	}

	/**
	 * Get x coordinate of left side relative to parent.
	 * 
	 * @return relative x
	 */
	public int getOffsetXParent() {
		return relativeX;
	}

	/**
	 * Get y coordinate of left side relative to parent.
	 * 
	 * @return relative y
	 */
	public int getOffsetYParent() {
		return relativeY;
	}

	/**
	 * Get x coordinate of left side relative to window.
	 * 
	 * @return absolute x
	 */
	public int getOffsetXWindow() {
		return relativeX + parent.getOffsetXWindow();
	}

	/**
	 * Get x coordinate of left side relative to window.
	 * 
	 * @return absolute y
	 */
	public int getOffsetYWindow() {
		return relativeY + parent.getOffsetYWindow();
	}


	protected void mouseEvent(int x, int y) {
		if (!visible || !enabled)
			return;

		if (relCoordsAreWithin(x, y)) {
			if (hoveredElement == null)
				hoveredElement = this;
			switch (currentMouseEvent.getAction()) {
			case MouseEvent.PRESS:
				focus();

				draggedElement = this;
				Frame.stopPropagation();
				press(currentMouseEvent);
				handleRegisteredEventMethod(PRESS_EVENT, currentMouseEvent);
				break;

			case MouseEvent.RELEASE:
				Frame.stopPropagation();
				release(currentMouseEvent);
				handleRegisteredEventMethod(RELEASE_EVENT, currentMouseEvent);
				break;

			case MouseEvent.MOVE:
				move(currentMouseEvent);
				handleRegisteredEventMethod(MOVE_EVENT, currentMouseEvent);
				break;

			case MouseEvent.WHEEL:
				mouseWheel(currentMouseEvent);
				handleRegisteredEventMethod(WHEEL_EVENT, currentMouseEvent);
				break;

			case MouseEvent.DRAG:
				// this code wont be reached anymore for every drag event will be caught by
				// frame
				// drag(e);
				// handleRegisteredEventMethod(DRAG_EVENT, e);
				// Frame.stopPropagation();
				break;
			}
		}

	}

	/*
	 * protected void press(MouseEvent e) { }
	 * 
	 * protected void release(MouseEvent e) { }
	 * 
	 * protected void enter(MouseEvent e) { }
	 * 
	 * protected void exit(MouseEvent e) { }
	 */

	protected void move(MouseEvent e) {
	}

	protected void drag(MouseEvent e) {
	}

	protected void mouseWheel(MouseEvent e) {
	}

	protected void enter(MouseEvent e) {
		visualBackgroundColor = hoverColor;
		update();
	}

	protected void exit(MouseEvent e) {
		visualBackgroundColor = backgroundColor;
		update();
	}

	protected void press(MouseEvent e) {
		visualBackgroundColor = pressedColor;
		update();
	}

	protected void release(MouseEvent e) {
		visualBackgroundColor = hoverColor;
		update();
	}

	// methods that will be called when key events occured and this Control is the
	// currently focused element.

	/**
	 * Called by {@link Frame} through KeyListener
	 * 
	 * @param e KeyEvent
	 */
	protected void keyPress(KeyEvent e) {
	}

	/**
	 * Called by {@link Frame} through KeyListener
	 * 
	 * @param e KeyEvent
	 */
	protected void keyRelease(KeyEvent e) {
	}

	/**
	 * Called by {@link Frame} through KeyListener
	 * 
	 * @param e KeyEvent
	 */
	protected void keyTyped(KeyEvent e) {
	}


	protected static void print(Object... v) {
		PApplet.println(v);
	}
}
