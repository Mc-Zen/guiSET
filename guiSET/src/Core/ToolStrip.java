package guiSET.core;


import guiSET.classes.*;
import processing.event.MouseEvent;

/**
 * @see MenuItem, which explains almost everything :).
 * 
 * 
 * @author Mc-Zen
 *
 */
public class ToolStrip extends Container {

	public ToolStrip() {
		super();
		setBackgroundColor(240);
		borderColor = -5592406; // color(170)
		borderWidth = 1;
		visible = false;
		z = 10;
	}

	@Override
	protected void calcBounds() {
		int usedSpace = paddingTop;

		for (int i = 0; i < content.size(); i++) {
			Control c = content.get(i);
			if (c.visible) {
				Frame.calcBoundsCount++;

				c.bounds.X0 = this.bounds.X0 + c.marginLeft + paddingLeft;
				c.bounds.Y0 = this.bounds.Y0 + usedSpace + c.marginTop;
				c.bounds.X = Math.min(c.bounds.X0 + c.width, this.bounds.X);
				c.bounds.Y = Math.min(c.bounds.Y0 + c.height, this.bounds.Y);
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
		// obtain needed width
		width = 100;
		for (int i = 0; i < content.size(); i++) {
			this.width = Math.max(this.width, content.get(i).minWidth);
		}

		// obtain needed height
		height = 1;
		for (int i = 0; i < content.size(); i++) {
			height += content.get(i).getHeight();
			content.get(i).width = width;
		}

		// we cheat here and give some extra size for shadow
		pg = Frame.frame0.papplet.createGraphics(width + 5, height + 5);
		pg.beginDraw();

		drawShadow(width, height, 5);

		drawDefaultBackground();

		// draw the little vertical line
		pg.strokeWeight(1);
		pg.stroke(220);
		pg.line(22, 0 + 3, 22, height - 3);
		pg.stroke(255);
		pg.line(23, 0 + 3, 23, height - 3);

		int usedSpace = paddingTop;
		for (int i = 0; i < content.size(); i++) {
			Control c = content.get(i);
			if (c.visible) {
				// set bounds of child so it has absolute values for listener processing
				/*c.bounds.X0 = this.bounds.X0 + c.marginLeft + paddingLeft;
				c.bounds.Y0 = this.bounds.Y0 + usedSpace + c.marginTop;
				c.bounds.X = Math.min(c.bounds.X0 + c.width, this.bounds.X);
				c.bounds.Y = Math.min(c.bounds.Y0 + c.height, this.bounds.Y);
				c.bounds.X0 = Math.max(c.bounds.X0, this.bounds.X0);
				c.bounds.Y0 = Math.max(c.bounds.Y0, this.bounds.Y0);*/

				containerRenderItem(c, c.marginLeft + paddingLeft, usedSpace + c.marginTop);

				usedSpace += (c.height + c.marginTop + c.marginBottom);
			}
		}
	}

	protected void drawShadow(int w, int h, int offset) {
		pg.noFill();
		int[] cl = { 115, 85, 41, 15, 5 };
		for (int i = 0; i < 5; i++) {
			pg.stroke(Color.create(0, cl[i]));
			pg.rect((offset - 1) * 2 - i, (offset - 1) * 2 - i, w - 2 * (4 - i), h - 2 * (4 - i));
		}
	}


	// also used internally by MenuItem
	/**
	 * Show this ToolStrip.
	 */
	public void show() {
		setVisible(true);
	}


	// also used internally by MenuItem
	/**
	 * Hide this ToolStrip.
	 */
	public void hide() {
		setVisible(false); // not sure if the update() is necessary here
	}

	/**
	 * Add menu items by passing their text.
	 * 
	 * @param strings arbitrary number of text Strings
	 */
	public void add(String... strings) {
		for (String s : strings) {
			MenuItem newItem = new MenuItem();
			newItem.setText(s);
			addItem(content.size(), newItem);
		}
		update();
	}


	/*
	 * New mouse event handling needs this here instead of in MenuItem: If clicked
	 * elsewhere the toolstrip should disappear
	 * 
	 * if this strip is closed than it has to be invisble - therefore nothing to do
	 * 
	 * else we check if propagation is still running (which is equal to no child
	 * item has been pressed) then we can close the header of the first item in this
	 * toolstrip.
	 * 
	 * Problem: this also closes the strip if the headerStrip is pressed -> and then
	 * re-opened by the click on the headerStrip but if it is already open we
	 * actually want to close this strip.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see guiSET.core.Container#mouseEvent(int, int)
	 */
	@Override
	protected void mouseEvent(int x, int y) {
		super.mouseEvent(x, y);

		// make the entire strip disappear when clicked elsewhere
		if (visible) {

			// this is true when no MenuItem has been clicked
			if (currentMouseEvent.getAction() == MouseEvent.PRESS && !Frame.isPropagationStopped()) {

				if (content.size() > 0) {

					// System.out.println(":" + content.get(0).text);
					// just get first item and close its headerStrip
					MenuItem headerOfThisStrip = ((MenuItem) content.get(0));
					headerOfThisStrip.headerStrip.close();
					Frame.stopPropagation();
				}
			}

		}


	}
}
