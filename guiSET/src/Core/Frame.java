package guiSET.core;


/*
 * MASTER OF THE GUI
 * 
 * Frame is the main container for all controls in one sketch and there must always be one frame per project. 
 * It handles all rendering of the elements, updating looks and feels and finally displaying the GUI. 
 * 
 * It is itself a container that fills out the entire size of the sketch window by default. 
 * 
 * When creating the frame you have to pass THE PARENT PAPPLET TO THE CONTRUCTOR in setup() like this: Frame f = new Frame(this);
 * 
 * 
 * 
 * Frame is also kind of an interface to change window settings such as icon or window title by communicating to the sketchs "surface" object. 
 * 
 */


import processing.core.*;
import processing.event.*;

import java.awt.Dimension;
//import java.awt.Font;
import java.lang.Exception;
import java.lang.reflect.Method;
import java.util.HashMap;

import java.util.ArrayList;

import java.lang.reflect.InvocationTargetException;


/**
 * 
 * Master Container that fills out entire sketch and controls the flow of events
 * and rendering of the GUI.
 *
 */
public class Frame extends Container {

	/*
	 * frame0 is the standard frame that can always be accessed. When the programmer
	 * creates a new Frame : Frame f = new Frame(PApplet) the static frame will be
	 * replaced by that new one.
	 * 
	 * It is necessary to do so for this library and each component needs the parent
	 * papplet. But in this way some functions can be used even before and it tidies
	 * things up a lot.
	 * 
	 * Obviously only one Frame can be created AND effectively used per sketch.
	 * 
	 */

	/**
	 * This is a static Frame that when a new Frame object is created is initialized
	 * with this exact Frame. It is available to all classes for exchanging
	 * information. Normally a programmer does not need it except when developing
	 * custom component classes. Only one Frame may exist per sketch.
	 */
	public static Frame frame0 = new Frame();
	private boolean isNullFrame = true; 		// indicates that frame0 is intially a placeholder, is set to false as soon as
										 		// another one is created

	protected boolean isNullFrame() {
		return isNullFrame;
	}

	private String versionCode = "Version 0.0.3";








	/**
	 * Parent sketch
	 */
	protected PApplet papplet;



	public static PApplet getPApplet() {
		return frame0.papplet;
	}

	protected boolean initialized = false;

	/**
	 * GUI Draw time Mode: Draw the GUI before the {@link PApplet#draw()} happens.
	 * Useful when the user wants to image some own stuff upon the GUI. The draw
	 * time mode needs to be specified at the beginning when Frame is created. This
	 * is the default mode.
	 */
	public static final int DRAW_PRE = 1; 		// draw GUI before draw() takes place

	/**
	 * GUI Draw time Mode: Draw the GUI after the {@link PApplet#draw()} happens.The
	 * draw time mode needs to be specified at the beginning when Frame is created.
	 * Everything that is drawn during {@link PApplet#draw()} will be overwritten
	 * without ever being visible, except when Frame is transparent.
	 */
	public static final int DRAW_POST = 2; 		// draw GUI after draw() takes place

	protected float scale = 1; 					// not implemented yet

	/**
	 * The element that has got focus currently (always gets the KeyEvents).
	 */
	protected Control focusedElement = this;

	java.awt.Frame awtFrame;





	/**
	 * A protected version without PApplet only needed for the static dummy before
	 * the real Frame is created.
	 */
	protected Frame() {
		super();
	}

	/**
	 * It is necessary to pass the sketch object here. In most cases (if not in
	 * scope of a class) just use "this".
	 * 
	 * @param pa papplet
	 */
	public Frame(PApplet pa) {
		this(pa, DRAW_PRE);
	}

