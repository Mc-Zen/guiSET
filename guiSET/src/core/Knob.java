package guiSET.core;

import java.math.RoundingMode;
import java.text.DecimalFormat;

import processing.event.MouseEvent;


/**
 * A value knob similar to the slider. The value can be set by clicking on a
 * part of the outer arc or by dragging the knob either horizontally or
 * vertically, depending on the orientation that has been set (default is
 * horizontal).
 * 
 * The dragging direction behavior can be reversed by setting dragSpeed to
 * negative values.
 * 
 * @author E-Bow
 *
 */
public class Knob extends Slider {



	public Knob() {
		setSize(70, 70);
		setWheelEnabled(true);
		setScrollSpeed(-1);
		setMinValue(-20f);
		setMaxValue(1);
	}

	public Knob(float minValue, float maxValue) {
		this(minValue, maxValue, minValue);
	}

	public Knob(float minValue, float maxValue, float value) {
		this();
		setMinValue(minValue);
		setMaxValue(maxValue);
		setValue(value);
	}
	

	protected void render() {
		float outerCircleRadius = Math.min(getWidth() * 0.8f, getHeight() * 0.8f);
		float valueCircleRadius = outerCircleRadius * 0.9f;
		float innerCircleRadius = outerCircleRadius * 0.6f;


		pg.stroke(getBorderColor());
		pg.strokeWeight(1);
		pg.fill(getBackgroundColor());

		// Knob circle
		pg.ellipse(getWidth() * 0.5f, getHeight() * 0.5f, outerCircleRadius, outerCircleRadius);

		// Little lines for min and max value
		float xx = (float) (0.5f * outerCircleRadius / Math.sqrt(2));
		pg.line(getWidth() * 0.5f - xx, getHeight() * 0.5f + xx, getWidth() * 0.5f - (xx * 0.6f), getHeight() * 0.5f + (xx * 0.6f));
		pg.line(getWidth() * 0.5f + xx, getHeight() * 0.5f + xx, getWidth() * 0.5f + (xx * 0.6f), getHeight() * 0.5f + (xx * 0.6f));
		pg.fill(getForegroundColor());

		// Draw arc in pie style
		float angle = (3 * PI / 2) / Math.abs(maxValue - minValue) * (value - minValue);
		pg.noStroke();
		pg.arc(getWidth() * 0.5f, getHeight() * 0.5f, valueCircleRadius, valueCircleRadius, 3 * PI / 4, 3 * PI / 4 + angle, PIE);

		// Remove center of pie
		pg.fill(getBackgroundColor());
		pg.noStroke();
		pg.ellipse(getWidth() * 0.5f, getHeight() * 0.5f, innerCircleRadius, innerCircleRadius);

		// Draw value as text
		pg.fill(BLACK);
		pg.textAlign(CENTER, CENTER);
		pg.textSize(outerCircleRadius / 5);

		/*
		 * Adapt number of displayed decimal places to length of interval
		 */
		int digits = (int) Math.round(Math.max(0, -Math.log10(getIntervalLength()) + 1));
		String s = "#";
		for (int i = 0; i < digits; ++i) {
			if (i == 0)
				s += ".";
			s += "#";
		}

		DecimalFormat df = new DecimalFormat(s);
		df.setRoundingMode(RoundingMode.HALF_EVEN);
		String text = df.format(value);
		pg.text(text, getWidth() / 2, getHeight() / 2);
	}



	float dragSpeed = 1;

	/**
	 * Set the speed of changing the value through dragging; default (1) allows for
	 * going from min to max when dragging 100px horizontally.
	 * 
	 * @param dragSpeed drag speed
	 */
	public void setDragSpeed(float dragSpeed) {
		this.dragSpeed = dragSpeed;
	}

	public float getDragSpeed() {
		return dragSpeed;
	}


	@Override
	public void setOrientation(Orientation orientation) {
		if (orientation == Orientation.HORIZONTAL) {
			this.orientation = orientation;
		} else {
			this.orientation = orientation;
		}
	}




	int startPos;
	float startValue;


	@Override
	protected void drag(MouseEvent e) {
		int mouse;
		if (orientation == Orientation.HORIZONTAL)
			mouse = e.getX() - getOffsetXWindow() - getWidth() / 2; // 0 is center of knob
		else
			mouse = -(e.getY() - getOffsetYWindow() - getHeight() / 2);
		setValue(startValue + (mouse - startPos) / 100f * getIntervalLength() * dragSpeed);
	}


	@Override
	protected void press(MouseEvent e) {
		int mouseX = e.getX() - getOffsetXWindow() - getWidth() / 2; // 0 is center of knob
		int mouseY = e.getY() - getOffsetYWindow() - getHeight() / 2;		// 0 is center of know

		// Transform into polar coordinates
		double r = Math.sqrt(mouseX * mouseX + mouseY * mouseY);
		double phi = Math.atan((float) -mouseX / mouseY);

		if (mouseY < 0)
			phi -= PI;
		if (phi <= 0)
			phi += 2 * PI;

		// Dont react to center of know and lower part between min and max
		if (r > 15 && phi >= PI / 4 && phi <= 7 * PI / 4) {
			double value = (phi - PI / 4) / (3 * PI / 2) * getIntervalLength() + minValue;
			setValue((float) value);
		}

		// prepare for dragging
		startPos = orientation == Orientation.HORIZONTAL ? mouseX : mouseY;
		startValue = value;
	}

}
