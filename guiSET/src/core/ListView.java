package guiSET.core;

import processing.event.*;

import java.util.ArrayList;
import java.util.Comparator;



/**
 * The list view allows to display easily a lot of items in a vertical list. When adding a String a
 * new ListItem will be created and added. You can also add items that inherit from {@link Control}
 * other than ListItems or derived but these will only raise the itemSelected event if they call
 * ListViews (protected) itemPressed(Control) method.
 * 
 * @author E-Bow
 *
 */
public class ListView extends VScrollContainer {

	// default background color for new Items generated with add(String)
	protected int newItemBackgroundColor;

	// default back color for selected items for new Items generated with
	// add(String)
	protected int selectionColor;

	// default hover color for selected items for new Items generated with
	// add(String)
	protected int selectionHoverColor;

	// allow selection of multiple items
	protected boolean multiSelect = false;

	// on every change of availableWidth (paddings or width), resize items to fill the ListView
	protected boolean makeItemsFillAvailableWidth = true;


	// currrently selected (or last selected) item
	protected Control selectedItem;


	// if multiSelect true then this list contains the selected items (one of which
	// is "selectedItem"
	protected ArrayList<Control> selectedItems;



	public ListView() {
		this(100, 150);
	}

	public ListView(int width, int height) {
		super(width, height);
		selectedItems = new ArrayList<Control>(0);

		setBorderWidth(1);
		setBackgroundColor(GuisetColor.WHITE);
		setSelectionColor(GuisetColor.SELECTION_BLUE);
		setNewItemBackgroundColor(GuisetColor.TRANSPARENT);
	}




	/*
	 * Method called by the list items
	 */

	protected void itemPressed(Control item) {
		focus();

		if (!multiSelect) {

			// just deselect previous selectedItem and select the new one
			if (item != selectedItem) { // dont raise itemSelected-event if already selected
				deselect(selectedItem);
				selectImpl(item);
			}

		} else { // if (multiSelect)

			if (getFrame().isControlDown()) {

				// if control pressed: add to selection if not yet selected, else deselect

				if (selectedItems.contains(item)) {
					deselect(item);
				} else {
					selectImpl(item);
				}

			} else if (getFrame().isShiftDown()) {

				// if shift pressed: select all items between this and previously selected item

				int selStart = getSelectionIndex();
				int selEnd = indexOf(item);

				if (selStart > -1 && selEnd > -1) {

					if (selStart > selEnd) {
						for (int i = selStart - 1; i >= selEnd; i--) {
							selectImpl(i);
						}

					} else if (selStart < selEnd) {

						for (int i = selStart + 1; i <= selEnd; i++) {
							selectImpl(i);
						}
					}
				}
			} else {
				// if no modifier pressed: deselect all selected and select the new item

				deselectAll();
				selectImpl(item);
			}
		}

	}






	/*
	 * Officially select an item internally (also checks if item is in this list).
	 * 
	 * selectedItem is only modified here ! selectedItems is only modified here and
	 * in deselect
	 * 
	 * The style of the selected item will be changed to fit background and hover
	 * color for selected items.
	 * 
	 */
	protected void selectImpl(int index) {
		if (index >= 0 && index < items.size()) {
			selectedItem = items.get(index);

			if (!selectedItems.contains(selectedItem)) {
				selectedItems.add(selectedItem);
			}

			try {
				// only raise event if item has not been selected before
				if (!((ListItem) selectedItem).selected) {
					((ListItem) selectedItem).selected = true; // dont use setter here
					selectedItem.update();

				}
			} catch (ClassCastException e) {
			}
			handleEvent(selectListener, selectedItem);

		} else {
			selectedItem = null;
		}
	}

	protected void selectImpl(Control item) {
		selectImpl(indexOf(item));
	}




	/*
	 * Officially deselects an item
	 */

	/**
	 * Deselect an item.
	 * 
	 * @param index index of item to deselect. Throws no error if index is bad.
	 */
	public void deselect(int index) {
		if (index >= 0 && index < items.size()) {
			Control c = items.get(index);
			if (c instanceof ListItem) {
				// only raise event if item has not been selected before
				if (((ListItem) c).isSelected()) {
					((ListItem) c).selected = false; // dont use setter here
					c.update();
				}
			}

			selectedItems.remove(c);	// does no harm if not in list
			if (selectedItem == c)		// if this is head-of-selected-items, deselect
				selectImpl(-1);
		}
	}

