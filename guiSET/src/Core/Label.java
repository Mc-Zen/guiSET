package guiSET.core;

import processing.core.*;


/**
 * A basic template for plain text labels.
 * 
 * @author E-Bow
 *
 */
public class Label extends Control {

	public Label() {
		this("", 12);
	}

	/**
	 * Constructor for specifying text to display. Default font size is 12 and
	 * background is transparent.
	 * 
	 * @param text
	 */
	public Label(String text) {
		this(text, 12);
	}



	/**
	 * Constructor for specifying text to display and font size. Background is
	 * transparent.
	 * 
	 * @param text     text for label
	 * @param fontSize font size
	 */
	public Label(String text, int fontSize) {
		super();
		setText(text);
		setFontSize(fontSize);
		borderWidth = 0;
		visualBackgroundColor = 0;
		setPadding(2); // might cut off a tiny part of the text otherwise
	}

	/**
	 * Create an image label. Width and height of label will be set to dimensions of
	 * image
	 * 
	 * @param image background image for label
	 */
	public Label(PImage image) {
		this(image, image.width, image.height);
	}

	/**
	 * Create an image label. Width and height of label will be set to dimensions of
	 * image multiplied by given scale factor
	 * 
	 * @param image image background image for label
	 * @param scale scale factor for size
	 */
	public Label(PImage image, float scale) {
		this(image, (int) (image.width * scale), (int) (image.height * scale));
	}

	/**
	 * Create an image label. Width and height of label are set to given dimensions
	 * 
	 * @param image  image background image for label
	 * @param width  width for label
	 * @param height height for label
	 */
	public Label(PImage image, int width, int height) {
		setImage(image);
		this.width = width;  // should be okay to set size without setter
		this.height = height;
		borderWidth = 0;
	}


	@Override
	protected void render() {
		drawDefaultBackground();
		drawDefaultText();
	}


	@Override
	protected void autosize() {
		width = (int) PApplet.constrain(textWidth(text) + paddingLeft + paddingRight, minWidth, maxWidth);
		int numLines = text.split("\n").length;
		height = (int) PApplet.constrain(numLines * textLeading() + paddingTop + paddingBottom, minHeight, maxHeight);
	}
}