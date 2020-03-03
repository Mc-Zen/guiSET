package guiSET.core;


/*
 * ScrollArea is a panel container which allows overflowing and scrolling in x and y direction. 
 * Items will be placed at their specific location. 
 * 
 * The full scroll width and height is determined new every time ScrollArea renders.  
 */





import processing.core.*;
import processing.event.*;


/**
 * A Container that allows both horizontal and vertical scrolling if the content
 * exceeds the ScrollAreas size. It does not layout its content as
 * {@link VScrollContainer} or {@link VScrollContainer} but keeps the relative x
 * and y positions of the items.
 * 
 * @author Mc-Zen
 *
 */
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
	protected void render() {
		drawDefaultBackground();

		fullScrollWidth = 0;
		fullScrollHeight = 0;

		for (Control c : items) {
			if (c.visible) {
				fullScrollWidth = Math.max(fullScrollWidth, c.x + c.width + c.marginLeft);
				fullScrollHeight = Math.max(fullScrollHeight, c.y + c.height + c.marginBottom);
			}
		}
		fullScrollWidth += scrollHandleStrength + 3;
		fullScrollHeight += scrollHandleStrength + 3;

		scrollPositionX = PApplet.constrain(scrollPositionX, 0, PApplet.max(0, fullScrollWidth - width));
		scrollPositionY = PApplet.constrain(scrollPositionY, 0, PApplet.max(0, fullScrollHeight - height));



		for (Control c : items) {
			if (c.visible) {
				renderItem(c, c.x - scrollPositionX, c.y - scrollPositionY);
			}
		}

		// rect at right bottom corner if both scrollbars active
		if (needsScrollbarH() && needsScrollbarV() && !slim_scrollhandle) {
			pg.fill(130);
			pg.noStroke();
			pg.rect(width - scrollHandleStrength - 3, height - scrollHandleStrength - 3, scrollHandleStrength + 3, scrollHandleStrength + 3);
		}

		drawScrollbarH();
		drawScrollbarV();
		drawDefaultDisabled();
	}



	// draw vertical scrollbar if needed
	protected void drawScrollbarV() {
		if (needsScrollbarV()) { // don't display scroll-bar when there's nothing to scroll

			pg.fill(150);
			pg.noStroke();

			if (slim_scrollhandle) {
				pg.rect(width - 4, scrollhandle_posY(), scrollHandleStrength, scrollhandle_height(), 15);

			} else {
				pg.rect(width - 2 - scrollHandleStrength, 0, scrollHandleStrength + 2, scrollbar_height());
				pg.fill((startHandleDragPos > -1 && whichScrollBar == V_SCROLLBAR) ? 170 : 190);
				pg.rect(width - 1 - scrollHandleStrength, scrollhandle_posY(), scrollHandleStrength, scrollhandle_height(), 3);
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
				pg.rect(0, height - 2 - scrollHandleStrength, scrollbar_width(), scrollHandleStrength + 3); // height is one more than necessary (just)																			 // a buffer)
				pg.fill((startHandleDragPos > -1 && whichScrollBar == H_SCROLLBAR) ? 170 : 190);
				pg.rect(scrollhandle_posX(), height - 1 - scrollHandleStrength, scrollhandle_width(), scrollHandleStrength, 3);
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
	protected int scrollhandle_height() {
		return height * scrollbar_height() / fullScrollHeight;
	}

	// get width of handle (of the horizontal scrollbar)
	protected int scrollhandle_width() {
		return width * scrollbar_width() / fullScrollWidth;
	}

	// get position of handle (of the vertical scrollbar)
	protected int scrollhandle_posY() {
		int scrollbar_height = scrollbar_height();
		float scrollhandle_height = scrollhandle_height();

		return (int) PApplet.constrain(scrollPositionY * (scrollbar_height - scrollhandle_height) / (fullScrollHeight - height), 1,
				scrollbar_height - scrollhandle_height - 2);
	}

	// get position of handle (of the horizontal scrollbar)
	protected int scrollhandle_posX() {
		int scrollbar_width = scrollbar_width();
		float scrollhandle_width = scrollhandle_width();

		return (int) PApplet.constrain(scrollPositionX * (scrollbar_width - scrollhandle_width) / (fullScrollWidth - width), 1,
				scrollbar_width - scrollhandle_width - 2);
	}





	/*
	 * SETTER
	 */
	/**
	 * Amount of pixels to scroll for each step with the mouse wheel in horizontal
	 * direction.
	 * 
	 * @param scrollSpeedX scrollSpeedX
	 */
	public void setScrollSpeedX(int scrollSpeedX) {
		this.scrollSpeedX = scrollSpeedX;
	}

	/**
	 * Amount of pixels to scroll for each step with the mouse wheel in vertical
	 * direction.
	 * 
	 * @param scrollSpeedY scrollSpeedY
	 */
	public void setScrollSpeedY(int scrollSpeedY) {
		this.scrollSpeedY = scrollSpeedY;
	}

	/**
	 * Set horizontal scroll position in pixel from left.
	 * 
	 * @param scrollPositionX scrollPositionX
	 */
	public void setScrollPositionX(int scrollPositionX) {
		this.scrollPositionX = scrollPositionX;
		update();
	}

	/**
	 * Set vertical scroll position in pixel from top.
	 * 
	 * @param scrollPositionY scrollPositionY
	 */
	public void setScrollPositionY(int scrollPositionY) {
		this.scrollPositionY = scrollPositionY;
		update();
	}

	/**
	 * Enable a slim (mobile phone like) scroll handle instead of the standard one.
	 * 
	 * @param slim_scrollhandle slim_scrollhandle
	 */
	public void setSlimScrollHandle(boolean slim_scrollhandle) {
		this.slim_scrollhandle = slim_scrollhandle;
		if (slim_scrollhandle) {
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
				stopPropagation();
			}
		} else {
			int temp = scrollPositionY;
			setScrollPositionY(scrollPositionY + e.getCount() * scrollSpeedY);
			if (scrollPositionY != temp) {
				stopPropagation();
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

	protected int startHandleDragPos = -1;
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

				int newScrollHandle_Pos = e.getX() - getOffsetXWindow() - startHandleDragPos;
				int newScrollPosition = newScrollHandle_Pos * (fullScrollWidth - width) / (scrollbar_width() - scrollhandle_width());
				setScrollPositionX(newScrollPosition);

			} else if (whichScrollBar == V_SCROLLBAR) {

				int newScrollHandle_Pos = e.getY() - getOffsetYWindow() - startHandleDragPos;
				int newScrollPosition = newScrollHandle_Pos * (fullScrollHeight - height) / (scrollbar_height() - scrollhandle_height());
				setScrollPositionY(newScrollPosition);

			}
		}
	}

	// Do this here and not in containerPreItemsMouseEvent called, because the
	// latter is not called when released outside this element.
	@Override
	protected void release(MouseEvent e) {
		super.release(e);
		startHandleDragPos = -1;
	}

	/**
	 * Need to check if mouse is over one of the scroll bars. If so, then content
	 * items should not receive this mouse event (thus return false here in this
	 * case).
	 * 
	 * 
	 * (non-Javadoc)
	 * 
	 * @see guiSET.core.Container#containerPreItemsMouseEvent(int, int)
	 */
	@Override
	protected boolean containerPreItemsMouseEvent(int x, int y) {
		boolean mouseIsOverScrollBarV = x > width - scrollHandleStrength - 3 && x < width && y > 0
				&& y < height - (needsScrollbarV() ? scrollHandleStrength + 3 : 0);
		boolean mouseIsOverScrollBarH = y > height - scrollHandleStrength - 3 && y < height && x > 0
				&& x < width - (needsScrollbarH() ? scrollHandleStrength + 3 : 0);


		if (currentMouseEvent.getAction() == MouseEvent.PRESS) {

			if (mouseIsOverScrollBarH) {
				whichScrollBar = H_SCROLLBAR;
				int scrollhandle_posX = scrollhandle_posX();

				// if clicked on scrollhandle itself (instead of entire scroll area) the
				// dragging is started
				if (x > scrollhandle_posX && x < scrollhandle_posX + scrollhandle_width()) {
					startHandleDragPos = x - scrollhandle_posX;
				}

			} else if (mouseIsOverScrollBarV) {
				whichScrollBar = V_SCROLLBAR;

				int scrollhandle_posY = scrollhandle_posY();

				// if clicked on scrollhandle itself (instead of entire scroll area) the
				// dragging is started
				if (y > scrollhandle_posY && y < scrollhandle_posY + scrollhandle_height()) {
					startHandleDragPos = y - scrollhandle_posY;
				}

			}

		}

		// return false if mouse is over either scroll bar.
		// This way the items will not receive this mouse event.
		return !(mouseIsOverScrollBarV || mouseIsOverScrollBarH);
	}

}