	/**
	 * Deselect an item.
	 * 
	 * @param item item to deselect.
	 */
	public void deselect(Control item) {
		deselect(indexOf(item));
	}

	/**
	 * Deselect all items
	 */
	public void deselectAll() {
		while (selectedItems.size() > 0) {
			deselect(selectedItems.get(0));
		}
		selectImpl(-1);
	}




	/**
	 * Select item with given index. If multiselect is not activated then this will deselect all other
	 * selected items. Throws no error if index is bad.
	 * 
	 * @param index index in listviews item list
	 */
	public void select(int index) {
		if (!multiSelect)
			deselectAll();
		selectImpl(index);
	}

	/**
	 * Select item by reference. If multiselect is not activated then this will deselect all other
	 * selected items.
	 * 
	 * @param item item to select
	 */
	public void select(Control item) {
		if (!multiSelect)
			deselectAll();
		selectImpl(item);
	}


	/**
	 * Set one item to be selected programatically. Also deselecting all previously selected items.
	 * 
	 * @param index index in listviews item list
	 */
	public void setSelectedItem(int index) {
		deselectAll();
		selectImpl(index);
		scrollToItem(selectedItem);
		update();
	}




	/**
	 * Simple adding method just giving the text as String will create automatically new ListItems.
	 * 
	 * @param newItems arbitrary number of item texts
	 */
	public void add(String... newItems) {
		for (String newItem : newItems) {
			add(newItem);
		}
	}

	/**
	 * Simple adding method just giving the text as String will create automatically new ListItems.
	 * 
	 * @param newItem item text
	 */
	public void add(String newItem) {
		ListItem newListItem = new ListItem();

		/*
		 * Some properties will be taken over from the listview to make adding listitems
		 * by string easier. Of course this works only for properties that listview
		 * itself doesn't use.
		 */
		newListItem.setText(newItem);
		newListItem.setFontSize(getFontSize());
		newListItem.setTextAlign(getTextAlign());
		newListItem.setSelectionColor(getSelectionColor());
		newListItem.setSelectionHoverColor(getSelectionHoverColor());
		newListItem.setTextColor(getTextColor());

		this.add(newListItem);
	}


	@Override
	public void add(Control... items) {
		for (Control item : items) {
			if (item instanceof ListItem) {
				item.setWidth(getAvailableWidth());
			}
			insertImpl(this.items.size(), item);
		}
		// no sorting
		update();
	}


	/**
	 * Remove all items
	 */
	@Override
	public void clear() {
		deselectAll();
		super.clear();
	}

	/**
	 * Remove item at position in item list.
	 * 
	 * @param index position
	 * @return the removed item
	 */
	@Override
	public Control remove(int index) {
		deselect(index);
		return super.remove(index);
	}

	/**
	 * Remove a specific item from item list.
	 * 
	 * @param item item to remove.
	 */
	@Override
	public boolean remove(Control item) {
		deselect(item);
		return super.remove(item);
	}


	@Override
	protected void availableWidthChanged() {
		if (makeItemsFillAvailableWidth) {
			int availableWidth = getAvailableWidth();
			for (Control item : items) {
				item.setWidth(availableWidth);
			}
		}
	}

	/**
	 * Sort items alphanumerically.
	 */
	public void sortAlphaNumerically() {
		sortAlphaNumerically(false);
	}

	/**
	 * Sort items alphanumerically. If argument is false then the items are sorted backwards.
	 * 
	 * 
	 * @param reversed sort reversed
	 */
	public void sortAlphaNumerically(boolean reversed) {
		sortItems(new Comparator<Control>() {
			@Override
			public int compare(Control a, Control b) {
				if (a instanceof TextBased && b instanceof TextBased) {
					return ((TextBased) a).getText().compareTo(((TextBased) b).getText()) * (reversed ? -1 : 1);
				} else {
					return 0;
				}
			}
		});
	}



	/*
	 * SETTER
	 */




	/**
	 * Settimg this color will affect all items added afterwards through the {@link #add(String)}
	 * method.
	 * 
	 * @param itemBackgroundColor background color for new items
	 */
	public void setNewItemBackgroundColor(int itemBackgroundColor) {
		this.newItemBackgroundColor = itemBackgroundColor;
		update();
	}


