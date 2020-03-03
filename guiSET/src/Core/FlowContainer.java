package guiSET.core;


/**
 * A Container that layouts its content from left to right and top to bottom.
 * 
 * @author Mc-Zen
 *
 */
public class FlowContainer extends Container {

	public FlowContainer() {
		this(100, 100);
	}

	public FlowContainer(int width, int height) {
		super(width, height);
		containerMakesAutoLayout = true; // this container overrides items x/y property and gives them new location
	}




	@Override
	protected void render() {
		drawDefaultBackground();
		int usedX = paddingLeft;
		int usedY = paddingTop;
		int lineY = 0;

		for (Control c : items) {
			if (c.visible) {
				lineY = Math.max(lineY, c.height + c.marginTop + c.marginBottom);
				int itemWid = c.width + c.marginLeft + c.marginRight;

				if (usedX + itemWid > width - paddingRight) {
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

}
