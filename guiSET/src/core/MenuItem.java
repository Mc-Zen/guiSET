package guiSET.core;


import processing.event.*;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;




/**
 * The MenuItems aren't displayed and rendered at the position of the hierachy
 * they are added to. Instead they create (if they have children/subitems at
 * all) a MenuStrip instance (called "dropDown") which then is added to an
 * instance of MenuSurface. This is necessary to be able to display it at any
 * location on the window. MenuSurface is created automatically once if it does
 * not exist yet and adds itself to Frame. Clicking on anywhere on the
 * MenuSurface which is not a MenuItem will close all open strips.
 * 
 * 
 * By default the MenuStrips are invisible and are only set to visible when
 * opening a strip by clicking on it. When removing all subitems from an item,
 * the MenuStrip will be removed too.
 */


/**
 * Sort of a singleton. It is created the first time a menu item is constructed. Its parent is the
 * Frame and itself the parent of all MenuStrips (menu "dropdowns"). By default, it is invisible and
 * only displayed if a MenuStrip is opened by clicking a MenuItem on the MenuBar or opening a
 * MenuStrip otherwise (i.e. through a custom right-click event).
 * 
 * 
 * @author Mc-Zen
 *
 */
class MenuSurface extends Container {
	protected static MenuSurface staticMS;

	private MenuSurface() {
		if (staticMS == null) {
			staticMS = this;
			getFrame().add(this);
			setVisible(false);
			setAnchors(Constants.LEFT, 0, Constants.RIGHT, 0, Constants.TOP, 0, Constants.BOTTOM, 0); // Fill entire Frame
			setZ(Constants.MenuSurfaceZIndex);
			setBackgroundColor(GuisetColor.TRANSPARENT);
		}
	}


	// Called by MenuItem constructor to notify MenuSurface that menus will be used.
	protected static void usingMenus() {
		if (staticMS == null)
			new MenuSurface();
	}


	protected static void addMenuStrip(MenuStrip t) {
		if (staticMS == null)
			new MenuSurface();

		staticMS.add(t);
	}

	protected static void openMenuSurface() {
		if (staticMS == null)
			return;
		staticMS.setVisible(true);
	}

	protected static void closeMenuSurface() {
		if (staticMS == null)
			return;
		staticMS.setVisible(false);
	}

	protected static void closeAllMenus() {
		MenuItem.closeOpenHeaders();
		closeMenuSurface();
	}


	// Definitely called after all MenuItems got the event.
	// Only called if propagation not yet stopped, which means, that no item has
	// been pressed.
	@Override
	protected void press(MouseEvent e) {
		closeAllMenus();

		// now that the menu has closed, allow pressing immediately with the same
		// click on other elements (even dragging works this way).
		resetPropagationState();
	}

}


/**
 * Basic brick for creating menus. Just add a new {@link MenuBar} to your {@link Frame} instance and
 * add MenuItems to it.
 * 
 * You can add other MenuItems to these and so on to create any structure of menu items. Also take a
 * look at the {@link MenuSeparator} which provides a non-clickable and slim line for separating
 * parts of the menu strip.
 * 
 * There is only this one class for every position in the menu tree.
 *
 * MenuItems and MenuStrips can also be used to create menus that pop up when i.e. right-clicking on
 * something. This is accomplished by creating a ToolStrip and adding items to it. The ToolStrip can
 * be be shown by calling myToolStrip.show().
 * 
 * 
 */


public class MenuItem extends TextBased {



	/*
	 * sub items (a menuitem is not really the parent of its subitems. Instead it
	 * references a MenuStrip that is placed in the Frame with high z-index and that
	 * is the real parent of all subitems.
	 * 
	 * It's easier to keep this list a Control-list and not a MenuItem-list because
	 * the MenuStrip extends Container which has a Control-list and the lists are
	 * synchronized.
	 * 
	 * MenuItem does not simply subclass container because the event handling of container 
	 * is not desired to be inherited. Also the item adding methods should only allow 
	 * MenuItems. 
	 */
	protected ArrayList<Control> items;

	protected MenuStrip dropDown;

