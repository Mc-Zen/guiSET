package guiSET.core;


import processing.core.*;


/**
 * A basic template for plain text or image labels.
 * 
 * @author E-Bow
 *
 */
public class Label extends TextBased {

	public Label() {
		this("", GuisetDefaultValues.fontSize);
	}

	/**
	 * Constructor for specifying text to display; font size 12, background
	 * transparent.
	 * 
	 * @param text text for label
	 */
	public Label(String text) {
		this(text, GuisetDefaultValues.fontSize);
	}

	/**
	 * Constructor for specifying text and position; font size 12, background
	 * transparent.
	 * 
	 * @param text text for label
	 * @param x    x position
	 * @param y    y position
	 */
	public Label(String text, int x, int y) {
		this(text, GuisetDefaultValues.fontSize);
		setPosition(x, y);
	}


	/**
	 * Constructor for specifying text to display and font size; background is
	 * transparent.
	 * 
	 * @param text     text for label
	 * @param fontSize font size
	 */
	public Label(String text, int fontSize) {
		super();
		setText(text);
		setFontSize(fontSize);
		setBackgroundColor(GuisetDefaultValues.labelBackgroundColor);
		setPadding(GuisetDefaultValues.labelPadding);
	}

	/**
	 * Create an image label; width and height of label will be set to dimensions of
	 * image.
	 * 
	 * @param image background image for label
	 */
	public Label(PImage image) {
		this(image, image.width, image.height);
	}

	/**
	 * Create an image label; width and height of label will be set to dimensions of
	 * image multiplied by given scale factor.
	 * 
	 * @param image image background image for label
	 * @param scale scale factor for size
	 */
	public Label(PImage image, float scale) {
		this(image, (int) (image.width * scale), (int) (image.height * scale));
	}

	/**
	 * Create an image label. Width and height of label are set to given dimensions.
	 * 
	 * @param image  image background image for label
	 * @param width  width for label
	 * @param height height for label
	 */
	public Label(PImage image, int width, int height) {
		setImage(image);
		setSizeWithoutUpdate(width, height);  
		setBorderWidth(0);
	}


	@Override
	protected void render() {
		drawDefaultBackground();
		drawDefaultText();
	}

	@Override
	protected int autoHeight() {
		return (int) textHeight(getText()) + getPaddingTop() + getPaddingBottom();
	}

	@Override
	protected int autoWidth() {
		return (int) textWidth(getText()) + getPaddingLeft ()+ getPaddingRight();
	}

}