package pGUI.core;

import processing.core.*;
import processing.event.*;

import java.util.ArrayList;

import pGUI.classes.*;

public class ListView extends VScrollContainer {

	// default background color for new Items generated with add(String)
	protected int itemBackgroundColor;
	// default back color for selected items for new Items generated with
	// add(String)
	protected int selectionColor;
	// default hover color for selected items for new Items generated with
	// add(String)
	protected int selectionHoverColor;

	// allow selection of multiple items
	protected boolean multiSelect = false;

	// currrently selected (or last selected) item
	protected Control selectedItem;
	// index of selectedItem in list
	protected int selectionIndex = -1;
	// if multiSelect true then this list contains the selected items (one of which
	// is "selectedItem")
	protected ArrayList<Control> selectedItems;



	public ListView() {
		this(100, 150);
	}

	public ListView(int width, int height) {
		super();
		setSize(width, height);
		selectedItems = new ArrayList<Control>();

		setupListeners(1);

		setBackgroundColor(-3618616); // light grey
		setSelectionColor(-12171706);

		itemBackgroundColor = -1; // white
		fontSize = 20;
		textAlign = 37;
	}


	/*
	 * Method called by the list items
	 */

	protected void itemSelected(Control item) {
		focus();

		if (!multiSelect) {

			// just deselect previous selectedItem and select the new one
			deselect(selectedItem);
			select(item);

		} else { // if(multiSelect)

			if (Frame.frame0.isControlDown()) {

				// if control pressed: add to selection if not yet selected, else deselect

				if (selectedItems.contains(item)) {
					deselect(item);
				} else {
					select(item);
				}

			} else if (Frame.frame0.isShiftDown()) {

				// if shift pressed: select all items between this and previously selected item

				int selStart = selectionIndex;
				int selEnd = getIndex(item);

				if (selStart > -1 && selEnd > -1) {

					if (selStart > selEnd) {
						for (int i = selStart - 1; i >= selEnd; i--) {
							select(i);
							if (!selectedItems.contains(content.get(i))) {
								selectedItems.add(content.get(i));
							}
						}

					} else if (selStart < selEnd) {

						for (int i = selStart + 1; i <= selEnd; i++) {
							select(i);
							if (!selectedItems.contains(content.get(i))) {
								selectedItems.add(content.get(i));
							}
						}
					}
				}
			} else {

				// if no modifier pressed: deselect all selected and select the new item

				for (Control control : selectedItems) {
					deselect(control);
				}
				selectedItems.clear();
				select(item);
			}
		}

	}

	// simple adding method just giving the text

	public void add(String... newItems) {
		for (String newItem : newItems) {
			add(newItem);
		}
	}

	public void add(String newItem) {
		ListItem newListItem = new ListItem();

		/*
		 * Some properties will be taken over from the listview to make adding listitems
		 * by string easier. Of course this works only for properties that listview
		 * itself doesn't use.
		 */
		newListItem.text = newItem;
		newListItem.fontSize = fontSize;
		newListItem.textAlign = textAlign;
		newListItem.selectionColor = selectionColor;
		newListItem.selectionHoverColor = selectionHoverColor;
		newListItem.width = this.width;

		newListItem.setBackgroundColor(itemBackgroundColor);
		newListItem.setForegroundColor(foregroundColor);
		this.add(newListItem);
	}




	/*
	 * get list index of an item, returns -1 if item is not in the list
	 */

	public int getIndex(Control item) {
		for (int i = 0; i < content.size(); i++) {
			if (item == content.get(i)) {
				return i;
			}
		}
		return -1;
	}

	/*
	 * Officially select an item internally (also checks if item is in this list)
	 * 
	 * The style of the selected item will be changed to fit background and hover
	 * color for selected items
	 * 
	 * Sets selectionIndex and selectedItem. Also adds to selectedItems-list if
	 * multiSelect is activated
	 */

	protected void select(int index) {
		if (index >= 0 && index < content.size()) {
			selectedItem = content.get(index);
			selectionIndex = index;

			if (multiSelect) {
				if (!selectedItems.contains(selectedItem)) {
					selectedItems.add(selectedItem);
				}
			}

			((ListItem) selectedItem).setSelected(true);
			selectedItem.update();

			handleRegisteredEventMethod(SELECT_EVENT, null);
		}
	}

