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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.io.File;
import java.lang.Exception;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;

import java.lang.reflect.InvocationTargetException;

import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import java.lang.reflect.Field;

/**
 * 
 * Master Container that fills out entire sketch and controls the flow of events and rendering of
 * the GUI.
 *
 */
public class Frame extends Container {

	/*
	 * frame0 is the standard frame that can always be accessed. When the programmer
	 * creates a new Frame : Frame f = new Frame(PApplet) the non-ready placeholder 
	 * frame will be replaced by that new one.
	 * 
	 * It is available to all classes for exchanging information through getFrame(). 
	 * Normally a programmer does not need it except when developing
	 * custom component classes. Only one Frame may exist per sketch.
	 * 
	 * It is necessary to do so for this library and each component needs the parent
	 * papplet. But in this way some functions can be used even before and it tidies
	 * things up a lot.
	 * 
	 * Obviously only one Frame can be created AND effectively used per sketch.
	 * 
	 */
	private static Frame frame0 = new Frame();
	private boolean isNullFrame = true; 		// indicates that frame0 is intially a placeholder, is set to false as soon as
										 		// another one is created


	/**
	 * Get the one and only Frame.
	 * 
	 * @return Frame
	 */
	public static Frame getFrame() {
		return frame0;
	}





	/**
	 * Parent sketch
	 */
	private PApplet papplet;



	public static PApplet getPApplet() {
		return frame0.papplet;
	}





	/*
	 * Initialization state. 
	 */
	protected enum InitializationState {
		NOT_INITIALIZED, INITIALIZING, INITIALIZED
	}

	private InitializationState initializationState = InitializationState.NOT_INITIALIZED;

	protected InitializationState getInitializationState() {
		return initializationState;
	}



	/**
	 * guiSET can draw the GUI either <i>before</i> ({@link DrawTime#PRE}, default) the Processing
	 * sketchs draw() method or <i>after</i> ({@link DrawTime#POST}). Currently, the decision needs to
	 * be made once at the creation time of {@link Frame}.
	 * 
	 * @see Frame#Frame(PApplet, DrawTime)
	 *
	 */
	public enum DrawTime {
		PRE, POST
	}

	/**
	 * GUI Draw time Mode: Draw the GUI before the {@link PApplet#draw()} happens. Useful when the user
	 * wants to image some own stuff upon the GUI. The draw time mode needs to be specified at the
	 * beginning when Frame is created. This is the default mode.
	 */
	public static final DrawTime DRAW_PRE = DrawTime.PRE; 		// draw GUI before draw() takes place

	/**
	 * GUI Draw time Mode: Draw the GUI after the {@link PApplet#draw()} happens.The draw time mode
	 * needs to be specified at the beginning when Frame is created. Everything that is drawn during
	 * {@link PApplet#draw()} will be overwritten without ever being visible, except when Frame is
	 * transparent.
	 */
	public static final DrawTime DRAW_POST = DrawTime.POST; 		// draw GUI after draw() takes place





	// private wrapper for registering some methods at papplet.
	private Protected_Frame protectedFrame;

	java.awt.Frame awtFrame;	// window if it is an AWTFrame
	GLWindow glWindow;			// window if it is a GLWindow




	/**
	 * A private version without PApplet only needed for the static dummy before the real Frame is
	 * created.
	 */
	private Frame() {
		super();
	}

	/**
	 * It is necessary to pass the sketch object here. In most cases (if not in scope of a class) just
	 * use "this".
	 * 
	 * @param pa papplet
	 */
	public Frame(PApplet pa) {
		this(pa, DrawTime.PRE);
	}



