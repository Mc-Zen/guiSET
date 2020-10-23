package guiSET.core;

/*
 * Slider to select a value between minValue and maxValue with the mouse (through dragging or clicking).
 * 
 * Orientation can be set to vertical or horizontal. 
 */

import processing.event.*;


/**
 * A value slider/progress bar based on the data type float.
 * 
 * The orientation can either be set to horizontally or vertically. The value is changed through
 * dragging or clicking on a position on the slider. Also, by setting
 * {@link #setWheelEnabled(boolean)}, the mouse wheel can be used to increment or decrement the
 * value stepwise (amount can be set through scrollSpeed).
 * 
 * 
 * @author Mc-Zen
 *
 */
public class Slider extends Control {

	protected float value;
	protected float minValue;
	protected float maxValue;


	public enum Orientation {
		HORIZONTAL, VERTICAL
	}


	protected int thickness = 4;
	protected int ballSize = 15;

	public static final Orientation HORIZONTAL = Orientation.HORIZONTAL;
	public static final Orientation VERTICAL = Orientation.VERTICAL;
	// is it a vertical or horzontal slider?
	protected Orientation orientation = Orientation.HORIZONTAL;

	protected boolean wheelEnabled = true;
	protected float relativeWheelSpeed = GuisetDefaultValues.sliderRelativeWheelSpeed; // Percentage-based scroll speed. Value of 1 means from min to max in one wheel
																					 // movement




	public Slider() {
		super();
		setBackgroundColor(GuisetDefaultValues.sliderBackgroundColor); // some gray
		setForegroundColor(GuisetDefaultValues.sliderForegroundColor); // orange
		setMinValue(GuisetDefaultValues.sliderMinValue);
		setMaxValue(GuisetDefaultValues.sliderMaxValue);
		setValue(GuisetDefaultValues.sliderMinValue);
		setSizeWithoutUpdate(150, (int) (ballSize * 1.3));
		setPadding(3, 0);
	}


	public Slider(float minValue, float maxValue) {
		this(minValue, maxValue, minValue);
	}

	public Slider(float minValue, float maxValue, float value) {
		this();
		setMinValue(minValue);
		setMaxValue(maxValue);
		setValue(value);
	}


	@Override
	protected void render() {
		pg.noStroke();
		float intervalLength = getIntervalLength();

		if (orientation == HORIZONTAL) {
			int sliderWidth = getWidth() - ballSize - 1; // width - 2 * 0.5 * ballSize - buffer (1)

			int lineHeight = thickness;


			// slider background
			pg.fill(getBackgroundColor());
			pg.rect(1 + ballSize / 2, getHeight() / 2 - lineHeight / 2, sliderWidth, lineHeight);

			// slider active background
			pg.fill(getForegroundColor());
			pg.rect(1 + ballSize / 2, getHeight() / 2 - lineHeight / 2, (float) (sliderWidth) / intervalLength * (value - minValue), lineHeight);

			// slider ball
			pg.ellipse(1 + (float) (sliderWidth) / intervalLength * (value - minValue) + ballSize / 2, getHeight() / 2, ballSize, ballSize);


		} else {
			int sliderHeight = getHeight() - ballSize - 1; // height - 2 * 0.5 * ballSize - buffer (1)

			int lineWidth = thickness;


			// slider background
			pg.fill(getBackgroundColor());
			pg.rect(getWidth() / 2 - lineWidth / 2, 1 + ballSize / 2, lineWidth, sliderHeight);

			// slider active background
			float activePartHeight = (float) (sliderHeight) / intervalLength * (value - minValue);
			pg.fill(getForegroundColor());
			pg.rect(getWidth() / 2 - lineWidth / 2, getHeight() - (1 + ballSize / 2) - activePartHeight, lineWidth, activePartHeight);

			// slider ball
			pg.ellipse(getWidth() / 2, getHeight() - (1 + (float) (sliderHeight) / intervalLength * (value - minValue) + ballSize / 2), ballSize, ballSize);

		}
	}



	/*
	 * Getter & Setter
	 */

	/**
	 * Set the value of the slider. This triggers the valueChanged event if value has actually changed.
	 * 
	 * @param value value
	 */
	public void setValue(float value) {
		float tempValue = this.value;
		this.value = value >= minValue ? (value <= maxValue ? value : maxValue) : minValue;


		// only invoke registered method if value really changed (might not because of
		// constraining)
		if (tempValue != this.value) {
			handleEvent(valueChangeListener, this);
		}
		update();
	}

	/**
	 * Set minimum value of the slider. If given value is greater the set maxmium value, an error
	 * message is printed.
	 * 
	 * @param minValue minValue
	 */
	public void setMinValue(float minValue) {
		if (minValue <= maxValue) {
			this.minValue = minValue;
			setValue(value); // might need constraining
			update();
		} else {
			System.out.println("minValue can't be larger than maxValue");
		}
	}

	/**
	 * Set maximum value of the slider. If given value is greater the set minimum value, an error
	 * message is printed.
	 * 
	 * @param maxValue maxValue
	 */
	public void setMaxValue(float maxValue) {
		if (maxValue >= minValue) {
			this.maxValue = maxValue;
			setValue(value); // might need constraining
			update();
		} else {
			System.out.println("maxValue can't be less than minValue");
		}
	}

