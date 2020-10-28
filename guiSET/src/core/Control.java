package guiSET.core;

import processing.core.*;
import processing.event.*;
import java.lang.reflect.Method;
import java.util.ArrayList;

import java.lang.reflect.InvocationTargetException;


/*
 * The code in this class is divided in a few sections:

	1. Most fields that deal with UI and UX
	
	2. Constructor
	
	3. Methods to be overriden by other classes:
			- initialized()
			- addedToParent()
			
	5. Focus
	
	4. Rendering methods, classes, default draw methods. 
	
	6. Anchors and Resizing
	
	7. Setters
	
	8. Getters
	
	9. EventListener
	
	10. Mouse event methods and protected static fields
	
	11. Event methods to be overriden by other classes
	
	12. Debugging functions
	
 */

/**
 * (Abstract) Base class for all other visual components.
 * 
 * It provides all basic style attributes and methods. It also comes with the essential event
 * handling and some useful default rendering methods.
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
	 * Parent container of this element. The Frame itself is the master container and every element that
	 * should be visible has to be a child of Frame in some nested way.
	 */
	protected Control parent;



	/**
	 * Image object that always contains the updated looks of this element. When parents are rendered
	 * they project the PGraphics of their children onto themselves. In this way only the changed parts
	 * have to be re-rendered and not the entire gui.
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
	 * Status properties
	 */
	protected boolean focusable = true; 		// Determines if this control can be focused at all
	protected boolean focused = false; 			// Don't change focused, Frame does that
	protected boolean stickyFocus = false; 		// If true, then its focused state can't be overridden by other elements requesting focus, only when
											 		// itself calls blur

	protected boolean enabled = true; 			// If false, then will not receive mouse events, also different look can be implemented
	protected boolean visible = true;



	// Coordinates and size relative to container (won't be changed by a flow/scroll container!).
	protected int x;
	protected int y;
	// z-index of element; only has an effect when element is in a panelcontainer (not a flow- or scroll
	// container)
	protected int z;

	private int width; 					// Is always between minWidth and maxWidth
	private int height; 				// Is always between minHeight and maxHeight

	protected int minWidth = Constants.MinimalMinWidth; 		// Never below Constants.MinimalMinWidth
	protected int maxWidth = Constants.DefaultMaxWidth;
	protected int minHeight = Constants.MinimalMinHeight; 		// Never below Constants.MinimalMinHeight
	protected int maxHeight = Constants.DefaultMaxHeight;

	protected int marginLeft; 			// Margins are not included in width/height and honored by containers when layouting their items
	protected int marginTop;
	protected int marginRight;
	protected int marginBottom;

	protected int paddingLeft; 			// Paddings are included in width/height and regarded when performing autosize
	protected int paddingRight;
	protected int paddingTop;
	protected int paddingBottom;


	/*
	 * Visuals
	 */
	protected PImage image;
	protected ImageMode imageMode = ImageMode.FILL_DISTORT;


	/*
	 * IMAGE MODES:
	 */
	/**
	 * Fill modes for background images. The element can either be filled with given image (and if
	 * necessary, the image is distorted) or the iamge is not distorted and resized to fill the entire
	 * element or be entirely visible inside the element.
	 */
	public enum ImageMode {
		FILL_DISTORT, FIT_FILL_ALL, FIT_INSIDE
	}

	/**
	 * Image mode: fill element with given image and if necessary, distort the image
	 */
	public static final ImageMode FILL = ImageMode.FILL_DISTORT;

	/**
	 * Image mode: enlarge image so it fills out entire object centered, parts of the image might be
	 * hidden; fits the larger side
	 */
	public static final ImageMode FIT = ImageMode.FIT_FILL_ALL;

	/**
	 * Image mode: place image centered in the object without cropping it, so it fits the smaller side
	 */
	public static final ImageMode FIT_INSIDE = ImageMode.FIT_INSIDE;

	protected int backgroundColor;
	protected int foregroundColor;
	protected int borderColor;
	protected int hoverColor; 			// First set automatically with backgroundColor, can be changed programatically
	protected int pressedColor; 		// First set automatically with backgroundColor, can be changed programatically

	/*
	 * Visual background color is the actually displayed bg-color, while
	 * backgroundColor is only the color in normal state (neither hovered on or
	 * pressed). When entering/pressing the elements visualBackgroundColor is set to
	 * hoverColor/pressedColor and back to backgroundColor when exiting/releasing
	 * the element.
	 */
	protected int visualBackgroundColor;

	protected int borderWidth;
	protected int borderRadius;

	protected int cursor = PApplet.ARROW;	// Type of cursor to display when hovering over this control

	protected float opacity = 1.0f; 	// Opacity in percent from 0 to 1


	/*
	 * Events
	 */

	// previous hovered/pressed state
	protected boolean pHovered = false;
	protected boolean pPressed = false;




	/*
	 * If true, then shortcuts registered to frame won't be handled if this element
	 * is focused. I.e. useful for textboxes (ctrl-c, x etc.)
	 */

	protected boolean overridesFrameShortcuts() {
		return false;
	}


	/**
	 * guiSET provides two rendering methods and for each element, the method can be chosen. Unbuffered
	 * rendering means, that the element draws onto the parents buffer.
	 * 
	 * Buffered rendering will make the element draw its looks onto an own pixel buffer (a PGraphics
	 * object). This <i>can</i> be more CPU-efficient for containers that have lots of items but a
	 * rather small size. When the gui changes, but the content of the container does not change
	 * frequently, the container and its items need not be drawn until they change. However, this comes
	 * at a price of memory (RAM). For items small in size this is not much though.
	 *
	 */
	public enum RenderingMethod {
		BUFFERED_RENDERING, UNBUFFERED_RENDERING
	}

	/**
	 * Elements graphics are drawn on a buffer image. This element needs to be re-rendered only if it
	 * changed and not if siblings or parents changed. This saves CPU resources but needs more memory.
	 */
	public static final RenderingMethod BUFFERED_RENDERING = RenderingMethod.BUFFERED_RENDERING;
	/**
	 * Elements graphics are drawn on parent buffer. Each change of any element makes a re-rendering of
	 * this element necessary. There are exceptions though: if this parents mode is
	 * {@link #BUFFERED_RENDERING} and a sibling of the parent has changed, then the element that is
	 * buffered by the parent needs no redrawing.
	 */
	public static final RenderingMethod UNBUFFERED_RENDERING = RenderingMethod.UNBUFFERED_RENDERING;

	/**
	 * Specify rendering type for any new element instances.
	 */
	public static RenderingMethod defaultRenderingMethod = UNBUFFERED_RENDERING;



	public class Constants {

		public static final int MinimalMinWidth = 1;
		public static final int MinimalMinHeight = 1;
		public static final int DefaultMaxWidth = 10000;
		public static final int DefaultMaxHeight = 10000;

		public final static int MinimalScrollHandleLength = 15;

		public static final int MenuSurfaceZIndex = 20; // The one and only MenuSurface's z-index is set to this
		public static final int MenuItemHeight = 23;    // default height for menu items and menu bars - looks good in my opinion
		public static final int MenuItemPaddingLeft = 27;
		public static final int MenuItemPaddingRight = 10;


		public static final int SCROLL_HANDLE_STRENGTH_STD = 12;
		public static final int SCROLL_HANDLE_STRENGTH_SLIM = 3;

		public static final float PI = (float) Math.PI;
		
		public static final int LEFT = PApplet.LEFT;
		public static final int RIGHT = PApplet.RIGHT;
		public static final int TOP = PApplet.TOP;
		public static final int BOTTOM = PApplet.BOTTOM;
		public static final int CENTER = PApplet.CENTER;
		public static final int BASELINE = PApplet.BASELINE;
		public static final int UP = PApplet.UP;
		public static final int DOWN = PApplet.DOWN;

	}


	public Control() {

		height = 50;
		width = 50;

		backgroundColor = GuisetDefaultValues.backgroundColor;
		visualBackgroundColor = GuisetDefaultValues.backgroundColor;
		foregroundColor = GuisetDefaultValues.foregroudColor;
		hoverColor = GuisetDefaultValues.backgroundColor;
		pressedColor = GuisetDefaultValues.backgroundColor;

		paddingLeft = GuisetDefaultValues.paddingLeft;
		paddingRight = GuisetDefaultValues.paddingRight;
		paddingTop = GuisetDefaultValues.paddingTop;
		paddingBottom = GuisetDefaultValues.paddingBottom;

		marginLeft = GuisetDefaultValues.marginLeft;
		marginRight = GuisetDefaultValues.marginRight;
		marginTop = GuisetDefaultValues.marginTop;
		marginBottom = GuisetDefaultValues.marginBottom;

		borderColor = GuisetDefaultValues.borderColor;
		borderRadius = GuisetDefaultValues.borderRadius;
		borderWidth = Math.max(0, GuisetDefaultValues.borderWidth);

		if (defaultRenderingMethod == RenderingMethod.BUFFERED_RENDERING) {
			renderer = new BasicBufferedRenderer();
		} else {
			renderer = new BasicUnbufferedRenderer();
		}

	}






	/**
	 * Called before rendering the first time. Don't call {@link #setZ(int)}(int) in
	 * {@link #initialize()} nor call any function or constructor (like {@link guiSET.core.MenuSurface})
	 * that does.
	 */
	protected void initialize() {

	}


	/*
	 * Called by containers when they add this object to their content list.
	 */
	protected void addedToParent() {

	}






	/**
	 * Request the Frame component to focus this object. The Frame decides upon the demand and can set
	 * this elements focused state to true. There can only be one focused element at a time and it is
	 * stored in the Frames focusedElement property.
	 * 
	 * @see guiSET.core.Frame#focusedElement
	 */
	public void focus() {
		getFrame().requestFocus(this);
	}


	/**
	 * Request the Frame component to blur this object (set focus to false).
	 * 
	 * @see guiSET.core.Frame#focusedElement
	 */
	public void blur() {
		getFrame().requestBlur(this);
	}

	/**
	 * Request the Frame component to blur this object (set focus to false), duplicate of
	 * {@link #blur()}.
	 * 
	 * @see guiSET.core.Frame#focusedElement
	 */
	public void unfocus() {
		blur();
	}

	protected boolean hasStickyFocus() {
		return stickyFocus;
	}



	/*
	 * DRAWING AND RENDERING
	 */

	/*
	 * Dimensions (width, height) from previous frame. Used to check if size
	 * changed. If so, the PGraphics needs to be created new with updated size.
	 */
	protected int pWidth, pHeight = -1;

	/*
	 * Method to be executed before calling render(). It prepares the PGraphics for rendering. 
	 */
	protected final void preRender() {
		// only create graphics when size changed
		if (getWidth() != pWidth || getHeight() != pHeight) {
			pg = getPApplet().createGraphics(getWidth(), getHeight());
			pWidth = getWidth();
			pHeight = getHeight();
			pg.beginDraw();
		} else {
			pg.beginDraw();
			pg.clear();
		}
	}


	/*
	 * Main drawing method that determines the looks of the object.
	 * 
	 * It has to be implemented for each control individually. Just start with
	 * something like:
	 * 
	 * pg.rect(...); pg.line(...) ... ,
	 * 
	 * using the standard drawing funcions. It is not necessary to create the
	 * PGraphics object nor to call beginDraw() or endDraw() as you are maybe used
	 * to.
	 * 
	 * You can call the drawDefaultBackgrond() and drawDefaultText() methods which
	 * do the standard drawing of text and background while paying attention to
	 * attributes like backgroundColor, borderColor, borderWidth, image, imageMode,
	 * ..., textAlign, fontColor, fontSize.
	 * 
	 * Containers need to check their items for visibility!
	 */
	protected abstract void render();

	/*
	 * Method used by containers to draw an item at specfied position. 
	 */
	protected final void renderItem(Control item, int x, int y) {
		// check visiblity in render(), some containers need to check it there anyway

		item.renderer.renderAll(x, y, pg);
	}


	/**
	 * The renderers renderAll method is called by renderItem(). There are different versions for best
	 * performance.
	 * 
	 * Standard is the basic renderer which does not implement opacity. If opacity (or in future shadow)
	 * is set, the renderer is replaced by an ExtendedRenderer.
	 * 
	 * Renderers do not check for visibility!
	 */
	protected Renderer renderer;

	abstract class Renderer {
		abstract void renderAll(int x, int y, PGraphics parentGraphics);

		// only to be called by renderer
		protected void drawBorder() {
			if (borderWidth > 0) {

				pg.noFill();
				pg.strokeWeight(borderWidth);
				pg.stroke(borderColor);

				// A 3x3 rect is drawn by PGraphics like this:
				// * * *
				// * * *
				// * * *
				// A 3x3 rect with 1px border is drawn by PGraphics like this:
				// + + + + ("+" demarking border pixels)
				// + * * +
				// + * * +
				// + + + +
				// Therefore, 1 px needs to be subtracted from border width/height when borderWidth==1
				float a = (borderWidth - 1) / 2f;

				if (borderRadius > 0)
					pg.rect(a, a, width - borderWidth, height - borderWidth, borderRadius); // this is slower
				else
					pg.rect(a, a, width - borderWidth, height - borderWidth);


			}
		}
	}

	abstract class UnbufferedRenderer extends Renderer {

//		protected void clip(PGraphics parentGraphics) {
//			// Old: only works with a nesting level of 2. 
//			// constrain drawing to this elements area and its parents area
////			int x0 = Math.max(0, offsetX), y0 = Math.max(0, offsetY);
////			int x1 = Math.min(offsetX + getWidth(), parent.getWidth()), y1 = Math.min(offsetY + getHeight(), parent.getHeight());
////			parentGraphics.clip(x0, y0, x1 - x0, y1 - y0);
//			
//			
//			
//		}


		protected void prepareGraphics(PGraphics parentGraphics) {
			intersectClip(offsetX, offsetY, offsetX + getWidth(), offsetY + getHeight());
			applyClip(parentGraphics);
			subtractFromClip(offsetX, offsetY);
			parentGraphics.pushMatrix();
			parentGraphics.translate(offsetX, offsetY);
			pg = parentGraphics;
		}

	}



	static int clipX0, clipY0, clipX1, clipY1; // Takes about half the time to use primitives instead of a rect class (measured on windows)

	static void intersectClip(int x0, int y0, int x1, int y1) {
		clipX0 = Math.max(clipX0, x0);
		clipY0 = Math.max(clipY0, y0);
		clipX1 = Math.min(clipX1, x1);
		clipY1 = Math.min(clipY1, y1);
	}

	static void subtractFromClip(int x, int y) {
		clipX0 -= x;
		clipX1 -= x;
		clipY0 -= y;
		clipY1 -= y;
	}

	static void applyClip(PGraphics pg) {
		pg.clip(clipX0, clipY0, clipX1 - clipX0, clipY1 - clipY0);
	}


	/*
	 * All renderers:
	 *  - call render()
	 *  - draw border
	 *  - set offsetX, offsetY
	 *  
	 * Buffer renderers:
	 *  - call preRender
	 * 	- project graphics onto parentGraphics
	 * 
	 * Non Buffer renderers:
	 *  - clip, translate
	 *  
	 * Extended renderers:
	 * 	- opacity
	 * 	- shadow
	 */

	class ShadowInformation {
		int size;
		int offsetX;
		int offsetY;
		int color;
		float opacity;

		ShadowInformation(int size, int offsetX, int offsetY, int color, float opacity) {
			this.size = size;
			this.offsetX = offsetX;
			this.offsetY = offsetY;
			this.color = color;
			this.opacity = opacity;
		}
	}

	protected void drawBoxShadow(PGraphics pg, ShadowInformation shadowInformation) {
		int r = (shadowInformation.color >> 16) & 0xFF;
		int g = (shadowInformation.color >> 8) & 0xFF;
		int b = shadowInformation.color & 0xFF;
		int size = shadowInformation.size;
		int halfsize = size / 2;
		int x = Control.this.offsetX + shadowInformation.offsetX - halfsize;
		int y = Control.this.offsetY + shadowInformation.offsetY - halfsize;

		pg.translate(x, y);
		pg.fill(GuisetColor.create(r, g, b, shadowInformation.opacity * 255));
		pg.noStroke();
		pg.rect(0, 0, width - size, height - size);

		pg.noFill();
		pg.strokeWeight(1);
		for (int i = 1; i < size; i++) {
			int alpha = Math.max(0, (int) (shadowInformation.opacity * (255 - i * (255 / size))));
			pg.stroke(GuisetColor.create(r, g, b, alpha));
			pg.rect(-i, -i, width + 2 * i - 1 - size, height + 2 * i - 1 - size);
		}
		pg.translate(-x, -y);
	}

	interface ExtendedRenderer {
		void setShadow(int size, int offsetX, int offsetY, int color, float opacity);

		void removeShadow();
	}

	// The standard renderer. Draw looks on own buffer graphics. Can get expensive
	// with memory if using a lot of elements.
	class BasicBufferedRenderer extends Renderer {
		void renderAll(int x, int y, PGraphics parentGraphics) {
			offsetX = x;
			offsetY = y;

			// no need to check for opacity. If it weren't 1 this would be an ExtendedBufferedRenderer

			if (dirty) {
				dirty = false; // before render(), maybe render() wants to call update for some reason
				preRender();
				render();
				drawBorder();
				pg.endDraw();
			}
			parentGraphics.image(pg, x, y);
		}
	}

	// Renderer that implements opacity and in future also box-shadows. Also draw
	// looks on own buffer graphics.
	class ExtendedBufferedRenderer extends Renderer implements ExtendedRenderer {

		private ShadowInformation shadowInformation;

		@Override
		void renderAll(int x, int y, PGraphics parentGraphics) {
			offsetX = x;
			offsetY = y;

			if (opacity == 0)
				return;

			if (dirty) {
				dirty = false;
				preRender();
				render();
				drawBorder();
				pg.endDraw();
			}

			if (shadowInformation != null) {
				drawBoxShadow(parentGraphics, shadowInformation);
			}
			if (opacity < 1.0f) {
				parentGraphics.tint(255, (int) (opacity * 256));
				parentGraphics.image(pg, x, y);
				parentGraphics.tint = false;
			} else {
				parentGraphics.image(pg, x, y);
			}
		}

		public void setShadow(int size, int offsetX, int offsetY, int color, float opacity) {
			shadowInformation = new ShadowInformation(size, offsetX, offsetY, color, opacity);
		}

		public void removeShadow() {
			shadowInformation = null;
		}
	}




	// This renderer doesn't use a buffer PGraphics -> save RAM
	// Instead draw on parent graphics.
	// Issues: Items can overflow parent and their own bounds
	class BasicUnbufferedRenderer extends UnbufferedRenderer {
		@Override
		void renderAll(int x, int y, PGraphics parentGraphics) {
			offsetX = x;
			offsetY = y;

			int dx0 = clipX0, dy0 = clipY0, dx1 = clipX1, dy1 = clipY1; // child items change the clip and we need to be able to reset it.
			prepareGraphics(parentGraphics);
			PFont f = parentGraphics.textFont;

			render(); // no preRender(), pg.endDraw() needed
			drawBorder();
			if (f != null)
				parentGraphics.textFont(f); // might need to reset font if textRenderer is an extended one

			parentGraphics.popMatrix();

			clipX0 = dx0;
			clipX1 = dx1;
			clipY0 = dy0;
			clipY1 = dy1;
			applyClip(parentGraphics); // AFTER resetting translation
		}
	}







	// As ParentGraphicsRenderer but implementing opactiy and in future box-shadows.
	private class ExtendedUnbufferedRenderer extends UnbufferedRenderer implements ExtendedRenderer {
		private ShadowInformation shadowInformation;


		@Override
		public void renderAll(int x, int y, PGraphics parentGraphics) {
			offsetX = x;
			offsetY = y;

			if (opacity == 0)
				return;

			if (shadowInformation != null) {
				drawBoxShadow(parentGraphics, shadowInformation);
			}

			if (opacity < 1.0f) {
				pg = getPApplet().createGraphics(getWidth(), getHeight());
				pg.beginDraw();

				render(); // no preRender() needed
				drawBorder();
				pg.endDraw();


				parentGraphics.tint(255, (int) (opacity * 256));
				parentGraphics.image(pg, x, y);
				parentGraphics.tint = false;

				pg = null; // gc may delete the temporary graphics now

			} else {
				// Should be exactly like BasicUnbufferedRenderer.renderAll() (except setting offsetX/Y)
				int dx0 = clipX0, dy0 = clipY0, dx1 = clipX1, dy1 = clipY1;
				prepareGraphics(parentGraphics);
				PFont f = parentGraphics.textFont;
				render(); // no preRender()/pg.endDraw() needed
				drawBorder();

				if (f != null)
					parentGraphics.textFont(f); // might need to reset font if textRenderer is an extended one

				parentGraphics.popMatrix();

				clipX0 = dx0;
				clipX1 = dx1;
				clipY0 = dy0;
				clipY1 = dy1;
				applyClip(parentGraphics); // AFTER resetting translation
			}
		}

		@Override
		public void setShadow(int size, int offsetX, int offsetY, int color, float opacity) {
			shadowInformation = new ShadowInformation(size, offsetX, offsetY, color, opacity);
		}

		@Override
		public void removeShadow() {
			shadowInformation = null;
		}
	}

	protected void setToBufferedRenderer() {
		if (renderer instanceof BasicUnbufferedRenderer) {
			renderer = new BasicBufferedRenderer();
		} else if (renderer instanceof ExtendedUnbufferedRenderer) {
			ShadowInformation si = ((ExtendedUnbufferedRenderer) renderer).shadowInformation;
			renderer = new ExtendedBufferedRenderer();
			((ExtendedBufferedRenderer) renderer).shadowInformation = si;
		}
	}

	protected void setToUnbufferedRenderer() {
		if (renderer instanceof BasicBufferedRenderer) {
			renderer = new BasicUnbufferedRenderer();
		} else if (renderer instanceof ExtendedBufferedRenderer) {
			ShadowInformation si = ((ExtendedBufferedRenderer) renderer).shadowInformation;
			renderer = new ExtendedUnbufferedRenderer();
			((ExtendedUnbufferedRenderer) renderer).shadowInformation = si;
		}
	}


	protected void enableExtendedRenderer() {
		if (renderer instanceof BasicBufferedRenderer) {
			renderer = new ExtendedBufferedRenderer();
		} else if (renderer instanceof BasicUnbufferedRenderer) {
			renderer = new ExtendedUnbufferedRenderer();
		}
	}

	/**
	 * Add rectangular box shadow to element.
	 * 
	 * @param size    shadow size
	 * @param offsetX x offset
	 * @param offsetY y offset
	 * @param color   shadow color
	 * @param opacity shadow opacity (0 is transparent, 1 is opaque)
	 */
	public void setBoxShadow(int size, int offsetX, int offsetY, int color, float opacity) {
		enableExtendedRenderer();
		((ExtendedRenderer) renderer).setShadow(size, offsetX, offsetY, color, opacity);
	}

	public void removeBoxShadow() {
		if (renderer instanceof ExtendedRenderer)
			((ExtendedRenderer) renderer).removeShadow();
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
	 * Method that is called when properties of a Component that influence the appearance change, i.e.
	 * called in most setters.
	 */
	protected void update() {
		dirty = true;
		if (parent != null) {
			parent.update();
		}
	}

	/**
	 * Force a re-render of this Component. This shouldn't be needed, but just in case.
	 */
	public void forceRepaint() {
		update();
	}











	/*	
	void setBGColor(int e) {
		cw = cw.setBackground(e);
	}
	
	
	ColorWrapper cw = new SingleColor();
	
	abstract class ColorWrapper {
	
		abstract int getBackground();
	
		abstract int getBackgroundHovered();
	
		abstract int getBackgroundPressed();
	
		abstract int getForeground();
	
		abstract int getForegroundHovered();
	
		abstract int getForegroundPressed();
	
		abstract int getBorder();
	
		abstract ColorWrapper setBackground(int e);
	
		abstract ColorWrapper setForeground(int e);
	
		abstract ColorWrapper setBorder(int e);
	
		abstract ColorWrapper setBackgroundHovered(int e);
	
		abstract ColorWrapper setBackgroundPressed(int e);
	
		abstract ColorWrapper setForegroundHovered(int e);
	
		abstract ColorWrapper setForegroundPressed(int e);
	
	}
	
	class SingleColor extends ColorWrapper {
		int all;
	
		@Override
		int getBackground() {
			return all;
		}
	
		@Override
		int getBackgroundHovered() {
			return all;
		}
	
		@Override
		int getBackgroundPressed() {
			return all;
		}
	
		@Override
		int getForeground() {
			return all;
		}
	
		@Override
		int getForegroundHovered() {
			return all;
		}
	
		@Override
		int getForegroundPressed() {
			return all;
		}
	
		@Override
		int getBorder() {
			return all;
		}
	
		@Override
		ColorWrapper setBackground(int e) {
			return new BasicColor(e, all, all);
		}
	
		@Override
		ColorWrapper setForeground(int e) {
			return new BasicColor(all, e, all);
		}
	
		@Override
		ColorWrapper setBorder(int e) {
			return new BasicColor(all, all, e);
		}
	
		@Override
		ColorWrapper setBackgroundHovered(int e) {
			return new ResponsiveColor(all, all, all, e, all, all, all);
		}
	
		@Override
		ColorWrapper setBackgroundPressed(int e) {
			return new ResponsiveColor(all, all, all, all, e, all, all);
		}
	
		@Override
		ColorWrapper setForegroundHovered(int e) {
			return new ResponsiveColor(all, all, all, all, all, e, all);
		}
	
		@Override
		ColorWrapper setForegroundPressed(int e) {
			return new ResponsiveColor(all, all, all, all, all, all, e);
		}
	
	}
	
	class BasicColor extends ColorWrapper {
		int background;
		int foreground;
		int border;
	
		BasicColor() {
	
		}
	
		BasicColor(int background, int foreground, int border) {
			this.background = background;
			this.foreground = foreground;
			this.border = border;
		}
	
		@Override
		int getBackground() {
			return background;
		}
	
		@Override
		int getBackgroundHovered() {
			return background;
		}
	
		@Override
		int getBackgroundPressed() {
			return background;
		}
	
		@Override
		int getForeground() {
			return foreground;
		}
	
		@Override
		int getForegroundHovered() {
			return foreground;
		}
	
		@Override
		int getForegroundPressed() {
			return foreground;
		}
	
		@Override
		int getBorder() {
			return border;
		}
	
		@Override
		ColorWrapper setBackground(int e) {
			this.background = e;
			return this;
		}
	
		@Override
		ColorWrapper setForeground(int e) {
			this.foreground = e;
			return this;
		}
	
		@Override
		ColorWrapper setBorder(int e) {
			this.border = e;
			return this;
		}
	
		@Override
		ColorWrapper setBackgroundHovered(int e) {
			return new ResponsiveColor(background, foreground, border, e, background, foreground, foreground);
		}
	
		@Override
		ColorWrapper setBackgroundPressed(int e) {
			return new ResponsiveColor(background, foreground, border, background, e, foreground, foreground);
		}
	
		@Override
		ColorWrapper setForegroundHovered(int e) {
			return new ResponsiveColor(background, foreground, border, background, background, e, foreground);
		}
	
		@Override
		ColorWrapper setForegroundPressed(int e) {
			return new ResponsiveColor(background, foreground, border, background, background, foreground, e);
		}
	}
	
	class ResponsiveColor extends BasicColor {
		int backgroundHovered;
		int backgroundPressed;
		int foregroundHovered;
		int foregroundPress;
	
		ResponsiveColor() {
	
		}
	
		ResponsiveColor(int bg, int fg, int b, int bg_h, int bg_p, int fg_h, int fg_p) {
			super(bg, fg, b);
			this.backgroundHovered = bg_h;
			this.backgroundPressed = bg_p;
			this.foregroundHovered = fg_h;
			this.foregroundPress = fg_p;
		}
	
		@Override
		int getBackgroundHovered() {
			return backgroundHovered;
		}
	
		@Override
		int getBackgroundPressed() {
			return backgroundPressed;
		}
	
		@Override
		int getForegroundHovered() {
			return foregroundHovered;
		}
	
		@Override
		int getForegroundPressed() {
			return foregroundPress;
		}
	
		@Override
		ColorWrapper setBackgroundHovered(int e) {
			this.backgroundHovered = e;
			return this;
		}
	
		@Override
		ColorWrapper setBackgroundPressed(int e) {
			this.backgroundPressed = e;
			return this;
		}
	
		@Override
		ColorWrapper setForegroundHovered(int e) {
			this.foregroundHovered = e;
			return this;
		}
	
		@Override
		ColorWrapper setForegroundPressed(int e) {
			this.foregroundPress = e;
			return this;
		}
	
	}
	*/

//	protected void drawShadow(PGraphics pg, int w, int h, int size, int offset) {
//		pg.noFill();
//		pg.strokeWeight(1);
//		pg.translate(offsetX, offsetY);
//		// pg.beginShape(PApplet.LINES);
//		for (int i = 0; i < size; i++) {
//			int alpha = 117 - 29 * i;
//			pg.stroke(Color.create(0, alpha));
//			// pg.stroke(Color.create(0, cl[i]));
//			pg.line(w + i, offset - i + 1, w + i, h + i - 1); // v line
//			pg.line(offset - i + 1, h + i, w + i, h + i); // lower h line
//			pg.line(offset - i, h, offset - i, h + i); // left v line
//			pg.line(w, size - i, w + i, size - i); // upper h line
//			// pg.rect((offset - 1) * 2 - i, (offset - 1) * 2 - i, w - 2 * (size-1 - i), h - 2 * (size-1 - i));
//			// pg.line(x1, y1, x2, y2);
//		}
//		pg.translate(-(offsetX), -(offsetY));
//		// pg.endShape();
//	}

//	public void drawBoxShadow(PGraphics pg, int size, int offsetX, int offsetY, int color, float transparency) {
//		int r = (color >> 16) & 0xFF;
//		int g = (color >> 8) & 0xFF;
//		int b = color & 0xFF;
//		int halfsize = size / 2;
//		int x = Control.this.offsetX + offsetX - halfsize;
//		int y = Control.this.offsetY + offsetY - halfsize;
//
//		pg.translate(x, y);
//		pg.fill(Color.create(r, g, b, transparency * 255));
//		pg.noStroke();
//		pg.rect(0, 0, width - size, height - size);
//
//		pg.noFill();
//		pg.strokeWeight(1);
//		// getPApplet().red();
//		// pg.beginShape(PApplet.LINES);
//		for (int i = 1; i < size; i++) {
//			int alpha = Math.max(0, (int) (transparency * (255 - i * (255 / size))));
//			pg.stroke(Color.create(r, g, b, alpha));
//			pg.rect(-i, -i, width + 2 * i - 1 - size, height + 2 * i - 1 - size);
//		}
//		pg.translate(-x, -y);
//		// pg.endShape();
//	}



	/**
	 * Standard background drawing method - this method can be used by any control to draw its
	 * background. Features backgroundColors, images and transparency. Borders are drawn in renderer.
	 */
	protected void drawDefaultBackground() {
		if (image == null) {
			// int visualBackgroundColor = pPressed ? pressedColor : pHovered ? hoverColor :
			// backgroundColor;

			if (visualBackgroundColor != 0) {
				pg.fill(visualBackgroundColor);
			} else {
				pg.noFill();
			}
			pg.noStroke();
			pg.rect(borderWidth / 2, borderWidth / 2, width - borderWidth, height - borderWidth, borderRadius);
			pg.rect(0, 0, getWidth(), getHeight(), borderRadius);

		} else {

			// All this stuff does not work :( now using mask() instead
			/*pg.beginShape(PApplet.QUADS);
			
			if (borderRadius != 0) {
				pg.vertex(width - borderRadius, 0);
				pg.quadraticVertex(width, 0, width, 0 + borderRadius);
			} else {
				pg.vertex(width, 0);
			}
			if (borderRadius != 0) {
				pg.vertex(width, height - borderRadius);
				pg.quadraticVertex(width, height, width - borderRadius, height);
			} else {
			}
			if (borderRadius != 0) {
				pg.vertex(0 + borderRadius, height);
				pg.quadraticVertex(0, height, 0, height - borderRadius);
			} else {
			}
			if (borderRadius != 0) {
				pg.vertex(0, 0 + borderRadius);
				pg.quadraticVertex(0, 0, 0 + borderRadius, 0);
			} else {
			}
			pg.endShape();*/

			if (imageMode == FILL) {

				pg.image(image, 0, 0, width, height);
			} else if (imageMode == FIT) {

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

			// If there is a border radius, we need to mask the image
			if (borderRadius > 0) {
				PGraphics maskImage = getPApplet().createGraphics(width, height);
				maskImage.beginDraw();
				maskImage.noStroke();
				maskImage.rect(borderWidth / 2, borderWidth / 2, width - borderWidth, height - borderWidth, borderRadius);
				maskImage.endDraw();
				pg.mask(maskImage);
			}

			// Draw pressedColor/hoverColor over image.
			if (borderWidth > 0) {
				if (pPressed) {
					pg.fill(pressedColor);
				} else if (pHovered) {
					pg.fill(hoverColor);
				} else {
					pg.noFill();
				}
				pg.noStroke();
				pg.rect(borderWidth / 2, borderWidth / 2, width - borderWidth, height - borderWidth, borderRadius);
			}
		}
	}


	/**
	 * Standard disabled-state drawing feature. If called at end of render() it will draw a transparent
	 * grey rectangle upon the control to indicate its disabled state.
	 */
	protected void drawDefaultDisabled() {
		if (!enabled) {
			pg.fill(200, 100);
			pg.noStroke();
			pg.rect(borderWidth / 2, borderWidth / 2, width - borderWidth, height - borderWidth, borderRadius);
		}
	}

	protected void setVisualBackgroundColor(int color) {
		if (color != visualBackgroundColor) {
			visualBackgroundColor = color;
			update();
		}
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
	 * parent container changes in size. I.e. when the RIGHT anchor is set, the
	 * control will keep the distance between its right edge and the right edge of
	 * the container (like a right-alignment). When RIGHT and LEFT anchors are set, both
	 * right and left edges will keep their distance to the containers bounds which
	 * results in a new size of this element. Same for TOP and BOTTOM. 
	 * 
	 * The anchors-array is set to all ANCHOR_NOT_SET (Integer.MIN_VALUE) per default which means they are
	 * inactive. When an anchor is added, it stores the distance from the top,
	 * bottom, left or right edge of this element to the according edge of the
	 * container. When the parentResized() function is called, the anchors will be checked
	 * and size/position of the element accordingly adjusted.
	 *
	 * The new size of the control will be constrained by minimal and maximal
	 * width/height obviously. If the new size cannot be fully attained, the control
	 * will orient at the top left of the container.
	 * 
	 * 
	 * Each anchor can be in PIXEL_MODE (default) or PERCENTAGE_MODE. 
	 * 
	 */

	// The anchor array used to store the anchors data.
	// In following order: TOP, RIGHT, BOTTOM, LEFT:
	private int[] anchors = { ANCHOR_INACTIVE, ANCHOR_INACTIVE, ANCHOR_INACTIVE, ANCHOR_INACTIVE };

	// from outside, PApplet.TOP, RIGHT... is used, but these match the indices in
	// the anchor array.
	private static final int TOP_ANCHOR = 0;
	private static final int RIGHT_ANCHOR = 1;
	private static final int BOTTOM_ANCHOR = 2;
	private static final int LEFT_ANCHOR = 3;




	private short anchorMode = 0; // Mode for each anchor. The anchors can be in pixel or percentage mode.
	private boolean anyAnchorsActive = false; // Is any anchor active at all? Enables quick checks

	/**
	 * The value of an inactive anchor is set to {@link #ANCHOR_INACTIVE}.
	 */
	public static final int ANCHOR_INACTIVE = Integer.MIN_VALUE;
	/**
	 * Anchor mode for percentage of parents size.
	 */
	public static final boolean PERCENTAGE_MODE = true;
	/**
	 * Anchor mode for absolute pixel values.
	 */
	public static final boolean PIXEL_MODE = false;

	// Indicates that setWidth/HeightNoUpdate() should not update the anchors.
	// Temporarily changed to true by parentResized() to set width without
	// disturbing anchors.
	private static boolean PAUSE_UPDATE_ANCHORS = false;




	/*
	 * Internal anchor helper functions
	 */


	// Check if certain anchor is active. Expects LEFT_ANCHOR, TOP_ANCHOR, RIGHT_ANCHOR or BOTTOM_ANCHOR
	private boolean isAnchorActive(int anchor) {
		return anchors[anchor] != ANCHOR_INACTIVE;
	}

	// Is at least one anchor active?
	private boolean anyAnchorsActive() {
		return anyAnchorsActive;
	}

	// Activate anchors. To be set, when an anchor is set to a specific value.
	private void activateUseOfAnchors() {
		anyAnchorsActive = true;
	}

	// Should only be called if all anchors are inactive.
	private void deactivateUseOfAnchors() {
		anyAnchorsActive = false;
	}

	// Deactivate a certain anchor. Expects LEFT_ANCHOR, TOP_ANCHOR, RIGHT_ANCHOR or BOTTOM_ANCHOR
	private void deactivateAnchor(int anchor) {
		anchors[anchor] = ANCHOR_INACTIVE;
	}

	private void setAnchorImpl(int anchor, int value) {
		anchors[anchor] = value;
		activateUseOfAnchors();
	}

	// Set the according bit in anchorMode to the given mode
	// Expects LEFT_ANCHOR, TOP_ANCHOR, RIGHT_ANCHOR or BOTTOM_ANCHOR
	// and
	// PIXEL_MODE or PERCENTAGE_MODE
	private void setAnchorModeImpl(int anchor, boolean percentage) {
		if (percentage)
			anchorMode |= 1 << anchor;
		else
			anchorMode &= ~1 << anchor;
	}

	// Returns either PIXEL_MODE or PERCENTAGE_MODE
	private boolean getAnchorModeImpl(int anchor) {
		return (anchorMode & (1 << anchor)) != 0;
	}




	// get left position in pixel according to LEFT_ANCHOR
	private final int getLeft() {
		if (getAnchorModeImpl(LEFT_ANCHOR) == PERCENTAGE_MODE)
			return anchors[LEFT_ANCHOR] * parent.getWidth() / 100;
		else
			return anchors[LEFT_ANCHOR];
	}

	// get position of right side from left of element in pixel according to
	// RIGHT_ANCHOR
	private final int getRight() {
		if (getAnchorModeImpl(RIGHT_ANCHOR) == PERCENTAGE_MODE)
			return parent.getWidth() - anchors[RIGHT_ANCHOR] * parent.getWidth() / 100;
		else
			return parent.getWidth() - anchors[RIGHT_ANCHOR];
	}

	// get top position in pixel according to TOP_ANCHOR
	private final int getTop() {
		if (getAnchorModeImpl(TOP_ANCHOR) == PERCENTAGE_MODE)
			return anchors[TOP_ANCHOR] * parent.getHeight() / 100;
		else
			return anchors[TOP_ANCHOR];
	}

	// get position of bottom side from top of element in pixel according to
	// BOTTOM_ANCHOR
	private final int getBottom() {
		if (getAnchorModeImpl(BOTTOM_ANCHOR) == PERCENTAGE_MODE)
			return parent.getHeight() - anchors[BOTTOM_ANCHOR] * parent.getHeight() / 100;
		else
			return parent.getHeight() - anchors[BOTTOM_ANCHOR];
	}







	/**
	 * Set an anchor of type TOP BOTTOM, LEFT or RIGHT to a certain value. This also immediately sets
	 * the position or size of this element according to the anchors if necessary.
	 * 
	 * It is not possible to use this method if this element has not been added to a parent container.
	 * 
	 * This version sets the anchor without changing its type like
	 * {@link #setPercentageAnchor(int, int)} and {@link #setPixelAnchor(int, int)}.
	 * 
	 * @param anchor anchor direction
	 * @param value  value
	 */
	public void setAnchor(int anchor, int value) {
		if (parent == null) {
			System.err.println("setAnchor() ignored: element " + this + " has no parent");
			return;
		}

		switch (anchor) {
		case Constants.TOP:
			setAnchorImpl(TOP_ANCHOR, value);

			// set y now, because resize only changes y if BOTTOM is set. (elements are
			// always "anchored" to TOP). This way everything is alright. Still call
			// parentResized because maybe BOTTOM_ANCHOR is set too.
			this.y = value;
			parentResized();
			break;
		case Constants.RIGHT:
			setAnchorImpl(RIGHT_ANCHOR, value);
			parentResized();
			break;
		case Constants.BOTTOM:
			setAnchorImpl(BOTTOM_ANCHOR, value);
			parentResized();
			break;
		case Constants.LEFT:
			setAnchorImpl(LEFT_ANCHOR, value);

			// set x now, because resize only changes x if RIGHT is set. This way everything is alright
			this.x = value;
			parentResized();
			break;
		default:
			return;
		}

	}


	/**
	 * Set any number of anchors by passing first a direction and then the value (more than four - one
	 * for each direction - don't make sense though). The anchor mode ({@link #PIXEL_MODE} or
	 * {@link #PERCENTAGE_MODE} is not changed. Example:
	 * 
	 * setAnchors(LEFT, 34, RIGHT, 50);
	 * 
	 * setAnchors(BOTTOM, 0, LEFT, 1, RIGHT, 20, TOP, 50);
	 * 
	 * @param anchors_and_values anchors and values
	 */
	public void setAnchors(int... anchors_and_values) {
		if (anchors_and_values.length < 2) // WTF has the user entered
			return;
		for (int i = 0; i < anchors_and_values.length; i += 2) {
			setAnchor(anchors_and_values[i], anchors_and_values[i + 1]);
		}
	}

	/**
	 * Set an anchor like with {@link #setAnchor(int, int)} but definitely use the {@link #PIXEL_MODE}.
	 * The anchor will be set to [value] pixels.
	 * 
	 * @param anchor LEFT, RIGHT, TOP or BOTTOM
	 * @param value  value
	 */
	public void setPixelAnchor(int anchor, int value) {
		switch (anchor) {
		case Constants.TOP:
			setAnchorModeImpl(TOP_ANCHOR, PIXEL_MODE);
			break;
		case Constants.RIGHT:
			setAnchorModeImpl(RIGHT_ANCHOR, PIXEL_MODE);
			break;
		case Constants.BOTTOM:
			setAnchorModeImpl(BOTTOM_ANCHOR, PIXEL_MODE);
			break;
		case Constants.LEFT:
			setAnchorModeImpl(LEFT_ANCHOR, PIXEL_MODE);
			break;
		}
		setAnchor(anchor, value);
	}

	/**
	 * Combination of {@link #setAnchors(int...)} and {@link #setPixelAnchor(int, int)}.
	 * 
	 * @param anchors_and_values anchors and values
	 */
	public void setPixelAnchors(int... anchors_and_values) {
		if (anchors_and_values.length < 2)
			return;
		for (int i = 0; i < anchors_and_values.length; i += 2) {
			setPixelAnchor(anchors_and_values[i], anchors_and_values[i + 1]);
		}
	}

	/**
	 * Set an anchor like in {@link #PERCENTAGE_MODE}. This will place or resize the element in percent
	 * relative to the size of the parent (give i.e. 100 for 100%).
	 * 
	 * @param anchor anchor
	 * @param value  value
	 */
	public void setPercentageAnchor(int anchor, int value) {
		switch (anchor) {
		case Constants.TOP:
			setAnchorModeImpl(TOP_ANCHOR, PERCENTAGE_MODE);
			break;
		case Constants.RIGHT:
			setAnchorModeImpl(RIGHT_ANCHOR, PERCENTAGE_MODE);
			break;
		case Constants.BOTTOM:
			setAnchorModeImpl(BOTTOM_ANCHOR, PERCENTAGE_MODE);
			break;
		case Constants.LEFT:
			setAnchorModeImpl(LEFT_ANCHOR, PERCENTAGE_MODE);
			break;
		}
		setAnchor(anchor, value);
	}

	/**
	 * Combination of {@link #setAnchors(int...)} and {@link #setPercentageAnchor(int, int)}.
	 * 
	 * @param anchors_and_values anchors and values
	 */
	public void setPercentageAnchors(int... anchors_and_values) {
		if (anchors_and_values.length < 2)
			return;
		for (int i = 0; i < anchors_and_values.length; i += 2) {
			setPercentageAnchor(anchors_and_values[i], anchors_and_values[i + 1]);
		}
	}




	/**
	 * Get anchor mode for given anchor direction. Returns a boolean {@link #PIXEL_MODE} (false) or
	 * {@link #PERCENTAGE_MODE} (true).
	 * 
	 * @param anchor anchor direction
	 * @return anchor mode
	 */
	public boolean getAnchorMode(int anchor) {
		switch (anchor) {
		case Constants.TOP:
			return getAnchorModeImpl(TOP_ANCHOR);
		case Constants.RIGHT:
			return getAnchorModeImpl(RIGHT_ANCHOR);
		case Constants.BOTTOM:
			return getAnchorModeImpl(BOTTOM_ANCHOR);
		case Constants.LEFT:
			return getAnchorModeImpl(LEFT_ANCHOR);
		default:
			return false;
		}
	}

	/**
	 * Get the value of an anchor, returns {@link #ANCHOR_INACTIVE} if the anchor is inactive.
	 * 
	 * @param anchor anchor direction
	 * @return anchor value in pixel or percent, depending on corresponding nchor mode
	 */
	public int getAnchorValue(int anchor) {
		switch (anchor) {
		case Constants.TOP:
			return anchors[TOP_ANCHOR];
		case Constants.RIGHT:
			return anchors[RIGHT_ANCHOR];
		case Constants.BOTTOM:
			return anchors[BOTTOM_ANCHOR];
		case Constants.LEFT:
			return anchors[LEFT_ANCHOR];
		default:
			return ANCHOR_INACTIVE;
		}
	}







	/**
	 * Add an anchor (types are TOP, RIGHT, BOTTOM, LEFT). Anchors ensure the element stays at fixed
	 * relative positions when the parent is resized. If opposite anchors are set, the element will be
	 * resized as well. When this function is called, the element will remember the CURRENT(!) distances
	 * to the parents bounds. Multiple anchors can be set with this method.
	 * 
	 * This is currently only intended with {@link #PIXEL_MODE} and not with {@link #PERCENTAGE_MODE}
	 * (mode will be set to the {@link #PIXEL_MODE} if the latter is currently selected).
	 * 
	 * It is not possible to use this method if this element has not been added to a parent.
	 * 
	 * @param anchor accepts TOP, RIGHT, LEFT or BOTTOM. Pass up to four anchor types (order doesn't
	 *               matter)
	 */
	public void addAutoAnchors(int... anchor) {
		if (parent == null) {
			System.err.println("addAutoAnchor() ignored: element " + this + " has no parent");
			return;
		}

		for (int a : anchor) {
			switch (a) {
			case Constants.TOP:
				setAnchorImpl(TOP_ANCHOR, getY());
				setAnchorModeImpl(TOP_ANCHOR, PIXEL_MODE);
				break;
			case Constants.RIGHT:
				setAnchorImpl(RIGHT_ANCHOR, parent.getWidth() - getWidth() - getX());
				setAnchorModeImpl(RIGHT_ANCHOR, PIXEL_MODE);
				break;
			case Constants.BOTTOM:
				setAnchorImpl(BOTTOM_ANCHOR, parent.getHeight() - getHeight() - getY());
				setAnchorModeImpl(BOTTOM_ANCHOR, PIXEL_MODE);
				break;
			case Constants.LEFT:
				setAnchorImpl(LEFT_ANCHOR, getX());
				setAnchorModeImpl(LEFT_ANCHOR, PIXEL_MODE);
				break;
			default:
				continue;
			}
		}
	}






	/**
	 * Remove a set anchor. Removing the last anchor will set all anchor.
	 * 
	 * @param anchor accepts TOP, RIGHT, LEFT or BOTTOM
	 */
	public void removeAnchor(int anchor) {
		switch (anchor) {
		case Constants.TOP:
			deactivateAnchor(TOP_ANCHOR);
			break;
		case Constants.RIGHT:
			deactivateAnchor(RIGHT_ANCHOR);
			break;
		case Constants.BOTTOM:
			deactivateAnchor(BOTTOM_ANCHOR);
			break;
		case Constants.LEFT:
			deactivateAnchor(LEFT_ANCHOR);
			break;
		}
		if (!isAnchorActive(TOP_ANCHOR) && !isAnchorActive(RIGHT_ANCHOR) && !isAnchorActive(LEFT_ANCHOR) && !isAnchorActive(BOTTOM_ANCHOR)) {
			deactivateUseOfAnchors();
		}
	}

	/**
	 * Fill x percent of parent width and center this element horizontally.
	 * 
	 * @param percent percent 100 for 100%
	 */
	public void fillParentWidth(int percent) {
		setAnchors(Constants.LEFT, (100 - percent) / 2, Constants.RIGHT, (100 - percent) / 2);
	}

	/**
	 * Fill parent width.
	 */
	public void fillParentWidth() {
		fillParentWidth(100);
	}

	/**
	 * Fill x percent of parent height and center this element vertically.
	 * 
	 * @param percent percent 100 for 100%
	 */
	public void fillParentHeight(int percent) {
		setAnchors(Constants.TOP, (100 - percent) / 2, Constants.BOTTOM, (100 - percent) / 2);
	}

	/**
	 * Fill parent height.
	 */
	public void fillParentHeight() {
		fillParentHeight(100);
	}

	/**
	 * Fill parent width and height.
	 */
	public void fillParent() {
		fillParent(100);
	}

	/**
	 * Fill x percent of parents width and height.
	 * 
	 * @param percent percent
	 */
	public void fillParent(int percent) {
		fillParent(percent, percent);
	}

	/**
	 * Fill widthPercent of parent width and heightPercent of parent height.
	 * 
	 * @param widthPercent  width percentage
	 * @param heightPercent height percentage
	 */
	public void fillParent(int widthPercent, int heightPercent) {
		fillParentHeight(heightPercent);
		fillParentWidth(widthPercent);
	}



	/**
	 * Containers call this for all items to inform them that their size has changed. Items might want
	 * to adjust their size or position according to that if they have active anchors.
	 */
	protected final void parentResized() {
		if (anyAnchorsActive()) {
			if (isAnchorActive(RIGHT_ANCHOR)) {
				if (isAnchorActive(LEFT_ANCHOR)) { // LEFT and RIGHT
					PAUSE_UPDATE_ANCHORS = true;
					setWidth(getRight() - getLeft());
					setX(getLeft());
					update();
					PAUSE_UPDATE_ANCHORS = false;
				} else { // Only RIGHT
					setX(getRight() - width);
				}
			} else if (isAnchorActive(LEFT_ANCHOR) && getAnchorModeImpl(LEFT_ANCHOR) == PERCENTAGE_MODE) { // Only LEFT
				setX(getLeft());
			}
			if (isAnchorActive(BOTTOM_ANCHOR)) {
				if (isAnchorActive(TOP_ANCHOR)) { // TOP and BOTTOM
					PAUSE_UPDATE_ANCHORS = true;
					setHeight(getBottom() - getTop());
					setY(getTop());
					update();
					PAUSE_UPDATE_ANCHORS = false;
				} else { // Only BOTTOM
					setY(getBottom() - height);
				}
			} else if (isAnchorActive(TOP_ANCHOR) && getAnchorModeImpl(TOP_ANCHOR) == PERCENTAGE_MODE) { // Only TOP
				setY(getTop());
			}
		}
	}










	/*__________________________________________________________________________________________________________
	 * 
	 * AUTO-SIZE
	 * 
	 * 
	 * autosizeRule specifies actions that will set width/height new when i.e.
	 * padding, text or fontSize is changed. Each class can override it to specify
	 * custom calculations.
	 * 
	 * If properties change that are regarded in autosizeRule(), their setter needs
	 * to call autosize().
	 * 
	 * I.e. setPadding(), setText(), setFontSize(), Checkbox.setCheckboxSize() all
	 * do.
	 * 
	 * 
	 * TODO
	 * As position anchors are stronger than autosize rules, autosizing is disabled
	 * for elements with anchors. ???
	 * 
	 * In autosizeRule() setWidthImpl() and
	 * setHeightImpl() should be used.
	 */
	public static int AUTOSIZE_NONE = 0;
	public static int AUTOSIZE_WIDTH = 1;
	public static int AUTOSIZE_HEIGHT = 2;
	public static int AUTOSIZE_BOTH = AUTOSIZE_WIDTH | AUTOSIZE_HEIGHT;
	int autosizing = AUTOSIZE_BOTH;

	/**
	 * Enable/disable autosizing.
	 * 
	 * @param autosizing autosizing
	 */
	public void setAutosizing(boolean autosizing) {
		this.autosizing = autosizing ? AUTOSIZE_BOTH : AUTOSIZE_NONE;
	}

	/**
	 * Disable autosizing.
	 */
	public void noAutosize() {
		autosizing = AUTOSIZE_NONE;
	}

	/**
	 * Called indirectly through autosize(). This is the component-specific implementation of
	 * autosizing.
	 */
	/**
	 * Autosize width routine to be overriden by subclasses. Do not set the width, just return the
	 * desired value. If a negative value is returned, then the result is ignored.
	 * 
	 * @return new desired width
	 */
	protected int autoWidth() {
		return -1;
	}

	/**
	 * Autosize width routine to be overriden by subclasses. Do not set the height, just return the
	 * desired value. If a negative value is returned, then the result is ignored.
	 * 
	 * @return new desired height
	 */
	protected int autoHeight() {
		return -1;
	}

	/**
	 * Called in setPadding, setText, setFontSize, setBold etc. Redirects to autoHeight()/autoWidth() if
	 * autosizing is enabled. Is protected because other classes might call it in other setters.
	 * 
	 * Returns true if size has actually changed.
	 */
	protected final boolean autosize() {
		boolean sizeChanged = false;
		if ((autosizing & AUTOSIZE_WIDTH) != 0) {
			int w = autoWidth();
			if (w >= 0) {
				sizeChanged = setWidthNoUpdate(w);
			}
		}
		if ((autosizing & AUTOSIZE_HEIGHT) != 0) {
			int h = autoHeight();
			if (h >= 0) {
				sizeChanged |= setHeightNoUpdate(h);
			}
		}
		return sizeChanged;
	}





	/* __________________________________________________________________________________________________________
	 * 
	 * STYLE SETTERS
	 * 
	 */

	/**
	 * Set x-coordinate of element relative to parent. Does not apply if element is added to containers
	 * that provide some own layout.
	 * 
	 * @param x x-coordinate in pixel
	 */
	public void setX(int x) {
		if (x == this.x)
			return;
		this.x = x;
		if (anyAnchorsActive()) {
			// only need to update anchor if in pixel mode

			if (isAnchorActive(LEFT_ANCHOR) && getAnchorModeImpl(LEFT_ANCHOR) == PIXEL_MODE) {
				setAnchorImpl(LEFT_ANCHOR, getX());
			}
			if (isAnchorActive(RIGHT_ANCHOR) && getAnchorModeImpl(RIGHT_ANCHOR) == PIXEL_MODE) {
				setAnchorImpl(RIGHT_ANCHOR, parent.getWidth() - getWidth() - getX());
			}
		}
		update();
	}



	/**
	 * Set y-coordinate of element relative to parent. Does not apply if element is added to containers
	 * that provide some own layout.
	 * 
	 * @param y y-coordinate in pixel
	 */
	public void setY(int y) {
		if (y == this.y)
			return;
		this.y = y;
		if (anyAnchorsActive()) {
			// only need to update anchor if in pixel mode

			if (isAnchorActive(TOP_ANCHOR) && getAnchorModeImpl(TOP_ANCHOR) == PIXEL_MODE) {
				setAnchorImpl(TOP_ANCHOR, getY());
			}
			if (isAnchorActive(BOTTOM_ANCHOR) && getAnchorModeImpl(BOTTOM_ANCHOR) == PIXEL_MODE) {
				setAnchorImpl(BOTTOM_ANCHOR, parent.getHeight() - getHeight() - getY());
			}
		}
		update();
	}

	/**
	 * Set z-coordinate of element. Elements are sorted by z-index when overlapping on the screen.
	 * 
	 * @param z z-index
	 */
	public void setZ(int z) {
		this.z = z;
		if (parent != null && getFrame().getInitializationState() != Frame.InitializationState.INITIALIZING) { // during intializing setting Z is not allowed.
			try {
				// no use to sort containers with autolayout (its bad actually because it could
				// change order)
				if (((Container) parent).needsSortingByZ())
					((Container) parent).sortItemsbyZ();
			} catch (ClassCastException e) {
			}
		}
		update();
	}

	/**
	 * Set position relative to parent.
	 * 
	 * @param x x-coordinate in pixel
	 * @param y y-coordinate in pixel
	 */
	public void setPosition(int x, int y) {
		setX(x); // update will be called twice, but that's better than accidently failing to match setPosition()
		setY(y); // with the methods setX() and setY() in the future
	}




	/*
	 * Some notes about size for me and people who need/want to know:
	 * 
	 * The values of width and height can be changed through a lot of different ways
	 * which follow a few rules.
	 * The set width can be overwritten by anchors, autosize and min/maxWidth (same with height). 
	 * 
	 * The hierachy is:
	 *  1. width as set by user (weakest)
	 *  2. autosize (i.e. in Button, so text fits)
	 *  3. anchors 
	 *  4. minWidth/maxWidth (strongest)
	 *  
	 *  
	 * 
	 * Changing width/height also needs to be followed by one or more of the
	 * following actions (no pun intended^^), depending on who changes it:
	 * - constrain between minWidth and maxWidth 
	 *   (as this is the strongest rule, it has to be executed always)
	 * - call parentResized() for children 
	 * - call update anchors
	 * - call update()
	 * 
	 * There is also a method setWidthNoUpdate() and setHeightNoUpdate() that can be used if an update should NOT happen. 
	 * 
	 * 
	 * 
	 * 	***** Constraining is always necessary (min and max width can NEVER be exceeded)
	 * 			that's why setWidthImpl() should be called and not "width = ..."
	 * 
	 * 
	 * 
	 * 
	 * Width (and height similarily) can be changed by: 
	 * - calling setWidth(int): 						need to call parentResized() for children, update anchors() and update()
	 * - setMin/MaxWidth: 								need to call parentResized() for children, update anchors() and update() (might need to set width new)
	 * - automatic change of width in parentResized(): 	don't call parentResized/update anchors!, do call update 
	 * - Default or given width in constructor: 		all unnecessary, nothing yet set
	 * - exceptions where it's changed in render(): 	for ListItem: no prob, has no children (yet)
	 * - in autosize():									parentResized() for children, update anchors unnecessary as none are set, update unnecessary (already called)
	 * - in Container.fitContent():						need to call parentResized() for children, update anchors() and update()
	 * - set width through animation: 					all, delegated to setWidth()
	 * 
	 * 
	 */


	/*
	 * In this library, never set width/height by calling "width = ...". Always use
	 * setWidthImpl(int) and setHeightImpl(int). Other classes should use 
	 * setWidthNoUpdate()/setHeightNoUpdate()
	 * 
	 * This ensures that dimensions are always constrained by min/max dimensions.
	 * 
	 */
	private void setWidthImpl(int width) {
		this.width = Math.max(Math.min(width, maxWidth), minWidth);
	}

	private void setHeightImpl(int height) {
		this.height = Math.max(Math.min(height, maxHeight), minHeight);
	}



	/*
	 * Open questions: 
	 * - Update if position changed in this method?
	 * TODO
	 * - use parent.getAvailableHeight for anchors?
	 * 
	 * Returns true if size has been changed
	 */
	protected boolean setWidthNoUpdate(int width) {
		int oldWidth = this.width;
		setWidthImpl(width);
		if (oldWidth == this.width) 		// no unnecessary resize event calling when setting min/max
			return false;

		availableWidthChanged();
		handleEvent(resizeListener, this); 	// width of this element has really changed

		// Need to update position or anchors depending on which anchors are set.
		if (!PAUSE_UPDATE_ANCHORS) {
			if (isAnchorActive(RIGHT_ANCHOR)) {
				if (isAnchorActive(LEFT_ANCHOR)) {
					// both l/r -> width is set new here, so change right anchor
					// left anchor is not touched
					if (getAnchorModeImpl(RIGHT_ANCHOR) == PIXEL_MODE) {
						// makes no difference if setWidthNoUpdate() is just called by parentResized()
						setAnchorImpl(RIGHT_ANCHOR, parent.getWidth() - getWidth() - getX());
					} else {
						// makes no difference if setWidthNoUpdate() is just called by parentResized()
						setAnchorImpl(RIGHT_ANCHOR, Math.round((parent.getWidth() - getWidth() - getLeft()) * 100f / parent.getWidth()));
					}
				} else { // only right anchor -> keep element fixed at right and change x-position
					x = getRight() - getWidth(); // Do not call setX()
				}
			}
		}

		return true;
	}




	protected boolean setHeightNoUpdate(int height) {
		int temp = this.height;
		setHeightImpl(height);
		if (temp == this.height) 			// no unnecessary resize event calling when setting min/max
			return false;

		availableHeightChanged();
		handleEvent(resizeListener, this); 	// height of this element has really changed

		if (!PAUSE_UPDATE_ANCHORS) {
			if (isAnchorActive(BOTTOM_ANCHOR)) {
				if (isAnchorActive(TOP_ANCHOR)) {
					// both t/d -> height is set new here, so change bottom anchor
					// top anchor is not touched
					if (getAnchorModeImpl(BOTTOM_ANCHOR) == PIXEL_MODE) {
						// makes no difference if setHeightNoUpdate() is just called by parentResized()
						setAnchorImpl(BOTTOM_ANCHOR, parent.getHeight() - getHeight() - getY());
					} else {
						// makes no difference if setHeightNoUpdate() is just called by parentResized()
						setAnchorImpl(BOTTOM_ANCHOR, Math.round((parent.getHeight() - getHeight() - getTop()) * 100f / parent.getHeight()));
					}
				} else { // only bottom anchor -> keep element fixed at bottom and change y-position
					y = getBottom() - getHeight(); // Do not call setY()
				}
			}
		}

		return true;
	}


	protected void setSizeWithoutUpdate(int width, int height) {
		setWidthNoUpdate(width);
		setHeightNoUpdate(height);
	}

	/**
	 * Set width of element.
	 * 
	 * @param width width in pixel
	 */
	public void setWidth(int width) {
		if (setWidthNoUpdate(width))
			update();
	}


	/**
	 * Set height of element.
	 * 
	 * @param height height in pixel
	 */
	public void setHeight(int height) {
		if (setHeightNoUpdate(height))
			update();
	}

	/**
	 * Convenience version that casts to int!
	 * 
	 * @param width width in pixel
	 */
	public void setWidth(float width) {
		setWidth((int) width);
	}

	/**
	 * Convenience version that casts to int!
	 * 
	 * @param height in pixel
	 */
	public void setHeight(float height) {
		setHeight((int) height);
	}

	/**
	 * Set width and height of element.
	 * 
	 * @param width  width in pixel
	 * @param height height in pixel
	 */
	public void setSize(int width, int height) {
		boolean widthChanged = setWidthNoUpdate(width);
		boolean heightChanged = setHeightNoUpdate(height);
		if (widthChanged || heightChanged) // dont put the two methods in here because the latter can be optimized away
			update();
	}

	/**
	 * Set size of element and disable autosizing.
	 * 
	 * @param width  width
	 * @param height height
	 */
	public void setFixedSize(int width, int height) {
		autosizing = AUTOSIZE_NONE;
		setSize(width, height);
	}

	/*
	 * To be overriden by subclasses. availableWidth and availableHeight change as paddings or width change.  
	 */
	protected void availableWidthChanged() {

	}

	protected void availableHeightChanged() {

	}

	/**
	 * Set minimum width of element in pixel. Width of element will never be less than minWidth and
	 * therefore might be adjusted. Minimum width cannot be smaller than 1 and not greater than
	 * minWidth.
	 * 
	 * @param minWidth minimum width in pixel
	 */
	public void setMinWidth(int minWidth) {
		// don't allow width ever to go below 1 (that produces errors when creating
		// graphics)
		this.minWidth = Math.max(Constants.MinimalMinWidth, Math.min(minWidth, maxWidth));
		setWidth(width);
	}

	/**
	 * Set maximum width of element in pixel. Width of element will never be greater than maxWidth.
	 * Default is 100000.
	 * 
	 * @param maxWidth maximum width in pixel
	 */
	public void setMaxWidth(int maxWidth) {
		this.maxWidth = Math.max(minWidth, maxWidth);
		setWidth(width);
	}



	/**
	 * Set minimum height of element in pixel. Height of element will never be less than minHeight and
	 * therefore might be adjusted. Minimum height cannot be smaller than 1 and not greater than
	 * maxHeight.
	 * 
	 * @param minHeight minimum height in pixel
	 */
	public void setMinHeight(int minHeight) {
		// don't allow height ever to go below 1 (that produces errors when creating
		// graphics)
		this.minHeight = Math.max(Constants.MinimalMinHeight, Math.min(minHeight, maxHeight));
		setHeight(height);
	}

	/**
	 * Set maximum height of element in pixel. Height of element will never be greater than maxHeight.
	 * Default is 100000.
	 * 
	 * @param maxHeight maximum height in pixel
	 */
	public void setMaxHeight(int maxHeight) {
		this.maxHeight = Math.max(minHeight, maxHeight);
		setHeight(height);
	}




	/**
	 * Set the plain background color of the element. Actual displayed color can vary if the element is
	 * i.e. hovered over.
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
	 * Automatically generates hover and pressed color of backgroundColor.
	 * 
	 * @param clr integer rgb color
	 */
	protected void setStatusBackgroundColorsAutomatically(int clr) {
		backgroundColor = clr;
		visualBackgroundColor = clr;

		int r = (int) getPApplet().red(clr);
		int g = (int) getPApplet().green(clr);
		int b = (int) getPApplet().blue(clr);
		int a = (int) getPApplet().alpha(clr);

		if (getPApplet().brightness(clr) > 40) { // darken color for HoverColor and PressedColor when color is
												 // bright enough
			int alpha = a == 255 ? 255 : a + 20;
			hoverColor = GuisetColor.create(r - 20, g - 20, b - 20, alpha);
			pressedColor = GuisetColor.create(r - 40, g - 40, b - 40, alpha);
		} else { // lighten color for HoverColor and PressedColor when color too dark
			int alpha = a == 255 ? 255 : a + 40;
			hoverColor = GuisetColor.create(r + 20, g + 20, b + 20, alpha);
			pressedColor = GuisetColor.create(r + 40, g + 40, b + 40, alpha);
		}
		image = null; // just in case
		update();
	}

	/**
	 * Set the foreground color (not really used anymore, except in slider and knob).
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
	 * Color of elements border.
	 * 
	 * @param clr integer rgb color
	 */
	public void setBorderColor(int clr) {
		borderColor = clr;
		update();
	}

	/**
	 * Stroke width of the elements border.
	 * 
	 * @param borderWidth border with in pixel
	 */
	public void setBorderWidth(int borderWidth) {
		this.borderWidth = Math.max(0, borderWidth);
		update();
	}

	/**
	 * Rounds the corners of the element. Negative values will be ignored.
	 * 
	 * @param borderRadius border radius
	 */
	public void setBorderRadius(int borderRadius) {
		this.borderRadius = borderRadius;
		update();
	}


	/**
	 * Shorthand property for setting all border properties.
	 * 
	 * @param borderWidth  border width
	 * @param borderColor  border color
	 * @param borderRadius border radius
	 */
	public void setBorder(int borderWidth, int borderColor, int borderRadius) {
		setBorderWidth(borderWidth);
		setBorderColor(borderColor);
		setBorderRadius(borderRadius);
	}

	/**
	 * Shorthand property for setting border width and color.
	 * 
	 * @param borderWidth border width
	 * @param borderColor border color
	 */
	public void setBorder(int borderWidth, int borderColor) {
		setBorderWidth(borderWidth);
		setBorderColor(borderColor);
	}

	/**
	 * Set the cursor displayed when mouse is over this element.
	 * 
	 * @param cursor Integer between 0 and 11. Can use constants like ARROW, CROSS, HAND, MOVE, TEXT, or
	 *               WAIT.
	 */
	public void setCursor(int cursor) {
		if (cursor >= 0 && cursor < 12) {
			this.cursor = cursor;
		}
	}

	/**
	 * Set the (background) image of the element. This is not implemented in all but in most classes.
	 * The image is copied!
	 * 
	 * @param image PImage or PGraphics object.
	 */
	public void setImage(PImage image) {
		try {
			this.image = (PImage) image.clone();
			if (hoverColor == backgroundColor) {
				hoverColor = GuisetColor.create(0, 40);
			}
			if (pressedColor == backgroundColor) {
				pressedColor = GuisetColor.create(0, 80);
			}
			update();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Set the background image filling mode. @see {@link #FILL}, @see {@link #FIT} @see
	 * {@link #FIT_INSIDE}.
	 * 
	 * @param imageMode. Use Control.FILL, Control.FIT or Control.FIT_INSIDE
	 */
	public void setImageMode(ImageMode imageMode) {
		this.imageMode = imageMode;
		update();
	}

	/**
	 * Set background to a vertical gradient between the two given colors. This removes the background
	 * image if set. At the moment, only vertical gradients are possible.
	 * 
	 * @param topColor    top gradient color
	 * @param bottomColor bottom gradient color
	 */
	public void setGradient(int topColor, int bottomColor) {
		PGraphics gradient = getPApplet().createGraphics(width, height);
		gradient.beginDraw();
		for (int i = 0; i < height; i++) {
			float inter = PApplet.map(i, 0, height, 0, 1);
			int c = PGraphics.lerpColor(topColor, bottomColor, inter, PGraphics.RGB);
			gradient.stroke(c);
			gradient.line(0, i, width, i);
		}
		gradient.endDraw();
		setImage(gradient);
	}

	/**
	 * Set the enabled state of this element. If false it will not receive any events and be displayed
	 * in a different (mostly greyish) way.
	 * 
	 * @param enabled enabled state
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		if (!enabled && focused)
			blur();
		update();
	}

	/**
	 * Set the visibility. Invisible elements will not be rendered and receive not events.
	 * 
	 * @param visible visibility state
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
		if (!visible && focused)
			blur();
		update();
	}

	/**
	 * Just in case someone expects a "setVisibility" method instead of "setVisible".
	 * 
	 * @param visible visibility state
	 */
	public void setVisibility(boolean visible) {
		setVisible(visible);
	}


	/**
	 * Set opacity (opposite of transparency), casts to float.
	 * 
	 * @param opacity opacity from 0 (transparent) to 1 (opaque).
	 */
	public void setOpacity(double opacity) {
		setOpacity((float) opacity);
	}

	/**
	 * Set opacity (opposite of transparency). Transparent elements are still clickable (like in html),
	 * even if the opacity is 0. Call {@code setVisibility(false)} or {@code setEnabled(false)} to
	 * prevent the element from receiving mouse events.
	 * 
	 * @param opacity opacity from 0 (transparent) to 1 (opaque).
	 */
	public void setOpacity(float opacity) {
		this.opacity = Math.max(0, Math.min(1, opacity));

		enableExtendedRenderer();
		update();
	}


	/**
	 * Apply margins to all sides of the element. In FlowContainers and similar containers neighboring
	 * elements this determines the distance to neighbor elements and the parents bounds.
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
		setPadding(all, all, all, all);
	}

	public void setPadding(int top_bottom, int left_right) {
		setPadding(top_bottom, left_right, top_bottom, left_right);
	}

	/**
	 * Apply individual padding to all sides of the element. Padding will create space inside the
	 * element between borders and content.
	 * 
	 * @param top    top padding in pixel
	 * @param right  right padding in pixel
	 * @param bottom bottom padding in pixel
	 * @param left   left padding in pixel
	 */
	public void setPadding(int top, int right, int bottom, int left) {
		if (top != paddingTop || bottom != paddingBottom) {
			paddingTop = top;
			paddingBottom = bottom;
			availableHeightChanged();
		}
		if (left != paddingLeft || right != paddingRight) {
			paddingLeft = left;
			paddingRight = right;
			availableWidthChanged();
		}
		autosize(); // might be called although nothing changed but that is unlikely.
		update();
	}

	public void setPaddingTop(int top) {
		if (top != paddingTop) { // Don't call autosize() and availableWidthChanged() unnecessarily
			paddingTop = top;
			availableHeightChanged();
			autosize();
			update();
		}
	}

	public void setPaddingRight(int right) {
		if (right != paddingRight) {
			paddingRight = right;
			availableWidthChanged();
			autosize();
			update();
		}
	}

	public void setPaddingBottom(int bottom) {
		if (bottom != paddingBottom) {
			paddingBottom = bottom;
			availableHeightChanged();
			autosize();
			update();
		}
	}

	public void setPaddingLeft(int left) {
		if (left != paddingLeft) {
			paddingLeft = left;
			availableWidthChanged();
			autosize();
			update();
		}
	}












	/* __________________________________________________________________________________________________________
	 * 
	 * STYLE GETTERS
	 * 
	 */

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
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

	/**
	 * Get available content width subtracting paddings.
	 * 
	 * @return available width
	 */
	public int getAvailableWidth() {
		return width - paddingRight - paddingLeft;
	}

	/**
	 * Get available content height subtracting paddings.
	 * 
	 * @return available height
	 */
	public int getAvailableHeight() {
		return height - paddingTop - paddingBottom;
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



	/*
	 * Enable user of the library to adopt a set of visual properties from another element. 
	 * 
	 * Call copyStyle(Control, attribs) to read given attributes (separated by bitwise OR) from given element c:
	 * 
	 * myElement.copyStyle(otherElement, PADDING_LEFT | MARGIN_BOTTOM, FOREGROUND_COLOR);
	 * 
	 * There some shortcuts to include several properties, like ALL, COLORS, MARGIN (all margins), PADDING (all paddings), 
	 * SPACING (all margins and paddings), BACKGROUND_COLORS (background-, hover-, and pressedColor).
	 * 
	 * 
	 */


	public static final long PADDING_LEFT = 1 << 0;
	public static final long PADDING_RIGHT = 1 << 1;
	public static final long PADDING_TOP = 1 << 2;
	public static final long PADDING_BOTTOM = 1 << 3;
	public static final long PADDING = PADDING_LEFT | PADDING_RIGHT | PADDING_TOP | PADDING_BOTTOM;

	public static final long MARGIN_LEFT = 1 << 4;
	public static final long MARGIN_RIGHT = 1 << 5;
	public static final long MARGIN_TOP = 1 << 6;
	public static final long MARGIN_BOTTOM = 1 << 7;
	public static final long MARGIN = MARGIN_LEFT | MARGIN_RIGHT | MARGIN_TOP | MARGIN_BOTTOM;

	public static final long BACKGROUND_COLOR = 1 << 8;
	public static final long HOVER_COLOR = 1 << 9;
	public static final long PRESSED_COLOR = 1 << 10;
	public static final long BACKGROUND_COLORS = BACKGROUND_COLOR | HOVER_COLOR | PRESSED_COLOR;

	public static final long FOREGROUND_COLOR = 1 << 11;


	public static final long BORDER_WIDTH = 1 << 12;
	public static final long BORDER_COLOR = 1 << 13;
	public static final long BORDER_RADIUS = 1 << 14;
	public static final long BORDER = BORDER_WIDTH | BORDER_COLOR | BORDER_RADIUS;

	public static final long OPACITY = 1 << 15;

	public static final long TEXT_COLOR = 1 << 16;
	public static final long FONT_SIZE = 1 << 17;
	public static final long TEXT_ALIGN = 1 << 18;
	public static final long TEXT_ALIGN_Y = 1 << 19;
	public static final long LINE_HEIGHT = 1 << 20;
	public static final long TEXT_PROPERTIES = TEXT_COLOR | FONT_SIZE | TEXT_ALIGN | TEXT_ALIGN_Y | LINE_HEIGHT;

	public static final long COLORS = BACKGROUND_COLORS | FOREGROUND_COLOR | BORDER_COLOR | TEXT_COLOR;
	public static final long SPACING = PADDING | MARGIN;

	public static final long ALL = SPACING | COLORS | OPACITY | TEXT_PROPERTIES;



	/**
	 * Enable user of the library to adopt a set of visual properties from another element. @see
	 * {@link Control#copyStyle(Control, long)}.
	 * 
	 * 
	 * @param source element to adopt properties from.
	 */
	public void copyStyle(Control source) {
		copyStyle(source, ALL);
	}


	/**
	 * Enable user of the library to adopt a set of visual style properties from another element.
	 * 
	 * Call copyStyle(Control, attribs) to read given attributes (separated by bitwise OR) from given
	 * element c:
	 * 
	 * {@code myElement.copyStyle(otherElement, PADDING_LEFT | MARGIN_BOTTOM, FOREGROUND_COLOR); }
	 * 
	 * <br>
	 * <br>
	 * 
	 * Options are: {@link #PADDING_LEFT}, {@link #PADDING_RIGHT}, {@link #PADDING_TOP},
	 * {@link #PADDING_BOTTOM}, {@link #MARGIN_LEFT}, {@link #MARGIN_RIGHT}, {@link #MARGIN_TOP},
	 * {@link #MARGIN_BOTTOM}, {@link #BACKGROUND_COLOR}, {@link #HOVER_COLOR}, {@link #PRESSED_COLOR},
	 * {@link #FOREGROUND_COLOR}, {@link #BORDER_WIDTH}, {@link #BORDER_RADIUS}, {@link #BORDER_COLOR},
	 * {@link #OPACITY},
	 * 
	 * <br>
	 * <br>
	 * Only if source and target have text properties: {@link #TEXT_COLOR}, {@link #TEXT_ALIGN},
	 * {@link #TEXT_ALIGN_Y}, {@link #FONT_SIZE}, {@link #LINE_HEIGHT}.
	 * 
	 * <br>
	 * <br>
	 * There some shortcuts to include several properties, like {@link #ALL}, {@link #COLORS},
	 * {@link #MARGIN} (all margins), {@link #PADDING} (all paddings), {@link #SPACING} (all margins and
	 * paddings), {@link #BACKGROUND_COLORS} (background-, hover-, and pressedColor), {@link #BORDER}
	 * (border-width, -color and -radius), {@link #TEXT_PROPERTIES} (all text properties).
	 * 
	 * 
	 * @param source  element to adopt properties from.
	 * @param attribs bitwise OR added attributes
	 */
	public void copyStyle(Control source, long attribs) {
		if ((attribs & PADDING) != 0)
			setPadding(source.paddingTop, source.paddingRight, source.paddingBottom, source.paddingLeft);
		else {
			if ((attribs & PADDING_LEFT) != 0)
				setPaddingLeft(source.paddingLeft);
			if ((attribs & PADDING_RIGHT) != 0)
				setPaddingRight(source.paddingRight);
			if ((attribs & PADDING_TOP) != 0)
				setPaddingTop(source.paddingTop);
			if ((attribs & PADDING_BOTTOM) != 0)
				setPaddingBottom(source.paddingBottom);
		}
		if ((attribs & MARGIN) != 0)
			setMargin(source.marginTop, source.marginRight, source.marginBottom, source.marginLeft);
		else {
			if ((attribs & MARGIN_LEFT) != 0)
				setMarginLeft(source.marginLeft);
			if ((attribs & MARGIN_RIGHT) != 0)
				setMarginRight(source.marginRight);
			if ((attribs & MARGIN_TOP) != 0)
				setMarginTop(source.marginTop);
			if ((attribs & MARGIN_BOTTOM) != 0)
				setMarginBottom(source.marginBottom);
		}
		if ((attribs & BACKGROUND_COLOR) != 0)
			setBackgroundColor(source.backgroundColor);
		if ((attribs & HOVER_COLOR) != 0)
			setHoverColor(source.hoverColor);
		if ((attribs & PRESSED_COLOR) != 0)
			setPressedColor(source.pressedColor);

		if ((attribs & FOREGROUND_COLOR) != 0)
			setForegroundColor(source.foregroundColor);

		if ((attribs & BORDER_WIDTH) != 0)
			setBorderWidth(source.borderWidth);
		if ((attribs & BORDER_RADIUS) != 0)
			setBorderRadius(source.borderRadius);
		if ((attribs & BORDER_COLOR) != 0)
			setBorderColor(source.borderColor);

		if ((attribs & OPACITY) != 0)
			setOpacity(source.opacity);
	}


	/**
	 * @see #doForAll(Setter, Object...)
	 *
	 * @param <T> type
	 */
	public static abstract class Setter<T> {
		public abstract void run(T c);
	}

	/**
	 * (A bit more advanced!) Do a given method for all given objects (not necessarily UI elements). The
	 * idea is to apply setters to several elements without boilerplate code.
	 * 
	 * <br>
	 * You can pass an instance of {@code Control.Setter<>}, override its run() method and then pass any
	 * number of elements.
	 * 
	 * <br>
	 * You need to specify an object class, i.e. Control if you only need to use methods from Control or
	 * Textbox, if you would like to use setters individual to Textbox. <br>
	 * Example:
	 * 
	 * <br>
	 * <br>
	 * 
	 * <pre>
	 * Control.doForAll(new{@code Control.Setter<Control>()} {
	 *    {@literal @}Override
	 *     public void run(Control c) {
	 *         c.setBackgroundColor(color(0)); 
	 *     }
	 * }, myButton, myLabel, myTextbox);
	 * </pre>
	 * 
	 * 
	 * <br>
	 * <br>
	 * or:
	 * 
	 * <br>
	 * <pre>
	 * Control.doForAll(new{@code Control.Setter<Container>()} {
	 *    {@literal @}Override
	 *     public void run(Container c) {
	 *         c.fitContent(); 
	 *     }
	 * }, myContainer, myHScrollContainer, myScrollArea, myFlowContainer);
	 * </pre>
	 * 
	 * @param          <T> type of object
	 * @param setter   a setter (need to override the run() method and specify template type)
	 * @param elements arbitrary number of elements that are of the given template type
	 */

	@SafeVarargs // well i guess an error could occur if the user passes a bad method
	public static <T> void doForAll(Setter<T> setter, T... elements) {
		for (T c : elements) {
			setter.run(c);
		}
	}



	/**
	 * Create a transition animation for a property of this Component. The {@link Frame} will deal with
	 * changing this property in appropriate steps to create the effect.
	 * 
	 * I.e. write:
	 * 
	 * {@code myObject.animate("x", 50, 500);}
	 * 
	 * to change the x-coordinate of myObject to 50 within half a second. Or
	 * 
	 * {@code myObject.animate("opacity", 0, 100);}
	 * 
	 * to create a fade-out transition.
	 * 
	 * @param attribute    name of attribute to animate
	 * @param aimedValue   final value for the attribute
	 * @param milliseconds time for animation in milliseconds
	 */
	public void animate(String attribute, float aimedValue, double milliseconds) {
		getFrame().animateImpl(attribute, this, aimedValue, milliseconds);
	}









	/*
	 * __________________________________________________________________________________________________________
	 * 
	 * EVENTS
	 * 
	 *
	 * The programmer can add certain listeners to his objects. Mouse listeners are available for all
	 * classes. They can be assigned and given a callback method with the setMouseListener() method,
	 * specifying the type with a string ("press", "release"...). If no target is specified with the
	 * overloaded setMouseListener() method, papplet will be assumed. Of each type, exactly one listener
	 * can be assigned to each object. A listener is null if it is not set/active.
	 * 
	 * Moreover classes like Frame and Textbox feature key listeners or an itemchanged-listener
	 * (ListView, MenuItem). These listeners are usually assigned with extra methods like
	 * "addItemChangeListener()" etc.
	 
	 * 
	 * 
	 * 
	 * 
	 * How to make an event listener in a new class
	 * --------------------------------------------
	 * - In the class, create an EventListener with 
	 *   an appropriate name (don't initialize it)
	 * - Provide adding and removing methods 
	 * - Also provide a simple adding method that 
	 *   defaults target to papplet (the sketch)
	 * 
	 *   An example with no arguments. 
	 *   
	 *   
	 
		 protected EventListener myListener;
		 
		 public void setMyListener(String methodName, Object target) {
		     myListener = createEventListener(methodName, target, null);
		 }
		 
		 public void setMyListener(String methodName) {
		     setMyListener(methodName, getPApplet());
		 }
		 
		 public void removeMyListener(){
	     	myListener = null; // setting listener to null is the way to do it
		 }
		 
	  	 protected void someFunctionThatChangesObervedState(){
	      	// do stuff
	      	handleEvent(myListener, null);
	     }  
	     
	
	 *
	 * If arguments shall be passed to the callback function, then pass their class to the
	 * createEventListener() method as third argument. When handling the event, pass the argument here.
	 * Look at the example from focusListener.
	 * 
	 */

	/*
	 * Base listener object implementing the handle() method
	 */
	abstract class EventListener {
		abstract void handle(Object... args);
	}

	/**
	 * Listener object, storing a callback method, the target object and if the callback shall be called
	 * with the optional args or without.
	 */
	protected class ReflectionEventListener extends EventListener {
		Method method;
		Object target;
		boolean invokeWithArgs;

		ReflectionEventListener(Method m, Object t, boolean invokeWithArgs) {
			method = m;
			target = t;
			this.invokeWithArgs = invokeWithArgs;
		}

		@Override
		void handle(Object... args) {
			try {
				if (this.invokeWithArgs) { // might have no args
					method.invoke(target, args);
				} else {
					method.invoke(target);
				}
			} catch (IllegalAccessException ie) {
				ie.printStackTrace();
			} catch (InvocationTargetException te) {
				te.printStackTrace();
			}
		}
	}

	/**
	 * Functional interface for a lambda expression with no parameters.
	 */
	public interface Predicate {
		void run();
	}

	/**
	 * Functional interface for a lambda expression with one parameter.
	 *
	 * @param <T> lambda parameter type
	 */
	public interface Predicate1<T> {
		void run(T args);
	}

	/**
	 * Functional interface for a lambda expression with two parameters.
	 *
	 * @param <T> first parameter type
	 * @param <U> second parameter type
	 */
	public interface Predicate2<T, U> {
		void run(T arg1, U arg2);
	}


	// Listener object, storing the lambda callback with no parameters
	protected class LambdaEventListener extends EventListener {
		Predicate p;

		LambdaEventListener(Predicate p) {
			this.p = p;
		}

		@Override
		void handle(Object... args) {
			p.run();
		}
	}

	// Listener object, storing the lambda callback with 1 parameter
	protected class LambdaEventListener1<T> extends EventListener {
		Predicate1<T> p;

		LambdaEventListener1(Predicate1<T> p) {
			this.p = p;
		}

		@SuppressWarnings("unchecked")
		@Override
		void handle(Object... args) {
			p.run((T) args[0]); // This cast is ok because we only call with the right object
		}
	}

	// Listener object, storing the lambda callback with 2 parameters
	protected class LambdaEventListener2<T, U> extends EventListener {
		Predicate2<T, U> p;

		LambdaEventListener2(Predicate2<T, U> p) {
			this.p = p;
		}

		@SuppressWarnings("unchecked")
		@Override
		void handle(Object... args) {
			p.run((T) args[0], (U) args[1]); // These casts are ok because we only call with the right objects
		}
	}




	/*
	 * Standard Listeners (slots)
	 */

	protected EventListener pressListener;
	protected EventListener releaseListener;
	protected EventListener enterListener;
	protected EventListener exitListener;
	protected EventListener moveListener;
	protected EventListener dragListener;
	protected EventListener wheelListener;
	protected EventListener resizeListener;
	protected EventListener focusListener;

	/*
	 * Called by the several guiSET classes. When an event occurs, the callback can be handled by
	 * calling this method with the listener and the arguments. If the listener is not set (null), the
	 * call is ignored.
	 */
	protected void handleEvent(EventListener callback, Object... args) {
		if (callback == null)
			return;
		callback.handle(args);
	}


	/*
	 * Create a new listener instance to assign to one of the existing listener slots.  
	 */
	protected EventListener createEventListener(String methodName, Object target, Class<?>... args_class) {
		Class<?> c = target.getClass();
		try { // try with args
			Method m = c.getMethod(methodName, args_class);
			return new ReflectionEventListener(m, target, true);
		} catch (NoSuchMethodException nsme) {
			try { // try without args (maybe user forgot or does not need them)
				Method m = c.getMethod(methodName);
				return new ReflectionEventListener(m, target, false);
			} catch (NoSuchMethodException nsme2) {
				getPApplet().die("There is no public " + methodName + "() method with the right arguments.");
			}
		}
		return null;
	}





	/**
	 * Set a mouse listener. The type can be "press", "release", "enter", "exit, "move", "drag" and
	 * "wheel". Each Component allows one listener per type. The given method can be implemented without
	 * arguments or with {@link MouseEvent} as parameter. If it is defined in another class than in the
	 * main PApplet scope use the overloaded method {@link #setMouseListener(String, String, Object)}
	 * that allows passing a target object.
	 * 
	 * @param type       String for event type
	 * @param methodName Name of method to invoke
	 * @return false if type is invalid, method is not accessible or a listener has already been
	 *         registered for this type. Returns true if successful.
	 */
	public boolean setMouseListener(String type, String methodName) {
		return setMouseListener(type, methodName, getPApplet());
	}

	/**
	 * Allows to pass a target object where the given method is declared.
	 * 
	 * @see #setMouseListener(String, String)
	 * 
	 * @param type       String for event type
	 * @param methodName name of callback method
	 * @param target     Object where the callback method is declared.
	 * @return false if type is invalid, method is not accessible or a listener has already been
	 *         registered for this type. Returns true if successful.
	 * 
	 */
	public boolean setMouseListener(String type, String methodName, Object target) {
		switch (type) {
		case "press":
			pressListener = createEventListener(methodName, target, MouseEvent.class);
			return pressListener == null;
		case "release":
			releaseListener = createEventListener(methodName, target, MouseEvent.class);
			return releaseListener == null;
		case "enter":
			enterListener = createEventListener(methodName, target, MouseEvent.class);
			return enterListener == null;
		case "exit":
			exitListener = createEventListener(methodName, target, MouseEvent.class);
			return exitListener == null;
		case "move":
			moveListener = createEventListener(methodName, target, MouseEvent.class);
			return moveListener == null;
		case "drag":
			dragListener = createEventListener(methodName, target, MouseEvent.class);
			return dragListener == null;
		case "wheel":
			wheelListener = createEventListener(methodName, target, MouseEvent.class);
			return wheelListener == null;
		}
		return false;
	}

	/**
	 * Set a mouse listener in form of a lambda expression. For possible types see
	 * {@link #setMouseListener(String, String)}.
	 * 
	 * Event parameters: none
	 * 
	 * @param type see {@link #setMouseListener(String, String)}
	 * @param lambda    lambda expression
	 */
	public void setMouseListener(String type, Predicate lambda) {
		switch (type) {
		case "press":
			pressListener = new LambdaEventListener(lambda);
			return;
		case "release":
			releaseListener = new LambdaEventListener(lambda);
			return;
		case "enter":
			enterListener = new LambdaEventListener(lambda);
			return;
		case "exit":
			exitListener = new LambdaEventListener(lambda);
			return;
		case "move":
			moveListener = new LambdaEventListener(lambda);
			return;
		case "drag":
			dragListener = new LambdaEventListener(lambda);
			return;
		case "wheel":
			wheelListener = new LambdaEventListener(lambda);
			return;
		}
	}

	/**
	 * Set a mouse listener in form of a lambda expression with a {@link MouseEvent} as argument. For
	 * possible types see {@link #setMouseListener(String, String)}.
	 * 
	 * Event parameters: {@link MouseEvent}
	 * 
	 * @param type see {@link #setMouseListener(String, String)}
	 * @param lambda    lambda expression with a {@link MouseEvent} as argument.
	 */
	public void setMouseListener(String type, Predicate1<MouseEvent> lambda) {
		switch (type) {
		case "press":
			pressListener = new LambdaEventListener1<MouseEvent>(lambda);
			return;
		case "release":
			releaseListener = new LambdaEventListener1<MouseEvent>(lambda);
			return;
		case "enter":
			enterListener = new LambdaEventListener1<MouseEvent>(lambda);
			return;
		case "exit":
			exitListener = new LambdaEventListener1<MouseEvent>(lambda);
			return;
		case "move":
			moveListener = new LambdaEventListener1<MouseEvent>(lambda);
			return;
		case "drag":
			dragListener = new LambdaEventListener1<MouseEvent>(lambda);
			return;
		case "wheel":
			wheelListener = new LambdaEventListener1<MouseEvent>(lambda);
			return;
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
			pressListener = null;
			return;
		case "release":
			releaseListener = null;
			return;
		case "enter":
			enterListener = null;
			return;
		case "exit":
			exitListener = null;
			return;
		case "move":
			moveListener = null;
			return;
		case "drag":
			dragListener = null;
			return;
		case "wheel":
			wheelListener = null;
			return;
		}
	}



	/**
	 * Set a resize listener that fires each time the element is resized by anchor resizing.
	 * 
	 * Event arguments: the {@link Control} that is resized
	 * 
	 * @param methodName name of callback method
	 * @param target     Object where the callback method is declared.
	 */
	public void setResizeListener(String methodName, Object target) {
		resizeListener = createEventListener(methodName, target, Control.class);
	}

	public void setResizeListener(String methodName) {
		setResizeListener(methodName, getPApplet());
	}

	/**
	 * Set a lambda focus listener with a {@link Control} as parameter that fires each time the element
	 * is resized by anchor resizing.
	 * 
	 * Event arguments: the {@link Control} that is resized
	 * 
	 * @param lambda lambda expression with {@link Control} parameter
	 */
	public void setResizeListener(Predicate1<Control> lambda) {
		resizeListener = new LambdaEventListener1<Control>(lambda);
	}

	/**
	 * Set a lambda focus listener that fires each time the element is resized by anchor resizing.
	 * 
	 * Event arguments: none
	 * 
	 * @param lambda lambda expression
	 */
	public void setResizeListener(Predicate lambda) {
		resizeListener = new LambdaEventListener(lambda);
	}

	public void removeResizeListener() {
		resizeListener = null;
	}


	/**
	 * Set a focus listener that fires when the element gets focus (through mouse click or
	 * programmatically).
	 * 
	 * Event arguments: The {@link Control} that got focus
	 * 
	 * @param methodName name of callback method
	 * @param target     Object where the callback method is declared.
	 */
	public void setFocusListener(String methodName, Object target) {
		focusListener = createEventListener(methodName, target, Control.class);
	}

	public void setFocusListener(String methodName) {
		setFocusListener(methodName, getPApplet());
	}

	/**
	 * Set a lambda focus listener with a {@link Control} as parameter that fires when the element gets
	 * focus (through mouse click, programmatically).
	 * 
	 * Event arguments: The {@link Control} that got focus
	 * 
	 * @param lambda lambda expression with {@link Control} parameter
	 */
	public void setFocusListener(Predicate1<Control> lambda) {
		focusListener = new LambdaEventListener1<Control>(lambda);
	}

	/**
	 * Set a lambda focus listener that fires when the element gets focus (through mouse click,
	 * programmatically).
	 * 
	 * Event arguments: none
	 * 
	 * @param lambda lambda expression
	 */
	public void setFocusListener(Predicate lambda) {
		focusListener = new LambdaEventListener(lambda);
	}

	public void removeFocusListener() {
		focusListener = null;
	}





	/* __________________________________________________________________________________________________________
	 * 
	 * HOW MOUSE EVENTS WORK INTERNALLY
	 * 
	 * 
	 * Frame registers a mouseEvent() method at papplet. Every container (also
	 * Frame) calls the mouseEvent for all its items. When an element gets a mouseEvent, it can decide
	 * to stop the propagation by calling stopPropagation(). This way elements with high z-index, which
	 * will be checked first and can prevent lower elements from getting the mouseEvent. This is needed
	 * for example with menus, spinners, popups etc.
	 * 
	 * 
	 * offsetX,offsetY store computed position origin of item relative container it is
	 * evaluated and set in renderItem(). Problem: Flowcontainers decide not to draw
	 * items that are out of the visible area and needs to set offsetX,offsetY to somewhere
	 * outside this area. 
	 * 
	 * hovering is now dealt with in a different way: the first item to find out
	 * that the mouse is over it sets the Control.hoveredElement to itself. At then
	 * end of mouseEvent processing Frame checks Control.hoveredElement and if this
	 * items pHovered is false then Frame calls enter(), handleEventCallback(..) and
	 * hoveredElement.pHovered = true; Also the cursor is set to the cursor of this
	 * item. Similarily exit() is called if Control.hoveredElement has changed
	 * through the event propagation.
	 * 
	 *
	 * Only one element can be hovered on at one time. 
	 * 
	 * 
	 * Note for me about changing the whole mouse event implementation:
	 * 
	 * Replaced bounds with offsetX, offsetY. Right and bottom bounds are not needed for an item
	 * gets the mouse event only if mouse is over parent. This way no constraining
	 * like in former calcbounds is needed. Instead of passing the mouseEvent, this is
	 * stored globally (in Control.currentMouseEvent) and relative x,y coords are passed. Each
	 * container subtracts its own offsetX,offsetY from them so the items only need to know
	 * their relative position to parent and not absolute position in window to
	 * determine if mouse is over the item.
	 * 
	 * 
	 */

	// Origin coordinates relative to parent. Set by parent in containerRenderItem(Control, int, int)
	protected int offsetX = 0, offsetY = 0;

	protected int getOffsetX() {
		return offsetX;
	}

	protected int getOffsetY() {
		return offsetY;
	}

	// Current mouse event is stored here by Frame, no need to carry it around
	protected static MouseEvent currentMouseEvent;

	// First control that notices that the mouse is over it sets this to itself.
	// This is used for ENTER/EXIT as well as setting the cursor
	protected static Control hoveredElement;

	// Currently dragged element
	protected static Control draggedElement;

	// Classes can set this to false in their release(MouseEvent) method. Then a
	// drag of this element will induce no drop event
	protected static boolean drop = true;

	// A class can set this to true upon press event if the following dragging
	// should be treated as a normal mouse movement and not a drag. Frame will set
	// this to false anytime a release is detected. This feature is used by MenuItem
	// to make it possible to press on a menu header, hold the mouse and navigate
	// through the menu without releasing the mouse as commonly possible with GUIs
	// (in windows at least).
	protected static boolean notDragging = false;


	/*
	 * stopPropagation is a very important property that is used to indicate that
	 * one element "swallowed up" the mouseEvent so no other element will get it.
	 * 
	 * I.e. when one element is locally below another one then only the above should
	 * get the click event.
	 */

	private static boolean propagationStopped = false;

	/**
	 * Stop mouse event propagation. If called then no Components lower or at the same level in the
	 * Container hierachy will receive the currently processed mouse event.
	 */
	public static void stopPropagation() {
		propagationStopped = true;
	}

	/**
	 * Check if the mouse event propagation has been stopped.
	 * 
	 * @return propagation stop state
	 */
	public static boolean isPropagationStopped() {
		return propagationStopped;
	}

	protected static void resetPropagationState() {
		propagationStopped = false;
	}

	/**
	 * Check if given coordinates (need to be relative to parent) are within this element (without
	 * constraining to parents bounds).
	 * 
	 * @param x x coordinate relative to parent origin
	 * @param y y coordinate relative to parent origin
	 * @return are coordintes within
	 */
	public boolean relativeCoordsAreWithin(int x, int y) {
		return x > offsetX && y > offsetY && x < offsetX + width && y < offsetY + height;
	}

	/**
	 * Get x coordinate of left side relative to parent.
	 * 
	 * @return relative x
	 */
	public int getOffsetXToParent() {
		return getOffsetX();
	}

	/**
	 * Get y coordinate of left side relative to parent.
	 * 
	 * @return relative y
	 */
	public int getOffsetYToParent() {
		return getOffsetY();
	}

	/**
	 * Get x coordinate of left side relative to window.
	 * 
	 * @return absolute x
	 */
	public int getOffsetXToWindow() {
		return offsetX + parent.getOffsetXToWindow();
	}

	/**
	 * Get x coordinate of left side relative to window.
	 * 
	 * @return absolute y
	 */
	public int getOffsetYToWindow() {
		return offsetY + parent.getOffsetYToWindow();
	}



	protected static ArrayList<Control> coordinateTrace = new ArrayList<Control>(0);


	/**
	 * @see Container#traceRelativeCoordinates(int, int) and
	 * @see Container#traceAbsoluteCoordinates(int, int)
	 * 
	 * @param relX relx
	 * @param relY rely
	 */
	protected void traceCoordsImpl(int relX, int relY) {
		if (visible && enabled && relativeCoordsAreWithin(relX, relY)) {
			coordinateTrace.add(this);
		}
	}







	protected void mouseEvent(int x, int y) {
		if (!visible || !enabled)
			return;

		if (relativeCoordsAreWithin(x, y)) {
			if (hoveredElement == null) {
				hoveredElement = this;
			}

			switch (currentMouseEvent.getAction()) {
			case MouseEvent.MOVE: // most often
				move(currentMouseEvent);
				handleEvent(moveListener, currentMouseEvent);
				break;
			case MouseEvent.PRESS:
				focus();
				draggedElement = this;
				stopPropagation();
				press(currentMouseEvent);
				handleEvent(pressListener, currentMouseEvent);
				pPressed = true;
				break;
			case MouseEvent.WHEEL:
				mouseWheel(currentMouseEvent);
				handleEvent(wheelListener, currentMouseEvent);
				break;
			case MouseEvent.RELEASE:
				// Happens rarely here. Usually a release is preceded by a press and in between
				// only drag events come. These and the final release event are handled by Frame.
				// Still this code might be executed i.e. if user presses two buttons at once.
				stopPropagation();
				release(currentMouseEvent);
				handleEvent(releaseListener, currentMouseEvent);
				pPressed = false;

				break;
			case MouseEvent.DRAG:
				// This code wont be reached anymore for every drag event will be caught by frame
				// drag(e);
				// handleRegisteredEventMethod(DRAG_EVENT, e);
				// Frame.stopPropagation();
				break;
			}
		}

	}



	/* __________________________________________________________________________________________________________
	 * 
	 * INTERNAL EVENT METHODS
	 * 
	 * need to change some colors when hovered over or pressed.
	 */
	protected void move(MouseEvent e) {
	}

	protected void drag(MouseEvent e) {
	}

	protected void mouseWheel(MouseEvent e) {

	}

	protected void enter(MouseEvent e) {
		setVisualBackgroundColor(hoverColor);
	}

	protected void exit(MouseEvent e) {
		setVisualBackgroundColor(backgroundColor);
	}

	protected void press(MouseEvent e) {
		setVisualBackgroundColor(pressedColor);
	}

	protected void release(MouseEvent e) {
		setVisualBackgroundColor(hoverColor);
	}

	protected void dragRelease(MouseEvent e) {
	}


	protected void focused() {
	}

	protected void blurred() {
	}


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

	// finally! should've done this earlier
	protected static void print(Object... v) {
		PApplet.println(v);
	}

	// debugging function - get text if textbased
	protected String textIfTextBased() {
		if (this instanceof TextBased) {
			return ((TextBased) this).getText();
		} else
			return this.toString();
	}

	protected static PApplet getPApplet() {
		return Frame.getPApplet();
	}

	protected static Frame getFrame() {
		return Frame.getFrame();
	}



	/**
	 * Get guiSET version. I really hope I'll always remember syncing this.
	 * 
	 * @return version
	 */
	static public String getVersionString() {
		return "Version 0.0.12";
	}

}