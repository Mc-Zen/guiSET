package guiSET.core;

import processing.core.*;
import processing.event.*;

/**
 * Basic button template with automatic background colro hover and press effect.
 * 
 * @author Mc-Zen
 *
 */
public class Button extends Control {

	public Button() {
		super();
		activateInternalMouseListener();
		setPadding(5);
		borderWidth = 1;
	}

	/**
	 * Constructor for specifying button text.
	 * 
	 * @param text
	 */
	public Button(String text) {
		this();
		this.setText(text);
	}

	/**
	 * Constructor for specifying button text and font size.
	 * 
	 * @param text
	 */
	public Button(String text, int fontSize) {
		this(text);
		this.setFontSize(fontSize);
	}

	/**
	 * Constructor for specifying button text and press callback method name.
	 * 
	 * @param text
	 */
	public Button(String text, String pressMethod) {
		this(text);
		addMouseListener("press", pressMethod);
	}

	/*
	 * DRAWING AND RENDERING
	 */

	@Override
	protected void render() {

		drawDefaultBackground();
		drawDefaultText();
		standardDisabled();

	}

	@Override
	protected void autosize() {
		width = (int) PApplet.constrain(textWidth(text) + paddingLeft + paddingRight, minWidth, maxWidth);
		int numLines = text.split("\n").length;
		// height = (int) PApplet.constrain(fontSize*1.1f + paddingTop + paddingBottom,
		// minHeight, maxHeight);
		height = (int) PApplet.constrain(numLines * textLeading() + paddingTop + paddingBottom, minHeight, maxHeight);
	}

	/*
	 * STYLE SETTING METHODS
	 */

	// automatically set HoverColor and PressedColor
	@Override
	public void setBackgroundColor(int clr) {
		setStatusBackgroundColorsAutomatically(clr);
	}

	/*
	 * EVENT METHODS
	 * 
	 * need to change some colors when hovered over or pressed.
	 */
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
		focus();
		visualBackgroundColor = pressedColor;
		update();
	}

	@Override
	protected void release(MouseEvent e) {
		visualBackgroundColor = hoverColor;
		update();
	}
}