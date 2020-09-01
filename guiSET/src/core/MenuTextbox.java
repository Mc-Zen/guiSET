package guiSET.core;


/**
 * A MenuItem that allows entering text.
 * 
 * @author E-Bow
 *
 */
public class MenuTextbox extends MenuItem {

	protected Textbox t;


	public MenuTextbox() {
		t = new Textbox();
		t.parent = this;
	}


	@Override
	protected void render() {
		renderItem(t, 0, 0);
	}


	@Override
	protected void mouseEvent(int x, int y) {
		x -= offsetX;
		y -= offsetY;
		t.mouseEvent(x, y);
	}

	@Override
	public void close() {
		super.close();
		t.blur();
	}
}
