
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

		setBackgroundColor(GuisetColor.TRANSPARENT); // tranparent
		setUncheckedBackgroundColor(GuisetDefaultValues.checkboxUncheckedColor);
		setCheckedBackgroundColor(GuisetDefaultValues.checkboxCheckedColor); 
		setCheckmarkColor(GuisetDefaultValues.checkboxCheckmarkColor); 
		setText(text);
		setTextAlign(Constants.LEFT);
		setPaddingLeft(checkboxSize + checkboxSize / 4);
		setPaddingRight(2);
		this.checked = checked; // no setChecked()
	}

	public Checkbox(String text, String toggleEventMethodName) {
		this(text, false);
		setToggleListener(toggleEventMethodName);
	}
	public Checkbox(String text, Predicate toggleCallback) {
		this(text, false);
		setToggleListener(toggleCallback);
	}

	public Checkbox(String text, String toggleEventMethodName, boolean checked) {
		this(text, checked);
		setToggleListener(toggleEventMethodName);
	}

	public Checkbox(String text, Predicate toggleCallback, boolean checked) {
		this(text, checked);
		setToggleListener(toggleCallback);
	}


	@Override
	protected void render() {

		drawDefaultBackground();

		// draw checkbox
		pg.fill(isChecked() ? checkedBackgroundColor : uncheckedBackgroundColor);
		pg.strokeWeight(checkboxSize / 20f);
		pg.stroke(borderColor);
		pg.rect(1, 1 + getPaddingTop(), checkboxSize - 2, checkboxSize - 2, 2);

		// draw check mark
		if (isChecked()) {
			pg.strokeWeight(checkboxSize / 10.0f);
			pg.stroke(checkmarkColor);
			pg.line(checkboxSize / 4, checkboxSize / 2 + getPaddingTop(), checkboxSize / 2.5f, 3 * checkboxSize / 4 + getPaddingTop());
			pg.line(checkboxSize / 2.5f, 3 * checkboxSize / 4 + getPaddingTop(), 3 * checkboxSize / 4, checkboxSize / 4 + getPaddingTop());
		}

		// draw text
		drawDefaultText();

		// grey out checkbox if disabled
		if (!enabled) {
			pg.noStroke();
			pg.fill(150, 150);
			pg.rect(1, 1 + getPaddingTop(), checkboxSize - 2, checkboxSize - 2, 2);
		}
	}


//	@Override
//	protected void autosizeRule() {
//		setWidthImpl((int) (textWidth(text) + paddingLeft + paddingRight));
//		setHeightImpl((int) (Math.max(checkboxSize, getFontSize() + textDescent()) + paddingTop + paddingBottom));
//	}
	@Override
	protected int autoHeight() {
		return (int) (Math.max(checkboxSize, textHeight(getText())) + getPaddingTop() + getPaddingBottom());
	}

	@Override
	protected int autoWidth() {
		return (int) textWidth(getText()) + getPaddingLeft() + getPaddingRight();
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
	 * Set a listener for when the checkbox has been checked/unchecked. Only triggered when the user
	 * presses the checkbox and not if set programatically.
	 * 
	 * @param methodName method name
	 * @param target     object
	 */
	public void setToggleListener(String methodName, Object target) {
		toggleListener = createEventListener(methodName, target, Checkbox.class);
	}

	public void setToggleListener(String methodName) {
		setToggleListener(methodName, getPApplet());
	}

	/**
	 * Set a listener lambda for when the checkbox has been checked/unchecked. Only triggered when
	 * the user presses the Checkbox and not if set programatically.
	 * 
	 * Event arguments: the {@link Checkbox} whose state has changed
	 * 
	 * @param lambda lambda expression with {@link Checkbox} parameter
	 */
	public void setToggleListener(Predicate1<Checkbox> lambda) {
		toggleListener = new LambdaEventListener1<Checkbox>(lambda);
	}

	/**
	 * Set a listener lambda for when the checkbox has been checked/unchecked. Only triggered when
	 * the user presses the Checkbox and not if set programatically.
	 * 
	 * Event arguments: none
	 * 
	 * @param lambda lambda expression
	 */
	public void setToggleListener(Predicate lambda) {
		toggleListener = new LambdaEventListener(lambda);
	}

	public void removeToggleListener() {
		toggleListener = null;
	}




	@Override
	protected void press(MouseEvent e) {
		if (!reactToEntireField) {
			int x_ = e.getX() - getOffsetXToWindow();
			int y_ = e.getY() - getOffsetYToWindow();
			if (!(x_ < checkboxSize && y_ > getPaddingTop() && y_ < checkboxSize + getPaddingTop()))
				return;
		}
		setChecked(!checked);
	}

}