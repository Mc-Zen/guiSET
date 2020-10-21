package guiSET.core;


/**
 * Default values are used when constructing new objects. They can be modified to ease the styling
 * of the gui.
 * 
 * I.e. if the {@link #backgroundColor} is set to black, gui item that are created afterwards will
 * be defaulted with a black background etc.
 */
public class GuisetDefaultValues {

	public static int backgroundColor = GuisetColor.WHITE;
	public static int foregroudColor = GuisetColor.BLACK;
	public static int borderColor = -15461356;
	public static int borderWidth = 0;
	public static int borderRadius = 0;

	public static int paddingLeft = 0;
	public static int paddingRight = 0;
	public static int paddingTop = 0;
	public static int paddingBottom = 0;

	public static int marginLeft = 0;
	public static int marginRight = 0;
	public static int marginTop = 0;
	public static int marginBottom = 0;

	public static int textColor = foregroudColor;
	public static int fontSize = 12;

	public static int labelPadding = 3;
	public static int labelBackgroundColor = GuisetColor.TRANSPARENT;

	public static int buttonPadding = 7;
	public static int buttonBorderWidth = 1;
	public static int buttonBackgroundColor = backgroundColor;
	public static int buttonHoverColor = GuisetColor.create(200);
	public static int buttonPressColor = GuisetColor.create(150);

	public static float sliderMinValue = 0;
	public static float sliderMaxValue = 100;
	public static int sliderBackgroundColor = -2302756;
	public static int sliderForegroundColor = -1926085;
	public static float sliderRelativeWheelSpeed = 0.02f;

	public static int checkboxUncheckedColor = -6250336; // gray
	public static int checkboxCheckedColor = -13732742;  // greenish
	public static int checkboxCheckmarkColor = -328966;  // almost white

	public static int containerWidth = 100;
	public static int containerHeight = 100;

	protected static int scrollSpeed = 40; // px per frame

	public static int menubarGradientTopColor = GuisetColor.create(230);
	public static int menubarGradientBottomColor = GuisetColor.create(190);
	public static int menuItemBackgroundColor = GuisetColor.TRANSPARENT;
	public static int menuHeaderHoverColor = 1342177280;
	public static int menuItemHoverColor = 671088660;
	public static int menuItemPressColor = 1677721600;

}