	/*
	 * There are two types of menuitems: one is the MENU_HEADER which is always visible on the screen
	 * and the parent of the entire strip it belongs to. MENU_HEADERs are all part of the MenuBar.
	 * 
	 * The other type is the MENU_ITEM, the basic items/subitems that make up the structure of the
	 * strip.
	 * 
	 * Both have different drawing/positioning and handling of special events etc...
	 * 
	 * Menu items should not change their tree position at runtime because the type change won't be
	 * recognized. Or not? Maybe it would work now.
	 * 
	 * "type" gives the possibility to discern between the two versions intenally. The type is only
	 * determined at runtime when the position of the item in the menu hierachy is known.
	 * 
	 */

	protected Type type = Type.UNKNOWN;

	protected enum Type {
		UNKNOWN, MENU_HEADER, NESTED_MENU_ITEM
	}

	/*
	 * Header that the menuitem belongs to. items and subitems etc. have the same
	 * headerStrip when belonging to the same strip at all.
	 */
	protected MenuItem headerStrip;

	/*
	 * Are subitems of this item visible at the moment?
	 */
	protected boolean open = false;

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
	protected static ArrayList<MenuItem> headers = new ArrayList<MenuItem>();

	/*
	 * Display checkmark if true
	 */
	protected boolean checked = false;


	public MenuItem() {
		this("");
	}

	public MenuItem(String text) {
		super();
		items = new ArrayList<Control>(0);
		setHeightNoUpdate(Constants.MenuItemHeight);
		setPadding(0, 6, 0, 6);
		setText(text);

		setBackgroundColor(GuisetDefaultValues.menuItemBackgroundColor);
		setPressedColor(GuisetDefaultValues.menuItemPressColor); 		// only used by menu headers

		MenuSurface.usingMenus();
	}


	/**
	 * Constructor for immediately setting text and name of method to call when this item is selected.
	 * 
	 * @param text           text
	 * @param selectCallback method name of callback for select event
	 */
	public MenuItem(String text, String selectCallback) {
		this(text);
		setSelectListener(selectCallback);
	}

	/**
	 * Constructor for immediately setting text and a lambda function to call when this item is
	 * selected.
	 * 
	 * @param text           text
	 * @param selectCallback lambda callback for select event
	 */
	public MenuItem(String text, Predicate selectCallback) {
		this(text);
		setSelectListener(selectCallback);
	}

	/**
	 * Similar to {@link #MenuItem(String, String)}, but also provide a shortcut. The shortcut is
	 * displayed on the menu item and when this shortcut is hit on the keyboard, the callback is
	 * executed as if the menu item had been pressed.
	 * 
	 * @param text           text
	 * @param selectCallback method name of callback for select event
	 * @param shortcut       shortcut
	 */
	public MenuItem(String text, String selectCallback, Shortcut shortcut) {
		this(text, selectCallback);
		setShortcut(shortcut);
		getFrame().registerShortcut(shortcut, selectCallback);
	}

	/**
	 * Similar to {@link #MenuItem(String, Predicate)}, but also provide a shortcut. The shortcut is
	 * displayed on the menu item and when this shortcut is hit on the keyboard, the callback is
	 * executed as if the menu item had been pressed.
	 * 
	 * @param text           text
	 * @param selectCallback lambda callback for select event
	 * @param shortcut       shortcut
	 */
	public MenuItem(String text, Predicate selectCallback, Shortcut shortcut) {
		this(text, selectCallback);
		setShortcut(shortcut);
		getFrame().registerShortcut(shortcut, selectCallback);
	}

	/**
	 * If strong set to true, then the shortcut might also be fired when a textbox has focus.
	 * 
	 * @param text       text
	 * @param methodName method name
	 * @param shortcut   shortcut
	 * @param strong     strong property
	 */
	public MenuItem(String text, String methodName, Shortcut shortcut, boolean strong) {
		this(text, methodName);
		setShortcut(shortcut);
		getFrame().registerShortcut(shortcut, methodName, getPApplet(), strong);
		setSelectListener(methodName);
	}

	// Used by MenuParser
	protected void registerShortcutAndMethod(String methodName, Shortcut shortcut) {
		setShortcut(shortcut);
		getFrame().registerShortcut(shortcut, methodName);
		setSelectListener(methodName);
	}



