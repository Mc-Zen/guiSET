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
		int usedX = getPaddingLeft();
		int usedY = getPaddingTop();
		int lineY = 0;

		for (Control c : items) {
			if (c.isVisible()) {
				lineY = Math.max(lineY, c.getHeight() + c.getMarginTop() + c.getMarginBottom());
				int itemWid = c.getWidth() + c.getMarginLeft() + c.getMarginRight();

				if (usedX + itemWid > getWidth() - getPaddingRight()) {
					// new line
					usedY += lineY;
					usedX = getPaddingLeft();
					lineY = 0;
				}
				renderItem(c, usedX + c.getMarginLeft(), usedY + c.getMarginTop());
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
