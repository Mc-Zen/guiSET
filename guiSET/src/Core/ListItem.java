package guiSET.core;

import guiSET.classes.Color;
import processing.core.*;
import processing.event.*;

/**
 * Optimal item to add to {@link ListView} as it features firing the
 * itemSelected event of ListView.
 * 
 * @author Mc-Zen
 *
 */
public class ListItem extends Control {

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
		activateInternalMouseListener();
		setPadding(3);
		borderWidth = 1;
		setSelectionColor(-12171706);
		selectionForegroundColor = 255;

		autosize();
	}

	/**
	 * Immediately set text of ListItem with this constructor.
	 * 
	 * @param text
	 */
	public ListItem(String text) {
		this();
		setText(text);
	}

	@Override
	protected void render() {
		// Exception: Width is set in render() method for once which is okay because
		// parent expects that
		width = PApplet.max(1, parent.width -parent.paddingLeft - parent.paddingRight - marginLeft - marginRight - 5); // getAvailableWidth() would be nice, but parent is a control not container

		pg = Frame.frame0.papplet.createGraphics(width, height);
		pg.beginDraw();

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

	@Override
	protected void autosize() {
		height = (int) (fontSize * 1.5) + paddingTop + paddingBottom;
	}

	/*
	 * Getter and Setter
	 */

	/**
	 * set selection color
	 */
	/**
	 * Sets the background color for when the item is in its selected state. This
	 * also sets selectionHoverColor (a little darker / brighter depending on the
	 * brightness of the selectionColor).
	 * 
	 * @param selectionColor selection color
	 */
	public void setSelectionColor(int selectionColor) {
		this.selectionColor = selectionColor;
		int r = (int) Frame.frame0.papplet.red(selectionColor);
		int g = (int) Frame.frame0.papplet.green(selectionColor);
		int b = (int) Frame.frame0.papplet.blue(selectionColor);

		if (Frame.frame0.papplet.brightness(selectionColor) > 40) {
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
	 * @param selectionHoverColor
	 */
	public void setSelectionHoverColor(int selectionHoverColor) {
		this.selectionHoverColor = selectionHoverColor;
		update();
	}

	/**
	 * Sets the text color for when the item is selected.
	 * 
	 * @param selectionForegroundColor
	 */
	public void setSelectionForegroundColor(int selectionForegroundColor) {
		this.selectionForegroundColor = selectionForegroundColor;
		update();
	}

	// only used by ListView (not anymore)
	protected void setSelected(boolean selected) {
		this.selected = selected;
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

	// all of these override empty methods so no need to call super method.

	@Override
	protected void enter(MouseEvent e) {
		visualBackgroundColor = hoverColor;
		update();
	}

	@Override
	protected void exit(MouseEvent e) {
		visualBackgroundColor = backgroundColor;
		update();
	}

	@Override
	protected void press(MouseEvent e) {
		super.press(e);
		try {
			((ListView) parent).itemPressed((Control) this);
		} catch (ClassCastException cce) {
			// just ignore
		}
	}

	@Override
	protected void release(MouseEvent e) {
		update();
	}
}