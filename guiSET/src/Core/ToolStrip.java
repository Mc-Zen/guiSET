package pGUI.core;


/*
 * Basically the ToolStrip is kind of a servant of the MenuItem. It does the rendering of 
 * strips and substrips and therefore always needs to be a child of Frame itself to have 
 * the freedom to be drawn anywhere on the sketch. 
 * The parent MenuItems keep a reference to their child ToolStrip which does nothing but 
 * the displaying when show() has been called and hiding when hide() has been called. 
 */


import pGUI.classes.*;

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
				c.bounds.X0 = this.bounds.X0 + c.marginLeft + paddingLeft;
				c.bounds.Y0 = this.bounds.Y0 + usedSpace + c.marginTop;
				c.bounds.X = Math.min(c.bounds.X0 + c.width, this.bounds.X);
				c.bounds.Y = Math.min(c.bounds.Y0 + c.height, this.bounds.Y);
				c.bounds.X0 = Math.max(c.bounds.X0, this.bounds.X0);
				c.bounds.Y0 = Math.max(c.bounds.Y0, this.bounds.Y0);

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

	public void show() {
		setVisible(true);
	}

	public void hide() {
		visible = false;
	}

	public void add(String... strings) {
		for (String s : strings) {
			MenuItem newItem = new MenuItem();
			newItem.setText(s);
			addItem(content.size(), newItem);
		}
		update();
	}
}
