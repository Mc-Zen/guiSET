package guiSET.core;


/**
 * This class stores values that are used globally and not bound to inidivual instances, such as
 * scroll bar colors etc.
 * 
 * All values can be changed to effect the behaviour or style of the gui. Changing a visual property
 * however might not be seen immediately but only when the concerning elements graphics are updated.
 */
public class GuisetGlobalValues {

	/**
	 * Border radius for all scroll handles
	 */
	public static int scrollHandleBorderRadius = 0;
	public static int scrollBarColor = GuisetColor.create(150);
	public static int scrollHandleColor = GuisetColor.create(170);
	public static int scrollHandlePressColor = GuisetColor.create(190);
	public static int scrollHandleBorderColor = GuisetColor.create(90);
	public static int scrollAreaBetweenScrollHandlesSquare = GuisetColor.create(130);


	/**
	 * MenuItems with sub items have a little triangle arrow that points to the right. If the MenuItem
	 * is enabled then this will be its color.
	 */
	public static int menuItemTriangleEnabledColor = GuisetColor.create(0);
	public static int menuItemTriangleDisabledColor = GuisetColor.create(150);
	public static int menuItemCheckmarkFillColor = GuisetColor.create(180, 180, 250, 130);
	public static int menuItemCheckmarkStrokeColor = GuisetColor.create(60, 60, 100, 150);
	public static int menuStripBackgroundColor = GuisetColor.create(240);
	public static int menuStripBorderColor = GuisetColor.create(170);

	/**
	 * Time a submenu waits before opening after the mouse has entered the parent item (in
	 * milliseconds). Should be positive obviously.
	 */
	public static int menuItemHoverTime = 100;
}