	/**
	 * Specify the draw time mode with this constructor.
	 * 
	 * @param pa         papplet
	 * @param timeToDraw DRAW_PRE or DRAW_POST
	 */
	public Frame(PApplet pa, DrawTime timeToDraw) {
		super();

		// if static Frame has been a nullFrame (not yet initialized correctly with
		// papplet) set this the new frame0
		if (frame0.isNullFrame) {
			frame0 = this;
			isNullFrame = false;
		}

		this.papplet = pa;



		// default size fills out entire sketch window
		setWidthNoUpdate(papplet.width);
		setHeightNoUpdate(papplet.height);

		keyListener = new KeyListener(this);
		animations = new ArrayList<Animation>();

		TextBased.init_text();

		protectedFrame = new Protected_Frame(timeToDraw); // register pre(), mouseEvent() and keyEvent() methods


		// Get the native window (either AWTFrame in Java2D mode or GLWindow in P2D/P3D
		// mode
		// to add some listeners for resizing and focus.
		Object nativeWindow = papplet.getSurface().getNative();

		if (nativeWindow instanceof processing.awt.PSurfaceAWT.SmoothCanvas) {

			awtFrame = ((processing.awt.PSurfaceAWT.SmoothCanvas) nativeWindow).getFrame();
			awtFrame.addWindowFocusListener(new java.awt.event.WindowFocusListener() {
				@Override
				public void windowLostFocus(java.awt.event.WindowEvent e) {
					handleEvent(windowFocusLostListener);
					MenuSurface.closeAllMenus();
				}

				@Override
				public void windowGainedFocus(java.awt.event.WindowEvent e) {
					handleEvent(windowFocusGainedListener);
				}

			});

			awtFrame.addComponentListener(new java.awt.event.ComponentAdapter() {
				public void componentResized(java.awt.event.ComponentEvent evt) {
					if (initializationState == InitializationState.INITIALIZED) {
						java.awt.Insets inset = awtFrame.getInsets(); // subtract window borders
						resized(awtFrame.getWidth() - inset.left - inset.right, awtFrame.getHeight() - inset.top - inset.bottom);
					}
				}
			});

			new DropTarget((Component) nativeWindow, new Guiset_Drop_Handler());

		} else if (nativeWindow instanceof GLWindow) {

			glWindow = ((GLWindow) nativeWindow);
			glWindow.addWindowListener(new com.jogamp.newt.event.WindowListener() {

				@Override
				public void windowLostFocus(com.jogamp.newt.event.WindowEvent e) {
					handleEvent(windowFocusLostListener);
					MenuSurface.closeAllMenus();
				}

				@Override
				public void windowGainedFocus(com.jogamp.newt.event.WindowEvent e) {
					handleEvent(windowFocusGainedListener);
				}

				@Override
				public void windowDestroyed(com.jogamp.newt.event.WindowEvent e) {
				}

				@Override
				public void windowDestroyNotify(com.jogamp.newt.event.WindowEvent e) {
				}

				@Override
				public void windowRepaint(com.jogamp.newt.event.WindowUpdateEvent e) {
				}

				@Override
				public void windowMoved(WindowEvent arg0) {

				}

				@Override
				public void windowResized(WindowEvent arg0) {
					if (initializationState == InitializationState.INITIALIZED) {
						resized(glWindow.getWidth(), glWindow.getHeight()); // in opengl mode, papplet does not know its size yet. Thus, we get it right from window
																			 // (already client area)
					}
				}
			});


		}

		try {
			pappletsRedrawField = PApplet.class.getDeclaredField("redraw");
			pappletsRedrawField.setAccessible(true);
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}

	}

	private Field pappletsRedrawField;


	// A protected inner frame that users cannot access so keyEvent, mouseEvent, pre
	// and draw are hidden from user. Only works protected (not private)
	protected class Protected_Frame {
		private boolean postRegistered = false;

		Protected_Frame(DrawTime timeToDraw) {
			papplet.registerMethod("keyEvent", this);
			papplet.registerMethod("mouseEvent", this);
			if (timeToDraw == DrawTime.PRE) {
				papplet.registerMethod("pre", this);
			} else if (timeToDraw == DrawTime.POST) {
				papplet.registerMethod("draw", this);
			} else {
				System.err.println("Error can't initialize Frame with these arguments: " + timeToDraw + ". Use Frame.DRAW_POST or Frame.DRAW_PRE");
			}
			// needed for animations to work in NO_LOOP mode
			if (refreshMode == RefreshMode.NO_LOOP) {
				registerPost();
			}
		}

		protected void registerPost() {
			if (!postRegistered) {
				papplet.registerMethod("post", this);
			}
		}

		protected void deregisterPost() {
			if (postRegistered) {
				papplet.unregisterMethod("post", this);
			}
		}

		public void pre() {
			Frame.this.display();
		}

		public void draw() {
			Frame.this.display();
		}

