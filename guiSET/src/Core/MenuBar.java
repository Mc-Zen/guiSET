package guiSET.core;

import guiSET.classes.Color;
import processing.core.*;


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
		height = MenuItem.MENUITEM_HEIGHT;
	}




	@Override
	protected void addedToParent() {
		width = parent.width;
		addAutoAnchor(PApplet.LEFT, PApplet.RIGHT);
	}


}