	/**
	 * Specify the draw time mode with this constructor.
	 * 
	 * @param pa         papplet
	 * @param timeToDraw DRAW_PRE or DRAW_POST
	 */
	public Frame(PApplet pa, int timeToDraw) {
		super();

		// if static Frame has been a nullFrame (not yet initialized correctly with
		// papplet) set this the new frame0
		if (Frame.frame0.isNullFrame) {
			Frame.frame0 = this;
			isNullFrame = false;
		}

		this.papplet = pa;



		// default size fills out entire sketch window
		width = papplet.width;
		height = papplet.height;
		offsetX = 0;
		offsetY = 0;

		keyListener = new KeyListener(this);

		animations = new ArrayList<Animation>();

		init_pfont();		// initialize a pfont for getting textWidth/textDescent...

		new Protected_Frame(timeToDraw);


		// jframe = (javax.swing.JFrame)((processing.awt.PSurfaceAWT.SmoothCanvas)getSurface().getNative()).getFrame();
		awtFrame = ((processing.awt.PSurfaceAWT.SmoothCanvas) papplet.getSurface().getNative()).getFrame();


		awtFrame.addWindowFocusListener(new java.awt.event.WindowFocusListener() {
			@Override
			public void windowLostFocus(java.awt.event.WindowEvent e) {

			}

			@Override
			public void windowGainedFocus(java.awt.event.WindowEvent e) {
			}
		});

		awtFrame.addComponentListener(new java.awt.event.ComponentAdapter() {
			public void componentResized(java.awt.event.ComponentEvent evt) {
				resize();
			}
		});
	}


	// a protected inner frame that users cannot access so keyEvent, mouseEvent, pre
	// and draw are hidden from user
	// only works protected
	protected class Protected_Frame {
		Protected_Frame(int timeToDraw) {
			papplet.registerMethod("keyEvent", this);
			papplet.registerMethod("mouseEvent", this);
			if (timeToDraw == DRAW_PRE) {
				papplet.registerMethod("pre", this);
			} else if (timeToDraw == DRAW_POST) {
				papplet.registerMethod("draw", this);
			} else {
				System.out.println("Error can't initialize frame with these arguments: " + timeToDraw + ". Use Frame.DRAW_POST or Frame.DRAW_PRE");
			}
		}

		public void pre() {
			Frame.this.display();
		}

		public void draw() {
			Frame.this.display();
		}

		public void mouseEvent(MouseEvent e) {
			Frame.this.mouseEvent(e);
		}

		public void keyEvent(KeyEvent e) {
			Frame.this.keyEvent(e);
		}
	}





	/*
	 * Draw mode determines how often the gui will be drawn:
	 *
	 * - In efficient mode the sketch runs as usual but the gui will be drawn only
	 * when changes occured. This saves resources. 
	 *
	 * - In continous mode the gui is drawn each frame new. This can be helpful when
	 * the guiSET is combined with normal drawing functions in the draw()-method.
	 * This way the gui itself will always be visible and not overdrawn in strange
	 * manners. It is the most inefficient mode but not by far.
	 *
	 * - no_loop is an experiment that shuts off the looping altogether. The
	 * draw()-method is only called when the gui changed due to any events. Still
	 * that leads to problems with resizing the window and details such as the
	 * cursor animation with textboxes.
	 */



	protected int drawMode = EFFICIENT;

	/**
	 * Draw frequency mode. Continous makes Frame draw the entire GUI EACH time
	 * (standard is 60 times per sec) if it has changed or not. This is useful if
	 * guiSET is combined with manual drawing on the sketch but it is the most
	 * wasteful mode.This is the default and for many cases recommended mode.
	 */
	public static final int CONTINOUS = 0;

	/**
	 * Draw frequency mode. Only refresh if an element has changed. It still keeps
	 * the {@link PApplet#draw()} loop running to check for some events but only
	 * redraws if necessary.
	 */
	public static final int EFFICIENT = 1;

	/**
	 * Most efficient mode (but not a lot more than EFFICIENT). The
	 * {@link PApplet#draw()} loop is interrupted and only key and mouse events are
	 * still received and can change the state of the GUI. Also animations have
	 * trouble working here (yet).
	 */
	public static final int NO_LOOP = 2;

	/**
	 * Set draw frequency mode {@link #EFFICIENT} {@link #CONTINOUS}
	 * {@link #NO_LOOP}
	 * 
	 * @param mode accepts Frame.EFFICIENT, Frame.CONTINOUS, Frame.NO_LOOP
	 */
	public void setMode(int mode) {
		this.drawMode = mode;
		if (mode == NO_LOOP) {
			papplet.noLoop();
			papplet.redraw();
		} else {
			papplet.loop();
		}
	}