	@Override
	protected void render() {
		// change color if open
		if (open) {
			if (type == Type.MENU_HEADER)
				visualBackgroundColor = pressedColor;
			else
				visualBackgroundColor = hoverColor;
		}

		/*
		 * gray out if disabled
		 */
		int temp = foregroundColor;
		if (!enabled)
			foregroundColor = GuisetColor.create(120);

		drawDefaultBackground();
		drawDefaultText();


		if (type == Type.NESTED_MENU_ITEM) {

			/*
			 * Draw triangle to indicate that this item has subitems
			 */
			if (items.size() > 0) {
				pg.fill(enabled ? GuisetGlobalValues.menuItemTriangleEnabledColor : GuisetGlobalValues.menuItemTriangleDisabledColor);
				pg.stroke(0);
				pg.strokeWeight(0);
				pg.triangle(getWidth() - 4, getHeight() / 2, getWidth() - 7, getHeight() / 2 + 3, getWidth() - 7, getHeight() / 2 - 3);
			}

			/*
			 * Draw shortcut if specified
			 */
			if (shortcut != null) {
				String textBKP = text;
				textRenderer.setTextAlign(Constants.RIGHT); // temporary RIGHT (no need to reset), no setter!! dont wanna call update always
				text = shortcut.toString() + " ";
				drawDefaultText();
				text = textBKP;
				textRenderer.setTextAlign(Constants.LEFT); // temporary RIGHT (no need to reset)
			}

			/*
			 * Draw checkmark
			 */
			if (checked) {
				pg.fill(GuisetGlobalValues.menuItemCheckmarkFillColor);
				pg.stroke(GuisetGlobalValues.menuItemCheckmarkStrokeColor);
				pg.rect(2, 3, 18, 18, 2); 	// box

				pg.strokeWeight(2);
				pg.line(8, 13, 10, 16);		// checkmark
				pg.line(10, 16, 15, 8);
			}
		}
		foregroundColor = temp;
	}




	/**
	 * When added to their parent, it is determined whether this item is a {@link Type#MENU_HEADER} or a
	 * {@link Type#NESTED_MENU_ITEM}. Some styles are set accordingly.
	 */

	@Override
	protected void addedToParent() {
		determineTypeAndSetup();
	}

	protected void determineTypeAndSetup() {
		if (parent instanceof MenuStrip) {
			type = Type.NESTED_MENU_ITEM;
			setStyleOfNestedMenuItem();


			// If this whole strip (that this item just has been added to) is already added
			// to a MenuBar or similar, we need to add the dropDown for this item separately
			if (isHeaderStripConnected()) {
				addMenuStrips(); // add all menustrips recursively (preserve right z-order)
			}
		} else {
			type = Type.MENU_HEADER;
			setStyleOfMenuHeader();

			// Set this as header recursively for all (sub...-) children. This is only
			// necessary for headers as it is already included in the add() method when
			// adding MenuItems.
			setHeader(this);
			addMenuStrips(); // add all menustrips recursively (preserve right z-order)

			// Add this item to static headers array
			headers.add(this);
		}
		autosize();
	}

	private void setStyleOfNestedMenuItem() {
		setTextAlign(Constants.LEFT);
		setHoverColor(GuisetDefaultValues.menuItemHoverColor);
		setPaddingLeft(Constants.MenuItemPaddingLeft);
		setPaddingRight(Constants.MenuItemPaddingRight);
	}

	private void setStyleOfMenuHeader() {
		setTextAlign(Constants.CENTER);
		setHoverColor(GuisetDefaultValues.menuHeaderHoverColor);
	}

	protected boolean isHeaderStripConnected() {
		return headerStrip != null && (headerStrip.parent != null || headerStrip instanceof ContextMenu);
	}


	protected void addMenuStrips() {
		if (dropDown != null) {
			if (dropDown.parent == null)
				MenuSurface.addMenuStrip(dropDown);
			for (Control c : items) {
				((MenuItem) c).addMenuStrips();
			}
		}
	}



