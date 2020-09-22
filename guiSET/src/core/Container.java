package guiSET.core; //<>// //<>// //<>// //<>// //<>//

import processing.event.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;




/*
 * Life's nothing without a basic panel container. 
 * 
 * Other controls can be added to the container and are displayed only within its bounds. 
 * The items/children receive events such as mouse events or resize events from their parent container. 
 * Also do containers draw their items looks onto themselves so that without any container nothing would be ever visible.
 * 
 * With containers it is possibly to give the gui a nested structure.    
 * The Frame object is a container itself and always the root of the created tree. It initiates a lot of the processes 
 * which then go through all containers recursively to reach every integrated element that is part of the tree structure. 
 * 
 * The most important recursions are:
 * 		- the render()-method: 
 * 				which calls the element to draw the looks on its PGraphics object.
 * 				Containers invoke this method on all children and then project all PGraphics on themselves. No items should be added to item list here. 
 * 		- the mouseEvent():
 * 				When Frame receives a mouse event from the sketch, is passes it to all children and so on until one element calls stopPropagation()
 * 		- the resize() function:
 * 				When the size (width or height) of a control has been changed, children can react to that if they have anchors enabled. In this way it's possible to 
 * 				align controls at the right of a container or make the resize too. Child items are notified by calling their resize() method
 * 		- the initialize() method:
 * 				Frame calls each element to initialize (recursively) when the draw() method of the sketch is running for the first time. Here new items may be added. 
 * 
 * The container has a useful feature - containerRenderItem(Control item, int x, int y). This method does all the stuff needed to check if looks of children have to 
 * be refreshed, if opacity has changed, local positioning for mouse events etc...
 * Other classes inheriting from Container should use this method to draw their children if they implement a new render() method. 
 * 
 * Panel containers (containers that pay respect to the position aspired by their children without setting them to new locations (containerMakesAutoLayout)) also need sorting the content by z coordinate!
 * This is already done and distinguished under the hood but it is necessary for each inheriting class that uses an automatic layout to change the containerMakesAutoLayout property to true!
 * 
 * 
 * 
 */


/**
 * Base class for all containers. Containers group items and can be nested to create complex
 * graphics structures.
 * 
 * @author Mc-Zen
 *
 */
public class Container extends TextBased {

	/*
	 * List of items
	 */
	protected ArrayList<Control> items;




	// For containers with scroll bars
	public static int SCROLL_HANDLE_BORDER_RADIUS = 0;
	public static int SCROLL_BAR_COLOR = Color.create(150);
	public static int SCROLL_HANDLE_COLOR = Color.create(170);
	public static int SCROLL_HANDLE_PRESSED_COLOR = Color.create(190);
	public static int SCROLL_HANDLE_BORDER_COLOR = Color.create(90);

	/**
	 * Default constructor sets width and height to 100
	 */
	public Container() {
		this(Constants.DefaultContainerWidth, Constants.DefaultContainerHeight);
	}


	public Container(int width, int height) {
		setSizeImpl(width, height);


		// It seems that ArrayList has a default capacity of 10 at first. This might be
		// wasteful if a lot of containers are used. Lets start with 1.
		items = new ArrayList<Control>(1);
	}




	@Override
	protected void initialize() {
		super.initialize();

		// no iterator here, as list might be modified
		// still a call to setZ() will modify the list and then it will break;
		for (int i = 0; i < items.size(); i++) {
			Control c = items.get(i);
			c.initialize();
		}
	}








	/*
	 * ANCHORS / AUTOMATIC RESIZING
	 *
	 * When resized also call resize for all children
	 */


	@Override
	protected boolean setWidthNoUpdate(int width) {
		boolean widthActuallyChanged = super.setWidthNoUpdate(width);
		if (widthActuallyChanged) {
			for (Control c : items) {
				c.parentResized();
			}
		}
		return widthActuallyChanged;
	}

	@Override
	protected boolean setHeightNoUpdate(int width) {
		boolean heightActuallyChanged = super.setHeightNoUpdate(width);
		if (heightActuallyChanged) {
			for (Control c : items) {
				c.parentResized();
			}
		}
		return heightActuallyChanged;
	}



