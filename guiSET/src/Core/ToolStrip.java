package guiSET.core;


/**
 * Create a tool/menu strip outside the main menu, i.e. for dropdown menus that
 * open upon rightclick. The dropdown can be shown/hidden width the show() and
 * hide() methods. The position is automatically set to the current mouse
 * position
 * 
 * @author Mc-Zen
 *
 */
public class ToolStrip extends MenuItem {

	public ToolStrip() {
		dropDown = new MenuStrip();
		// sync DropDown content with items
		dropDown.items = items;
		addedToParent();
		setVisible(false); // needs never to be visible

		// this is also always the header of the strip
	}

	@Override
	protected void update() {
		// needs no updates
	}

	/**
	 * Show this tool strip.
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
