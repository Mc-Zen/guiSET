package pGUI.core;

/*
 * Slider to select a value between minValue and maxValue with the mouse (through dragging or clicking)
 * 
 * Orientation can be set to vertical or horizontal. 
 */


import processing.event.*;

public class Slider extends Control {

	protected float value;
	protected float minValue = 0;
	protected float maxValue = 200;









	// zoom size
	protected int size = 15;
	protected int ballSize = 15;

	public static final int HORIZONTAL = 0;
	public static final int VERTICAL = 1;
	// is it a vertical or horzontal slider?
	protected int orientation = HORIZONTAL;

	protected boolean wheelEnabled = false;
	protected int scrollSpeed = 3;




	public Slider() {
		super();
		width = 400;

		backgroundColor = -2302756; // some gray
		foregroundColor = -1926085; // orange

		height = (int) (size * 1.3);

		setupListeners(1);

		activateInternalMouseListener();
	}

	public Slider(float minValue, float maxValue) {
		this();
		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	public Slider(float minValue, float maxValue, float value) {
		this();
		this.minValue = minValue;
		this.maxValue = maxValue;
		setValue(value);
	}

	@Override
	protected void render() {
		pg.noStroke();

		if (orientation == HORIZONTAL) {
			int sliderWidth = width - ballSize - 1; // width - 2 * 0.5 * ballSize - buffer (1)

			int lineHeight = size / 4;


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

			int lineWidth = size / 4;


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

	public void setValue(float value) {
		float tempValue = this.value;
		this.value = value >= minValue ? (value <= maxValue ? value : maxValue) : minValue;


		// only invoke registered method if value really changed (might not because of
		// constraining)
		if (tempValue != this.value) {
			handleRegisteredEventMethod(CHANGE_EVENT, null);
		}
		update();
	}

	public void setMinValue(float value) {
		if (value < maxValue) {
			minValue = value;
			update();
		} else {
			System.out.println("minValue can't be larger than maxValue");
		}
	}

	public void setMaxValue(float value) {
		if (value > minValue) {
			maxValue = value;
			update();
		} else {
			System.out.println("minValue can't be larger than maxValue");
		}
	}

	public void setSize(int size) {
		this.size = size;
		this.ballSize = size;

		// adjust height
		if (orientation == HORIZONTAL)
			height = (int) (size * 1.3);
		else
			width = (int) (size * 1.3);
		update();
	}

	public void setBallSize(int ballSize) {
		this.ballSize = ballSize;

		// adjust height to larger of Size and BallSize
		if (orientation == HORIZONTAL)
			height = (int) (Math.max(size, ballSize) * 1.3);
		else
			width = (int) (Math.max(size, ballSize) * 1.3);
		update();
	}

	public void setOrientation(int orientation) {
		if (orientation == HORIZONTAL) {
			if (this.orientation == VERTICAL) {
				width = height;
				height = (int) (Math.max(size, ballSize) * 1.3);
			}
			this.orientation = orientation;
		}
		if (orientation == VERTICAL) {
			if (this.orientation == HORIZONTAL) {
				height = width;
				width = (int) (Math.max(size, ballSize) * 1.3);
			}
			this.orientation = orientation;
		}
		update();
	}


	public void setWheelEnabled(boolean wheelEnabled) {
		this.wheelEnabled = wheelEnabled;
	}

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

	public int getSize() {
		return size;
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

	protected static final int CHANGE_EVENT = numberMouseListeners;

	public void addValueChangedListener(String methodName, Object target) {
		registerEventRMethod(CHANGE_EVENT, methodName, target, null);
	}

	public void addValueChangedListener(String methodName) {
		addValueChangedListener(methodName, Frame.frame0.papplet);
	}

	public void removeValueChangedListener() {
		deregisterEventRMethod(CHANGE_EVENT);
	}

	protected void setBallPosition(float position) {
		if (orientation == HORIZONTAL) {
			setValue(((position - bounds.X0 - ballSize / 2) * Math.abs(maxValue - minValue) / (float) (width - ballSize)) + minValue);
		} else {
			setValue(((height - (position - bounds.Y0) - ballSize / 2) * Math.abs(maxValue - minValue) / (float) (height - ballSize)) + minValue);

		}

	}

	/*
	 * Special mouseEvent method that enables starting to drag within the slider and
	 * continue dragging when mouse leaves the slider while dragging
	 * 
	 * @see pGUI.core.Control#mouseEvent(processing.event.MouseEvent)
	 */

	protected boolean startedDrag = false;

	@Override
	protected void drag(MouseEvent e) {
		setBallPosition(orientation == HORIZONTAL ? e.getX() : e.getY());
	}

	@Override
	protected void press(MouseEvent e) {
		setBallPosition(orientation == HORIZONTAL ? e.getX() : e.getY());
	}

	@Override
	protected void mouseWheel(MouseEvent e) {
		if (wheelEnabled)
			setValue(value + e.getCount() * scrollSpeed);
	}
}