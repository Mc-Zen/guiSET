package pGUI.core;

public class VFlowContainer extends Container {

	public VFlowContainer() {
		this(100,100);
	}

	public VFlowContainer(int Width, int Height) {
		super(Width, Height);
		autoLayout = true;
	}

	@Override
	protected void calcBounds() {
		int usedSpace = paddingTop;
		
		for (int i = 0; i < content.size(); i++) {
			Control c = content.get(i);
			if (c.visible) {
				Frame.calcBoundsCount++;

				// set bounds of child so it has absolute values for listener processing
				c.bounds.X0 = this.bounds.X0 + c.marginLeft + paddingLeft;
				c.bounds.Y0 = this.bounds.Y0 + usedSpace + c.marginTop;

				// crop overflow
				c.bounds.X = Math.min(c.bounds.X0 + c.width, this.bounds.X);
				c.bounds.Y = Math.min(c.bounds.Y0 + c.height, this.bounds.Y);

				// constrain after computing X,Y so no data will be lost by constraining
				c.bounds.X0 = Math.max(c.bounds.X0, this.bounds.X0);
				c.bounds.Y0 = Math.max(c.bounds.Y0, this.bounds.Y0);

				usedSpace += (c.height + c.marginTop + c.marginBottom);
				
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

		int usedSpace = paddingTop;

		for (int i = 0; i < content.size(); i++) {
			Control c = content.get(i);

			if (c.visible) {
				// set bounds of child so it has absolute values for listener processing
				/*
				 * c.bounds.X0 = this.bounds.X0 + c.marginLeft + paddingLeft; c.bounds.Y0 =
				 * this.bounds.Y0 + usedSpace + c.marginTop;
				 * 
				 * // crop overflow c.bounds.X = Math.min(c.bounds.X0 + c.width, this.bounds.X);
				 * c.bounds.Y = Math.min(c.bounds.Y0 + c.height, this.bounds.Y);
				 * 
				 * // constrain after computing X,Y so no data will be lost by constraining
				 * c.bounds.X0 = Math.max(c.bounds.X0, this.bounds.X0); c.bounds.Y0 =
				 * Math.max(c.bounds.Y0, this.bounds.Y0);
				 */
				
				containerRenderItem(c, c.marginLeft + paddingLeft, usedSpace + c.marginTop);

				/*if (c.changedVisuals) {
					c.changedVisuals = false;
					c.render();
				}

				pg.image(c.getGraphics(), c.marginLeft + paddingLeft, usedSpace + c.marginTop);*/
				usedSpace += (c.height + c.marginTop + c.marginBottom);
			}
		}

	}

}