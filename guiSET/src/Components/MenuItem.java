package pGUI.core;

import processing.core.PApplet;

import processing.event.*;
import pGUI.classes.*;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


/*
 * Basic brick for creating menus. Just take a HFlowContainer, make its width
 * fill out the window and height = 25 and add MenuItems to it.
 * 
 * You can add other MenuItems to them and so on to create any structure of menu
 * items. Also take a look at the MenuSeparator which provides a non-clickable
 * and slim version for separating parts of the menu strip.
 * 
 * There is only this one class for every position in the structure tree (read
 * more about this in the comment about the type parameter).
 * 
 * 
 * The MenuItems aren't displayed and rendered at the position of the hierachy
 * they are added to. Instead they create a ToolStrip (called "dropdown") Object
 * (if they have children/subitems at all) which then is added to the frame
 * directly. This is necessary to be able to display it at any place. By default
 * the ToolStrips are invisible and are only set to visible when opening a strip
 * by clicking on it. When removing all subitems from an item the ToolStrip will
 * be removed too.
 * 
 * MenuItems and ToolStrips can also be used to create menus that pop up when i.e. 
 * right-clicking on something. 
 * 
 */


public class MenuItem extends Control {


	/*
	 * sub items (a menuitem is not really the parent of its subitems. Instead it
	 * references a Toolstrip that is placed in the Frame with high z-index and that
	 * is the real parent of all subitems.
	 */
	protected ArrayList<Control> items;

	protected ToolStrip dropDown;

	/*
	 * There are two types of menuitems: one is the "menu header" which is always
	 * visible on the screen and the parent of the entire strip it belongs to. The
	 * other on e is the "menu item", the basic items/subitems that make up the
	 * structure of the strip.
	 * 
	 * Both have different drawing and handling of special events etc... MenuItems
	 * should not change their position at runtime because the type change won't be
	 * recognized.
	 * 
	 * @param type gives the possibility to discern between the two versions The
	 * type is only determined at runtime when the graphics have been initialized
	 * and the position of the item is obvious.
	 * 
	 */

	protected int type = 0;
	protected static final int MENU_HEADER = 1;
	protected static final int MENU_ITEM = 2;

	/*
	 * Header that the menuitem belongs to. items and subitems etc. have the same
	 * headerStrip when belonging to the same strip at all.
	 */
	protected MenuItem headerStrip;

	/*
	 * subitems of this item are visible
	 */
	protected boolean open = false;
	protected boolean init = false;

	/*
	 * Shortcut to display. When setting shortcut it does not necessarily do
	 * anything at all except showing it on the MenuItem as the shortcut is not
	 * automatically registered at Frame. This has to be done manually.
	 */
	protected Shortcut shortcut;

	/*
	 * We keep a list of all header-style MenuItems to deal with some effects like
	 * closing entire strips when another one ie opened - or opening on hover when
	 * any header is already open (no clicking needed).
	 */
	private static MenuItem headers[] = {};

	protected boolean checked = false;



	public MenuItem() {
		this("");
	}

	public MenuItem(String text) {
		super();
		height = 23;
		setPadding(0, 6, 10, 6);
		setText(text);
		fontSize = 13;

		backgroundColor = 0;
		visualBackgroundColor = 0;
		hoverColor = 1342177280; 		// just darken menucontainer backgroundcolor a bit
		pressedColor = 1677721600; 		// only used by menu headers


		items = new ArrayList<Control>();
		setupListeners(1); // 1 additional listener (itemSelectedListener)

		activateInternalMouseListener();
	}


	public MenuItem(String text, String method) {
		this(text);

		// specified method will be executed on mose release
		addMouseListener("release", method);
	}











	@Override
	protected void render() {
		if (!init) {
			init();
		}

		// change color when active
		if (open) {
			if (type == MENU_HEADER)
				visualBackgroundColor = pressedColor;
			else
				visualBackgroundColor = hoverColor;
		}


		if (type == MENU_ITEM)
			textAlign = PApplet.LEFT;
		else if (type == MENU_HEADER)
			textAlign = PApplet.CENTER;

		/*
		 * grey out if disabled
		 */
		int temp = foregroundColor;
		if (!enabled)
			foregroundColor = Color.create(120);

		drawDefaultBackground();
		drawDefaultText();


		/*
		 * draw triangle to indicate that this item has subitems
		 */

		if (items.size() > 0 && type == MENU_ITEM) {
			pg.fill(enabled ? 0 : 150);
			pg.stroke(0);
			pg.strokeWeight(0);
			pg.triangle(width - 4, height / 2, width - 7, height / 2 + 3, width - 7, height / 2 - 3);
		}


		/*
		 * Draw shortcut if specified
		 */

		if (shortcut != null && type == MENU_ITEM) {
			String textBKP = text;
			textAlign = PApplet.RIGHT; // temporary RIGHT (no need to reset)
			text = shortcut.toString() + " ";
			drawDefaultText();
			text = textBKP;
		}

		/*
		 * draw checkmark
		 */
		if (checked) {
			pg.fill(180, 180, 250, 130);
			pg.stroke(60, 60, 100, 150);
			pg.rect(2, 3, 18, 18, 2); 	// box

			pg.strokeWeight(2);
			pg.line(8, 13, 10, 16);		// checkmark
			pg.line(10, 16, 15, 8);
		}

		foregroundColor = temp;
	}