	/**
	 * Called each draw loop
	 */
	private void display() {


		if (!initialized) {

			initialize();	// recursive procedure going through all elements connected to Frame
			handleEvent(guiInitializedListener, null);
			initialized = true;


			if (resizable) {
				// somehow sometimes a second render is important if resizable is active.
				// In fact that's because a PFont() is created which calls some stuff. Has to do
				// with something in Graphics awt java class
				render();
				update();
				return;
			}
		}

		/*
		 * check if window has been resized
		 */
		/*if (currentWidth != papplet.width || currentHeight != papplet.height) { // window resized
			currentWidth = papplet.width;
			currentHeight = papplet.height;
		
			handleEvent(windowResizeListener, null);
		
			// always resize frame to window size
			this.width = papplet.width;
			this.height = papplet.height;
		
			// perform own and childrens internal resize event
			resize();
		
			// frame resize event (need to call this because in resize no anchors are set
			// usually
			// yes, this is basically redundant to the WINDOW_RESIZE_EVENT (for the Frame
			// class) but it's easier for users.
			handleEvent(resizeListener, null);
		
			update();
		}
		*/
		/*
		 * handle animations
		 */
		for (int i = 0; i < animations.size(); i++) {
			if (!animations.get(i).animate()) { // if animation returns false it has finished and can be removed
				animations.remove(i);
			}
		}

		/*
		 * re-render if graphics have been changed
		 */

		if (visible) {
			render();
		}
	}


	@Override
	protected void render() {
		if (dirty) {

			// calcBoundsCount = 0;
			// renderCount = 0;
			// renderedObjects = "";

			dirty = false;
			// long t0 = System.nanoTime();

			preRender(); // for frame
			super.render();    // render everything
			pg.endDraw();

			// System.out.println((System.nanoTime() - t0));


			if (drawMode == EFFICIENT) {
				papplet.image(pg, 0, 0);
			}
		}

		// project graphics onto papplet
		if (drawMode == CONTINOUS || drawMode == NO_LOOP) {
			papplet.image(pg, 0, 0);
		}

	}




	@Override // - Frame doesn't need to call its parent to update
	protected void update() {
		dirty = true;

		/*
		 * call redraw upon sketch when changed occured
		 */
		if (drawMode == NO_LOOP) {
			papplet.redraw();
		}
	}

	@Override
	protected void resize() {
		handleEvent(windowResizeListener, null);

		// always resize frame to window size
		this.width = papplet.width;
		this.height = papplet.height;

		// perform own and childrens internal resize event
		super.resize();

		// frame resize event (need to call this because in resize no anchors are set
		// usually
		// yes, this is basically redundant to the WINDOW_RESIZE_EVENT (for the Frame
		// class) but it's easier for users.
		handleEvent(resizeListener, null);

		update();
	}


	protected boolean setupFinished() {
		return initialized;
	}



	// some debugging vars

	protected static int calcBoundsCount = 0;
	protected static int renderCount = 0;
	protected static String renderedObjects;









	/*
	 * SHORTCUTS
	 */

	// list of registered shortcuts
	protected HashMap<Shortcut, ShortcutDetails> shortcutMethods = new HashMap<Shortcut, ShortcutDetails>();


	// store a method and the object it shall be invoked on in one class:
	class ShortcutDetails {
		Method method;
		Object object;

		// if shortcut is strong then it will be executed even if the focused element
		// ovverrides shortcuts
		boolean strong;

		ShortcutDetails(Method method, Object object, boolean strong) {
			this.method = method;
			this.object = object;
			this.strong = strong;
		}
	}

	/**
	 * Register a strong shortcut to the sketch and fire the given method when the
	 * combination is hit on the keyboard.
	 * 
	 * @param shortcut   shortcut to register
	 * @param methodName method to execute when shortcut is pressed.
	 * @return true if registering has been successful.
	 */
	public boolean registerShortcut(Shortcut shortcut, String methodName) {
		return registerShortcut(shortcut, methodName, papplet, false);
	}

	/**
	 * @see #registerShortcut(Shortcut, String) version with custom target object.
	 * 
	 * @param shortcut   shortcut to register
	 * @param methodName method to execute when shortcut is pressed.
	 * @param target     object that declares the callback method.
	 * @return true if registering has been successful.
	 */
	public boolean registerShortcut(Shortcut shortcut, String methodName, Object target) {
		return registerShortcut(shortcut, methodName, target, false);
	}

	/**
	 * If the shortcut has been removed returns true. This is not the case if given
	 * shortcut has never been registered. The given shortcut does not need to be
	 * THE exact same as the registered one. It can be a new one with the same
	 * attributes.
	 * 
	 * @param shortcut shortcut to deregister
	 * @return true if deregistering has been successful.
	 */
	public boolean deregisterShortcut(Shortcut shortcut) {
		int size = shortcutMethods.size();
		shortcutMethods.remove(shortcut);
		return shortcutMethods.size() < size;
	}


