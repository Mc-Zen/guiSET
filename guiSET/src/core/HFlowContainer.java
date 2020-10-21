package guiSET.core;

/**
 * A Container that layouts its content horizontally.
 * 
 * @author Mc-Zen
 *
 */
public class HFlowContainer extends Container {

	public HFlowContainer() {
		super();
	}

	public HFlowContainer(int width, int height) {
		super(width, height);
	}

	@Override
	protected void render() {
		drawDefaultBackground();

		int usedSpace = getPaddingLeft();

		for (Control c : items) {
			if (c.isVisible()) {
				renderItem(c, usedSpace + c.getMarginLeft(), c.getMarginTop() + getPaddingTop());
				usedSpace += (c.getWidth() + c.getMarginLeft() + c.getMarginRight());
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