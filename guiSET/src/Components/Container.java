package pGUI.core; //<>// //<>// //<>// //<>// //<>//

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
 * The Frame object is a container itself and the always root of the created tree. It initiates a lot of the processes 
 * which then go through all containers recursively to reach every integrated element that is part of the tree structure. 
 * 
 * The most important recursions are:
 * 		- the render()-method: 
 * 				which calls the element to draw the looks on its PGraphics object
 * 				containers invoke this method on all children and then project all PGraphics on themselves
 * 		- the calcBounds()-method: 
 * 				at the beginning of each frame it is important for all objects to know their positions and size (relative to window origin) 
 * 				so they can react to mouse events. This is done via this method. It is initiated by the Frame after the rendering has been done. 
 * 		- the mouseEvent():
 * 				when the frame receives a mouse event from the sketch, is passes it to all children and so on...
 * 				If no element interrupts the event propagation by calling frame0.stopPropagation() every element will get this event which is especially
 * 				useful for dragging events. 
 * 		- the resize() function:
 * 				When the size (width or height) of a control has been changed, children can react to that if they have anchors enabled. In this way it's possible to 
 * 				align controls at the right of a container or make the resize too. 
 * 		- the initialize() method:
 * 				This function isn't really being used at the moment. It is planned that the Frame calls each element to intialize when the draw() method of the sketch is running for the first time
 * 
 * 
 * The container has a useful feature - containerRenderItem(). This method does all the stuff needed to check if looks of children have to be refreshed, if opacity is changing etc...
 * Other classes inheriting from Container should use this method if they implement a new render() method. 
 * 
 * Panel containers (containers that pay respect to the position aspired by their children without setting them to new locations (autolayout)) also need sorting the content by z coordinate!
 * This is already done and distinguished under the hood but it is necessary for each inheriting class that uses an automatic layout to change the autoLayout property to true!
 * 
 * 
 * 
 */


/**
 * Base Class for all Containers. Containers group Components and can be nested
 * to create complex graphics structures.
 * 
 * @author Mc-Zen
 *
 */
public class Container extends Control {

	/*
	 * list of items
	 */
	protected ArrayList<Control> content;
	protected boolean autoSize = false;


	/*
	 * If a child of this class applies auto layout (overrides coordinates of items)
	 * then set this to true.
	 */
	protected boolean autoLayout = false;


	/**
	 * Default Constructor sets width and height to 100.
	 * 
	 * @param width
	 * @param height
	 */
	public Container() {
		this(100, 100);
	}


	public Container(int width, int height) {
		this.width = width;
		this.height = height;
		cType = CONTAINER;

		activateInternalMouseListener();

		content = new ArrayList<Control>();
	}




	@Override
	protected void initialize() {
		super.initialize();

		for (Control c : content) {
			c.initialize();
		}
	}








	/*
	 * ANCHORS / AUTOMATIC RESIZING
	 *
	 * When resized also call resize for all children
	 */

	@Override
	protected void resize() {
		super.resize();
		for (Control c : content)
			c.resize();
	}








	/*
	 * DRAWING AND RENDERING
	 */

	@Override
	protected void calcBounds() {
		for (Control c : content) {
			if (c.visible) {
				Frame.calcBoundsCount++;

				// set bounds of child so it has absolute values for listener processing
				c.bounds.X0 = Math.max(this.bounds.X0 + c.x, this.bounds.X0);
				c.bounds.Y0 = Math.max(this.bounds.Y0 + c.y, this.bounds.Y0);
				// crop overflow
				c.bounds.X = Math.min(this.bounds.X0 + c.x + c.width, this.bounds.X);
				c.bounds.Y = Math.min(this.bounds.Y0 + c.y + c.height, this.bounds.Y);

				if (c.cType == CONTAINER) {
					c.calcBounds();
				}
			}
		}
	}

	@Override
	protected void render() {
		// if AutoSize is on, first get minimal dimensions
		if (autoSize) {
			setAutoSize();
		}
		drawDefaultBackground();

		for (int i = 0; i < content.size(); i++) {
			Control c = content.get(i);
			if (c.visible) {
				containerRenderItem(c, c.x, c.y);

			}
		}
	}