	/**
	 * Set the thickness of the slider bar in pixel.
	 * 
	 * @param thickness thickness
	 */
	public void setThickness(int thickness) {
		this.thickness = thickness;
		if (orientation == HORIZONTAL)
			setHeightNoUpdate((int) (Math.max(thickness, ballSize) * 1.3));
		else
			setWidthNoUpdate((int) (Math.max(thickness, ballSize) * 1.3));
		update();
	}


	/**
	 * Set diameter of the slider value "ball".
	 * 
	 * @param ballSize ballSize
	 */
	public void setBallSize(int ballSize) {
		this.ballSize = ballSize;

		// adjust height to larger of Size and BallSize
		if (orientation == Orientation.HORIZONTAL)
			setHeightNoUpdate((int) (Math.max(thickness, ballSize) * 1.3));
		else
			setWidthNoUpdate((int) (Math.max(thickness, ballSize) * 1.3));
		update();
	}

	/**
	 * Set orientation to horizontal or vertical
	 * 
	 * @param orientation {@link Slider#HORIZONTAL} or {@link Slider#VERTICAL}
	 */
	public void setOrientation(Orientation orientation) {
		if (orientation == Orientation.HORIZONTAL) {
			if (this.orientation == Orientation.VERTICAL) {
				setWidthNoUpdate(getHeight());
				setHeightNoUpdate((int) (Math.max(thickness, ballSize) * 1.3));
			}
			this.orientation = orientation;
		} else {
			if (this.orientation == Orientation.HORIZONTAL) {
				setHeightNoUpdate(getWidth());
				setWidthNoUpdate((int) (Math.max(thickness, ballSize) * 1.3));
			}
			this.orientation = orientation;
		}
		update();
	}


	/**
	 * Specify if scrolling while mouse is over the slider changes the value. Default is true.
	 * 
	 * @param wheelEnabled wheelEnabled
	 */
	public void setWheelEnabled(boolean wheelEnabled) {
		this.wheelEnabled = wheelEnabled;
	}

	/**
	 * Set the speed for changing the value with the mouse wheel; the value represents the relative
	 * speed in percent of the set interval length (abs(min-max)). The default value of 0.02 changes the
	 * current value by 2 percent of the interval length. The user needs to turn his/her mouse wheel 50
	 * ticks to get from minimum value to maximum value.
	 * 
	 * @param setRelativeWheelSpeed setRelativeWheelSpeed
	 */
	public void setRelativeWheelSpeed(float setRelativeWheelSpeed) {
		this.relativeWheelSpeed = setRelativeWheelSpeed;
	}




	public float getValue() {
		return value;
	}

	public float getMinValue() {
		return minValue;
	}

	public float getMaxValue() {
		return maxValue;
	}

	public int getThickness() {
		return thickness;
	}

	public int getBallSize() {
		return ballSize;
	}

	public Orientation getOrientation() {
		return orientation;
	}

	public boolean isWheelEnabled() {
		return wheelEnabled;
	}

	public float getRelativeWheelSpeed() {
		return relativeWheelSpeed;
	}







	/*
	 * Events
	 */

	protected EventListener valueChangeListener;


	/**
	 * Add a value-change listener. The event is also triggered when the value is changed
	 * programatically.
	 * 
	 * @param methodName methodName
	 * @param target     target
	 */
	public void addValueChangeListener(String methodName, Object target) {
		valueChangeListener = createEventListener(methodName, target, Slider.class);
	}

	public void addValueChangeListener(String methodName) {
		addValueChangeListener(methodName, getPApplet());
	}

	/**
	 * Add a value-change listener lambda. The event is also triggered when the value is changed
	 * programatically.
	 * 
	 * Event arguments: the {@link Slider} whose state has changed
	 * 
	 * @param lambda lambda expression with {@link Slider} parameter
	 */
	public void addValueChangeListener(Predicate1<Slider> lambda) {
		valueChangeListener = new LambdaEventListener1<Slider>(lambda);
	}

	/**
	 * Add a value-changed lambda listener. The event is also triggered when the value is changed
	 * programatically.
	 * 
	 * Event arguments: none
	 * 
	 * @param lambda lambda expression
	 */
	public void addValueChangeListener(Predicate lambda) {
		valueChangeListener = new LambdaEventListener(lambda);
	}

	public void removeValueChangeListener() {
		valueChangeListener = null;
	}


	protected float getIntervalLength() {
		return Math.abs(maxValue - minValue);
	}



	/*
	 * internal method for setting the actual position of the ball
	 */
	protected void setBallPosition(float position) {
		if (orientation == Orientation.HORIZONTAL) {
			setValue(((position - ballSize / 2) * getIntervalLength() / (float) (getWidth() - ballSize)) + minValue);
		} else {
			setValue(((getHeight() - position - ballSize / 2) * getIntervalLength() / (float) (getHeight() - ballSize)) + minValue);

		}

	}

	@Override
	protected void drag(MouseEvent e) {
		setBallPosition(orientation == Orientation.HORIZONTAL ? e.getX() - getOffsetXToWindow() : e.getY() - getOffsetYToWindow());
	}

	@Override
	protected void press(MouseEvent e) {
		setBallPosition(orientation == Orientation.HORIZONTAL ? e.getX() - getOffsetXToWindow() : e.getY() - getOffsetYToWindow());
	}

	@Override
	protected void mouseWheel(MouseEvent e) {
		int delta = e.getCount() < 0 ? 1 : -1;
		if (wheelEnabled)
			setValue(value + delta * relativeWheelSpeed * getIntervalLength());
	}
}



