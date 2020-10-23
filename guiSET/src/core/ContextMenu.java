package guiSET.core;


/**
 * Create a tool/menu strip outside the main menu (i.e. for dropdown menus) that opens upon an
 * action such as rightclick. A ContextMenu does not need to (and should not) be added to a
 * container or anything. The dropdown can be shown/hidden width the show() and hide() methods. The
 * position is automatically set to the current mouse position.
 * 
 * @author Mc-Zen
 *
 */
public class ContextMenu extends MenuItem {

	public ContextMenu() {
		createDropDownIfNecessary();
		setVisible(false); // Needs never to be visible
		determineTypeAndSetup();

		// This is also always the header of the strip
	}

	/**
	 * Show this tool strip. Position is automatically set to current mouse position.
	 */
	public void show() {
		open();
		dropDown.setPosition(currentMouseEvent.getX(), currentMouseEvent.getY()); // after open
	}

	/**
	 * Hide this tool strip
	 */
	public void hide() {
		dropDown.hide();
	}
}
