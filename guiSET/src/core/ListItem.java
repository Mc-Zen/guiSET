package guiSET.core;

import processing.event.*;

/**
 * Optimal item to add to {@link ListView} as it features firing the itemSelected event of ListView.
 * 
 * @author Mc-Zen
 *
 */
public class ListItem extends TextBased {

	// back color for selected state
	protected int selectionColor;
	
	// hover color for selected state
	protected int selectionHoverColor;

	// true if this item has been selected by the parent listview
	protected boolean selected;

	
	public ListItem() {
		super();
		setPadding(3);
		setBorderWidth(1);
		setSelectionColor(GuisetColor.SELECTION_BLUE);
		setBackgroundColor(GuisetColor.TRANSPARENT);
		setHoverColor(GuisetColor.create(0, 0, 0, 30));
		setPressedColor(GuisetColor.create(0, 0, 0, 50));
	}

	/**
	 * Immediately set text of ListItem with this constructor.
	 * 
	 * @param text text
	 */
	public ListItem(String text) {
		this();
		setText(text);
	}

	@Override
	protected void render() {

		if (selected) {
			int temp = visualBackgroundColor;
			visualBackgroundColor = pHovered ? selectionHoverColor : selectionColor;
			drawDefaultBackground();
			visualBackgroundColor = temp;

			drawDefaultText();

		} else {
			drawDefaultBackground();
			drawDefaultText();
		}

	}


	@Override
	protected int autoHeight() {
		return (int) textHeight(getText()) + getPaddingTop() + getPaddingBottom();
	}








	/*
	 * Getter and Setter
	 */



	// automatically set HoverColor and PressedColor
	@Override
	public void setBackgroundColor(int clr) {
		setStatusBackgroundColorsAutomatically(clr);
	}




	/**
	 * Sets the background color for when the item is in its selected state. This also sets
	 * selectionHoverColor (a little darker / brighter depending on the brightness of the
	 * selectionColor).
	 * 
	 * @param selectionColor selection color
	 */
	public void setSelectionColor(int selectionColor) {
		this.selectionColor = selectionColor;
		int r = (int) getPApplet().red(selectionColor);
		int g = (int) getPApplet().green(selectionColor);
		int b = (int) getPApplet().blue(selectionColor);

		if (getPApplet().brightness(selectionColor) > 40) {
			// darken color for selectionHoverColor when color is bright enough
			selectionHoverColor = GuisetColor.create(r - 20, g - 20, b - 20);
		} else {
			// lighten color for selectionHoverColor when color too dark
			selectionHoverColor = GuisetColor.create(r + 20, g + 20, b + 20);
		}
		update();
	}

	/**
	 * Sets the background color for when the item is selecteed AND hovered over with the mouse.
	 * 
	 * @param selectionHoverColor selection hover color
	 */
	public void setSelectionHoverColor(int selectionHoverColor) {
		this.selectionHoverColor = selectionHoverColor;
		update();
	}

	public int getSelectionColor() {
		return selectionColor;
	}

	public int getSelectionHoverColor() {
		return selectionHoverColor;
	}

	protected boolean isSelected() {
		return selected;
	}





	/*
	 * EVENTS
	 */

	@Override
	protected void press(MouseEvent e) {
		super.press(e);
		if (parent instanceof ListView) {
			((ListView) parent).itemPressed((Control) this);
		}
	}
}