	/*
	 * DRAWING AND RENDERING
	 */

	// content list should not be changed in items render() while iterating
	@Override
	protected void render() {
		drawDefaultBackground();

		for (Control c : items) {
			if (c.visible) {
				renderItem(c, c.x, c.y);
			}
		}
		drawDefaultDisabled();
	}





	/*
	 * Content Operations
	 */

	// internal item adding method
	protected void insertImpl(int position, Control item) {
		items.add(position, item);
		item.parent = this;
		item.addedToParent(); // notify control that it has been added to this parent
		// update(); // called once by public add/insert
	}


	/**
	 * Add items to Container. Containers that don't layout their items automatically like i.e.
	 * {@link VFlowContainer} or {@link VScrollContainer} sort them by z-index!
	 * 
	 * @param items arbitrary number of items.
	 */
	public void add(Control... items) {
		for (Control c : items) {
			insertImpl(this.items.size(), c);
		}
		if (needsSortingByZ()) { // don't sort layouting containers!
			sortItemsbyZ();
		}
		update();
	}

	/**
	 * Insert items at given index position.
	 * 
	 * @param position index to insert items into list.
	 * @param items arbitrary number of items.
	 */
	public void insert(int position, Control... items) {
		for (int i = 0; i < items.length; i++) {
			insertImpl(position + i, items[i]);
		}
		if (needsSortingByZ()) { // don't sort layouting containers!
			sortItemsbyZ();
		}
		update();
	}

	/**
	 * Remove all items.
	 */
	public void clear() {
		items.clear();

		// not really necessary. But imagine clearing a huge list and now we have just a
		// lot of null pointers
		items.trimToSize();
		update();
	}

	/**
	 * Remove item at position in item list. Throws error if index is bad.
	 * 
	 * @param index position
	 */
	public void remove(int index) {
		items.remove(index);
		update();
	}

	/**
	 * Remove a specific item from item list.
	 * 
	 * @param item item to remove.
	 * @return true if the item has actually been removed
	 */
	public boolean remove(Control item) {
		boolean result = items.remove(item);
		if (result)
			update();
		return result;
	}

	/**
	 * Get list index of given item. Returns -1 if item is no child of this container.
	 * 
	 * @param item item to get index to
	 * @return index
	 */
	public int indexOf(Control item) {
		return items.indexOf(item);
	}

	/**
	 * Retrieve all items as Control array.
	 * 
	 * @return array of items
	 */
	public Control[] getItems() {
		Control[] c = new Control[items.size()];
		for (int i = 0; i < items.size(); i++) {
			c[i] = items.get(i);
		}
		return c;
	}

	/**
	 * Get item in item list at given index. Throws error if index exceeds list length.
	 * 
	 * @param index index of requested item
	 * @return item
	 */
	public Control get(int index) {
		return items.get(index);
	}

	/**
	 * Get number of items.
	 * 
	 * @return number of items
	 */
	public int getNumItems() {
		return items.size();
	}


	/**
	 * Sort items providing a Comparator.
	 * 
	 * @param comp Comparator
	 */
	public void sortItems(Comparator<Control> comp) {
		Collections.sort(items, comp);
	}


	protected void sortItemsbyZ() {
		sortItems(new Comparator<Control>() {
			@Override
			public int compare(Control c1, Control c2) {
				return c1.z - c2.z;
			}
		});
	}



	/**
	 * Resize container once so it fits its content.
	 */
	public void fitContent() {
		int w = Constants.MinimalMinWidth;
		int h = Constants.MinimalMinHeight;
		for (Control item : items) {
			if (item.isVisible()) {
				w = Math.max(w, item.getOffsetX() + item.getWidth());
				h = Math.max(h, item.getOffsetY() + item.getHeight());
			}
		}
		setSize(w, h);
	}


	/*
	 * If a subclass of container applies layouting (overrides x,y-coordinates of
	 * items) then return false in constructor. If content does not overlap, it is 
	 * not necessary to sort the contents by z-Index. This container needs it. 
	 */
	protected boolean needsSortingByZ() {
		return true;
	}









