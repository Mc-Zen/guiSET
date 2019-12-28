package pGUI.core;


/*
 * ScrollArea is a panel container which allows overflowing and scrolling in x and y direction. 
 * Items will be placed at their specific location. 
 * 
 * The full scroll width and height is determined new every time ScrollArea renders.  
 */





import processing.core.*;
import processing.event.*;

public class ScrollArea extends Container {

	protected int scrollPositionX;
	protected int scrollPositionY;

	protected int fullScrollWidth;
	protected int fullScrollHeight;

	protected int scrollSpeedX = 10;
	protected int scrollSpeedY = 10;


	protected boolean slim_scrollhandle = false;


	public ScrollArea() {
		super();
	}

	public ScrollArea(int width, int height) {
		super(width, height);
	}





	@Override
	protected void calcBounds() {
		for (Control c : content) {

			if (c.visible) {

				c.bounds.X0 = this.bounds.X0 + c.x - scrollPositionX;
				c.bounds.Y0 = this.bounds.Y0 + c.y - scrollPositionY;
				// crop overflow
				c.bounds.X = Math.min(c.bounds.X0 + c.width, this.bounds.X - (needsScrollbarV() ? scrollHandleStrength + 3 : 0));
				c.bounds.Y = Math.min(c.bounds.Y0 + c.height, this.bounds.Y - (needsScrollbarH() ? scrollHandleStrength + 3 : 0));

				// constrain after computing X,Y so no data will be lost by constraining
				c.bounds.X0 = Math.max(c.bounds.X0, this.bounds.X0);
				c.bounds.Y0 = Math.max(c.bounds.Y0, this.bounds.Y0);

				if (c.cType == CONTAINER) {
					c.calcBounds();
				}
			}
		}
	}



	@Override
	protected void render() {

		drawDefaultBackground();

		scrollPositionX = PApplet.constrain(scrollPositionX, 0, PApplet.max(0, fullScrollWidth - width));
		scrollPositionY = PApplet.constrain(scrollPositionY, 0, PApplet.max(0, fullScrollHeight - height));

		for (Control c : content) {
			if (c.visible) {
				containerRenderItem(c, c.x - scrollPositionX, c.y - scrollPositionY);
			}
		}

		fullScrollWidth = 0;
		fullScrollHeight = 0;

		for (int i = 0; i < content.size(); i++) {
			Control c = content.get(i);

			if (c.visible) {
				fullScrollWidth = Math.max(fullScrollWidth, c.x + c.width + c.marginLeft);
				fullScrollHeight = Math.max(fullScrollHeight, c.y + c.height + c.marginBottom);
			}
		}
		fullScrollWidth += scrollHandleStrength + 3;
		fullScrollHeight += scrollHandleStrength + 3;


		if (needsScrollbarH() && needsScrollbarV() && !slim_scrollhandle) {
			pg.fill(130);
			pg.noStroke();
			pg.rect(width - scrollHandleStrength - 3, height - scrollHandleStrength - 3, scrollHandleStrength + 3, scrollHandleStrength + 3);
		}

		drawScrollbarH();
		drawScrollbarV();
	}



	// draw vertical scrollbar if needed
	protected void drawScrollbarV() {
		if (needsScrollbarV()) { // don't display scroll-bar when there's nothing to scroll

			pg.fill(150);
			pg.noStroke();

			if (slim_scrollhandle) {
				pg.rect(width - 4, scrollhandle_posY(), scrollHandleStrength, scrollhandle_height(), 15);

			} else {
				pg.rect(width - 3 - scrollHandleStrength, 0, scrollHandleStrength + 3, scrollbar_height());
				pg.fill(190);
				pg.rect(width - 2 - scrollHandleStrength, scrollhandle_posY(), scrollHandleStrength, scrollhandle_height(), 3);
			}
		}
	}


