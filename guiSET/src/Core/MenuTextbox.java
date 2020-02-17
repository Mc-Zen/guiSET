package guiSET.core;


/**
 * A MenuItem that allows entering text.
 * 
 * @author E-Bow
 *
 */
public class MenuTextbox extends MenuItem {

	Textbox t;


	public MenuTextbox() {
		t = new Textbox();
		t.parent = this;
	}


	@Override
	protected void render() {
		containerRenderItem(t, 0, 0);
	}


	@Override
	protected void mouseEvent(int x, int y) {
		x -= relativeX;
		y -= relativeY;
		t.mouseEvent(x, y);
	}

	@Override
	public void close() {
		super.close();
		t.blur();
	}
}