	/**
	 * @see #registerShortcut(Shortcut, String, Object)
	 * 
	 *      If strong is set to true then this shortcut will even work when a
	 *      textbox has focus! Default is false.
	 * 
	 * 
	 * @param shortcut   shortcut to register
	 * @param methodName method to execute when shortcut is pressed.
	 * @param target     object that declares the callback method.
	 * @param strong     Should this shortcut even work when a textbox has focus?
	 * @return true if registering has been successful.
	 */
	public boolean registerShortcut(Shortcut shortcut, String methodName, Object target, boolean strong) {
		Class<?> c = target.getClass();
		try {

			Method method = c.getMethod(methodName);
			shortcutMethods.put(shortcut, new ShortcutDetails(method, target, strong));
		} catch (NoSuchMethodException nsme) {
			papplet.die("There is no public " + methodName + "() method in the class " + target.getClass().getName());
		} catch (Exception e) {
			papplet.die("Could not register " + methodName + " +() for " + target, e);
		}
		return false;
	}

	protected boolean checkShortcut(Shortcut shortcut) {

		ShortcutDetails sd = shortcutMethods.get(shortcut);
		if (sd != null) {
			if (!focusedElement.overridesFrameShortcuts || sd.strong) { // don't handle shortcut if focused element
																		 // overrides shortcuts, but only if shortcut
																		 // isn't strong
				handleShortcut(sd);
			}
			return true;
		}
		return false;
	}

	protected void handleShortcut(ShortcutDetails shortcutDetails) {
		Method method = shortcutDetails.method;
		Object object = shortcutDetails.object;
		try {
			method.invoke(object);
		} catch (Exception e) {
			// check for wrapped exception and get the root exception
			Throwable t;
			if (e instanceof InvocationTargetException) {
				InvocationTargetException ite = (InvocationTargetException) e;
				t = ite.getCause();
			} else {
				t = e;
			}
			// check for a RuntimeException and allow to bubble up
			if (t instanceof RuntimeException) {
				// re-throw exception
				throw (RuntimeException) t;
			} else {
				t.printStackTrace();
			}
		}
	}











	/*
	 * FOCUS HANDLING
	 * 
	 * when other controls request focus or blur
	 * 
	 */

	protected void requestFocus(Control control) {
		if (focusedElement == control)
			return;

		if (control.focusable && !focusedElement.stickyFocus) {
			focusedElement.focused = false;
			focusedElement.update();

			focusedElement = control;
			focusedElement.focused = true;
			focusedElement.update();

			focusedElement.handleEvent(focusedElement.focusListener, focusedElement);
		}
	}

	protected void requestBlur(Control control) {
		if (focusedElement == control) {
			focusedElement.focused = false;
			focusedElement.update();
			focusedElement = this; // focusedElement should never be null!
		}
	}







	/*
	 * ANIMATIONS
	 */

	protected ArrayList<Animation> animations;

	protected void animateImpl(String attribute, Control target, float aimedValue, double milliseconds) {

		Animation newAnimation = new Animation(attribute, target, aimedValue, milliseconds);

		// replace animation of same kind (same object and attribute)
		for (int i = 0; i < animations.size(); i++) {
			if (animations.get(i).compare(newAnimation)) {
				animations.remove(i);
			}
		}
		animations.add(newAnimation);
	}








	/*
	 * WINDOW STYLE SETTER (using the papplets PSurface)
	 */

	/**
	 * Set application icon.
	 * 
	 * @param img icon image
	 */
	public void setIcon(PImage img) {
		papplet.getSurface().setIcon(img);
	}

	/**
	 * Set application title.
	 * 
	 * @param title title
	 */
	public void setTitle(String title) {
		papplet.getSurface().setTitle(title);
	}

	protected boolean resizable = false;


	/**
	 * Make Application user-resizable. Default: false.
	 * 
	 * @param resizable resizable
	 */
	public void setResizable(boolean resizable) {
		this.resizable = resizable;
		papplet.getSurface().setResizable(resizable);
	}

	/**
	 * Check if application has been set to resizable.
	 * 
	 * @return resizable resizable
	 */
	public boolean isResizable() {
		return resizable;
	}

	/**
	 * Set application location on computer screen.
	 * 
	 * @param x in pixel from the left
	 * @param y in pixel from the top
	 */
	public void setLocation(int x, int y) {
		papplet.getSurface().setLocation(x, y);
	}

