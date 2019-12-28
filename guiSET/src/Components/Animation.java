package pGUI.core;

import java.lang.reflect.Field;
import pGUI.classes.*;


/**
 * Animations enable the user of the library to create transitions of numeric
 * values easily.
 * 
 * Just call the animate(String attribute, int aimedValue, double milliseconds
 */
public class Animation {

	protected Object target;
	protected Field field;
	protected String attributeName;
	protected String fieldType;   // "int", "double", "float" etc.

	protected int animationType;  // enable i.e. color animations
	static final protected int NUMBER = 0;
	static final protected int COLOR = 1;

	// number animation steps (frames) the animations needs to complete
	protected int numberOfSteps;
	protected int counter = 0;


	protected double currentValue;

	// for numbers
	protected double valueStart;
	protected double valueEnd;

	// for colors
	protected int a1, r1, g1, b1;
	protected int a2, r2, g2, b2;

	protected boolean needToCallAutosize;




	/**
	 * Create a new animation. The time in milliseconds will not be precise though.
	 * At the moment this is only estimated through the current framerate of the
	 * sketch.
	 * 
	 * 
	 * @param attribute    Attribute to animate as String
	 * @param target       Object on which to perform the animation
	 * @param aimedValue   Final value
	 * @param milliseconds Time to perform the animation in.
	 */
	public Animation(String attribute, Object target, float aimedValue, double milliseconds) {

		this.target = target;

		if (milliseconds < 100000 && milliseconds >= 0) { // prevent very long animations, or negative ones

			try {
				// try to find the field in the object.
				field = getField(target.getClass(), attribute);
				fieldType = field.getType().toString();

				// Make it accessible even if its private or protected.
				field.setAccessible(true);

				this.attributeName = attribute;


				// get initial value of this attribute
				currentValue = field.getDouble(target);

				// calculate needed number of frames to complete animation, never less than 1!
				numberOfSteps = (int) Math.max(1, (Frame.frame0.papplet.frameRate * milliseconds / 1000));


				// colors need to be animate differently than ordinary numerics
				if (attribute.contains("Color")) { // all color attributes have the substring "Color" in them (:
					animationType = COLOR;
					a1 = ((int) currentValue >> 24) & 0xff;
					r1 = ((int) currentValue >> 16) & 0xff;
					g1 = ((int) currentValue >> 8) & 0xff;
					b1 = ((int) currentValue) & 0xff;

					a2 = ((int) aimedValue >> 24) & 0xff;
					r2 = ((int) aimedValue >> 16) & 0xff;
					g2 = ((int) aimedValue >> 8) & 0xff;
					b2 = ((int) aimedValue) & 0xff;
					//
				} else {
					animationType = NUMBER;
					this.valueStart = currentValue;
					this.valueEnd = aimedValue;
					/*
					 * switch(attribute) { case "MinHeight": needToCallAutosize = true; break; case
					 * "Maxheight": needToCallAutosize = true; break; case "MinWidth":
					 * needToCallAutosize = true; break; case "MaxWidth": needToCallAutosize = true;
					 * break; case "FontSize": needToCallAutosize = true; break; case "Text":
					 * needToCallAutosize = true; break; case "PaddingTop": needToCallAutosize =
					 * true; break; case "PaddingBottom": needToCallAutosize = true; break; case
					 * "PaddingRight": needToCallAutosize = true; break; case "PaddingLeft":
					 * needToCallAutosize = true; break; }
					 */
				}
			} catch (NoSuchFieldException nsfe) {
				nsfe.printStackTrace();
			} catch (IllegalAccessException iae) {
				iae.printStackTrace();
			}
		}
	}






	/*
	 * animation process
	 */

	protected boolean animate() {

		// check if there's still work to do
		if (counter <= numberOfSteps) {
			switch (animationType) {
			case NUMBER:
				currentValue = valueStart + (valueEnd - valueStart) / (float) numberOfSteps * counter;
				break;

			case COLOR:
				double ac = (a1 + (a2 - a1) / (float) numberOfSteps * counter);
				double rc = (r1 + (r2 - r1) / (float) numberOfSteps * counter);
				double gc = (g1 + (g2 - g1) / (float) numberOfSteps * counter);
				double bc = (b1 + (b2 - b1) / (float) numberOfSteps * counter);

				currentValue = Color.create((int) rc, (int) gc, (int) bc, (int) ac);
				break;
			}
		} else {
			return false;      // end animation with false, which clears it off animation queue (in Frame)
		}

		counter++;

		// set value
		try {
			switch (fieldType) {
			case "int":
				field.setInt(target, (int) currentValue);
				break;
			case "float":
				field.setInt(target, (int) currentValue);
				break;
			case "double":
				field.setInt(target, (int) currentValue);
				break;
			case "long":
				field.setInt(target, (int) currentValue);
				break;
			}
		} catch (IllegalAccessException ia) {
			ia.printStackTrace();
		}
		// if (needToCallAutosize) { ((Control)target).autosize();

		((Control) target).update();
		return true;
	}


	// get even protected fields
	private Field getField(Class<?> classs, String fieldName) throws NoSuchFieldException {
		try {

			return classs.getDeclaredField(fieldName);

		} catch (NoSuchFieldException nsfe) {

			Class<?> superClass = classs.getSuperclass();

			if (superClass == null) {
				throw nsfe;
			} else {
				return getField(superClass, fieldName);
			}
		}
	}


	/*
	 * comparing function, returns true if it's the same attribute on the sameobject
	 * (we don't want more than one animation at a time with equal target and
	 * attribute
	 */

	public boolean compare(Animation other) {
		if (this.attributeName.equals(other.attributeName) && this.target == other.target)
			return true;
		else
			return false;
	}
}