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

}