
package guiSET.core;


import processing.event.MouseEvent;


/**
 * Standard checkbox that allows to display some text to the right of the checkbox.
 * 
 * Can be checked/unchecked by clicking. If readToEntireField is activated, then the state of the
 * checkbox can be changed not only by clicking on the box itself but also on the entire element
 * including text. All colors can be set individually.
 * 
 * Use the checkedChangedListener to subscribe to any changed in the checked state (happens also
 * when setting the state programatically).
 * 
 * @author Mc-Zen
 *
 */
public class Checkbox extends TextBased {

	protected int uncheckedBackgroundColor;
	protected int checkedBackgroundColor;
	protected int checkmarkColor;
	protected int checkboxSize = 20; // size of checkbox, equals height of the checkbox

	// determines if a mouse click anywhere on this control changes the checked
	// state or only a click on the checkbox itself
	protected boolean reactToEntireField = true;

	protected boolean checked = false;

	public Checkbox() {
		this("", false);
	}

	/**
	 * Initalize checkbox with text.
	 * 
	 * @param text text to display
	 */
	public Checkbox(String text) {
		this(text, false);
	}

	/**
	 * Initialize checkbox with checked state.
	 * 
	 * @param checked checked state
	 */
	public Checkbox(boolean checked) {
		this("", checked);
	}

	/**
	 * Initalize checkbox with text and set checked.
	 * 
	 * @param text    text
	 * @param checked checked
	 */
	public Checkbox(String text, boolean checked) {
		super();

		setBackgroundColor(TRANSPARENT); // tranparent
		setUncheckedBackgroundColor(-6250336); // grey
		setCheckedBackgroundColor(-13732742); // greenish
		setCheckmarkColor(-328966); // almost white
		setText(text);
		setTextAlign(LEFT);
		setPaddingLeft(checkboxSize + checkboxSize / 4);
		setPaddingRight(2);
		this.checked = checked; // no setChecked()
	}

	public Checkbox(String text, String toggleEventMethodName) {
		this(text, false);
		addToggleListener(toggleEventMethodName);
	}

	public Checkbox(String text, String toggleEventMethodName, boolean checked) {
		this(text, checked);
		addToggleListener(toggleEventMethodName);
	}


	@Override
	protected void render() {

		drawDefaultBackground();

		// draw checkbox
		pg.fill(checked ? checkedBackgroundColor : uncheckedBackgroundColor);
		pg.strokeWeight(checkboxSize / 20f);
		pg.stroke(borderColor);
		pg.rect(1, 1 + paddingTop, checkboxSize - 2, checkboxSize - 2, 2);

		// draw check mark
		if (checked) {
			pg.strokeWeight(checkboxSize / 10.0f);
			pg.stroke(checkmarkColor);
			pg.line(checkboxSize / 4, checkboxSize / 2 + paddingTop, checkboxSize / 2.5f, 3 * checkboxSize / 4 + paddingTop);
			pg.line(checkboxSize / 2.5f, 3 * checkboxSize / 4 + paddingTop, 3 * checkboxSize / 4, checkboxSize / 4 + paddingTop);
		}

		// draw text
		drawDefaultText();

		// grey out checkbox if disabled
		if (!enabled) {
			pg.noStroke();
			pg.fill(150, 150);
			pg.rect(1, 1 + paddingTop, checkboxSize - 2, checkboxSize - 2, 2);
		}
	}


//	@Override
//	protected void autosizeRule() {
//		setWidthImpl((int) (textWidth(text) + paddingLeft + paddingRight));
//		setHeightImpl((int) (Math.max(checkboxSize, getFontSize() + textDescent()) + paddingTop + paddingBottom));
//	}
	@Override
	protected int autoHeight() {
		return (int) (Math.max(checkboxSize, textHeight(text)) + paddingTop + paddingBottom);
	}

	@Override
	protected int autoWidth() {
		return (int) textWidth(text) + paddingLeft + paddingRight;
	}




	/*
	 * SETTER
	 */

	/**
	 * Set back color of checkbox when not checked.
	 * 
	 * @param clr unchecked background color
	 */
	public void setUncheckedBackgroundColor(int clr) {
		uncheckedBackgroundColor = clr;
		update();
	}

	/**
	 * Set back color of checkbox when checked.
	 * 
	 * @param clr checked background color
	 */
	public void setCheckedBackgroundColor(int clr) {
		checkedBackgroundColor = clr;
		update();
	}

	/**
	 * Set color of the check mark.
	 * 
	 * @param clr check mark color
	 */
	public void setCheckmarkColor(int clr) {
		checkmarkColor = clr;
		update();
	}

	/**
	 * Set scale factor for the checkbox.
	 * 
	 * @param checkboxSize checkbox scale factor
	 */
	public void setCheckboxSize(int checkboxSize) {
		this.checkboxSize = Math.max(checkboxSize, 0);
		setPaddingLeft(this.checkboxSize + this.checkboxSize / 4);
		update();
		autosize();
	}


	/**
	 * Set checked state.
	 * 
	 * @param checked checked state
	 */
	public void setChecked(boolean checked) {
		if (this.checked == checked)
			return;
		this.checked = checked;
		handleEvent(toggleListener, this);
		update();
	}




	/**
	 * If set to true, pressing anywhere on the element toggles the checkbox, else only the box itself
	 * reacts to clicking.
	 * 
	 * @param reactToEntireField reactToEntireField
	 */
	public void setReactToEntireField(boolean reactToEntireField) {
		this.reactToEntireField = reactToEntireField;
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

	public int getCheckboxSize() {
		return checkboxSize;
	}

	public boolean isChecked() {
		return checked;
	}

	public boolean isReactToEntireComponent() {
		return reactToEntireField;
	}






	/*
	 * EVENTS
	 */

	protected EventListener toggleListener;

	/**
	 * Add a listener for when the checkbox has been checked/unchecked. Only triggered when the user
	 * presses the Checkbox and not if set programatically.
	 * 
	 * @param methodName method name
	 * @param target     object
	 */
	public void addToggleListener(String methodName, Object target) {
		toggleListener = createEventListener(methodName, target, Checkbox.class);
	}

	public void addToggleListener(String methodName) {
		addToggleListener(methodName, getPApplet());
	}

	/**
	 * Add a listener lambda for when the checkbox has been checked/unchecked. Only triggered when
	 * the user presses the Checkbox and not if set programatically.
	 * 
	 * Event arguments: the {@link Checkbox} whose state has changed
	 * 
	 * @param p lambda expression with {@link Checkbox} parameter
	 */
	public void addToggleListener(Predicate1<Checkbox> p) {
		toggleListener = new LambdaEventListener1<Checkbox>(p);
	}

	/**
	 * Add a listener lambda for when the checkbox has been checked/unchecked. Only triggered when
	 * the user presses the Checkbox and not if set programatically.
	 * 
	 * Event arguments: none
	 * 
	 * @param p lambda expression
	 */
	public void addToggleListener(Predicate p) {
		toggleListener = new LambdaEventListener(p);
	}

	public void removeToggleListener() {
		toggleListener = null;
	}




	@Override
	protected void press(MouseEvent e) {
		if (!reactToEntireField) {
			int x_ = e.getX() - getOffsetXWindow();
			int y_ = e.getY() - getOffsetYWindow();
			if (!(x_ < checkboxSize && y_ > paddingTop && y_ < checkboxSize + paddingTop))
				return;
		}
		setChecked(!checked);
	}

}