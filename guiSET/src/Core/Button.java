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
		setPadding(5);
		borderWidth = 1;
		hoverColor = -3618616; 		// color(200)
		pressedColor = -6908266;	// color(150)
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

	/**
	 * Create an image button. Width and height of button will be set to dimensions
	 * of given image.
	 * 
	 * @param image background image for button
	 */
	public Button(PImage image) {
		this(image, image.width, image.height);
	}

	/**
	 * Create an image button. Width and height of button will be set to dimensions
	 * of image multiplied by given scale factor.
	 * 
	 * @param image image background image for button
	 * @param scale scale factor for size
	 */
	public Button(PImage image, float scale) {
		this(image, (int) (image.width * scale), (int) (image.height * scale));
	}

	/**
	 * Create an image label. Width and height of button are set to given
	 * dimensions.
	 * 
	 * @param image  image background image for button
	 * @param width  width for label
	 * @param height height for label
	 */
	public Button(PImage image, int width, int height) {
		setImage(image);
		this.width = width;  // should be okay to set size without setter
		this.height = height;
		borderWidth = 0;
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
		visualBackgroundColor = pressedColor;
		update();
	}

	@Override
	protected void release(MouseEvent e) {
		visualBackgroundColor = hoverColor;
		update();
	}


}