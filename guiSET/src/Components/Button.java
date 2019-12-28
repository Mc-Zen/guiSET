package pGUI.core;

import processing.core.*;
import processing.event.*;

public class Button extends Control {

	public Button() {
		super();
		activateInternalMouseListener();
		setPadding(5);
		borderWidth = 1;
	}

	public Button(String Text) {
		this();
		this.setText(Text);
	}

	public Button(String Text, int fontSize) {
		this(Text);
		this.setFontSize(fontSize);
	}

	public Button(String Text, String pressMethod) {
		this(Text);
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
		height = (int) PApplet.constrain(fontSize + textDescent() + paddingTop + paddingBottom, minHeight, maxHeight);
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