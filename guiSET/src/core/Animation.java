package guiSET.core;

//import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;




/**
 * Animations enable the user of the library to create transitions of numeric
 * values easily.
 * 
 * Just call the animate(String attribute, int aimedValue, double milliseconds)
 */
public class Animation {

	private Control target;
	private String attributeName;

	private int animationType;  // enable i.e. color animations, which need to be done differently
	private static final int NUMBER = 0;
	private static final int COLOR = 1;

	// number animation steps (frames) the animations needs to complete
	private int numberOfSteps;
	private int counter = 0;


	private double currentValue;

	// for numbers
	private double valueStart;
	private double valueEnd;

	// for colors
	private int a1, r1, g1, b1;
	private int a2, r2, g2, b2;



	private Method setter;	// setter for animated attribute
	private Invoker invoker;

	/**
	 * Create a new animation. The time in milliseconds will not be precise though.
	 * At the moment this is only estimated through the current framerate of the
	 * sketch.
	 * 
	 * 
	 * @param attributeName Attribute to animate as String
	 * @param target        Object on which to perform the animation
	 * @param aimedValue    Final value
	 * @param milliseconds  Time to perform the animation in.
	 */
	public Animation(String attributeName, Control target, float aimedValue, double milliseconds) {

		if (attributeName.length() == 0) {
			System.err.println("Animation error: There is no attribute \"" + attributeName + "\"");
			cancelAnimation();
			return;
		}

		this.target = target;

		if (milliseconds < 100000 && milliseconds >= 0) { // prevent very long animations, or negative ones

			try {
				// try to find the field in the object.

				// Field field = getField(target.getClass(), attributeName);
				// String fieldType = field.getType().toString();
				String attributeNameFirstCharCaps = java.lang.Character.toUpperCase(attributeName.charAt(0)) + attributeName.substring(1);

				// We need the getter to retrieve the attribute type
				Method getter = target.getClass().getMethod("get" + attributeNameFirstCharCaps);
				String variableType = getter.getReturnType().toString();

				// Only int, short, float, double and long areanimatable
				switch (variableType) {
				case "int":
					invoker = new IntInvoker();
					break;
				case "float":
					invoker = new FloatInvoker();
					break;
				case "double":
					invoker = new DoubleInvoker();
					break;
				case "long":
					invoker = new LongInvoker();
					break;
				case "short":
					invoker = new ShortInvoker();
					break;
				default:
					System.err.println("The property \"" + attributeName + "\" is not animatable");
					cancelAnimation();
					return;
				}

				// get setter
				setter = target.getClass().getMethod("set" + attributeNameFirstCharCaps, getter.getReturnType());


				this.attributeName = attributeName;


				// get initial value of this attribute
				// currentValue = field.getDouble(target);
				currentValue = ((Number) getter.invoke(target)).doubleValue();


				// Calculate needed number of frames to complete animation, never less than 1!
				// If not looping, the papplet.frameRate tells us nothing about time
				float frameRate = Control.getFrame().getRefreshMode() == Frame.RefreshMode.NO_LOOP ? 60 : Control.getPApplet().frameRate;
				numberOfSteps = (int) Math.max(1, (frameRate * milliseconds / 1000));


				// colors need to be animate differently than ordinary numerics
				if (attributeName.contains("Color")) { // all color attributes actually have the substring "Color" in them (:
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
				}



			} catch (IllegalAccessException iae) {
				System.err.println("Animation error: The attribute \"" + attributeName + "\" is not accessible");
				cancelAnimation();
			} catch (NoSuchMethodException e) {
				System.err.println("Animation error: There is no attribute \"" + attributeName + "\"");
				cancelAnimation();
			} catch (IllegalArgumentException e) {
				System.err.println("Animation error: There is no attribute \"" + attributeName + "\"");
				cancelAnimation();
			} catch (InvocationTargetException e) {
				System.err.println("Animation error: There is no attribute \"" + attributeName + "\"");
				cancelAnimation();
			}
		}
	}

	/**
	 * Cancel the animation next frame.
	 */
	public void cancelAnimation() {
		cancel = true;
	}

	private boolean cancel = false;


	/**
	 * Animation process, called by {@link Frame}
	 * 
	 * @return false if animation finished
	 */

	protected boolean animate() {
		if (cancel)
			return false;


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
			counter++;
		} else {
			return false;      // end animation with false, which clears it off animation queue (in Frame)
		}

		// set value
		try {
			invoker.invoke();
		} catch (IllegalAccessException ie) {
			ie.printStackTrace();
		} catch (InvocationTargetException te) {
			te.printStackTrace();
		}

		return true;
	}

	// Base wrapper class for invoking the setter with differenz types
	abstract class Invoker {
		abstract void invoke() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;
	}

	class IntInvoker extends Invoker {
		@Override
		void invoke() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			setter.invoke(target, (int) currentValue);
		}
	}

	class FloatInvoker extends Invoker {
		@Override
		void invoke() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			setter.invoke(target, (float) currentValue);
		}
	}

	class DoubleInvoker extends Invoker {
		@Override
		void invoke() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			setter.invoke(target, currentValue);
		}
	}

	class LongInvoker extends Invoker {
		@Override
		void invoke() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			setter.invoke(target, (long) currentValue);
		}
	}

	class ShortInvoker extends Invoker {
		@Override
		void invoke() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			setter.invoke(target, (short) currentValue);
		}
	}




//	// get even protected fields
//	private Field getField(Class<?> class_, String fieldName) throws NoSuchFieldException {
//		try {
//
//			return class_.getDeclaredField(fieldName);
//
//		} catch (NoSuchFieldException nsfe) {
//
//			Class<?> superClass = class_.getSuperclass();
//
//			if (superClass == null) {
//				throw nsfe;
//			} else {
//				return getField(superClass, fieldName);
//			}
//		}
//	}



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