package guiSET.core;


import processing.core.*;
import processing.event.MouseEvent;


/**
 * Standard container for top bar menu strips. Sets automatic anchors, special
 * height and background greyish gradient.
 * 
 * @author E-Bow
 *
 */
public class MenuBar extends HFlowContainer {

	public MenuBar() {
		setGradient(Color.create(230), Color.create(200));
		setZ(MenuItem.MenuZIndex);
		setHeightImpl(MenuItem.MENUITEM_HEIGHT);
	}

	@Override
	protected void addedToParent() {
		setWidthImpl(parent.width);
		addAutoAnchors(PApplet.LEFT, PApplet.RIGHT);
	}

	// enable MenuSurface to close when clicked somewhere on menubar that is not an
	// item
	@Override
	protected void press(MouseEvent e) {
		propagationStopped = false;
	}
}
