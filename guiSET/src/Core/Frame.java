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

//import java.awt.Font;
import java.lang.Exception;
import java.lang.reflect.Method;
import java.util.HashMap;

import guiSET.classes.*;

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
	 * custom Component classes. Only one Frame may exist per sketch.
	 */
	public static Frame frame0 = new Frame();
	protected boolean isNullFrame = true; 		// indicates that frame0 is intially a placeholder, is set to false as soon as
											 		// another one is created

	private String versionCode = "Version 0.8.1";








	/**
	 * Parent sketch
	 */
	protected PApplet papplet;

	public PApplet getPApplet() {
		return papplet;
	}

	public static PApplet papplet() {
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
	 * @param pa
	 */
	public Frame(PApplet pa) {
		this(pa, DRAW_PRE);
	}

	/**
	 * Specify the draw time mode with this constructor.
	 * 
	 * @param pa
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

		/*
		 * if (timeToDraw == DRAW_PRE) { papplet.registerMethod("pre", this); } else if
		 * (timeToDraw == DRAW_POST) { papplet.registerMethod("draw", this); } else {
		 * System.out.println("error can't initialize frame with these arguments: " +
		 * timeToDraw); }
		 */

		// default size fills out entire sketch window
		width = papplet.width;
		height = papplet.height;
		bounds.X = papplet.width;
		bounds.Y = papplet.height;
		relativeX = 0;
		relativeY = 0;
		currentWidth = width;
		currentHeight = height;

		setupListeners(5); // add 5 additional listeners: key, enter, exit, resize, init

		// papplet.registerMethod("keyEvent", this);
		keyListener = new KeyListener(this);

		// papplet.registerMethod("mouseEvent", this);

		animations = new ArrayList<Animation>();


		Control.init_textinfo_graphics();		// initialize a 1x1 dummy graphics used for getting textwidth

		@SuppressWarnings("unused")
		Protected_Frame f = new Protected_Frame(timeToDraw);
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



	// PFont myFont = createFont(new Font("Lucida Sans", Font.PLAIN, 1), 1, true,
	// null, false);;

	// private PFont createFont(Font baseFont, float size, boolean smooth, char[]
	// charset, boolean stream) {
	// return new PFont(baseFont.deriveFont(size * 2), smooth, charset, stream, 2);
	// }



	/*
	 * Draw mode determines how often the gui will be drawn:
	 *
	 * - In economic mode the sketch runs as usual but the gui will be drawn only
	 * when changes occured. This can save ressources
	 *
	 * - In continous mode the gui is drawn each frame new. This can be helpful when
	 * the guiSET is combined with normal drawing functions in the draw()-method.
	 * This way the gui itself will always be visible and not overdrawn in strange
	 * manners. It is the most inefficient mode but not by far.
	 *
	 * - super_eco is an experiment that shuts off the looping altogether. The
	 * draw()-method is only called when the gui changed due to any events. Still
	 * that leads to problems with resizing the window and details such as the
	 * cursor animation with textboxes.
	 */



	protected int drawMode = EFFICIENT;

	/**
	 * Draw frequency mode. Continous makes Frame draw the entire GUI EACH time
	 * (standard is 60 times per sec) if it hsa changed or not. This is useful if
	 * guiSET is combined with manual drawing on the sketch but it is the most
	 * wasteful mode.
	 */
	public static final int CONTINOUS = 0;

	/**
	 * Draw frequency mode. Only refresh if any Component has changed. It still
	 * keeps the {@link PApplet#draw()} loop running to check for some events but
	 * only redraws if necessary.
	 */
	public static final int EFFICIENT = 1;

	/**
	 * Most efficient mode (but not a lot more than EFFICIENT). The
	 * {@link PApplet#draw()} loops is interrupted and only key and mouse events are
	 * still received and can change the state of the GUI. Also animations have
	 * trouble working here (yet). This is the default and for many cases
	 * recommended mode.
	 */
	public static final int SUPER_EFF = 2; // noLoop(), only draw when necessary (still problems with animations though)

	/**
	 * Set draw frequency mode {@link #EFFICIENT} {@link #CONTINOUS}
	 * {@link #SUPER_EFF}
	 * 
	 * @param mode accepts Frame.EFFICIENT, Frame.CONTINOUS, Frame.SUPER_EFF
	 */
	public void setMode(int mode) {
		this.drawMode = mode;
		if (mode == SUPER_EFF) {
			papplet.noLoop();
			papplet.redraw();
			if (resizable) {
				System.out.println("resizing doesn't really work together with SUPER_ECO drawing mode");
			}
		} else {
			papplet.loop();
		}

	}



	// size copies for resize checking
	protected int currentWidth;
	protected int currentHeight;

	// before draw() already draw GUI, so the user may draw above it, has to be
	// public


	/**
	 * DO NOT CALL THIS METHOD. It is automatically called by the sketch and needs
	 * to be public for that reason.
	 */
	/*
	 * public void pre() { this.display(); }
	 */

	// Post version. (not really post() because then we would not see the changes).
	// after draw() happened draw GUI, so the gui overwrites everything that has
	// been drawn before in this frame, has to be public

	/**
	 * DO NOT CALL THIS METHOD. It is automatically called by the sketch and needs
	 * to be public for that reason.
	 */
	/*
	 * public void draw() { this.display(); }
	 */



	/**
	 * Called each draw loop
	 */
	private void display() {

		// int t1 = papplet.millis();



		/*
		 * check if window has been resized
		 */
		if (currentWidth != papplet.width || currentHeight != papplet.height) { // window resized
			currentWidth = papplet.width;
			currentHeight = papplet.height;

			handleRegisteredEventMethod(WINDOW_RESIZE_EVENT, null);

			// always resize frame to window size
			this.width = papplet.width;
			this.height = papplet.height;
			bounds.X = papplet.width;
			bounds.Y = papplet.height;

			// perform own and childrens internal resize event
			resize();

			// frame resize event (need to call this because in resize no anchors aare set
			// usually
			// yes, this is basically redundant to the WINDOW_RESIZE_EVENT (for the Frame
			// class) but
			// it's easier for users.
			handleRegisteredEventMethod(RESIZE_EVENT, null);

			update();
		}

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
		if (dirty && visible) {

//			calcBoundsCount = 0;
//			renderCount = 0;
//			renderedObjects = "";

			dirty = false;
			// long t0 = System.nanoTime();

			calcBounds();

			preRender(); // for frame
			render();    // render everything
			pg.endDraw();

			// System.out.println((System.nanoTime() - t0));
			// t0 = System.nanoTime();
			// System.out.println(System.nanoTime() - t0 + " " + calcB + " " + render);

			/*
			 * first render, then calcBounds?
			 * 
			 * then controls could change their dimensions in render();
			 * 
			 * but scrollcontainers need their items to know their bounds so they can check
			 * which ones are actually visible on screen
			 */


			if (drawMode == EFFICIENT) {
				papplet.image(pg, 0, 0);
			}
		}

		if (!initialized) {

<<<<<<< HEAD
			initialize();	// recursive procedure going through all elements connected to Frame
			handleRegisteredEventMethod(GUI_INITIALIZED_EVENT, null);
			
=======
			handleRegisteredEventMethod(GUI_INITIALIZED_EVENT, null);
			// initialize(); // no yet really implemented
>>>>>>> branch 'master' of https://github.com/Mc-Zen/guiSET.git
			if (resizable)
				// somehow an update is important a second time if resizable is active
				// in fact thats because a PFont() is created which calls some stuff. Has to do
				// with something in Graphics awt java class
				update();

			initialized = true;
		}


		// project graphics onto papplet
		if (visible && (drawMode == CONTINOUS || drawMode == SUPER_EFF)) {
			papplet.image(pg, 0, 0);
		}

	}





	@Override // - Frame doesn't need to call its parent to update
	protected void update() {
		dirty = true;

		/*
		 * call redraw upon sketch when changed occured
		 */
		if (drawMode == SUPER_EFF) {
			papplet.redraw();
		}
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
	 * @param shortcut
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
			papplet.die("Could not register " + methodName + " + () for " + target, e);
		}
		return false;
	}

	protected boolean checkShortcut(Shortcut shortcut) {

		ShortcutDetails sd = shortcutMethods.get(shortcut);
		if (sd != null) {
			if (!focusedElement.overridesFrameShortcuts || sd.strong) { // don't handle shortcut if focused element
<<<<<<< HEAD
																		 // overrides shortcuts, but only if shortcut
																		 // isn't strong
=======
																			 // overrides shortcuts, but only if shortcut
																			 // isn't strong
>>>>>>> branch 'master' of https://github.com/Mc-Zen/guiSET.git
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
			// check for wrapped exception, get root exception
			Throwable t;
			if (e instanceof InvocationTargetException) {
				InvocationTargetException ite = (InvocationTargetException) e;
				t = ite.getCause();
			} else {
				t = e;
			}
			// check for RuntimeException, and allow to bubble up
			if (t instanceof RuntimeException) {
				// re-throw exception
				throw (RuntimeException) t;
			} else {
				// trap and print as usual
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

			focusedElement.handleRegisteredEventMethod(FOCUS_EVENT, focusedElement);
		}
	}

	protected void requestBlur(Control control) {
		if (focusedElement == control) {
			focusedElement.focused = false;
			focusedElement.update();
			focusedElement = this;
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
		if (drawMode == SUPER_EFF) {
			System.out.println("resizing doesn't really work together with SUPER_ECO drawing mode");
		}
		this.resizable = resizable;
		papplet.getSurface().setResizable(resizable);
	}

	/**
	 * Check if application has been set to resizable.
	 * 
	 * @return resizable
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
	}








	/*
	 * EVENTS
	 */


	public static final int KEY_EVENT = Frame.numberMouseListeners;

	public static final int GUI_INITIALIZED_EVENT = Frame.numberMouseListeners + 1;

	public static final int WINDOW_RESIZE_EVENT = Frame.numberMouseListeners + 2;

	public static final int MOUSE_ENTER_WINDOW_EVENT = Frame.numberMouseListeners + 3;
	public static final int MOUSE_EXIT_WINDOW_EVENT = Frame.numberMouseListeners + 4;


	/**
	 * Add some more special listeners.
	 * 
	 * @param type       {@link #KEY_EVENT} {@link #GUI_INITIALIZED_EVENT}
	 *                   {@value #WINDOW_RESIZE_EVENT}
	 *                   {@link #MOUSE_ENTER_WINDOW_EVENT}
	 *                   {@link #MOUSE_EXIT_WINDOW_EVENT}
	 * @param methodName callback method name
	 * @return true if adding has been successful
	 */
	public boolean addListener(int type, String methodName) {
		return addListener(type, methodName, papplet);
	}

	/**
	 * @see #addListener(int, String) . Version with custom target.
	 * @param type
	 * @param methodName callback method name
	 * @param target
	 * @return true if adding has been successful
	 */
	public boolean addListener(int type, String methodName, Object target) {
		switch (type) {
		case KEY_EVENT:
			return registerEventRMethod(KEY_EVENT, methodName, target, KeyEvent.class);
		case GUI_INITIALIZED_EVENT:
			return registerEventRMethod(GUI_INITIALIZED_EVENT, methodName, target, null);
		case WINDOW_RESIZE_EVENT:
			return registerEventRMethod(WINDOW_RESIZE_EVENT, methodName, target, null);
		case MOUSE_ENTER_WINDOW_EVENT:
			return registerEventRMethod(MOUSE_ENTER_WINDOW_EVENT, methodName, target, MouseEvent.class);
		case MOUSE_EXIT_WINDOW_EVENT:
			return registerEventRMethod(MOUSE_EXIT_WINDOW_EVENT, methodName, target, MouseEvent.class);
		}
		return false;
	}

	/**
	 * Remove special listeners set with {@link #addListener(int, String, Object)}
	 * 
	 * @param type
	 * @param methodName
	 * @param target
	 */
	public void removeListener(int type, String methodName, Object target) {
		switch (type) {
		case KEY_EVENT:
			deregisterEventRMethod(KEY_EVENT);
			break;
		case GUI_INITIALIZED_EVENT:
			deregisterEventRMethod(GUI_INITIALIZED_EVENT);
			break;
		case WINDOW_RESIZE_EVENT:
			deregisterEventRMethod(WINDOW_RESIZE_EVENT);
			break;
		case MOUSE_ENTER_WINDOW_EVENT:
			deregisterEventRMethod(MOUSE_ENTER_WINDOW_EVENT);
			break;
		case MOUSE_EXIT_WINDOW_EVENT:
			deregisterEventRMethod(MOUSE_EXIT_WINDOW_EVENT);
			break;
		}
	}



	/*
	 * stopPropagation is a very important property that is used to indicate that
	 * one element "swallowed up" the mouseEvent so no other element will get it.
	 * 
	 * I.e. when one element is locally below another one then only the above should
	 * get the click event.
	 */

	protected boolean stopPropagation = false;

	/**
	 * Stop mouse event propagation. If called then no Components lower or at the
	 * same level in the Container hierachy will receive the currently processed
	 * mouse event.
	 */
	public static void stopPropagation() {
		frame0.stopPropagation = true;
	}

	/**
	 * Check if the mouse event propagation has been stopped.
	 * 
	 * @return propagation stop state
	 */
	public static boolean isPropagationStopped() {
		return frame0.stopPropagation;
	}



	/**
	 * DO NOT CALL THIS METHOD. Handled by the sketch and needs to be public.
	 */
	@Override
	protected void mouseEvent(MouseEvent e) {
		/*
		 * dragging is handled separately and only for the draggedElement (which is
		 * always set when clicking on a control).
		 */
<<<<<<< HEAD

		topmost = null;

		Control.currentMouseEvent = e;				// store mouse event statically here. No need to carry it around all the time
		Control prevHoveredElement = hoveredElement;	// control that has been hovered over during the previous frame
		Control.hoveredElement = null;		// reset to find out the control that is being hovered over this frame

		int mousex = e.getX();
		int mousey = e.getY();

		// handle window mouse enter/exit events
		// not beautiful to call super.mouseEvents() in each case but anyway
		switch (e.getAction()) {
		case MouseEvent.RELEASE:
			if (draggedElement != null) {

				// release after drag: Only draggedElement should receive this event, so
				// propagation needs to be stopped. But then "hoveredElement" will be null so
				// set hoveredElement
				// to draggedElement because otherwise we get an immediate exit event.
				Frame.stopPropagation();
				hoveredElement = draggedElement;

				draggedElement.release(e);
				draggedElement.handleRegisteredEventMethod(RELEASE_EVENT, e);
				draggedElement = null;
			}
			if (useNewMouseEvent)
				super.mouseEvent(mousex, mousey);
			else
				super.mouseEvent(e);
			break;
		case MouseEvent.DRAG:
			if (draggedElement != null) {
				Frame.stopPropagation();
				draggedElement.drag(e);
				draggedElement.handleRegisteredEventMethod(DRAG_EVENT, e);
			}
=======
		/*
		 * if (e.getAction() == MouseEvent.DRAG) { if (draggedElement != null) {
		 * draggedElement.drag(e); } } else { super.mouseEvent(e);
		 * 
		 * } if (e.getAction() == MouseEvent.RELEASE) { draggedElement = null; }
		 */
		topmost = null;

		// handle window mouse enter/exit events
		// not beautiful to call super.mouseEvents() in each case but anyway
		switch (e.getAction()) {
		case MouseEvent.RELEASE:
			draggedElement = null;
			super.mouseEvent(e);
			break;
		case MouseEvent.DRAG:
			if (draggedElement != null) {
				draggedElement.drag(e);
			}
			super.mouseEvent(e);
>>>>>>> branch 'master' of https://github.com/Mc-Zen/guiSET.git
			break;
		case MouseEvent.ENTER:
			handleRegisteredEventMethod(MOUSE_ENTER_WINDOW_EVENT, e);
<<<<<<< HEAD
			if (useNewMouseEvent)
				super.mouseEvent(mousex, mousey);
			else
				super.mouseEvent(e);
=======
			super.mouseEvent(e);
>>>>>>> branch 'master' of https://github.com/Mc-Zen/guiSET.git
			break;
		case MouseEvent.EXIT:
			handleRegisteredEventMethod(MOUSE_EXIT_WINDOW_EVENT, e);
<<<<<<< HEAD
			if (useNewMouseEvent)
				super.mouseEvent(mousex, mousey);
			else
				super.mouseEvent(e);
=======
			super.mouseEvent(e);
>>>>>>> branch 'master' of https://github.com/Mc-Zen/guiSET.git
			break;

<<<<<<< HEAD
//			case MouseEvent.MOVE:
//			// move happens quite often which can be expensive. Only call move if there is
//			// at least one element using a move listener
//			// EDIT: this idea does not work because then enter/exit cant work
//			if (moveListenersCount() != 0 ) 
//			break;
//
		default:
			if (useNewMouseEvent)
				super.mouseEvent(mousex, mousey);
			else
				super.mouseEvent(e);
=======
		// move happens quite often which can be expensive. Only call move if there is
		// at least one element using a move listener
		/*
		 * case MouseEvent.MOVE: if (moveListenersCount != 0 || true) {
		 * super.mouseEvent(e); } break;
		 */
		default:
			super.mouseEvent(e);
>>>>>>> branch 'master' of https://github.com/Mc-Zen/guiSET.git
		}
		if (topmost != null)
			papplet.cursor(topmost.cursor);



		if (useNewMouseEvent) {
			if (prevHoveredElement != hoveredElement && prevHoveredElement != null) {
				prevHoveredElement.pHovered = false;
				prevHoveredElement.exit(e);
				prevHoveredElement.handleRegisteredEventMethod(EXIT_EVENT, e);
			}
			if (hoveredElement != null) {
				papplet.cursor(hoveredElement.cursor);
				// PApplet.println(hoveredElement);

				if (!hoveredElement.pHovered) {
					hoveredElement.pHovered = true;
					hoveredElement.enter(e);
					hoveredElement.handleRegisteredEventMethod(ENTER_EVENT, e);
				}
			}
		} else {
			if (topmost != null)
				papplet.cursor(topmost.cursor);
		}


		// reset stopPropagation for next frame (after handling mouseEvent and not
		// before, in case mode is ECONOMIC)
		stopPropagation = false;
	}

	@Override
	public int getOffsetXWindow() {
		return relativeX;
	}

	@Override
	public int getOffsetYWindow() {
		return relativeY;
	}


	private static int moveListenersCount = 0; // if zero do not process move events.

	protected static void incrementMoveListenersCount() {
		moveListenersCount++;
	}

	protected static void decrementMoveListenersCount() {
		moveListenersCount = Math.max(0, --moveListenersCount);
	}

	protected static Control topmost;

	protected static Control topmost;


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

	/**
	 * DO NOT CALL THIS METHOD. Handled by the sketch and needs to be public.
	 */
	protected void keyEvent(KeyEvent e) {
		keyListener.handleKeyEvent(e, focusedElement);
		handleRegisteredEventMethod(KEY_EVENT, e);
	}

	/**
	 * Check if the CONTROL key is currently being hold down
	 * 
	 * @return
	 */
	public boolean isControlDown() {
		return keyListener.isControlDown();
	}

	/**
	 * Check if the SHIFT key is currently being hold down
	 * 
	 * @return
	 */
	public boolean isShiftDown() {
		return keyListener.isShiftDown();
	}

	/**
	 * Check if the ALT key is currently being hold down
	 * 
	 * @return
	 */
	public boolean isAltDown() {
		return keyListener.isAltDown();
	}

	/**
	 * Check if the META key is currently being hold down
	 * 
	 * @return
	 */
	public boolean isMetaDown() {
		return keyListener.isMetaDown();
	}







	/**
	 * Get guiSET version.
	 * 
	 * @return
	 */
	public String getVersion() {
		return versionCode;
	}
}