	@Override
	protected int autoWidth() {
		float shortcutWidth = (shortcut != null ? textWidth(shortcut.toString()) + 30 : 0);
		int baseWidth = (int) (textWidth(text) + shortcutWidth);

		/*
		 * if subitem, then only require this as minimal width; as header it's the
		 * actual width
		 */
		if (type == Type.NESTED_MENU_ITEM) {
			setMinWidth(baseWidth + paddingLeft + paddingRight); // 27 is the left padding
			return (int) (baseWidth + shortcutWidth + paddingLeft + paddingRight);
		} else if (type == Type.MENU_HEADER) {
			return baseWidth + paddingLeft + paddingRight;
		} else {
			// undefined state (before this item has fully been initialized).
			// At least when called in addedToParent(), the type is defined.
		}
		return -1; // will be ignored
	}



	/*
	 * Setting header recursively (also for the subitems etc). Method is called at
	 * addedToParent and always when new item added
	 */
	protected void setHeader(MenuItem header) {
		this.headerStrip = header;
		for (Control c : items) {
			((MenuItem) c).setHeader(header);
		}
	}




	/*
	 * Internal method for opening this strip properly. Called by press event, long
	 * hover and hover over header if other header already open.
	 * 
	 * If it has no subitems, select item
	 */
	protected void open() {
		if (items.size() > 0) { // has subitems itself -> open them

			if (type == Type.MENU_HEADER) {
				closeOpenHeaders();
			} else {
				// first close all potentially open siblings
				try {
					for (Control c : ((MenuStrip) parent).items) {
						((MenuItem) c).close();
					}
				} catch (ClassCastException cce) {
					// ignore casting errors
				} catch (NullPointerException e) {
					//
				}
			}

			open = true;

			/*
			 * Draw first layer items BENEATH this item and all other layers always NEXT to
			 * this item
			 */
			if (type == Type.MENU_HEADER) {
				dropDown.x = offsetX;
				dropDown.y = Constants.MenuItemHeight;

				// reset timer (when closing the strip the timer is always ceased)
				hoverTimer = new Timer();
			} else {
				dropDown.x = getOffsetXToWindow() + getWidth() - 10;
				dropDown.y = getOffsetYToWindow() - MenuSurface.staticMS.offsetY;
			}



			// make dropdown (menustrip) visible
			dropDown.show();


		} else { // has no subitems -> close everything

			// mark it open at first, so it will be closed properly
			open = true;

			// notify header that an item has been selected, header will start the closing
			if (headerStrip != null) {
				headerStrip.childSelected(this);
			} else {

				// only used for free MenuStrip unbound to a menu
				((MenuStrip) parent).itemSelected(this);
			}
		}
	}






	/**
	 * Close this item/strip. Recursively closes all subitems.
	 */
	public void close() {
		if (open) {
			visualBackgroundColor = backgroundColor;

			open = false;

			// close sub items
			for (Control c : items) {
				((MenuItem) c).close();
			}

			if (dropDown != null) {
				dropDown.hide();
			}
			update();

			// headers need to stop the timer and close the surface
			if (type == Type.MENU_HEADER) {

				MenuSurface.closeMenuSurface();

				if (hoverTimerTask != null) {
					hoverTimerTask.cancel();
					hoverTimerTask = null;

					// cease timer completely
					hoverTimer.cancel();
					hoverTimer.purge();
				}
			}
		}
	}


	protected static void closeOpenHeaders() {
		for (MenuItem header : headers) {
			header.close();
		}
	}

	/**
	 * Set the displayed shortcut (shortcut has no real effect unless set manually at Frame).
	 * 
	 * @param shortcut shortcut
	 */
	public void setShortcut(Shortcut shortcut) {
		this.shortcut = shortcut;
		autosize();
	}

	public Shortcut getShortcut() {
		return shortcut;
	}


	/*
	 * Only for header items. When an item is selected by clicking then it calls
	 * this method for its header. The header then closes up the entire strip.
	 */
	protected void childSelected(MenuItem item) {
		close();
		if (item != this) { // not sure if this can even happen
			item.selected(item);
		}
		handleEvent(childSelectListener, item);
	}

	/*
	 * For all items. Header calls this after being selected
	 */
	protected void selected(MenuItem item) {
		handleEvent(selectListener, item);
	}









