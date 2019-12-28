package pGUI.core;


/*
 * Version of Checkbox that uses different looks and a switching animation. 
 */


import pGUI.classes.*;
import processing.core.PApplet;
import processing.event.MouseEvent;

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

	public Switch(String text, boolean checked) {
		super(text, checked);

		uncheckedBackgroundColor = Color.create(160);
		checkedBackgroundColor = Color.create(46, 116, 122);
		checkmarkColor = Color.create(100, 50, 50);

		// initialize currentPosition
		currentPosition = (int) (checked ? (1.75 * size - (size - size / 4) / 2 - (size / 4) + 1) : ((size - size / 4) / 2 + (size / 4) - 1));

	}





	protected void render() {

		if (visualBackgroundColor != 0) {
			pg.background(visualBackgroundColor);
		}

		// draw rounded rectangle as switch background
		pg.noStroke();
		pg.fill(checked ? checkedBackgroundColor : uncheckedBackgroundColor);
		pg.rect(0, height / 2 - size / 2, 1.75f * size, size, 100);

		//
		// create smooth animation from one to the other position
		// call update() if animation isn't finished yet
		//
		int aimedPosition = (int) (checked ? (1.75 * size - (size - size / 4) / 2 - (size / 4) + 1) : ((size - size / 4) / 2 + (size / 4) - 1));

		if (Frame.frame0.drawMode == Frame.SUPER_EFF) {
			// no animation with super_eco-mode
			currentPosition = aimedPosition;
		} else {

			if (currentPosition < aimedPosition && checked) {
				currentPosition = Math.min(currentPosition + size / 20f, aimedPosition);
				update();
			} else if (currentPosition > aimedPosition && !checked) {
				currentPosition = Math.max(currentPosition - size / 20f, aimedPosition);
				update(); // update again next frame
			}

		}

		// draw shadow of switch "ball"
		pg.fill(0);
		pg.ellipse(currentPosition, height / 2 + 1, size - size / 4, size - size / 4);

		// draw switch "ball"
		pg.fill(checkmarkColor);
		pg.ellipse(currentPosition, height / 2, size - size / 4, size - size / 4);

		// draw text
		pg.textSize(fontSize);
		pg.fill(foregroundColor);
		pg.textAlign(37, 3);
		pg.text(text, size * 1.75f + size / 4 + paddingLeft, height / 2);

		// grey out if switch is disabled
		if (!enabled) {
			pg.fill(150, 150);
			pg.rect(0, height / 2 - size / 2, 1.75f * size, size, 100);
		}

	}







	@Override
	protected void autosize() {
		pg = Frame.frame0.papplet.createGraphics(1, 1);
		pg.beginDraw();
		pg.textSize(fontSize);

		// Width: checkbox width + padding + textwidth
		width = (int) (1.75f * size + size / 4 + pg.textWidth(text) + paddingLeft + paddingRight) + 2;
		// Height: (greater of checkobs height or text height) + padding
		height = (int) PApplet.max(size, fontSize + pg.textDescent(), 1) + paddingTop + paddingBottom;
	}


	@Override
	public void setSize(int size) {
		this.size = size;
		currentPosition = (int) (checked ? (1.75 * size - (size - size / 4) / 2 - (size / 4) + 1) : ((size - size / 4) / 2 + (size / 4) - 1));
		autosize();
	}


	@Override
	protected void press(MouseEvent e) {
		if (enabled) {
			if (!reactToEntireField) {
				if (!(e.getX() > bounds.X0 && e.getX() < bounds.X0 + 1.75f * size && e.getY() > bounds.Y0 + height / 2 - size / 2 && e.getY() < bounds.Y0 + height / 2 + size / 2)) 
					return;
			}
			checked = !checked;
			handleRegisteredEventMethod(CHECK_EVENT, null);
			update();
		}
	}
}