	/*
	 * Menu items need to be initialized once by analyzing if they are a child or
	 * header of a menustrip and make appropriate adjustments
	 */

	protected void init() {

		/*
		 * preferrably add dropdown here, so the order of items to subitems is honored
		 * (Strips from different layers overlap a bit).
		 * 
		 * When adding items at runtime the adding method takes care of it.
		 */


		if (items.size() > 0) {
			Frame.frame0.add(dropDown);
		}


		/*
		 * if the parent is a toolstrip then this must be a MENU_ITEM, else it will be a
		 * header. This changes some of the visual attributes.
		 */

		try {
			@SuppressWarnings("unused")
			ToolStrip pa = ((ToolStrip) parent);

			type = MENU_ITEM;

			hoverColor = 671088660;
			paddingLeft = 27;

		} catch (ClassCastException cce) {

			type = MENU_HEADER;

			setHeader(this);

			/*
			 * add this item to headers array
			 */
			MenuItem headersTemp[] = new MenuItem[headers.length + 1];
			for (int i = 0; i < headers.length; i++) {
				headersTemp[i] = headers[i];
			}
			headersTemp[headers.length] = this;
			headers = headersTemp;
		}

		init = true;
	}




	@Override
	protected void autosize() {			
		float shortcutWidth = (shortcut != null ? textWidth(shortcut.toString()) + 30 : 0);
		int w = (int) (textWidth(text) + paddingLeft + paddingRight + shortcutWidth);

		/*
		 * if subitem then only require this as minimal width; as header it's the actual
		 * width
		 */
		if (type == MENU_ITEM)
			minWidth = w + 27; // 27 is the left padding

		else if (type == MENU_HEADER)
			width = w;

		else {					// undefined state (before this item has fully been initialized)
			minWidth = w + 27; // 27 is the left padding
			width = w;
		}
	}



	/*
	 * Setting header recursively (also for the subitems etc). Method is called at
	 * initializing and always when new item added
	 */

	protected void setHeader(MenuItem header) {
		this.headerStrip = header;
		for (int i = 0; i < items.size(); i++) {
			((MenuItem) items.get(i)).setHeader(header);
		}
	}




	/*
	 * open this strip properly if it has subitems, else select item
	 */

	protected void open() {
		if (items.size() > 0) { // has subitems itself -> open them

			// first close all potentially open siblings
			try {
				for (int i = 0; i < ((ToolStrip) parent).content.size(); i++)
					((MenuItem) ((ToolStrip) parent).content.get(i)).close();
			} catch (ClassCastException cce) {
				// ignore casting errors
			}

			open = true;


			/*
			 * Draw first layer items BENEATH this item and all other layers always NEXT to
			 * this item
			 */

			if (type == MENU_HEADER) {
				dropDown.x = this.bounds.X0;
				dropDown.y = this.bounds.Y;
				// reset timer (when closing the strip the timer is always ceased)
				timer = new Timer();
			} else {
				dropDown.x = this.bounds.X - 10;
				dropDown.y = this.bounds.Y0;
			}

			// make toolstrip (dropdown) visible
			dropDown.show();


		} else { // has no subitems -> close everything

			// mark it open at first, so it will be closed properly
			open = true;

			// notify header that an item has been selected, header will start the closing

			if (headerStrip != null)
				headerStrip.itemSelected(this);

		}
	}





	/*
	 * close recursively all items from (first layer menu items (header)) to
	 * subitems
	 */

	public void close() {
		if (open) {
			visualBackgroundColor = backgroundColor;

			open = false;

			// close sub items
			for (int i = 0; i < items.size(); i++) {
				((MenuItem) items.get(i)).close();
			}

			if (dropDown != null)
				dropDown.hide();

			update();

			// headers need to stop the timer
			if (type == MENU_HEADER) {
				if (tt != null) {
					tt.cancel();
					tt = null;

					// cease timer completely
					timer.cancel();
					timer.purge();
				}
			}
		}
	}



	/*
	 * set the displayed shortcut (shortcut has no real effect unless set manually
	 * at Frame)
	 */

	public void setShortcut(Shortcut s) {
		this.shortcut = s;
		autosize();
	}




	/*
	 * Only for header items. When an item is selected by clicking then it calls
	 * this method for its header. The header then closes up the entire strip.
	 */
	protected void itemSelected(Control c) {
		close();
		handleRegisteredEventMethod(ITEM_SELECTED_EVENT, c);
	}


	protected static final int ITEM_SELECTED_EVENT = Frame.numberMouseListeners;

	public void addItemSelectedListener(String methodName, Object target) {
		registerEventRMethod(ITEM_SELECTED_EVENT, methodName, target, MenuItem.class);
	}

