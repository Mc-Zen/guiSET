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
		setGradient(GuisetDefaultValues.menubarGradientTopColor, GuisetDefaultValues.menubarGradientBottomColor);
		setZ(Constants.MenuSurfaceZIndex);
		setHeightNoUpdate(Constants.MenuItemHeight);
	}

	@Override
	protected void addedToParent() {
		setWidthNoUpdate(parent.getWidth());
		addAutoAnchors(PApplet.LEFT, PApplet.RIGHT);
	}

	@Override
	protected void press(MouseEvent e) {
		resetPropagationState();
	}
}
