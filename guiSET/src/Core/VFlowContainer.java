package guiSET.core;


/**
 * A Container that layouts its content vertically.
 * 
 * @author Mc-Zen
 *
 */
public class VFlowContainer extends Container {

	public VFlowContainer() {
		this(100, 100);
	}

	public VFlowContainer(int width, int height) {
		super(width, height);
		containerMakesAutoLayout = true; // this container overrides items x/y property and gives them new location
	}

	@Override
	protected void calcBounds() {
		int usedSpace = paddingTop;

		for (int i = 0; i < content.size(); i++) {
			Control c = content.get(i);
			if (c.visible) {
				Frame.calcBoundsCount++; // profiling/debugging value

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

				if (c.cType == CONTAINER) { // if item is a container too, call calcBounds for it here
					c.calcBounds();
				}
			}
		}
	}

	@Override
	protected void render() {

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

				/*
				 * if (c.changedVisuals) { c.changedVisuals = false; c.render(); }
				 * 
				 * pg.image(c.getGraphics(), c.marginLeft + paddingLeft, usedSpace +
				 * c.marginTop);
				 */
				usedSpace += (c.height + c.marginTop + c.marginBottom);
			}
		}

	}

}