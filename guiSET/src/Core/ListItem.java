package guiSET.core;

import processing.event.*;

/**
 * Optimal item to add to {@link ListView} as it features firing the
 * itemSelected event of ListView.
 * 
 * @author Mc-Zen
 *
 */
public class ListItem extends TextBased {

	// back color for selected state
	protected int selectionColor;
	// hover color for selected state
	protected int selectionHoverColor;
	// hover color for selected state
	protected int selectionForegroundColor;

	// true if this item has been selected by the parent listview
	protected boolean selected;

	public ListItem() {
		super();
		setPadding(3);
		setBorderWidth(1);
		setSelectionColor(SELECTION_BLUE);
		setSelectionForegroundColor(WHITE);
		setBackgroundColor(TRANSPARENT);
		setHoverColor(Color.create(0,0,0,30));
		setPressedColor(Color.create(0,0,0,50));

		autosize();
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
		// Exception: Width is set in render() method for once which is okay because
		// parent expects that

		//setWidthImpl(((Container) parent).getAvailableWidth());
//		updateAnchors(); // not really necessary or elegant but for now (just in case listitem has
						 // children)
//		resize();        // ...

		// only if parent is not a ParentGraphicsRenderer (temporary solution)
		if (pg != parent.pg) {
			pg = getPApplet().createGraphics(width, height);
			pg.beginDraw();
		}

		if (selected) {
			int temp = visualBackgroundColor;
			visualBackgroundColor = pHovered ? selectionHoverColor : selectionColor;
			drawDefaultBackground();
			visualBackgroundColor = temp;

			temp = foregroundColor;
			foregroundColor = selectionForegroundColor;

			drawDefaultText();
			foregroundColor = temp;

		} else {
			drawDefaultBackground();
			drawDefaultText();
		}

	}

//	@Override
//	protected void autosizeRule() {
//		setHeightImpl((int) (getFontSize() * 1.5) + paddingTop + paddingBottom);
//	}

	@Override
	protected int autoHeight() {
		return (int) (getFontSize() * 1.5) + paddingTop + paddingBottom;
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
	 * Sets the background color for when the item is in its selected state. This
	 * also sets selectionHoverColor (a little darker / brighter depending on the
	 * brightness of the selectionColor).
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
			selectionHoverColor = Color.create(r - 20, g - 20, b - 20);
		} else {
			// lighten color for selectionHoverColor when color too dark
			selectionHoverColor = Color.create(r + 20, g + 20, b + 20);
		}
		update();
	}

	/**
	 * Sets the background color for when the item is selecteed AND hovered over
	 * with the mouse.
	 * 
	 * @param selectionHoverColor selection hover color
	 */
	public void setSelectionHoverColor(int selectionHoverColor) {
		this.selectionHoverColor = selectionHoverColor;
		update();
	}

	/**
	 * Sets the text color for when the item is selected.
	 * 
	 * @param selectionForegroundColor selection foreground color
	 */
	public void setSelectionForegroundColor(int selectionForegroundColor) {
		this.selectionForegroundColor = selectionForegroundColor;
		update();
	}


	public int getSelectionColor() {
		return selectionColor;
	}

	public int getSelectionHoverColor() {
		return selectionHoverColor;
	}

	public int getSelectionForegroundColor() {
		return selectionForegroundColor;
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