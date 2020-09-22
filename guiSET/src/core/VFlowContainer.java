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

		int usedSpace = paddingTop;

		for (Control c : items) {
			if (c.visible) {
				renderItem(c, c.marginLeft + paddingLeft, usedSpace + c.marginTop);
				usedSpace += (c.getHeight() + c.marginTop + c.marginBottom);
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