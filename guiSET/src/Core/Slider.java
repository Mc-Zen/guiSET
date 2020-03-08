package guiSET.core;

/*
 * Slider to select a value between minValue and maxValue with the mouse (through dragging or clicking).
 * 
 * Orientation can be set to vertical or horizontal. 
 */


import processing.event.*;


/**
 * A value slider/progress bar.
 * 
 * 
 * @author Mc-Zen
 *
 */
public class Slider extends Control {

	protected float value;
	protected float minValue = 0;
	protected float maxValue = 100;





	protected int thickness = 4;
	protected int ballSize = 15;

	public static final int HORIZONTAL = 0;
	public static final int VERTICAL = 1;
	// is it a vertical or horzontal slider?
	protected int orientation = HORIZONTAL;

	protected boolean wheelEnabled = false;
	protected int scrollSpeed = 3;




	public Slider() {
		super();
		backgroundColor = -2302756; // some gray
		foregroundColor = -1926085; // orange
		setWidthImpl(400);
		setHeightImpl((int) (ballSize * 1.3));
	}


	public Slider(float minValue, float maxValue) {
		this(minValue, maxValue, 0);
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

		if (orientation == HORIZONTAL) {
			int sliderWidth = width - ballSize - 1; // width - 2 * 0.5 * ballSize - buffer (1)

			int lineHeight = thickness;


			// slider background
			pg.fill(backgroundColor);
			pg.rect(1 + ballSize / 2, height / 2 - lineHeight / 2, sliderWidth, lineHeight);

			// slider active background
			pg.fill(foregroundColor);
			pg.rect(1 + ballSize / 2, height / 2 - lineHeight / 2, (float) (sliderWidth) / Math.abs(maxValue - minValue) * (value - minValue),
					lineHeight);

			// slider ball
			pg.ellipse(1 + (float) (sliderWidth) / Math.abs(maxValue - minValue) * (value - minValue) + ballSize / 2, height / 2, ballSize, ballSize);


		} else {
			int sliderHeight = height - ballSize - 1; // height - 2 * 0.5 * ballSize - buffer (1)

			int lineWidth = thickness;


			// slider background
			pg.fill(backgroundColor);
			pg.rect(width / 2 - lineWidth / 2, 1 + ballSize / 2, lineWidth, sliderHeight);

			// slider active background
			float activePartHeight = (float) (sliderHeight) / Math.abs(maxValue - minValue) * (value - minValue);
			pg.fill(foregroundColor);
			pg.rect(width / 2 - lineWidth / 2, height - (1 + ballSize / 2) - activePartHeight, lineWidth, activePartHeight);

			// slider ball
			pg.ellipse(width / 2, height - (1 + (float) (sliderHeight) / Math.abs(maxValue - minValue) * (value - minValue) + ballSize / 2), ballSize,
					ballSize);

		}
	}



	/*
	 * Getter & Setter
	 */

	/**
	 * Set the value of the slider. This triggers the valueChanged event if value
	 * has actually changed.
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
	 * Set minimum value of the slider. If given value is greater the set maxmium
	 * value, an error message is printed.
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
	 * Set maximum value of the slider. If given value is greater the set minimum
	 * value, an error message is printed.
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
			setHeightImpl((int) (Math.max(thickness, ballSize) * 1.3));
		else
			setWidthImpl((int) (Math.max(thickness, ballSize) * 1.3));
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
		if (orientation == HORIZONTAL)
			setHeightImpl((int) (Math.max(thickness, ballSize) * 1.3));
		else
			setWidthImpl((int) (Math.max(thickness, ballSize) * 1.3));
		update();
	}

	/**
	 * Set orientation to horizontal or vertical
	 * 
	 * @param orientation {@link Slider#HORIZONTAL} or {@link Slider#VERTICAL}
	 */
	public void setOrientation(int orientation) {
		if (orientation == HORIZONTAL) {
			if (this.orientation == VERTICAL) {
				setWidthImpl(height);
				setHeightImpl((int) (Math.max(thickness, ballSize) * 1.3));
			}
			this.orientation = orientation;
		}
		if (orientation == VERTICAL) {
			if (this.orientation == HORIZONTAL) {
				setHeightImpl(width);
				setWidthImpl((int) (Math.max(thickness, ballSize) * 1.3));
			}
			this.orientation = orientation;
		}
		update();
	}


	/**
	 * Specify if scrolling while mouse is over the slider changes the value.
	 * Default is false.
	 * 
	 * @param wheelEnabled wheelEnabled
	 */
	public void setWheelEnabled(boolean wheelEnabled) {
		this.wheelEnabled = wheelEnabled;
	}

	/**
	 * Set the scroll speed a.k.a the amount to scroll if wheelEnabled is set to
	 * true.
	 * 
	 * @param scrollSpeed scrollSpeed
	 */
	public void setScrollSpeed(int scrollSpeed) {
		this.scrollSpeed = scrollSpeed;
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

	public int getOrientation() {
		return orientation;
	}

	public boolean isWheelEnabled() {
		return wheelEnabled;
	}

	public int getScrollSpeed() {
		return scrollSpeed;
	}







	/*
	 * Events
	 */

	protected EventListener valueChangeListener;


	/**
	 * Add a value changed listener. The event is also triggered when the value is
	 * changed programatically.
	 * 
	 * @param methodName methodName
	 * @param target     target
	 */
	public void addValueChangeListener(String methodName, Object target) {
		valueChangeListener = createEventListener(methodName, target, Control.class);
	}

	public void addValueChangeListener(String methodName) {
		addValueChangeListener(methodName, getPApplet());
	}

	public void removeValueChangeListener() {
		valueChangeListener = null;
	}

	
	
	
	
	/*
	 * internal method for setting the actual position of the ball
	 */
	protected void setBallPosition(float position) {
		if (orientation == HORIZONTAL) {
			setValue(((position - ballSize / 2) * Math.abs(maxValue - minValue) / (float) (width - ballSize)) + minValue);
		} else {
			setValue(((height - position - ballSize / 2) * Math.abs(maxValue - minValue) / (float) (height - ballSize)) + minValue);

		}

	}

	protected boolean startedDrag = false;

	@Override
	protected void drag(MouseEvent e) {
		setBallPosition(orientation == HORIZONTAL ? e.getX() - getOffsetXWindow() : e.getY() - getOffsetYWindow());
	}

	@Override
	protected void press(MouseEvent e) {
		setBallPosition(orientation == HORIZONTAL ? e.getX() - getOffsetXWindow() : e.getY() - getOffsetYWindow());
	}

	@Override
	protected void mouseWheel(MouseEvent e) {
		if (wheelEnabled)
			setValue(value + e.getCount() * scrollSpeed);
	}
}


