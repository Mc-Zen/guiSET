package guiSET.core;


/**
 * A Container that layouts its content from left to right and top to bottom.
 * 
 * @author Mc-Zen
 *
 */
public class FlowContainer extends Container {

	public FlowContainer() {
		super();
	}

	public FlowContainer(int width, int height) {
		super(width, height);
	}




	@Override
	protected void render() {
		drawDefaultBackground();
		int usedX = paddingLeft;
		int usedY = paddingTop;
		int lineY = 0;

		for (Control c : items) {
			if (c.visible) {
				lineY = Math.max(lineY, c.getHeight() + c.marginTop + c.marginBottom);
				int itemWid = c.getWidth() + c.marginLeft + c.marginRight;

				if (usedX + itemWid > getWidth() - paddingRight) {
					// new line
					usedY += lineY;
					usedX = paddingLeft;
					lineY = 0;
				}
				renderItem(c, usedX + c.marginLeft, usedY + c.marginTop);
				usedX += itemWid;
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