	public void addItemSelectedListener(String methodName) {
		addItemSelectedListener(methodName, Frame.frame0.papplet);
	}

	public void removeItemSelectedListener() {
		deregisterEventRMethod(ITEM_SELECTED_EVENT);
	}


	/*
	 * Timer for allowing automatic opening of sub-strips when hovering .4s over an
	 * item that has subitems. We only need one timer and one task, as it is only
	 * possible to hover over one menu item at a time. When entering another item,
	 * the task is canceled and set new.
	 * 
	 * When closing the entire strip the timer is discarded and recreated when the
	 * strip is opened again.
	 */
	private static Timer timer = new Timer();
	private static TimerTaskk tt;

	/*
	 * Special TimerTask version that keeps track of the item that has summoned the
	 * timer
	 */
	private static class TimerTaskk extends TimerTask {
		MenuItem control;

		public TimerTaskk(MenuItem control) {
			super();
			this.control = control;
		}

		@Override
		public void run() {
			if (control.pHovered && !control.open) {
				if (control.items.size() > 0)
					control.open();
				else {
					// close all siblings
					for (Control i : ((ToolStrip) control.parent).content) {
						((MenuItem) i).close();
					}
				}
			}
		}
	}

	/*
	 * When hovering over a menu item the substrip - if existent - is shown.
	 * Therefore we wait for 0.4s and open the substrip and if there's none we close
	 * the substrips of all siblings.
	 */

	@Override
	protected void enter(MouseEvent e) { // when clicked hovering is sufficient for changing the dropdown
		visualBackgroundColor = hoverColor;

		if (type == MENU_ITEM) {

			// cancel task when having left another item in under 0.4s
			if (tt != null)
				tt.cancel();

			// create task new
			tt = new TimerTaskk(this);
			timer.schedule(tt, 400);

		} else if (type == MENU_HEADER) {
			// check if another header is open, if so then close it and open this one
			// immediately (doesn't apply if this header is the open one)

			if (!open) {
				for (int i = 0; i < headers.length; i++) {
					if (headers[i].open) {
						headers[i].close();
						open();
						break;
					}
				}
			}
		}
		update();
	}


	@Override
	protected void exit(MouseEvent e) {
		visualBackgroundColor = backgroundColor;
		update();
	}


	@Override
	protected void press(MouseEvent e) {

		// if item is menu header open and close on press
		if (type == MENU_HEADER) {
			if (open) {
				close();
				visualBackgroundColor = hoverColor;
			} else {
				open();
			}
			update();
		}

		Frame.stopPropagation();
	}

	@Override
	protected void release(MouseEvent e) {

		// if item is subitem open and close on release
		if (type == MENU_ITEM) {
			open();
		}

		update();
		Frame.stopPropagation();
	}


	@Override
	protected void mouseEvent(MouseEvent e) {
		super.mouseEvent(e);

		// make the entire strip disappear when clicked elsewhere
		if (!Frame.isPropagationStopped()) {
			if (e.getAction() == MouseEvent.PRESS && type == MENU_HEADER) {
				close();
			}
		}
	}









	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
		update();
	}




	/*
	 * Content Operations
	 */


	// internal adding method

	protected void addItem(int position, Control c) {
		items.add(position, c);

		// need to create the toolstrip if not already existent
		if (dropDown == null) {
			dropDown = new ToolStrip();
			// sync DropDown content with items
			dropDown.content = items;


			/*
			 * when adding items at runtime we need to add this new strip to Frame but when
			 * first creating the gui the dropdowns are added in the init() function to
			 * preserve the order (in z-indices).
			 */

			if (init)
				Frame.frame0.add(dropDown);
		}

		// !!!parent has to be the dropdown because dropdown is the real parent when
		// drawing
		c.parent = dropDown;

		update();
		((MenuItem) c).setHeader(this.headerStrip);
	}


	public void add(Control... controls) {
		for (Control c : controls) {
			addItem(items.size(), c);
		}
		update();
	}


	public void add(String... strings) {
		for (String s : strings) {
			addItem(items.size(), new MenuItem(s));
		}
		update();
	}


	public void insert(int position, Control... controls) {
		for (int i = 0; i < controls.length; i++) {
			addItem(position + i, controls[i]);
		}
	}


	public void clear() {
		items.clear();
		// remove dropdown
		Frame.frame0.remove(dropDown);
		dropDown = null;

		update();
	}


	public void remove(int index) {
		remove(items.get(index));
	}


	public void remove(Control c) {
		items.remove(c);

		// if dropdown empty, remove it
		if (items.size() == 0) {
			Frame.frame0.remove(dropDown);
			dropDown = null;
		}
		update();
	}


	public Control[] getItems() {
		Control c[] = new Control[items.size()];
		for (int i = 0; i < items.size(); i++) {
			c[i] = items.get(i);
		}
		return c;
	}


	public Control getItem(int index) {
		if (index >= 0 && index < items.size()) {
			return items.get(index);
		} else {
			return null;
		}
	}
}

