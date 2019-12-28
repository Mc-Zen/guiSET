
package pGUI.core;


/*
 * Standard checkbox that allows to display some text to the right of the checkbox.
 * 
 * Can be checked/unchecked by clicking. When readToEntireField is activated then the state 
 * of the checkbox can be changed not only by clicking on the box itself but also on the 
 * entire element including text. All colors can be set individually. 
 * 
 * Use the checkedChangedListener to subscribe to any changed in the checked state 
 * (happens also when setting the state programatically). 
 */


import processing.core.*;
import processing.event.*;

public class Checkbox extends Control {

	protected int uncheckedBackgroundColor;
	protected int checkedBackgroundColor;
	protected int checkmarkColor;
	protected int size; // size of switch, equals height of the checkbox

	// determines if a mouse click anywhere on this control changes the checked
	// state or only a click on the checkbox itself
	protected boolean reactToEntireField = true;

	protected boolean checked = false;

	public Checkbox() {
		this("", false);

	}

	public Checkbox(String text) {
		this(text, false);
	}

	public Checkbox(String text, boolean checked) {
		super();
		size = 20;
		height = size;

		setBackgroundColor(0);
		uncheckedBackgroundColor = -6250336; // grey
		checkedBackgroundColor = -13732742; // greenish
		checkmarkColor = -328966; // almost white

		setupListeners(1); // add one additional listener

		activateInternalMouseListener();
		setText(text);
		setChecked(checked);
	}


	protected void render() {

		if (visualBackgroundColor != 0) {
			pg.background(visualBackgroundColor);
		}

		// draw checkbox

		pg.fill(checked ? checkedBackgroundColor : uncheckedBackgroundColor);
		pg.strokeWeight(size / 20f);
		pg.stroke(borderColor);
		pg.rect(1, 1 + paddingTop, size - 2, size - 2, 2);

		// draw check mark

		if (checked) {
			pg.strokeWeight(size / 10.0f);
			pg.stroke(checkmarkColor);
			pg.line(size / 4, size / 2 + paddingTop, size / 2.5f, 3 * size / 4 + paddingTop);
			pg.line(size / 2.5f, 3 * size / 4 + paddingTop, 3 * size / 4, size / 4 + paddingTop);
		}
		pg.noStroke();

		// draw text

		pg.textSize(fontSize);
		pg.fill(foregroundColor);
		pg.textAlign(37, 3);
		pg.text(text, size + paddingLeft + size / 4, height / 2);

		// grey out disabled checkbox

		if (!enabled) {
			pg.fill(150, 150);
			pg.rect(1, 1 + paddingTop, size - 2, size - 2, 2);
		}

	}

	//
	//
	//

	/*
	 * SETTER
	 */

	// back color of checkbox when not checked
	public void setUncheckedBackgroundColor(int clr) {
		uncheckedBackgroundColor = clr;
	}

	// back color of checkbox when checked
	public void setCheckedBackgroundColor(int clr) {
		checkedBackgroundColor = clr;
	}

	// color of the check sign
	public void setCheckmarkColor(int clr) {
		checkmarkColor = clr;
	}

	// zoom factor for the checkbox
	public void setSize(int size) {
		this.size = size;
		autosize();
	}

	@Override
	protected void autosize() {
		pg = Frame.frame0.papplet.createGraphics(1, 1);
		pg.beginDraw();
		pg.textSize(fontSize);

		width = (int) (size + size / 4 + pg.textWidth(text) + paddingLeft + paddingRight) + 2;
		// Height: (greater of checkbox height or text height) + padding
		height = (int) PApplet.max(size, fontSize + pg.textDescent(), 1) + paddingTop + paddingBottom;

	}

	public void setChecked(boolean checked) {
		if (enabled) {
			this.checked = checked;
			handleRegisteredEventMethod(CHECK_EVENT, null);
			update();
		}
	}

	public boolean isReactToEntireField() {
		return reactToEntireField;
	}

	/*
	 * GETTER
	 */

	public int getUncheckedBackgroundColor() {
		return uncheckedBackgroundColor;
	}

	public int getCheckedBackgroundColor() {
		return checkedBackgroundColor;
	}

	public int getCheckmarkColor() {
		return checkmarkColor;
	}

	public int getSize() {
		return size;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setReactToEntireField(boolean reactToEntireField) {
		this.reactToEntireField = reactToEntireField;
	}






	/*
	 * EVENTS
	 */

	protected static final int CHECK_EVENT = Frame.numberMouseListeners;

	public void addCheckedChangedListener(String methodName, Object target) {
		registerEventRMethod(CHECK_EVENT, methodName, target, null);
	}

	public void addCheckedChangedListener(String methodName) {
		addCheckedChangedListener(methodName, Frame.frame0.papplet);
	}

	public void removeCheckedChangedListener() {
		deregisterEventRMethod(CHECK_EVENT);
	}

	protected void press(MouseEvent e) {
		if (enabled) {
			if (!reactToEntireField) {
				if (!(e.getX() > bounds.X0 && e.getX() < bounds.X0 + size && e.getY() > bounds.Y0 + paddingTop && e.getY() < bounds.Y0 + size + paddingTop)) 
					return;
			}
			checked = !checked;
			handleRegisteredEventMethod(CHECK_EVENT, null);
			update();
		}
	}
}