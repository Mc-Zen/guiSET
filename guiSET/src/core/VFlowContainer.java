package guiSET.core;

/**
 * A Container that layouts its content vertically.
 * 
 * @author Mc-Zen
 *
 */
public class VFlowContainer extends Container {

	public VFlowContainer() {
		super();
	}

	public VFlowContainer(int width, int height) {
		super(width, height);
	}

	@Override
	protected void render() {
		drawDefaultBackground();

		int usedSpace = getPaddingTop();

		for (Control c : items) {
			if (c.isVisible()) {
				renderItem(c, c.getMarginLeft() + getPaddingLeft(), usedSpace + c.getMarginTop());
				usedSpace += (c.getHeight() + c.getMarginTop() + c.getMarginBottom());
			}
		}
		drawDefaultDisabled();
	}

	// this container overrides items x/y property and gives them new location. All items are next to
	// each other and will not overlap
	@Override
	protected boolean needsSortingByZ() {
		return false;
	}

}