package pGUI.core;

import processing.event.MouseEvent;

public class MenuSeparator extends MenuItem {
	public MenuSeparator() {
		super();
		height = 5;
	}

	@Override
	protected void render() {

		drawDefaultBackground();
		pg.fill(foregroundColor);

		pg.strokeWeight(1);
		pg.stroke(220);
		pg.line(23, height / 2 - 1, width - 2, height / 2 - 1);
		pg.stroke(255);
		pg.line(23, height / 2, width - 2, height / 2);

	}

	@Override
	protected void enter(MouseEvent e) {
	}

	@Override
	protected void exit(MouseEvent e) {
	}

	@Override
	protected void press(MouseEvent e) {
	}

	@Override
	protected void release(MouseEvent e) {
	}

	@Override
	protected void mouseEvent(MouseEvent e) {
	}
}
