package guiSET.core;


import processing.core.*;
import processing.event.MouseEvent;


/**
 * Top bar for menu strips which automatically fills out parent, has special menu height and a
 * greyish background gradient.
 * 
 * @author E-Bow
 *
 */
public class MenuBar extends HFlowContainer {

	public MenuBar() {
		setGradient(GuisetDefaultValues.menubarGradientTopColor, GuisetDefaultValues.menubarGradientBottomColor);
		setZ(Constants.MenuSurfaceZIndex + 1); // Can't be less however. Items on MenuBar must stay clickable etc.
		setHeightNoUpdate(Constants.MenuItemHeight);
	}

	/**
	 * z should not be less than {@link Constants#MenuSurfaceZIndex}. Values less than
	 * ({@link Constants#MenuSurfaceZIndex} + 1) are ignored.
	 * 
	 * @param z z-index
	 */
	@Override
	public void setZ(int z) {
		if (z > Constants.MenuSurfaceZIndex)
			super.setZ(z);
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