		public void post() {
			// Used in RefreshMode.NO_LOOP to schedule another redraw.
			if (refreshAgain) {
				getPApplet().redraw();
				refreshAgain = false;
			}
		}

		public void mouseEvent(MouseEvent e) {
			Frame.this.mouseEvent(e);
		}

		public void keyEvent(KeyEvent e) {
			Frame.this.keyEvent(e);
		}
	}







	/*
	 * RefreshMode determines how often the gui will be drawn:
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

	public enum RefreshMode {
		NO_LOOP, EFFICIENT, CONTINOUS
	}


	private RefreshMode refreshMode = RefreshMode.EFFICIENT;

	/**
	 * Draw frequency mode. Continous makes Frame draw the entire GUI EACH time (standard is 60 times
	 * per sec) if it has changed or not. This is useful if guiSET is combined with manual drawing on
	 * the sketch but it is the most wasteful mode.This is the default and for many cases recommended
	 * mode.
	 */
	public static final RefreshMode CONTINOUS = RefreshMode.CONTINOUS;

	/**
	 * Draw frequency mode. Only refresh if an element has changed. It still keeps the
	 * {@link PApplet#draw()} loop running to check for some events but only redraws if necessary.
	 */
	public static final RefreshMode EFFICIENT = RefreshMode.EFFICIENT;


	/**
	 * Most efficient mode. The {@link PApplet#draw()} loop is interrupted and only key and mouse events
	 * are still received and can change the state of the GUI. Animation timing is not as good.
	 */
	public static final RefreshMode NO_LOOP = RefreshMode.NO_LOOP;


	/**
	 * Set draw frequency mode {@link #EFFICIENT} {@link #CONTINOUS} {@link #NO_LOOP}. It is no
	 * recommended to change the mode very frequently (like every few frames) to {@link #NO_LOOP} and
	 * back to one of the others as thus might reduce performance.
	 * 
	 * @param mode accepts Frame.EFFICIENT, Frame.CONTINOUS, Frame.NO_LOOP
	 */
	public void setRefreshMode(RefreshMode mode) {
		this.refreshMode = mode;
		if (mode == RefreshMode.NO_LOOP) {
			papplet.noLoop();
			papplet.redraw();

			protectedFrame.registerPost();
		} else {
			papplet.loop();
			protectedFrame.deregisterPost();
		}
	}

	public RefreshMode getRefreshMode() {
		return refreshMode;
	}



	/*
	 * When RefreshMode is NO_LOOP, a call to redraw usually enables refreshing the gui.
	 * During drawing however, redraw() will do nothing as the redraw flag in PApplet 
	 * is only reset after drawing. Therefore Frame.update() will set this flag to true 
	 * when called during drawing. The post() method will check it and if the flag is 
	 * set, will schedule another redraw. 
	 * 
	 * This issue also occurs randomly when calling redraw() outside of the drawing process. 
	 * Therefore, the PApplets redraw flag is checked. If it is false but the Frame just got 
	 * dirty in this call of update() we know there is something wrong: PApplets redraw flag 
	 * should be false. Anyway, we just refresh again.     
	 */
	private boolean refreshAgain = false;



	@Override
	protected void update() {

		if (refreshMode == NO_LOOP) {
			try {
				// No other way to access this. But we are in NO_LOOP mode and performance is not a real issue.
				// When update is called very often very quick, this is evaluated much faster anyway due to
				// optimization.
				boolean redraw = (boolean) pappletsRedrawField.get(papplet);
				if (redraw && !dirty) { // PApplets redraw is already set to true but that's old news, as the Frame got dirty just now.
					refreshAgain = true;
				}
			} catch (IllegalArgumentException | IllegalAccessException e) { // will (should) not happen
				e.printStackTrace();
			}
			papplet.redraw(); // Call redraw upon sketch when changed occured

		}
		dirty = true;
	}


	@Override
	protected void initialize() {
		initializationState = InitializationState.INITIALIZING;
		super.initialize();
		handleEvent(guiInitializedListener);
		initializationState = InitializationState.INITIALIZED;

		if (resizable) {
			// somehow sometimes a second render is important if resizable is active.
			// In fact that's because a PFont() is created which calls some stuff. Has to do
			// with something in Graphics awt java class
			render();
			update();
			return;
		}
	}


