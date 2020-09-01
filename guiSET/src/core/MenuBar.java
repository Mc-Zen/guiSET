package guiSET.core;


import processing.core.*;
import processing.event.MouseEvent;


/**
 * Top bar for menu strips which automatically fills out parent, has special menu
 * height and a greyish background gradient.
 * 
 * @author E-Bow
 *
 */
public class MenuBar extends HFlowContainer {

	public MenuBar() {
		setGradient(Color.create(230), Color.create(190));
		setZ(MenuItem.MenuZIndex);
		setHeightImpl(MenuItem.MENUITEM_HEIGHT);
	}

	@Override
	protected void addedToParent() {
		setWidthImpl(parent.getWidth());
		addAutoAnchors(PApplet.LEFT, PApplet.RIGHT);
	}

	@Override
	protected void press(MouseEvent e) {
		resetPropagationState();
	}
}
