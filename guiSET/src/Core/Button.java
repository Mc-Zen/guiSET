package guiSET.core;

import processing.core.*;

/**
 * Basic button template for text or image buttons.
 * 
 * @author Mc-Zen
 *
 */

public class Button extends Control {

	public Button() {
		super();
		setPadding(5);
		setBorderWidth(1);
		setHoverColor(-3618616); 		// color(200)
		setPressedColor(-6908266);	// color(150)
	}

	/**
	 * Constructor for specifying button text.
	 * 
	 * @param text text
	 */
	public Button(String text) {
		this();
		this.setText(text);
	}

	/**
	 * Constructor for specifying button text and font size.
	 * 
	 * @param text     text
	 * @param fontSize font size
	 */
	public Button(String text, int fontSize) {
		this(text);
		this.setFontSize(fontSize);
	}

	/**
	 * Constructor for specifying button text and press callback method name.
	 * 
	 * @param text        text
	 * @param pressMethod callback for press event
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
		setWidthImpl(width);  // should be okay to set size without setter
		setHeightImpl(height);
		setBorderWidth(0);
	}



	/*
	 * DRAWING AND RENDERING
	 */

	@Override
	protected void render() {
		drawDefaultBackground();
		drawDefaultText();
		drawDefaultDisabled();
	}


	@Override
	protected void autosizeRule() {
		setWidthImpl((int) (textWidth(text) + paddingLeft + paddingRight));
		int numLines = text.split("\n").length;
		setHeightImpl((int) (numLines * textLeading() + paddingTop + paddingBottom));
	}



	/*
	 * STYLE SETTING METHODS
	 */

	// automatically set HoverColor and PressedColor
	@Override
	public void setBackgroundColor(int clr) {
		setStatusBackgroundColorsAutomatically(clr);
	}


}