	/**
	 * Set the size of the application window.
	 * 
	 * @param width  width in pixel
	 * @param height height in pixel
	 */
	public void setWindowSize(int width, int height) {
		papplet.getSurface().setSize(width, height);
		setSize(width, height);
	}

	/**
	 * Set the minimum size of the application window
	 * 
	 * @param minWidth minimum width
	 * @param minHeight minimum height
	 */
	public void setMinimumWindowSize(int minWidth, int minHeight) {
		awtFrame.setMinimumSize(new Dimension(minWidth, minHeight));
	}

	/**
	 * Set the maximum size of the application window+
	 * 
	 * @param maxWidth maximum width
	 * @param maxHeight maximum height
	 */
	public void setMaximumWindowSize(int maxWidth, int maxHeight) {
		awtFrame.setMaximumSize(new Dimension(minWidth, minHeight));

	}

	protected void setAlwaysOnTop(boolean alwaysOnTop) {
		awtFrame.setAlwaysOnTop(alwaysOnTop);
	}


	// Dont allow setting these attributes for Frame

	@Override
	public void setWidth(int width) {
	}

	@Override
	public void setHeight(int height) {
	}

	@Override
	public void setX(int x) {
	}

	@Override
	public void setY(int y) {
	}

	@Override
	public void addAutoAnchors(int... anchors) {
	}

	@Override
	public void setAnchor(int anchorType, int value) {
	}




	/*
	 * EVENTS
	 */

	protected EventListener openKeyListener;
	protected EventListener guiInitializedListener;
	protected EventListener windowResizeListener;
	protected EventListener enterWindowListener;
	protected EventListener exitWindowListener;

	/**
	 * Add a key listener which fires on key press, type and release events.
	 * 
	 * @param methodName method name
	 * @param target     object
	 */
	public void addKeyListener(String methodName, Object target) {
		openKeyListener = createEventListener(methodName, target, null);
	}

	public void addKeyListener(String methodName) {
		addKeyListener(methodName, getPApplet());
	}

	public void removeKeyListener() {
		openKeyListener = null;
	}

	/**
	 * Called when gui is initialized and draw() runs for the first time.
	 * 
	 * @param methodName method name
	 * @param target     object
	 */
	public void addGuiInitializedListener(String methodName, Object target) {
		guiInitializedListener = createEventListener(methodName, target, null);
	}

	public void addGuiInitializedListener(String methodName) {
		addGuiInitializedListener(methodName, getPApplet());
	}

	public void removeGuiInitializedListener() {
		guiInitializedListener = null;
	}

	/**
	 * Called when the application window is resized.
	 * 
	 * @param methodName method name
	 * @param target     object
	 */
	public void addWindowResizeListener(String methodName, Object target) {
		windowResizeListener = createEventListener(methodName, target, MouseEvent.class);
	}

	public void addWindowResizeListener(String methodName) {
		addWindowResizeListener(methodName, getPApplet());
	}

	public void removeWindowResizeListener() {
		windowResizeListener = null;
	}

	/**
	 * Listen for the mouse entering the window.
	 * 
	 * @param methodName method name
	 * @param target     object
	 */
	public void addEnterWindowListener(String methodName, Object target) {
		enterWindowListener = createEventListener(methodName, target, MouseEvent.class);
	}

	public void addEnterWindowListener(String methodName) {
		addEnterWindowListener(methodName, getPApplet());
	}

	public void removeEnterWindowListener() {
		enterWindowListener = null;
	}

	/**
	 * Listen for the mouse exiting the window.
	 * 
	 * @param methodName method name
	 * @param target     object
	 */
	public void addExitWindowListener(String methodName, Object target) {
		exitWindowListener = createEventListener(methodName, target, MouseEvent.class);
	}

	public void addExitWindowListener(String methodName) {
		addExitWindowListener(methodName, getPApplet());
	}

	public void removeExitWindowListener() {
		exitWindowListener = null;
	}





	/*
	 * Mouse event uniqe to the Frame class. 
	 * This is called by PApplet through Protected_Frame. 
	 * From here all other mouseEvents are executed.  
	 * 
	 * (non-Javadoc)
	 * @see guiSET.core.Control#mouseEvent(processing.event.MouseEvent)
	 */

