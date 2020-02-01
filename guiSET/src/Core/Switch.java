package guiSET.core;


import guiSET.classes.*;
import processing.core.PApplet;
import processing.event.MouseEvent;


/**
 * Switch class derived from Checkbox that looks more like an android switch and
 * has a toggeling animation.
 * 
 * @author Mc-Zen
 *
 */
public class Switch extends Checkbox {

	// used for animating the switch movement
	// stores the position of the switch "ball" for one frame while animating
	private float currentPosition;





	public Switch() {
		this("", false);
	}

	public Switch(String text) {
		this(text, false);
	}

	public Switch(boolean checked) {
		this("", checked);
	}

	public Switch(String text, boolean checked) {
		super(text, checked);

		uncheckedBackgroundColor = Color.create(160);
		checkedBackgroundColor = Color.create(46, 116, 122);
		checkmarkColor = Color.create(100, 50, 50);

		// initialize currentPosition
		currentPosition = (int) (checked ? (1.75 * checkboxSize - (checkboxSize - checkboxSize / 4) / 2 - (checkboxSize / 4) + 1) : ((checkboxSize - checkboxSize / 4) / 2 + (checkboxSize / 4) - 1));

	}





	protected void render() {

		if (visualBackgroundColor != 0) {
			pg.background(visualBackgroundColor);
		}

		// draw rounded rectangle as switch background
		pg.noStroke();
		pg.fill(checked ? checkedBackgroundColor : uncheckedBackgroundColor);
		pg.rect(0, height / 2 - checkboxSize / 2, 1.75f * checkboxSize, checkboxSize, 100);

		//
		// create smooth animation from one to the other position
		// call update() if animation isn't finished yet
		//
		int aimedPosition = (int) (checked ? (1.75 * checkboxSize - (checkboxSize - checkboxSize / 4) / 2 - (checkboxSize / 4) + 1) : ((checkboxSize - checkboxSize / 4) / 2 + (checkboxSize / 4) - 1));

		if (Frame.frame0.drawMode == Frame.SUPER_EFF) {
			// no animation with super_eco-mode
			currentPosition = aimedPosition;
		} else {

			if (currentPosition < aimedPosition && checked) {
				currentPosition = Math.min(currentPosition + checkboxSize / 20f, aimedPosition);
				update();
			} else if (currentPosition > aimedPosition && !checked) {
				currentPosition = Math.max(currentPosition - checkboxSize / 20f, aimedPosition);
				update(); // update again next frame
			}

		}

		// draw shadow of switch "ball"
		pg.fill(0);
		pg.ellipse(currentPosition, height / 2 + 1, checkboxSize - checkboxSize / 4, checkboxSize - checkboxSize / 4);

		// draw switch "ball"
		pg.fill(checkmarkColor);
		pg.ellipse(currentPosition, height / 2, checkboxSize - checkboxSize / 4, checkboxSize - checkboxSize / 4);

		// draw text
		pg.textSize(fontSize);
		pg.fill(foregroundColor);
		pg.textAlign(37, 3); // LEFT, CENTER
		pg.text(text, checkboxSize * 1.75f + checkboxSize / 4 + paddingLeft, height / 2 - 0.07f * fontSize);

		// grey out if switch is disabled
		if (!enabled) {
			pg.fill(150, 150);
			pg.rect(0, height / 2 - checkboxSize / 2, 1.75f * checkboxSize, checkboxSize, 100);
		}

	}







	@Override
	protected void autosize() {
		width = (int) PApplet.constrain(1.75f * checkboxSize + checkboxSize / 4 + textWidth(text) + paddingLeft + paddingRight, minWidth, maxWidth);
		height = (int) PApplet.constrain(PApplet.max(checkboxSize, fontSize + textDescent(), 1) + paddingTop + paddingBottom, minHeight, maxHeight);

	}


	@Override
	public void setCheckboxSize(int size) {
		this.checkboxSize = size;
		currentPosition = (int) (checked ? (1.75 * size - (size - size / 4) / 2 - (size / 4) + 1) : ((size - size / 4) / 2 + (size / 4) - 1));
		autosize();
	}


	@Override
	protected void press(MouseEvent e) {
		if (enabled) {
			if (!reactToEntireField) {
				if (!(e.getX() > bounds.X0 && e.getX() < bounds.X0 + 1.75f * checkboxSize && e.getY() > bounds.Y0 + height / 2 - checkboxSize / 2
						&& e.getY() < bounds.Y0 + height / 2 + checkboxSize / 2))
					return;
			}
			checked = !checked;
			handleRegisteredEventMethod(CHECK_EVENT, null);
			update();
		}
	}
}