	// draw horizontal scrollbar if needed
	protected void drawScrollbarH() {
		if (needsScrollbarH()) { // don't display scroll-bar when there's nothing to scroll

			pg.fill(150);
			pg.noStroke();

			if (slim_scrollhandle) {
				pg.rect(scrollhandle_posX(), height - 4, scrollhandle_width(), 3, 15);
			} else {
				pg.rect(0, height - 3 - scrollHandleStrength, scrollbar_width(), scrollHandleStrength + 3); // height is one more than necessary (just
																											 // a buffer)
				pg.fill(190);
				pg.rect(scrollhandle_posX(), height - 2 - scrollHandleStrength, scrollhandle_width(), scrollHandleStrength, 3);
			}
		}
	}


	/*
	 * some methods needed for controlling and drawing the scrollbars
	 */

	// vertical scrollbar needed?
	protected boolean needsScrollbarV() {
		return height < fullScrollHeight;
	}

	// horizontal scrollbar needed?
	protected boolean needsScrollbarH() {
		return width < fullScrollWidth;
	}

	// get height of entire vertical scrollbar (usually full height of element
	// except when also having a horizontal scrollbar
	protected int scrollbar_height() {
		return height - (needsScrollbarH() ? scrollHandleStrength + 3 : 0);
	}

	// get width of entire horizontal scrollbar (usually full width of element
	// except when also having a vertical scrollbar
	protected int scrollbar_width() {
		return width - (needsScrollbarV() ? scrollHandleStrength + 3 : 0);
	}

	// get height of handle (of the vertical scrollbar)
	protected float scrollhandle_height() {
		return (float) height / fullScrollHeight * scrollbar_height();
	}

	// get width of handle (of the horizontal scrollbar)
	protected float scrollhandle_width() {
		return (float) width / fullScrollWidth * scrollbar_width();
	}

	// get position of handle (of the vertical scrollbar)
	protected float scrollhandle_posY() {
		int scrollbar_height = scrollbar_height();
		float scrollhandle_height = scrollhandle_height();

		return PApplet.constrain(scrollPositionY * (scrollbar_height - scrollhandle_height) / (fullScrollHeight - height), 1,
				scrollbar_height - scrollhandle_height - 2);
	}

	// get position of handle (of the horizontal scrollbar)
	protected float scrollhandle_posX() {
		int scrollbar_width = scrollbar_width();
		float scrollhandle_width = scrollhandle_width();

		return PApplet.constrain(scrollPositionX * (scrollbar_width - scrollhandle_width) / (fullScrollWidth - width), 1,
				scrollbar_width - scrollhandle_width - 2);
	}





	/*
	 * SETTER
	 */

	public void setScrollSpeedX(int s) {
		scrollSpeedX = s;
	}

	public void setScrollSpeedY(int s) {
		scrollSpeedY = s;
	}

	public void setScrollPositionX(int x) {
		scrollPositionX = x;
		update();
	}

	public void setScrollPositionY(int y) {
		scrollPositionY = y;
		update();
	}

	public void setSlimScrollHandle(boolean light_scrollhandle) {
		this.slim_scrollhandle = light_scrollhandle;
		if (light_scrollhandle) {
			scrollHandleStrength = SCROLL_HANDLE_STRENGTH_SLIM;
		} else {
			scrollHandleStrength = SCROLL_HANDLE_STRENGTH_STD;
		}
		update();
	}

	/*
	 * GETTER
	 */

	public int getScrollSpeedX() {
		return scrollSpeedX;
	}

	public int getScrollSpeedY() {
		return scrollSpeedY;
	}

	public int getFullScrollWidth() {
		return fullScrollWidth;
	}

	public int getFullScrollHeight() {
		return fullScrollHeight;
	}

	public boolean isSlimScrollHandle() {
		return slim_scrollhandle;
	}

	public int getScrollPositionX() {
		return scrollPositionX;
	}

	public int getScrollPositionY() {
		return scrollPositionY;
	}



	/*
	 * MOUSE EVENTS
	 */

	@Override
	protected void mouseWheel(MouseEvent e) {
		if (Frame.frame0.isShiftDown()) {
			int temp = scrollPositionX;
			setScrollPositionX(scrollPositionX + e.getCount() * scrollSpeedX);
			if (scrollPositionX != temp) {
				Frame.stopPropagation();
			}
		} else {
			int temp = scrollPositionY;
			setScrollPositionY(scrollPositionY + e.getCount() * scrollSpeedY);
			if (scrollPositionY != temp) {
				Frame.stopPropagation();
			}
		}
	}