	/**
	 * MenuItems can be checked / unchecked with this method (little icon on the left of the MenuItem
	 * text).
	 * 
	 * @param checked checked
	 */
	public void setChecked(boolean checked) {
		this.checked = checked;
		update();
	}

	public boolean isChecked() {
		return checked;
	}

	/*
	 * There are two cases in which the dropdown should be added to the MenuSurface.
	 * 
	 *  1. A MenuItem with an existing dropdown is "connected" to the gui. "Connected" means
	 *  that there is . In this case, all child/grandchild... items need to add their dropdown 
	 *  (if existent) to the MenuSurface. 
	 *  2. An already connected MenuItem with no items (dropdown == null) gets a subitem. 
	 *  The dropdown is created and now it needs to be added to the MenuSurface. 
	 * 
	 */


	protected void createDropDownIfNecessary() {
		if (dropDown == null) {
			dropDown = new MenuStrip();
			dropDown.items = items; // sync dropDown items with items of this MenuItem

			if (isHeaderStripConnected())
				MenuSurface.addMenuStrip(dropDown);

			// The dropdowns parent will be the static MenuSurface staticMS.
			// a dropdown is added to staticMS when a menu-header is added to its container
			// (usually a MenuBar) by calling addMenuStrips().
			// This method recursively adds the MenuStrips for all (sub...-) items that have
			// one.
		}
	}



	/*
	 * Content Operations
	 */


	// internal adding method

	protected void insertImpl(int position, MenuItem item) {
		items.add(position, item); // Update called here
		createDropDownIfNecessary(); // Need to create the dropdown if not already existent

		// !!! Parent has to be the dropdown because dropdown is the real logic parent when drawing and
		// receiving input events
		item.parent = dropDown;
		item.setHeader(this.headerStrip); // setHeader before addedToParent() because the latter checks header
		item.addedToParent();
	}


	/**
	 * Add subitems for this item.
	 * 
	 * @param items newItems
	 */
	public void add(MenuItem... items) {
		for (MenuItem c : items) {
			insertImpl(this.items.size(), c);
		}
		dropDown.update(); // dont need to update this
	}


	/**
	 * Create and add subitems for each text String passed. Passing an empty String will create a
	 * {@link MenuSeparator}.
	 * 
	 * @param strings arbitrary number of text.
	 */
	public void add(String... strings) {
		for (String s : strings) {
			if (s.length() == 0) {
				insertImpl(items.size(), new MenuSeparator());
				continue;
			}
			insertImpl(items.size(), new MenuItem(s));
		}
		dropDown.update();// dont need to update this
	}


	/**
	 * Insert subitems at given position.
	 * 
	 * @param position position
	 * @param items    arbitrary number of items
	 */
	public void insert(int position, MenuItem... items) {
		for (int i = 0; i < items.length; i++) {
			insertImpl(position + i, items[i]);
		}
		dropDown.update();// dont need to update this
	}


	/**
	 * Remove all subitems.
	 */
	public void clear() {
		items.clear();
		// remove dropdown
		if (dropDown != null) {
			dropDown.update();// dont need to update this
			MenuSurface.staticMS.remove(dropDown);
			dropDown = null;
		}
	}


	/**
	 * Remove item at given index.
	 * 
	 * @param index index of item to remove
	 */
	public void remove(int index) {
		remove(items.get(index));
	}


	/**
	 * Remove item.
	 * 
	 * @param item item to remove
	 */
	public void remove(Control item) {
		((MenuItem) item).close();
		items.remove(item);

		if (dropDown != null) {
			dropDown.update();// dont need to update this
		}
		// if dropdown empty, remove it
		if (items.size() == 0) {
			if (dropDown != null)
				dropDown.hide();
			MenuSurface.staticMS.remove(dropDown);
			dropDown = null;

		}
	}


	/**
	 * Get sub items.
	 * 
	 * @return sub items
	 */
	public Control[] getItems() {
		MenuItem c[] = new MenuItem[items.size()];
		for (int i = 0; i < items.size(); i++) {
			c[i] = (MenuItem) items.get(i);
		}
		return c;
	}

	/**
	 * Get item at given index.
	 * 
	 * @param index index of item to get
	 * @return item
	 */
	public MenuItem getItem(int index) {
		if (index >= 0 && index < items.size()) {
			return (MenuItem) items.get(index);
		} else {
			return null;
		}
	}


