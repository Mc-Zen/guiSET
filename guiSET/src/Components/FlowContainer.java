package pGUI.core;


public class FlowContainer extends Container {

	public FlowContainer() {
		this(100, 100);
	}

	public FlowContainer(int width, int height) {
		super(width, height);
		autoLayout = true;
	}



	@Override
	protected void calcBounds() {

		int usedX = paddingLeft;
		int usedY = paddingTop;
		int lineY = 0;

		for (Control c : content) {

			if (c.visible) {
				lineY = Math.max(lineY, c.height + c.marginTop + c.marginBottom);
				int itemWid = c.width + c.marginLeft + c.marginRight;

				if (usedX + itemWid > width - paddingRight) {
					// new line
					usedY += lineY;
					usedX = paddingLeft;
					lineY = 0;
				}

				c.bounds.X0 = this.bounds.X0 + usedX + c.marginLeft;
				c.bounds.Y0 = this.bounds.Y0 + usedY + c.marginTop;

				c.bounds.X = Math.min(c.bounds.X0 + c.width, this.bounds.X);
				c.bounds.Y = Math.min(c.bounds.Y0 + c.height, this.bounds.Y);

				c.bounds.X0 = Math.max(c.bounds.X0, this.bounds.X0);
				c.bounds.Y0 = Math.max(c.bounds.Y0, this.bounds.Y0);


				usedX += itemWid;

				if (c.cType == CONTAINER) {
					c.calcBounds();
				}
			}
		}
	}




	@Override
	protected void render() {
		drawDefaultBackground();
		int usedX = paddingLeft;
		int usedY = paddingTop;
		int lineY = 0;

		for (Control c : content) {
			if (c.visible) {
				lineY = Math.max(lineY, c.height + c.marginTop + c.marginBottom);
				int itemWid = c.width + c.marginLeft + c.marginRight;

				if (usedX + itemWid > width - paddingRight) {
					// new line
					usedY += lineY;
					usedX = paddingLeft;
					lineY = 0;
				}
				containerRenderItem(c, usedX + c.marginLeft, usedY + c.marginTop);
				usedX += itemWid;
			}
		}
	}

}