	/*
	 * ScrollHandle
	 * 
	 * When scrollHandle is dragged it sets scrollposition to corresponding
	 * position. Therefore the position of mouse when dragging started needs to be
	 * captured at press (in startHandleDragPos) and also which of the possibly two
	 * scrollbars. Of course this is only done when clicking on the handle. A
	 * release will result in resetting startHandleDragPos to -1.
	 * 
	 * When drag() is called by Frame the new scrollPosition is calculated.
	 * 
	 * 
	 */

	protected float startHandleDragPos = -1;
	private int whichScrollBar;
	private static final int H_SCROLLBAR = 0;
	private static final int V_SCROLLBAR = 1;

	protected int scrollHandleStrength = SCROLL_HANDLE_STRENGTH_STD; // thickness
	protected static final int SCROLL_HANDLE_STRENGTH_STD = 12;
	protected static final int SCROLL_HANDLE_STRENGTH_SLIM = 3;

	@Override
	protected void drag(MouseEvent e) {
		if (startHandleDragPos > -1) {
			if (whichScrollBar == H_SCROLLBAR) {

				float newScrollHandle_Pos = e.getX() - bounds.X0 - startHandleDragPos;
				float newScrollPosition = newScrollHandle_Pos * (float) (fullScrollWidth - width) / (scrollbar_width() - scrollhandle_width());
				setScrollPositionX((int) newScrollPosition);

			} else if (whichScrollBar == V_SCROLLBAR) {

				float newScrollHandle_Pos = e.getY() - bounds.Y0 - startHandleDragPos;
				float newScrollPosition = newScrollHandle_Pos * (float) (fullScrollHeight - height) / (scrollbar_height() - scrollhandle_height());
				setScrollPositionY((int) newScrollPosition);

			}
		}
	}


	@Override
	protected void mouseEvent(MouseEvent e) {
		if (visible) {

			/*
			 * handle the scrollbar dragging
			 */

			boolean mouseIsOverScrollAreaH = e.getY() > bounds.Y - scrollHandleStrength - 3 && e.getY() < bounds.Y && e.getX() > bounds.X0
					&& e.getX() < bounds.X - (needsScrollbarV() ? scrollHandleStrength + 3 : 0);

			boolean mouseIsOverScrollAreaV = e.getX() > bounds.X - scrollHandleStrength - 3 && e.getX() < bounds.X && e.getY() > bounds.Y0
					&& e.getY() < bounds.Y - (needsScrollbarH() ? scrollHandleStrength + 3 : 0);


			switch (e.getAction()) {
			case MouseEvent.PRESS:
				if (mouseIsOverScrollAreaH) {
					whichScrollBar = H_SCROLLBAR;

					float scrollhandle_posX = scrollhandle_posX();

					// if clicked on scrollhandle itself (instead of entire scroll area) the
					// dragging is started
					if (e.getX() > scrollhandle_posX + bounds.X0 && e.getX() < scrollhandle_posX + bounds.X0 + scrollhandle_width()) {
						startHandleDragPos = e.getX() - bounds.X0 - scrollhandle_posX;
					}

				} else if (mouseIsOverScrollAreaV) {
					whichScrollBar = V_SCROLLBAR;

					float scrollhandle_posY = scrollhandle_posY();

					// if clicked on scrollhandle itself (instead of entire scroll area) the
					// dragging is started
					if (e.getY() > scrollhandle_posY + bounds.Y0 && e.getY() < scrollhandle_posY + bounds.Y0 + scrollhandle_height()) {
						startHandleDragPos = e.getY() - bounds.Y0 - scrollhandle_posY;
					}
				}
				break;

			case MouseEvent.RELEASE:

				// stop dragging scrollbar
				startHandleDragPos = -1;
				break;
			}

			super.mouseEvent(e);
		}
	}


}