	/**
	 * Get number of sub items.
	 * 
	 * @return number of sub items
	 */
	public int getNumItems() {
		return items.size();
	}




	/*
	 * Listeners
	 */


	protected EventListener selectListener;
	protected EventListener childSelectListener;

	/**
	 * Set a listener for the item selected event (called when this item has been selected).
	 * 
	 * Event arguments: this {@link #MenuItem()}
	 * 
	 * @param methodName method name
	 * @param target     target
	 */
	public void setSelectListener(String methodName, Object target) {
		selectListener = createEventListener(methodName, target, MenuItem.class);
	}

	public void setSelectListener(String methodName) {
		setSelectListener(methodName, getPApplet());
	}

	/**
	 * Set a listener lambda for when this item is selected.
	 * 
	 * Event arguments: this {@link #MenuItem()}
	 * 
	 * @param lambda lambda expression with {@link #MenuItem()} parameter
	 */
	public void setSelectListener(Predicate1<MenuItem> lambda) {
		selectListener = new LambdaEventListener1<MenuItem>(lambda);
	}

	/**
	 * Set a listener lambda for when this item is selected.
	 * 
	 * Event arguments: none
	 * 
	 * @param lambda lambda expression
	 */
	public void setSelectListener(Predicate lambda) {
		selectListener = new LambdaEventListener(lambda);
	}

	public void removeSelectListener() {
		selectListener = null;
	}



	/**
	 * Set a listener for when a child of this header has been selected.
	 * 
	 * Event arguments: the selected child {@link #MenuItem()}
	 * 
	 * @param methodName method name
	 * @param target     target
	 */
	public void setChildSelectListener(String methodName, Object target) {
		childSelectListener = createEventListener(methodName, target, MenuItem.class);
	}

	public void setChildSelectListener(String methodName) {
		setChildSelectListener(methodName, getPApplet());
	}

	/**
	 * Set a listener for when a child of this header has been selected.
	 * 
	 * Event arguments: the selected child {@link #MenuItem()}
	 * 
	 * @param lambda lambda expression with {@link #MenuItem()} parameter
	 */
	public void setChildSelectListener(Predicate1<MenuItem> lambda) {
		childSelectListener = new LambdaEventListener1<MenuItem>(lambda);
	}

	/**
	 * Set a listener for when a child of this header has been selected.
	 * 
	 * Event arguments: none
	 * 
	 * @param lambda lambda expression
	 */
	public void setChildSelectListener(Predicate lambda) {
		childSelectListener = new LambdaEventListener(lambda);
	}

	public void removeChildSelectListener() {
		childSelectListener = null;
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
	private static Timer hoverTimer = new Timer();
	private static HoverTimerTask hoverTimerTask;

	/*
	 * Special TimerTask version that keeps track of the item that has summoned the
	 * timer
	 */
	private static class HoverTimerTask extends TimerTask {
		MenuItem item;

		public HoverTimerTask(MenuItem item) {
			super();
			this.item = item;
		}

		@Override
		public void run() {
			// if still hovered over after time, then open this strip

			if (item.pHovered && !item.open) {

				// if has items, then open. If not then dont call open as this will call
				// itemSelected and close all
				if (item.items.size() > 0) {
					item.open();
				} else {
					// close all siblings
					for (Control c : ((MenuStrip) item.parent).items) {
						((MenuItem) c).close();
					}
				}
			}
		}
	}

	protected void startHoverTimer() {
		// cancel task when having left another item in under MenuItemHoverMilliseconds
		if (hoverTimerTask != null)
			hoverTimerTask.cancel();

		// create task new
		hoverTimerTask = new HoverTimerTask(this);
		hoverTimer.schedule(hoverTimerTask, Math.max(0, GuisetGlobalValues.menuItemHoverTime));
	}

	/*
	 * When hovering over a menu item the substrip - if existent - is shown.
	 * Therefore we wait for 0.4s and open the substrip and if there's none we close
	 * the substrips of all siblings.
	 */

	@Override
	protected void enter(MouseEvent e) { // when clicked hovering is sufficient for changing the dropdown
		visualBackgroundColor = hoverColor;

		if (type == Type.NESTED_MENU_ITEM) {
			startHoverTimer();
		} else if (type == Type.MENU_HEADER) {
			// check if another header is open, if so then close it and open this one
			// immediately (doesn't apply if this header is the open one)

			if (!open) {
				for (MenuItem header : headers) {
					if (header.open) {
						header.close();
						open();
						break;
					}
				}
			}
		}
		update();
	}



	@Override
	protected void press(MouseEvent e) {
		// enable user to select a nested item after pressing and holding a menu header
		// without releasing the mouse in between. We act like this was a move event.
		// notDragging is set to false again in Frame when the next release happens
		notDragging = true;
		draggedElement = null;

		// if item is menu header, open and close on press
		if (type == Type.MENU_HEADER) {
			if (open) {
				close();
				visualBackgroundColor = hoverColor;
			} else {
				open();
			}
			update();
		}

		stopPropagation();
	}



	@Override
	protected void release(MouseEvent e) {

		// if item is subitem, open and close on release
		if (type == Type.NESTED_MENU_ITEM) {
			open();
		}
		update();
		stopPropagation();
	}


}



/**
 * Container for MenuItems
 *
 */

class MenuStrip extends Container {

