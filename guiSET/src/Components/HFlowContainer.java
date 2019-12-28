package pGUI.core;

public class HFlowContainer extends Container {

	protected boolean Break = false;

	public HFlowContainer() {
		this(100, 100);
	}

	public HFlowContainer(int Width, int Height) {
		super(Width, Height);
		autoLayout = true;
	}

	@Override
	protected void calcBounds() {
		int usedSpace = paddingLeft;

		for (Control c : content) {
			if (c.visible) {
				Frame.calcBoundsCount++;

				c.bounds.X0 = this.bounds.X0 + usedSpace + c.marginLeft;
				c.bounds.Y0 = this.bounds.Y0 + c.marginTop + paddingTop;
				// crop overflow
				c.bounds.X = Math.min(c.bounds.X0 + c.width, this.bounds.X);
				c.bounds.Y = Math.min(c.bounds.Y0 + c.height, this.bounds.Y);
				// constrain after computing X,Y so no data will be lost by constraining
				c.bounds.X0 = Math.max(c.bounds.X0, this.bounds.X0);
				c.bounds.Y0 = Math.max(c.bounds.Y0, this.bounds.Y0);



				usedSpace += (c.width + c.marginLeft + c.marginRight);

				if (c.cType == CONTAINER) {
					c.calcBounds();
				}
			}
		}
	}

	@Override
	protected void render() {
		// if AutoSize is on, first get minimal dimensions
		if (autoSize) {
			setAutoSize();
		}
		drawDefaultBackground();

		int usedSpace = paddingLeft;

		for (Control c : content) {

			if (c.visible) {

				containerRenderItem(c, usedSpace + c.marginLeft, c.marginTop + paddingTop);

				usedSpace += (c.width + c.marginLeft + c.marginRight);
			}
		}
	}

}