	/*
	 * EVENTS
	 */

	@Override
	protected void mouseEvent(int x, int y) {
		if (!visible || !enabled)
			return;

		if (relativeCoordsAreWithin(x, y)) {
			int x_ = x - offsetX;
			int y_ = y - offsetY;


			if (containerPreItemsMouseEvent(x_, y_)) { // allows container to peek into the event

				// reverse iteration direction (as to drawing) so topmost elements will
				// get the chance to stop the event propagation for objects below
				for (int i = items.size() - 1; i >= 0; i--) {

					// don't allow further listening when event propagation has been stopped
					if (isPropagationStopped()) {
						return;
					}

					items.get(i).mouseEvent(x_, y_);


					// it's possible that item list has changed meanwhile, but iterator not nice
					// here
					i = Math.min(i, items.size());
				}
			}


			if (!isPropagationStopped()) {
				containerPostItemsMouseEvent(x_, y_); // execute real mouseEvent for this object afterwards
			}
		}
	}


	/**
	 * Called by {@link #mouseEvent(int, int)} before dealing with the items. It enables the container
	 * to process the mouse event before the items do and if this returns false the items will not
	 * receive the event at all.
	 * 
	 * @param x y
	 * @param y y
	 * @return should items receive the current event
	 */
	protected boolean containerPreItemsMouseEvent(int x, int y) {
		return true;
	}


	/**
	 * Almost a copy of {@link Control#mouseEvent(int x, int y) } but not again checking for visible,
	 * enabled and relCoordsAreWithin.
	 * 
	 * Also this method can be overriden and accessed even by more deeply nested classes.
	 * 
	 * 
	 * @param x x
	 * @param y y
	 */
	protected void containerPostItemsMouseEvent(int x, int y) {
		if (hoveredElement == null)
			hoveredElement = this;

		switch (currentMouseEvent.getAction()) {
		case MouseEvent.MOVE: // most often - try first
			move(currentMouseEvent);
			handleEvent(moveListener, currentMouseEvent);
			break;
		case MouseEvent.PRESS:
			focus();
			draggedElement = this;
			stopPropagation();
			press(currentMouseEvent);
			handleEvent(pressListener, currentMouseEvent);
			break;
		case MouseEvent.WHEEL:
			mouseWheel(currentMouseEvent);
			handleEvent(wheelListener, currentMouseEvent);
			break;
		case MouseEvent.RELEASE:
			// Happens rarely here. Usually a release is preceded by a press and in between
			// only
			// drag events come. These and the final release event are handled by Frame.
			// Still this code might be executed i.e. if user presses two buttons at once.
			stopPropagation();
			release(currentMouseEvent);
			handleEvent(releaseListener, currentMouseEvent);
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

	/**
	 * Get a list of arbitrarly nested child elements that given coordinates go through, ordered by
	 * layer on the screen. This version takes relative coordinates of this object.
	 * 
	 * @param x x coordinate on this element
	 * @param y y coordinate on this element
	 * @return list of traces elements
	 */
	public ArrayList<Control> traceRelativeCoordinates(int x, int y) {
		coordinateTrace.clear();
		traceCoordsImpl(x, y);
		return coordinateTrace;
	}

	/**
	 * Get a list of arbitrarly nested child elements that given coordinates go through, ordered by
	 * layer on the screen. This version takes absolute window coordinates. Invisible and disabled
	 * elements are ignored.
	 * 
	 * @param x absolute x window coordinate
	 * @param y absolute y window coordinate
	 * @return list of traces elements
	 */
	public ArrayList<Control> traceAbsoluteCoordinates(int x, int y) {
		coordinateTrace.clear();
		traceCoordsImpl(x - getOffsetXWindow(), y - getOffsetYWindow());
		return coordinateTrace;
	}

	@Override
	protected void traceCoordsImpl(int relX, int relY) {
		if (visible && enabled && relativeCoordsAreWithin(relX, relY)) {
			for (int i = items.size() - 1; i >= 0; i--) {
				items.get(i).traceCoordsImpl(relX - offsetX, relY - offsetY);
			}
			coordinateTrace.add(this);
		}
	}
}