	protected final void containerRenderItem(Control item, int x, int y) {
		if (item.changedVisuals) {
			item.changedVisuals = false; // do this before render, so it can use animations by calling update again

			item.preRender();
			item.render();
			item.pg.endDraw();

			Frame.renderCount++;
			Frame.renderedObjects += item.getClass();
		}

		// apply opacity (tinting carries out quite a lot of calculations in PGraphics,
		// while noTint() doesn't)
		if (item.opacity < 1) {
			pg.tint(255, (int) (item.opacity * 256));
		} else {
			pg.tint = false;
		}

		if (item.opacity > 0)
			pg.image(item.getGraphics(), x, y);
	}








	/*
	 * Content Operations
	 */

	// internal adding method
	protected void addItem(int position, Control c) {
		content.add(position, c);
		c.parent = this;
		update();
	}

	/**
	 * Add Components in given order to Container. Containers that don't layout
	 * their items automatically like i.e. {@link VFlowContainer} or
	 * {@link VScrollContainer} sort them by z-index!
	 * 
	 * @param controls pass arbitrary number of Components.
	 */
	public void add(Control... controls) {
		for (Control c : controls) {
			addItem(content.size(), c);
		}
		if (!autoLayout) { // don't sort auto-layout containers!
			sortContent();
		}
	}

	/**
	 * Insert Components at given index position.
	 * 
	 * @param position index in item list.s
	 * @param controls pass arbitrary number of Components.
	 */
	public void insert(int position, Control... controls) {
		for (int i = 0; i < controls.length; i++) {
			addItem(position + i, controls[i]);
		}
		if (!autoLayout) { // don't sort auto-layout containers!
			sortContent();
		}
	}

	/**
	 * Remove all items.
	 */
	public void clear() {
		content.clear();
		update();
	}

	/**
	 * Remove item at position in item list.
	 * 
	 * @param index position
	 */
	public void remove(int index) {
		content.remove(index);
		update();
	}

	/**
	 * Remove a specific Component from item list.
	 * 
	 * @param c
	 */
	public void remove(Control c) {
		content.remove(c);
		update();
	}

	/**
	 * Retrieve all items.
	 * 
	 * @return array of items
	 */
	public Control[] getItems() {
		Control[] c = new Control[content.size()];
		for (int i = 0; i < content.size(); i++) {
			c[i] = content.get(i);
		}
		return c;
	}

	// sort Content by z-Index
	protected void sortContent() {
		Collections.sort(content, new Comparator<Control>() {
			@Override
			public int compare(Control c1, Control c2) {
				return c1.z - c2.z;
			}
		});
	}

	/**
	 * Resize container once so it fits its content
	 */
	public void setAutoSize() {
		int mWidth = 1;
		int mHeight = 1;
		for (int i = 0; i < content.size(); i++) {
			Control c = content.get(i);
			if (c.visible) {
				if (c.x + c.width > mWidth) {
					mWidth = c.x + c.width;
				}
				if (c.y + c.height > mHeight) {
					mHeight = c.y + c.height;
				}
			}
		}
		width = mWidth;
		height = mHeight;
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





	/*
	 * EVENTS
	 */

	@Override
	protected void mouseEvent(MouseEvent e) {
		if (visible) {

			// reverse iteration direction (as to drawing) so topmost elements will
			// get the chance to stop the event propagation for objects below
			for (int i = content.size() - 1; i >= 0; i--) {

				// don't allow further listening when event propagation has been stopped
				if (Frame.isPropagationStopped())
					return;

				if (content.get(i).activatedInternalMouseListener) {
					content.get(i).mouseEvent(e);
				}

				// it's possible that contents have changed meanwhile
				i = Math.min(i, content.size());
			}
			if (!Frame.isPropagationStopped()) {
				super.mouseEvent(e); // execute mouseEvent for this object afterwards
			}
		}
	}
}