	/**
	 * Background color for selected items.
	 * 
	 * @param selectionColor selection color
	 */
	public void setSelectionColor(int selectionColor) {
		this.selectionColor = selectionColor;
		int r = (int) getPApplet().red(selectionColor);
		int g = (int) getPApplet().green(selectionColor);
		int b = (int) getPApplet().blue(selectionColor);

		if (getPApplet().brightness(selectionColor) > 40) {
			// darken color for HoverColor and PressedColor whencolor is bright enough
			selectionHoverColor = GuisetColor.create(r - 20, g - 20, b - 20);
		} else {
			// lighten color for HoverColor and PressedColor when color too dark
			selectionHoverColor = GuisetColor.create(r + 20, g + 20, b + 20);
		}
		update();
	}


	/**
	 * Hover color for selected items.
	 * 
	 * @param selectionHoverColor selection hover color
	 */
	public void setSelectionHoverColor(int selectionHoverColor) {
		this.selectionHoverColor = selectionHoverColor;
		update();
	}

	/**
	 * Enable/Disable multiple selections.
	 * 
	 * @param multiSelect multiSelect
	 */
	public void setMultiSelect(boolean multiSelect) {
		this.multiSelect = multiSelect;

		if (!multiSelect) {
			// if set multiselect to false while multiple items are selected:
			// deselect all and re-select the most recent
			Control temp = selectedItem;
			deselectAll();
			selectImpl(temp);
		}
	}

	public void setMakeItemsFillAvailableWidth(boolean makeItemsFillAvailableWidth) {
		this.makeItemsFillAvailableWidth = makeItemsFillAvailableWidth;
	}


	/*
	 * GETTER
	 */

	public int getItemBackgroundColor() {
		return newItemBackgroundColor;
	}

	public int getSelectionColor() {
		return selectionColor;
	}

	public int getSelectionHoverColor() {
		return selectionHoverColor;
	}

	/**
	 * Get most recently selected item.
	 * 
	 * @return selected item
	 */
	public Control getSelectedItem() {
		return selectedItem;
	}

	public int getSelectionIndex() {
		return items.indexOf(selectedItem);
	}

	/**
	 * Get all selected items (if multiselect enabled).
	 * 
	 * @return selected items
	 */
	public ArrayList<Control> getSelectedItems() {
		return selectedItems;
	}

	public boolean getMultiSelect() {
		return multiSelect;
	}

	public boolean isMakeItemsFillAvailableWidth() {
		return makeItemsFillAvailableWidth;
	}






	/*
	 * EVENTS
	 */

	protected EventListener selectListener;

	/**
	 * Set a listener for when an item is selected.
	 * 
	 * Event arguments: the {@link Control} whose state has changed
	 * 
	 * @param methodName methodName
	 * @param target     target
	 */

	public void setItemSelectListener(String methodName, Object target) {
		selectListener = createEventListener(methodName, target, Control.class);
	}

	public void setItemSelectListener(String methodName) {
		setItemSelectListener(methodName, getPApplet());
	}

	/**
	 * Set a listener lambda for when an item is selected. The event passes the item that has been
	 * selected.
	 * 
	 * Event arguments: the {@link Control} whose state has changed
	 * 
	 * @param lambda lambda expression with {@link Control} parameter
	 */
	public void setItemSelectListener(Predicate1<Control> lambda) {
		selectListener = new LambdaEventListener1<Control>(lambda);
	}

	/**
	 * Set a listener lambda for when an item is selected.
	 * 
	 * Event arguments: none
	 * 
	 * @param lambda lambda expression
	 */
	public void setItemSelectListener(Predicate lambda) {
		selectListener = new LambdaEventListener(lambda);
	}

	public void removeItemSelectListener() {
		selectListener = null;
	}



	@Override
	protected void keyPress(KeyEvent e) {
		super.keyPress(e);
		switch (e.getKeyCode()) {
		case Constants.DOWN:
			int selectionIndex = getSelectionIndex();
			if (selectionIndex < items.size() - 1) {
				itemPressed(items.get(selectionIndex + 1));
				scrollToItem(selectionIndex + 1);
			}
			break;
		case Constants.UP:
			selectionIndex = getSelectionIndex();
			if (selectionIndex > 0) {
				itemPressed(items.get(selectionIndex - 1));
				scrollToItem(selectionIndex - 1);
			}
			break;
		}
	}
}