	protected void mouseEvent(MouseEvent e) {
		/*
		 * dragging is handled separately and only for the draggedElement (which is
		 * always set when clicking on a control).
		 */

		currentMouseEvent = e;							// store mouse event statically here. No need to carry it around all the time

		Control prevHoveredElement = hoveredElement;	// control that has been hovered over during the previous frame
		hoveredElement = null;							// reset to find out the control that is being hovered over this frame

		int mousex = e.getX();
		int mousey = e.getY();


		// handle window mouse enter/exit events
		// not beautiful to call super.mouseEvents() in each case but anyway
		switch (e.getAction()) {
		case MouseEvent.DRAG:
			if (draggedElement != null) {
				stopPropagation(); // not even necessary, we dont call mouseEvent()
				hoveredElement = draggedElement;
				draggedElement.drag(e);
				draggedElement.handleEvent(draggedElement.dragListener, e);
			}
			break;
		case MouseEvent.RELEASE:
			if (draggedElement != null) {

				// release after drag: Only draggedElement should receive this event.
				stopPropagation(); // not even necessary, we dont call mouseEvent()

				draggedElement.release(e);
				draggedElement.handleEvent(draggedElement.releaseListener, e);

				// now we need to check if mouse is still over the element
				ArrayList<Control> trace = traceAbsoluteCoordinates(mousex, mousey);

				if (trace.size() > 0) {
					if (trace.get(0) == draggedElement) {
						hoveredElement = draggedElement; // hoveredElement not set, because not calling mouseEvent
					} else {
						//print("drop on ", trace.get(0));
					}
				}

				draggedElement = null;
			} else {
				mouseEvent(mousex, mousey);
			}
			break;
		case MouseEvent.ENTER:
			handleEvent(enterWindowListener, e);
			mouseEvent(mousex, mousey);
			break;
		case MouseEvent.EXIT:
			handleEvent(exitWindowListener, e);
			mouseEvent(mousex, mousey);
			break;

//			case MouseEvent.MOVE:
//			// move happens quite often which can be expensive. Only call move if there is
//			// at least one element using a move listener
//			// EDIT: this idea does not work because then enter/exit cant work
//			if (moveListenersCount() != 0 ) 
//			break;
//
		default:
			mouseEvent(mousex, mousey);
		}



		// exit previously hovered element
		if (prevHoveredElement != hoveredElement && prevHoveredElement != null) {
			prevHoveredElement.pHovered = false;
			prevHoveredElement.exit(e);
			prevHoveredElement.handleEvent(prevHoveredElement.exitListener, e);
		}

		// enter currently hovered element
		if (hoveredElement != null) {
			papplet.cursor(hoveredElement.cursor);

			if (!hoveredElement.pHovered) {
				hoveredElement.pHovered = true;
				hoveredElement.enter(e);
				hoveredElement.handleEvent(hoveredElement.enterListener, e);
			}
		}

		// reset stopPropagation for next frame (after handling mouseEvent and not
		// before, in case render mode is EFFICIENT)
		propagationStopped = false;
	}



	@Override
	public int getOffsetXWindow() {
		return offsetX;
	}

	@Override
	public int getOffsetYWindow() {
		return offsetY;
	}





	/*
	 * KEY EVENTS
	 */

	/*
	 * There's only one keyListener per GUI, it hands the recognized keystrokes over
	 * to the FocusedElement.
	 * 
	 * Independently of the focusedElement keystrokes are always compared to the
	 * registered shortcuts.
	 */

	protected KeyListener keyListener;


	private void keyEvent(KeyEvent e) {
		keyListener.handleKeyEvent(e, focusedElement);
		handleEvent(openKeyListener, e);
	}

	/**
	 * Check if the CONTROL key is currently being hold down
	 * 
	 * @return true if control key is down
	 */
	public boolean isControlDown() {
		return keyListener.isControlDown();
	}

	/**
	 * Check if the SHIFT key is currently being hold down
	 * 
	 * @return true if shift key is down
	 */
	public boolean isShiftDown() {
		return keyListener.isShiftDown();
	}

	/**
	 * Check if the ALT key is currently being hold down
	 * 
	 * @return true if alt key is down
	 */
	public boolean isAltDown() {
		return keyListener.isAltDown();
	}

	/**
	 * Check if the META key is currently being hold down
	 * 
	 * @return true if meta key is down
	 */
	public boolean isMetaDown() {
		return keyListener.isMetaDown();
	}







	/**
	 * Get guiSET version.
	 * 
	 * @return version
	 */
	public String getVersion() {
		return versionCode;
	}
}