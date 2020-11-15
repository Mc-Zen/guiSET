package guiSET.core;


import processing.event.MouseEvent;


/**
 * Switch class derived from Checkbox that looks more like an android switch and has a toggel
 * animation.
 * 
 * @author Mc-Zen
 *
 */
public class Switch extends Checkbox {



	public Switch() {
		this("", false);
	}

	public Switch(String text) {
		this(text, false);
	}

	public Switch(boolean checked) {
		this("", checked);
	}



	public Switch(String text, String toggleEventMethodName) {
		this(text, false);
		setToggleListener(toggleEventMethodName);
	}

	public Switch(String text, String toggleEventMethodName, boolean checked) {
		this(text, checked);
		setToggleListener(toggleEventMethodName);
	}

	public Switch(String text, boolean checked) {
		super(text, checked);

		setUncheckedBackgroundColor(GuisetColor.create(160));
		setCheckedBackgroundColor(GuisetColor.create(46, 116, 122));
		setCheckmarkColor(GuisetColor.create(100, 50, 50));

		// initialize currentPosition
		int checkboxSize = getCheckboxSize();
		currentPosition = (int) (checked ? (1.75 * checkboxSize - (checkboxSize - checkboxSize / 4) / 2 - (checkboxSize / 4) + 1)
				: ((checkboxSize - checkboxSize / 4) / 2 + (checkboxSize / 4) - 1));

		setPaddingLeft((int) (checkboxSize * 1.75f + checkboxSize / 4));
	}



	protected void render() {

		drawDefaultBackground();

		int checkboxSize = getCheckboxSize();

		// draw rounded rectangle as switch background
		pg.noStroke();
		pg.fill(isChecked() ? getCheckedBackgroundColor() : getUncheckedBackgroundColor());
		pg.rect(0, getHeight() / 2 - checkboxSize / 2, 1.75f * checkboxSize, checkboxSize, 100);

		//
		// create smooth animation from one to the other position
		// call update() if animation isn't finished yet
		//

//		int aimedPosition = (int) (checked ? (1.75 * checkboxSize - (checkboxSize - checkboxSize / 4) / 2 - (checkboxSize / 4) + 1)
//				: ((checkboxSize - checkboxSize / 4) / 2 + (checkboxSize / 4) - 1));
//
//		if (Frame.frame0.drawMode == Frame.NO_LOOP) { // no animation with super_eco-mode currentPosition = aimedPosition;
//		} else {
//
//			if (currentPosition < aimedPosition && checked) {
//				currentPosition = Math.min(currentPosition + checkboxSize / 20f, aimedPosition);
//				update();
//			} else if (currentPosition > aimedPosition && !checked) {
//				currentPosition = Math.max(currentPosition - checkboxSize / 20f, aimedPosition);
//				update(); // update again next frame
//			}
//
//		}


		// draw shadow of switch "ball"
		pg.fill(0);
		pg.ellipse(currentPosition, getHeight() / 2 + 1, checkboxSize - checkboxSize / 4, checkboxSize - checkboxSize / 4);

		// draw switch "ball"
		pg.fill(getCheckmarkColor());
		pg.ellipse(currentPosition, getHeight() / 2, checkboxSize - checkboxSize / 4, checkboxSize - checkboxSize / 4);

		// draw text
		drawDefaultText();

		// grey out if switch is disabled
		if (!isEnabled()) {
			pg.noStroke();
			pg.fill(150, 150);
			pg.rect(0, getHeight() / 2 - checkboxSize / 2, 1.75f * checkboxSize, checkboxSize, 100);
		}

	}

	/**
	 * Do not call this function - it is just used internally for the switch animation.
	 * 
	 * @param c c
	 */
	public void setCurrentPosition(float c) {
		this.currentPosition = c;
		update();
	}

	/**
	 * No one needs this function - it is just used internally for the switch animation.
	 * 
	 * @return c
	 */
	public float getCurrentPosition() {
		return currentPosition;
	}

	// used for animating the switch movement
	// stores the position of the switch "ball" for one frame while animating
	protected float currentPosition;

	@Override
	public void setChecked(boolean checked) {
		super.setChecked(checked);
		int checkboxSize = getCheckboxSize();
		int aimedPosition = (int) (checked ? (1.75 * checkboxSize - (checkboxSize - checkboxSize / 4) / 2 - (checkboxSize / 4) + 1)
				: ((checkboxSize - checkboxSize / 4) / 2 + (checkboxSize / 4) - 1));
		animate("currentPosition", aimedPosition, 300);
	}




	@Override
	public void setCheckboxSize(int size) {
		size = Math.max(size, 0);
		this.checkboxSize = size;
		currentPosition = (int) (isChecked() ? (1.75 * size - (size - size / 4) / 2 - (size / 4) + 1) : ((size - size / 4) / 2 + (size / 4) - 1));

		setPaddingLeft(getCheckboxSize() * 2); // (checkboxSize * 1.75f + checkboxSize / 4)
		update();
		autosize();
	}


	@Override
	protected void press(MouseEvent e) {
		if (!isReactToEntireComponent()) {
			int x_ = e.getX() - getOffsetXToWindow();
			int y_ = e.getY() - getOffsetYToWindow();
			if (!(x_ < getCheckboxSize() * 1.75f && y_ > getHeight() / 2 - getCheckboxSize() / 2 && y_ < getHeight() / 2 + getCheckboxSize() / 2))
				return;
		}
		setChecked(!isChecked());
	}
}