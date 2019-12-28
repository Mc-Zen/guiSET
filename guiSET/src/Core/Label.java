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

	public Label(String text) {
		this(text, 12);
	}

	/**
	 * Constructor for specifying text content and font size.
	 * 
	 * @param text
	 * @param fontSize
	 */
	public Label(String text, int fontSize) {
		super();
		this.setText(text);
		this.setFontSize(fontSize);
		borderWidth = 0;
		visualBackgroundColor = 0;
	}

	@Override
	protected void render() {

		drawDefaultBackground();
		drawDefaultText();

	}

	@Override
	protected void autosize() {
		width = (int) PApplet.constrain(textWidth(text) + paddingLeft + paddingRight, minWidth, maxWidth);
		height = (int) PApplet.constrain(fontSize + textDescent() + paddingTop + paddingBottom, minHeight, maxHeight);
	}
}