	/**
	 * Called each draw loop
	 */
	private void display() {
		if (initializationState == InitializationState.NOT_INITIALIZED) {
			initialize();	// recursive procedure going through all elements connected to Frame
		}

		/*
		 * re-render if graphics have been changed
		 */
		if (visible) {
			render();
		}

		/*
		 * handle animations (after render, because only then redraw can work when sketch is not looping
		 */
		for (int i = 0; i < animations.size(); i++) {
			if (!animations.get(i).animate()) { // if animation returns false it has finished and can be removed
				animations.remove(i);
			}
		}
	}



	@Override
	protected void render() {
		if (dirty) {
			clipX0 = 0;
			clipY0 = 0;
			clipX1 = getWidth();
			clipY1 = getHeight();
			dirty = false;

			preRender(); 		// for frame
			super.render();		// render everything
			pg.endDraw();
			// System.out.println((System.nanoTime() - t0));

			if (refreshMode == EFFICIENT || refreshMode == NO_LOOP) {
				papplet.image(pg, 0, 0);
			}
		}

		// project graphics onto papplet
		if (refreshMode == CONTINOUS) {
			papplet.image(pg, 0, 0);
		}
	}



	protected void resized(int w, int h) {

		// always resize frame to window size
		setWidthNoUpdate(w);
		setHeightNoUpdate(h);

		handleEvent(windowResizeListener);

		// frame resize event (need to call this because in resize no anchors are set
		// usually
		// yes, this is basically redundant to the WINDOW_RESIZE_EVENT (for the Frame
		// class) but it's easier for users.
		handleEvent(resizeListener);
		for (Control c : items) {
			c.parentResized();
		}

		update();
	}




	protected boolean isSetupFinished() {
		return initializationState == InitializationState.INITIALIZED;
	}



	// some debugging vars

	protected static int calcBoundsCount = 0;
	protected static int renderCount = 0;
	protected static String renderedObjects;









	/*
	 * SHORTCUTS
	 */


	/*
	 * Registered shortcuts are put in a map with the callback mechanism. 
	 * This can either be a method (together with the object it shall be invoked on) - or a lambda (Predicate). 
	 * 
	 * A shortcut is handled by calling the virtual method shortcutCallback.handle(). 
	 */
	private HashMap<Shortcut, ShortcutCallback> shortcutMethods = new HashMap<Shortcut, ShortcutCallback>();



	private abstract class ShortcutCallback {
		boolean executeEvenIfFocusedElementOverridesNormalShortcuts = false;

		abstract void handle();

		boolean executeEvenIfFocusedElementOverridesNormalShortcuts() {
			return executeEvenIfFocusedElementOverridesNormalShortcuts;
		}
	}

	// Store a method and the object it shall be invoked on in one class
	private class ShortcutCallbackMethod extends ShortcutCallback {
		Method method;
		Object object;

		public ShortcutCallbackMethod(Method method, Object object, boolean executeEvenIfFocusedElementOverridesNormalShortcuts) {
			this.method = method;
			this.object = object;
			this.executeEvenIfFocusedElementOverridesNormalShortcuts = executeEvenIfFocusedElementOverridesNormalShortcuts;
		}