	protected void select(Control item) {
		select(getIndex(item));
	}

	/*
	 * Officially deselects an item
	 */

	protected void deselect(int index) {
		if (index >= 0 && index < content.size()) {
			((ListItem) content.get(index)).setSelected(false);
			content.get(index).update();
		}
	}

	protected void deselect(Control item) {
		deselect(getIndex(item));
	}










	/*
	 * SETTER
	 */

	public void setItemBackgroundColor(int itemBackgroundColor) {
		this.itemBackgroundColor = itemBackgroundColor;
		update();
	}

	public void setSelectionColor(int selectionColor) {
		this.selectionColor = selectionColor;
		int r = (int) Frame.frame0.papplet.red(selectionColor);
		int g = (int) Frame.frame0.papplet.green(selectionColor);
		int b = (int) Frame.frame0.papplet.blue(selectionColor);

		if (Frame.frame0.papplet.brightness(selectionColor) > 40) {
			// darken color for HoverColor and PressedColor whencolor is bright enough
			selectionHoverColor = Color.create(r - 20, g - 20, b - 20);
		} else {
			// lighten color for HoverColor and PressedColor when color too dark
			selectionHoverColor = Color.create(r + 20, g + 20, b + 20);
		}
		update();
	}



	public void setSelectionHoverColor(int selectionHoverColor) {
		this.selectionHoverColor = selectionHoverColor;
	}

	public void setMultiSelect(boolean multiSelect) {
		this.multiSelect = multiSelect;

		if (multiSelect) {
			// if set multiselect while one item is already selected -> add it to list
			if (selectedItem != null) {
				selectedItems.add(selectedItem);
			}

		} else {
			// if set multiselect to false while multiple items are selected:
			// deselect all but the most recent and clear selectedItems list.

			for (Control control : selectedItems) {
				deselect(control);
			}
			selectedItems.clear();
			if (selectedItem != null) {
				selectedItems.add(selectedItem);
			}
		}
	}

	/*
	 * Set the selected item programatically. Also deselecting all previously
	 * selected items.
	 */

	public void setSelectedItem(int index) {

		if (multiSelect) {
			for (Control control : selectedItems) {
				deselect(control);
			}
			selectedItems.clear();
		} else {
			deselect(selectionIndex);
		}

		select(index);
		scrollToItem(selectionIndex);

		update();
	}

	/*
	 * GETTER
	 */

	public int getItemBackgroundColor() {
		return itemBackgroundColor;
	}

	public int getSelectionColor() {
		return selectionColor;
	}

	public int getSelectionHoverColor() {
		return selectionHoverColor;
	}

	public Control getSelectedItem() {
		return selectedItem;
	}

	public int getSelectionIndex() {
		return selectionIndex;
	}

	public ArrayList<Control> getSelectedItems() {
		return selectedItems;
	}

	public boolean getMultiSelect() {
		return multiSelect;
	}






	/*
	 * EVENTS
	 */

	protected static final int SELECT_EVENT = Frame.numberMouseListeners;

	public void addItemChangedListener(String methodName) {
		addItemChangedListener(methodName, Frame.frame0.papplet);
	}

	public void addItemChangedListener(String methodName, Object target) {
		registerEventRMethod(SELECT_EVENT, methodName, target, null);
	}

	public void removeItemChangedListener() {
		deregisterEventRMethod(SELECT_EVENT);
	}

	@Override
	protected void press(MouseEvent e) {
		focus();
	}


	@Override
	protected void onKeyPress(KeyEvent e) {
		if (enabled) {
			switch (e.getKeyCode()) {
			case PApplet.DOWN:
				if (selectionIndex < content.size() - 1) {
					itemSelected(content.get(selectionIndex + 1));
					scrollToItem(selectionIndex);
				}
				break;
			case PApplet.UP:
				if (selectionIndex > 0) {
					itemSelected(content.get(selectionIndex - 1));
					scrollToItem(selectionIndex);
				}
				break;
			}
		}
	}
}
