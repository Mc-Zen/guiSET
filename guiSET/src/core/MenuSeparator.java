package guiSET.core;

import processing.event.MouseEvent;


/**
 * Separator line in menus between MenuItems {@link MenuItem}.
 * 
 * @author E-Bow
 *
 */
public class MenuSeparator extends MenuItem {
	public MenuSeparator() {
		super();
		setHeightNoUpdate(5);
	}

	@Override
	protected void render() {
		drawDefaultBackground();
		pg.fill(foregroundColor);

		pg.strokeWeight(1);
		pg.stroke(220);
		pg.line(23, getHeight() / 2 - 1, getWidth() - 2, getHeight() / 2 - 1);
		pg.stroke(255);
		pg.line(23, getHeight() / 2, getWidth() - 2, getHeight() / 2);
	}

	// remove the functionality of all these events from MenuItem
	@Override
	protected void enter(MouseEvent e) {
	}

	@Override
	protected void press(MouseEvent e) {
	}

	@Override
	protected void release(MouseEvent e) {
	}

}
