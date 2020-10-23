package guiSET.core;

import processing.core.*;
import processing.event.MouseEvent;

/**
 * Basic button template for text or image buttons.
 * 
 * @author Mc-Zen
 *
 */

public class Button extends TextBased {

	protected boolean clickEventOnPress = false; // If true, then click event is fired on press

	public Button() {
		super();
		setPadding(GuisetDefaultValues.buttonPadding);
		setBorderWidth(GuisetDefaultValues.buttonBorderWidth);
		setBackgroundColor(GuisetDefaultValues.buttonBackgroundColor);
		setHoverColor(GuisetDefaultValues.buttonHoverColor);
		setPressedColor(GuisetDefaultValues.buttonPressColor);
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
	 * Constructor for specifying button text, font size an background color.
	 * 
	 * @param text            text
	 * @param fontSize        font size
	 * @param backgroundColor background color
	 */
	public Button(String text, int fontSize, int backgroundColor) {
		this(text);
		this.setFontSize(fontSize);
		this.setBackgroundColor(backgroundColor);
	}

	/**
	 * Constructor for specifying button text and press callback method name.
	 * 
	 * @param text          text
	 * @param clickCallback callback for click event
	 */
	public Button(String text, String clickCallback) {
		this(text);
		addClickListener(clickCallback);
	}

	/**
	 * Constructor for specifying button text and press callback lambda.
	 * 
	 * @param text          text
	 * @param clickCallback callback lambda for click event
	 */
	public Button(String text, Predicate clickCallback) {
		this(text);
		addClickListener(clickCallback);
	}

	/**
	 * Create an image button. Width and height of button are set to given dimensions.
	 * 
	 * @param image  image background image for button
	 * @param width  width for label
	 * @param height height for label
	 */
	public Button(PImage image, int width, int height) {
		setImage(image);
		setSizeWithoutUpdate(width, height);
		setBorderWidth(0);
	}

	/**
	 * Create an image button. Width and height of button will be set to dimensions of given image.
	 * 
	 * @param image background image for button
	 */
	public Button(PImage image) {
		this(image, image.width, image.height);
	}

	/**
	 * Create an image button. Width and height of button will be set to dimensions of image multiplied
	 * by given scale factor.
	 * 
	 * @param image background image for button
	 * @param scale scale factor for image
	 */
	public Button(PImage image, float scale) {
		this(image, (int) (image.width * scale), (int) (image.height * scale));
	}

	/**
	 * Create an image button and provide a mouse press callback method name, width and height of button
	 * will be set to dimensions of given image.
	 * 
	 * @param image         background image for button
	 * @param clickCallback callback for click event
	 */
	public Button(PImage image, String clickCallback) {
		this(image, image.width, image.height);
		addClickListener(clickCallback);
	}

	/**
	 * Create an image button and provide a mouse press callback lambda, width and height of button will
	 * be set to dimensions of given image.
	 * 
	 * @param image         background image for button
	 * @param clickCallback callback lambda for click event
	 */
	public Button(PImage image, Predicate clickCallback) {
		this(image, image.width, image.height);
		addClickListener(clickCallback);
	}

	/**
	 * Create an image button and provide a mouse press callback method name, width and height of button
	 * will be set to dimensions of image multiplied by given scale factor.
	 * 
	 * @param image         background image for button
	 * @param scale         scale factor for image
	 * @param clickCallback callback for click event
	 */
	public Button(PImage image, float scale, String clickCallback) {
		this(image, scale);
		addClickListener(clickCallback);
	}

	/**
	 * Create an image button and provide a mouse press callback lambda, width and height of button will
	 * be set to dimensions of image multiplied by given scale factor.
	 * 
	 * @param image         background image for button
	 * @param scale         scale factor for image
	 * @param clickCallback callback lambda for click event
	 */
	public Button(PImage image, float scale, Predicate clickCallback) {
		this(image, scale);
		addClickListener(clickCallback);
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
	protected int autoHeight() {
		return (int) textHeight(getText()) + getPaddingTop() + getPaddingBottom();
	}

	@Override
	protected int autoWidth() {
		return (int) textWidth(getText()) + getPaddingLeft ()+ getPaddingRight();
	}


	/*
	 * SETTER
	 */

	// automatically set hoverColor and pressedColor
	@Override
	public void setBackgroundColor(int clr) {
		setStatusBackgroundColorsAutomatically(clr);
	}

	/**
	 * The click event is called when the button is pressed down. The click event is either called when
	 * the button is pressed OR when it is released.
	 * 
	 * @see #callClickEventOnRelease()
	 */
	public void callClickEventOnPress() {
		clickEventOnPress = true;
	}

	/**
	 * The click event is called when the button is released (default). The click event is either called
	 * when the button is pressed OR when it is released.
	 * 
	 * @see #callClickEventOnPress()
	 */
	public void callClickEventOnRelease() {
		clickEventOnPress = false;
	}




	/*
	 * EVENTS
	 */
	protected EventListener clickListener;

	/**
	 * Add a listener for when the button has been clicked.
	 * 
	 * @param methodName method name
	 * @param target     object
	 */
	public void addClickListener(String methodName, Object target) {
		clickListener = createEventListener(methodName, target, MouseEvent.class);
	}

	public void addClickListener(String methodName) {
		addClickListener(methodName, getPApplet());
	}

	/**
	 * Add a listener lambda for when the button has been clicked.
	 * 
	 * Event arguments: the {@link Button} whose state has changed
	 * 
	 * @param lambda lambda expression with {@link MouseEvent} parameter
	 */
	public void addClickListener(Predicate1<MouseEvent> lambda) {
		clickListener = new LambdaEventListener1<MouseEvent>(lambda);
	}

	/**
	 * Add a listener lambda for when the button has been clicked.
	 * 
	 * Event arguments: none
	 * 
	 * @param lambda lambda expression
	 */
	public void addClickListener(Predicate lambda) {
		clickListener = new LambdaEventListener(lambda);
	}

	public void removeClickListener() {
		clickListener = null;
	}


	@Override
	protected void press(MouseEvent e) {
		super.press(e);
		if (clickEventOnPress)
			handleEvent(clickListener, e);
	}

	@Override
	protected void release(MouseEvent e) {
		super.release(e);
		if (!clickEventOnPress)
			handleEvent(clickListener, e);
	}
}