	public MenuStrip() {
		super();
		setBackgroundColor(GuisetGlobalValues.menuStripBackgroundColor);
		setBorder(1, GuisetGlobalValues.menuStripBorderColor);
		setVisible(false);
		setBoxShadow(4, 7, 7, GuisetColor.BLACK, .4f);
	}


	@Override
	protected void render() {
		// obtain needed width (maximum of item minWidth)
		int w = 100;
		for (Control item : items) {
			w = Math.max(w, item.minWidth);
		}
		setWidth(w);

		// obtain needed height (sum of item heights), also set items width
		int h = 1;
		for (Control item : items) {
			h += item.getHeight();
			item.setWidthNoUpdate(getWidth()); // is already being updated
		}
		setHeight(h);

		// only if parent is not a ParentGraphicsRenderer (temporary solution)
		if (pg != parent.pg) {
			// we cheat here and give some extra size for shadow
			pg = getPApplet().createGraphics(getWidth() + 5, getHeight() + 5);
			pg.beginDraw();
		}

		// drawShadow(getWidth(), getHeight(), 5);

		drawDefaultBackground();

		// draw the thin vertical line
		pg.strokeWeight(1);
		pg.stroke(220);
		pg.line(22, 0 + 3, 22, getHeight() - 3);
		pg.stroke(255);
		pg.line(23, 0 + 3, 23, getHeight() - 3);

		int usedSpace = paddingTop;

		for (Control item : items) {
			if (item.visible) {
				renderItem(item, item.marginLeft + paddingLeft, usedSpace + item.marginTop);
				usedSpace += (item.getHeight() + item.marginTop + item.marginBottom);
			}
		}
	}

	protected void drawShadow(int w, int h, int offset) {
		pg.noFill();
		pg.strokeWeight(1);
		int[] cl = { 115, 85, 41, 15, 5 };
		for (int i = 0; i < 5; i++) {
			pg.stroke(GuisetColor.create(0, cl[i]));
			pg.rect((offset - 1) * 2 - i, (offset - 1) * 2 - i, w - 2 * (4 - i), h - 2 * (4 - i));
		}
	}


	// also used internally by MenuItem
	/**
	 * Show this MenuStrip.
	 */
	public void show() {
		MenuSurface.openMenuSurface();
		setVisible(true);
	}


	// also used internally by MenuItem
	/**
	 * Hide this MenuStrip.
	 */
	public void hide() {
		setVisible(false); // not sure if the update() is necessary here
	}

	/**
	 * Add menu items by passing their text.
	 * 
	 * @param strings arbitrary number of text Strings
	 */
	public void add(String... strings) {
		for (String s : strings) {
			MenuItem newItem = new MenuItem();
			newItem.setText(s);
			insert(items.size(), newItem);
		}
		update();
	}

	// called by MenuItem and only used by free MenuStrips that are unbound to a
	// menu
	protected void itemSelected(MenuItem item) {
		item.close();
		hide();

	}
}