		@Override
		void handle() {
			try {
				method.invoke(object);
			} catch (Exception e) {
				Throwable t; // check for wrapped exception and get the root exception
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
	}

	// Store a lambda function as callback
	private class ShortcutCallbackLambda extends ShortcutCallback {
		Predicate p;

		public ShortcutCallbackLambda(Predicate p, boolean executeEvenIfFocusedElementOverridesNormalShortcuts) {
			this.p = p;
			this.executeEvenIfFocusedElementOverridesNormalShortcuts = executeEvenIfFocusedElementOverridesNormalShortcuts;
		}

		@Override
		void handle() {
			p.run();
		}
	}



	/**
	 * Register a shortcut to the sketch and fire the given lambda callback when the combination is hit
	 * on the keyboard.
	 * 
	 * @param shortcut shortcut to register
	 * @param callback lambda callback
	 * @return success
	 */
	public boolean registerShortcut(Shortcut shortcut, Predicate callback) {
		shortcutMethods.put(shortcut, new ShortcutCallbackLambda(callback, false));
		return true;
	}

	/**
	 * Register a shortcut to the sketch and fire the given lambda callback when the combination is hit
	 * on the keyboard.
	 * 
	 * @param shortcut                                            shortcut to register
	 * @param callback                                            lambda callback
	 * @param executeEvenIfFocusedElementOverridesNormalShortcuts Should this shortcut even work when
	 *                                                            i.e. a textbox has focus?
	 * @return success
	 */
	public boolean registerShortcut(Shortcut shortcut, Predicate callback, boolean executeEvenIfFocusedElementOverridesNormalShortcuts) {
		shortcutMethods.put(shortcut, new ShortcutCallbackLambda(callback, executeEvenIfFocusedElementOverridesNormalShortcuts));
		return true;
	}

	/**
	 * Register a shortcut to the sketch and fire the given method when the combination is hit on the
	 * keyboard.
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
	 * @see #registerShortcut(Shortcut, String, Object)
	 * 
	 *      If strong is set to true then this shortcut will even work when a textbox has focus! Default
	 *      is false.
	 * 
	 * 
	 * @param shortcut                                            shortcut to register
	 * @param methodName                                          method to execute when shortcut is
	 *                                                            pressed.
	 * @param target                                              object that declares the callback
	 *                                                            method.
	 * @param executeEvenIfFocusedElementOverridesNormalShortcuts Should this shortcut even work when a
	 *                                                            textbox has focus?
	 * @return true if registering has been successful.
	 */
	public boolean registerShortcut(Shortcut shortcut, String methodName, Object target, boolean executeEvenIfFocusedElementOverridesNormalShortcuts) {
		Class<?> c = target.getClass();
		try {

			Method method = c.getMethod(methodName);
			shortcutMethods.put(shortcut, new ShortcutCallbackMethod(method, target, executeEvenIfFocusedElementOverridesNormalShortcuts));
		} catch (NoSuchMethodException nsme) {
			papplet.die("There is no public " + methodName + "() method in the class " + target.getClass().getName());
		} catch (Exception e) {
			papplet.die("Could not register " + methodName + "() for " + target, e);
		}
		return false;
	}

	/**
	 * If the shortcut has been removed returns true. This is not the case if given shortcut has never
	 * been registered. The given shortcut does not need to be THE exact same as the registered one. It
	 * can be a new one with the same attributes.
	 * 
	 * @param shortcut shortcut to deregister
	 * @return true if deregistering has been successful.
	 */
	public boolean deregisterShortcut(Shortcut shortcut) {
		int size = shortcutMethods.size();
		shortcutMethods.remove(shortcut);
		return shortcutMethods.size() < size;
	}

	// KeyListener accesses this
	protected boolean checkShortcut(Shortcut shortcut) {
		ShortcutCallback sd = shortcutMethods.get(shortcut);
		if (sd != null) {
			// Don't handle shortcut if focused element overrides shortcuts. Exception: Shortcuts that
			// "executeEvenIfFocusedElementOverridesNormalShortcuts" xD
			if (!focusedElement.overridesFrameShortcuts() || sd.executeEvenIfFocusedElementOverridesNormalShortcuts()) {
				sd.handle();
			}
			return true;
		}
		return false;
	}












	/*
	 * FOCUS HANDLING
	 * 
	 * when other controls request focus or blur
	 * 
	 */


	/**
	 * The element that has got focus currently (always gets the KeyEvents).
	 */
	protected Control focusedElement = this;

	protected void requestFocus(Control control) {
		if (focusedElement == control)
			return;

		if (control.isFocusable() && !focusedElement.hasStickyFocus()) {
			focusedElement.focused = false;
			focusedElement.blurred();
			focusedElement.update();

			focusedElement = control;
			focusedElement.focused = true;
			focusedElement.focused();
			focusedElement.update();

			focusedElement.handleEvent(focusedElement.focusListener, focusedElement);
		}
	}

	protected void requestBlur(Control control) {
		if (focusedElement == control) {
			focusedElement.focused = false;
			focusedElement.blurred();
			focusedElement.update();
			focusedElement = this; // focusedElement should never be null!
		}
	}







	/*
	 * ANIMATIONS
	 */

	private ArrayList<Animation> animations;

	protected void animateImpl(String attribute, Control target, float aimedValue, double milliseconds) {

		Animation newAnimation = new Animation(attribute, target, aimedValue, milliseconds);

		// replace animation of same kind (same object and attribute)
		for (int i = 0; i < animations.size(); i++) {
			if (animations.get(i).compare(newAnimation)) {
				animations.remove(i);
				break; // we shoule have finished now
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
	 * Set the minimum size of the application window.
	 * 
	 * @param minWidth  minimum width
	 * @param minHeight minimum height
	 */
	public void setMinimumWindowSize(int minWidth, int minHeight) {
		if (awtFrame != null) {
			awtFrame.setMinimumSize(new Dimension(minWidth, minHeight));
		} else {
			System.err.println("In P2D/P3D mode minimum window size is not supported.");
		}
	}

	/**
	 * Set the maximum size of the application window.
	 * 
	 * @param maxWidth  maximum width
	 * @param maxHeight maximum height
	 */
	public void setMaximumWindowSize(int maxWidth, int maxHeight) {
		if (awtFrame != null) {
			awtFrame.setMaximumSize(new Dimension(minWidth, minHeight));
		} else {
			System.err.println("In P2D/P3D mode maximum window size is not supported.");
		}

	}

	protected void setAlwaysOnTop(boolean alwaysOnTop) {
		if (awtFrame != null) {
			awtFrame.setAlwaysOnTop(alwaysOnTop);
		} else if (glWindow != null) {
			glWindow.setAlwaysOnTop(alwaysOnTop);
		}
	}


	// Dont allow setting these attributes for Frame

	@Override
	public void setWidth(int width) {
		System.err.println("Use frame.setWindowSize() to set the size of the window. The Frame fills out the entire window");
	}

	@Override
	public void setHeight(int height) {
		System.err.println("Use frame.setWindowSize() to set the size of the window. The Frame fills out the entire window");
	}

	@Override
	public void setX(int x) {
		System.err.println("The Frame fills out the entire window. It cannot be positioned");
	}

	@Override
	public void setY(int y) {
		System.err.println("The Frame fills out the entire window. It cannot be positioned");
	}

	@Override
	public void addAutoAnchors(int... anchors) {
		System.err.println("Frame neither supports nor needs anchors.");
	}

	@Override
	public void setAnchor(int anchorType, int value) {
		System.err.println("Frame neither supports nor needs anchors.");
	}




	/*
	 * EVENTS
	 */

	protected EventListener openKeyListener;
	protected EventListener guiInitializedListener;
	protected EventListener windowResizeListener;
	protected EventListener enterWindowListener;
	protected EventListener exitWindowListener;
	protected EventListener dropElementListener;
	protected EventListener windowFocusGainedListener;
	protected EventListener windowFocusLostListener;
	protected EventListener externalDropListener;

	/**
	 * Add a key listener which fires on key press, type and release events.
	 * 
	 * Event arguments: {@link KeyEvent}
	 * 
	 * @param methodName method name
	 * @param target     object
	 */
	public void addKeyListener(String methodName, Object target) {
		openKeyListener = createEventListener(methodName, target, KeyEvent.class);
	}

	public void addKeyListener(String methodName) {
		addKeyListener(methodName, getPApplet());
	}

	public void addKeyListener(Predicate lambda) {
		openKeyListener = new LambdaEventListener(lambda);
	}

	public void addKeyListener(Predicate1<KeyEvent> lambda) {
		openKeyListener = new LambdaEventListener1<KeyEvent>(lambda);
	}

	public void removeKeyListener() {
		openKeyListener = null;
	}

	/**
	 * Called when gui is initialized and draw() runs for the first time.
	 * 
	 * Event arguments: none
	 * 
	 * @param methodName method name
	 * @param target     object
	 */
	public void addGuiInitializedListener(String methodName, Object target) {
		guiInitializedListener = createEventListener(methodName, target);
	}

	public void addGuiInitializedListener(String methodName) {
		addGuiInitializedListener(methodName, getPApplet());
	}

	public void addGuiInitializedListener(Predicate lambda) {
		guiInitializedListener = new LambdaEventListener(lambda);
	}

	public void removeGuiInitializedListener() {
		guiInitializedListener = null;
	}

	/**
	 * Called when the application window is resized.
	 * 
	 * Event arguments: {@link MouseEvent}
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

	public void addWindowResizeListener(Predicate lambda) {
		windowResizeListener = new LambdaEventListener(lambda);
	}

	public void addWindowResizeListener(Predicate1<MouseEvent> lambda) {
		windowResizeListener = new LambdaEventListener1<MouseEvent>(lambda);
	}

	public void removeWindowResizeListener() {
		windowResizeListener = null;
	}

	/**
	 * Listen for the mouse entering the window.
	 * 
	 * Event arguments: {@link MouseEvent}
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

	public void addEnterWindowListener(Predicate lambda) {
		enterWindowListener = new LambdaEventListener(lambda);
	}

	public void addEnterWindowListener(Predicate1<MouseEvent> lambda) {
		enterWindowListener = new LambdaEventListener1<MouseEvent>(lambda);
	}

	public void removeEnterWindowListener() {
		enterWindowListener = null;
	}

	/**
	 * Listen for the mouse exiting the window.
	 * 
	 * Event arguments: {@link MouseEvent}
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

	public void addExitWindowListener(Predicate lambda) {
		exitWindowListener = new LambdaEventListener(lambda);
	}

	public void addExitWindowListener(Predicate1<MouseEvent> lambda) {
		exitWindowListener = new LambdaEventListener1<MouseEvent>(lambda);
	}

	public void removeExitWindowListener() {
		exitWindowListener = null;
	}



	/**
	 * Internal drop listener for any element in the application. First arg: dropped element, Second
	 * arg: target on which the first on has been dropped.
	 * 
	 * Event arguments: {@link Control}, {@link Control}
	 * 
	 * @param methodName method name
	 * @param target     object
	 */
	public void addDropElementListener(String methodName, Object target) {
		dropElementListener = createEventListener(methodName, target, Control.class, Control.class);
	}

	public void addDropElementListener(String methodName) {
		addDropElementListener(methodName, getPApplet());
	}

	public void addDropElementListener(Predicate lambda) {
		dropElementListener = new LambdaEventListener(lambda);
	}

	public void addDropElementListener(Predicate2<Control, Control> lambda) {
		dropElementListener = new LambdaEventListener2<Control, Control>(lambda);
	}

	public void removeDropElementListener() {
		dropElementListener = null;
	}

	/**
	 * Fired when the window gained focus.
	 * 
	 * Event arguments: none
	 * 
	 * @param methodName method name
	 * @param target     object
	 */
	public void addWindowFocusGainedListener(String methodName, Object target) {
		windowFocusGainedListener = createEventListener(methodName, target);
	}

	public void addWindowFocusGainedListener(String methodName) {
		addWindowFocusGainedListener(methodName, getPApplet());
	}

	public void addWindowFocusGainedListener(Predicate lambda) {
		windowFocusGainedListener = new LambdaEventListener(lambda);
	}

	public void removeWindowFocusGainedListener() {
		windowFocusGainedListener = null;
	}

	/**
	 * Fired when the window lost focus.
	 * 
	 * Event arguments: none
	 * 
	 * @param methodName method name
	 * @param target     object
	 */
	public void addWindowFocusLostListener(String methodName, Object target) {
		windowFocusLostListener = createEventListener(methodName, target);
	}

	public void addWindowFocusLostListener(String methodName) {
		addWindowFocusLostListener(methodName, getPApplet());
	}

	public void addWindowFocusLostListener(Predicate lambda) {
		windowFocusLostListener = new LambdaEventListener(lambda);
	}

	public void removeWindowFocusLostListener() {
		windowFocusLostListener = null;
	}

	/**
	 * External drop listener for data dropped to the application.
	 * 
	 * Event arguments:
	 * <ol>
	 * <li>int that describes data type ({@link Frame#DROP_STRING} or {@link Frame#DROP_FILE})</li>
	 * <li>Object instance that is the dropped data (needs casting to String or File)</li>
	 * <li>Control instance - the element that the data has been dropped on</li>
	 * </ol>
	 * 
	 * @param methodName method name
	 * @param target     object
	 */
	public void addExternalDropListener(String methodName, Object target) {
		if (awtFrame == null)
			System.err.println("In P2D/P3D mode external drop is not yet supported.");
		externalDropListener = createEventListener(methodName, target, int.class, Object.class, Control.class);
	}

	public void addExternalDropListener(String methodName) {
		addExternalDropListener(methodName, getPApplet());
	}

	public void removeExternalDropListener() {
		externalDropListener = null;
	}





	/*
	 * Mouse event unique to the Frame class. 
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
			if (notDragging) { // treat event as move and not drag
				mouseEvent(mousex, mousey);
			} else {
				if (draggedElement != null) {
					stopPropagation(); // not even necessary, we dont call mouseEvent()
					hoveredElement = draggedElement;
					draggedElement.drag(e);
					draggedElement.handleEvent(draggedElement.dragListener, e);
				}
			}
			break;
		case MouseEvent.RELEASE:
			notDragging = false; // reset this in case it has been set to true during a drag

			if (draggedElement != null) {

				// release after drag: Only draggedElement should receive this event.
				stopPropagation(); // not even necessary, we dont call mouseEvent()

				draggedElement.release(e);
				draggedElement.dragRelease(e);
				draggedElement.pPressed = false;
				draggedElement.handleEvent(draggedElement.releaseListener, e);

				// now we need to check if mouse is still over the element
				ArrayList<Control> trace = traceAbsoluteCoordinates(mousex, mousey);

				if (trace.size() > 0) {
					if (trace.get(0) == draggedElement) {
						hoveredElement = draggedElement; // hoveredElement not set, because not calling mouseEvent
					} else {
						if (Control.drop) {
							handleEvent(dropElementListener, draggedElement, trace.get(0));
						}
						drop = true;
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
			if (hoveredElement.cursor != currentCursor) {
				currentCursor = hoveredElement.cursor;
				papplet.cursor(currentCursor);
			}

			if (!hoveredElement.pHovered) {
				hoveredElement.pHovered = true;
				hoveredElement.enter(e);
				hoveredElement.handleEvent(hoveredElement.enterListener, e);
			}
		}

		// reset stopPropagation for next frame (after handling mouseEvent and not
		// before, in case render mode is EFFICIENT)
		resetPropagationState();
	}

	private int currentCursor = PApplet.ARROW;


	@Override
	public int getOffsetXToWindow() {
		return offsetX;
	}

	@Override
	public int getOffsetYToWindow() {
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












	public static final int DROP_STRING = 0;
	public static final int DROP_FILE = 1;

	protected class Guiset_Drop_Handler implements java.awt.dnd.DropTargetListener {
		@Override
		public void dragEnter(DropTargetDragEvent event) {
		}

		@Override
		public void dropActionChanged(DropTargetDragEvent event) {
		}

		@Override
		public void dragOver(DropTargetDragEvent dtde) {
		}

		@Override
		public void dragExit(DropTargetEvent dte) {
		}

		void handleDrop(DropTargetDropEvent e, int type, Object o) {
			// check what element is currently hovered over
			ArrayList<Control> trace = traceAbsoluteCoordinates(e.getLocation().x, e.getLocation().y);
			handleEvent(externalDropListener, type, o, trace.size() > 0 ? trace.get(0) : null);
		}

		@Override
		public void drop(DropTargetDropEvent e) {
			try {
				e.acceptDrop(DnDConstants.ACTION_MOVE);
				Transferable transferable = e.getTransferable();
				DataFlavor[] flavors = transferable.getTransferDataFlavors();

				for (int i = 0; i < flavors.length; i++) {
					DataFlavor d = flavors[i];
					Object o = transferable.getTransferData(d);
					if (d.equals(DataFlavor.javaFileListFlavor)) {

						@SuppressWarnings("unchecked")
						java.util.List<File> fileList = (java.util.List<File>) o;

						Iterator<File> it = fileList.iterator();
						while (it.hasNext()) {
							File f = it.next();
							handleDrop(e, DROP_FILE, f);
						}
					}
					if (d.equals(DataFlavor.stringFlavor)) {
						String s = (String) transferable.getTransferData(d);
						handleDrop(e, DROP_STRING, s);
					}
				}
				e.dropComplete(true);

			} catch (Throwable t) {
				System.err.println("Data transfer error: " + t.getMessage());
				t.printStackTrace();
				e.dropComplete(false);
			}
		}
	}

}