package guiSET.core;

import processing.core.PApplet;
import processing.event.MouseEvent;

/**
 * A Container that allows both horizontal and vertical scrolling if the content exceeds the
 * ScrollAreas size. It does not layout its content as {@link VScrollContainer} or
 * {@link VScrollContainer} but keeps the relative x and y positions of the items.
 * 
 * @author Mc-Zen
 *
 */
public class ScrollArea extends Container {

	protected int scrollPositionX;
	protected int scrollPositionY;

	protected int fullScrollWidth;
	protected int fullScrollHeight;

	protected int scrollSpeedX = GuisetDefaultValues.scrollSpeed;
	protected int scrollSpeedY = GuisetDefaultValues.scrollSpeed;


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
			if (c.isVisible()) {
				fullScrollWidth = Math.max(fullScrollWidth, c.getX() + c.getWidth() + c.getMarginLeft());
				fullScrollHeight = Math.max(fullScrollHeight, c.getY() + c.getHeight() + c.getMarginBottom());
			}
		}
		fullScrollWidth += scrollHandleStrength + 3;
		fullScrollHeight += scrollHandleStrength + 3;

		scrollPositionX = PApplet.constrain(scrollPositionX, 0, PApplet.max(0, fullScrollWidth - getWidth()));
		scrollPositionY = PApplet.constrain(scrollPositionY, 0, PApplet.max(0, fullScrollHeight - getHeight()));


		for (Control c : items) {
			if (c.isVisible()) {
				renderItem(c, c.getX() - scrollPositionX, c.getY() - scrollPositionY);
			}
		}

		// rect at right bottom corner if both scrollbars active
		if (needsScrollbarH() && needsScrollbarV() && !slim_scrollhandle) {
			pg.fill(GuisetGlobalValues.scrollAreaBetweenScrollHandlesSquare);
			pg.noStroke();
			pg.rect(getWidth() - getScrollbarStrength(), getHeight() - getScrollbarStrength(), getScrollbarStrength(), getScrollbarStrength());
		}

		drawScrollbarH();
		drawScrollbarV();
		drawDefaultDisabled();
	}



	// draw vertical scrollbar if needed
	protected void drawScrollbarV() {
		if (needsScrollbarV()) { // don't display scroll-bar when there's nothing to scroll

			pg.fill(GuisetGlobalValues.scrollBarColor);
			pg.noStroke();

			if (slim_scrollhandle) {
				pg.rect(getWidth() - 1 - Constants.SCROLL_HANDLE_STRENGTH_SLIM, scrollhandle_posY(), Constants.SCROLL_HANDLE_STRENGTH_SLIM, scrollhandle_height(), 15);

			} else {
				pg.rect(getWidth() - getScrollbarStrength(), 0, getScrollbarStrength(), scrollbar_height());
				pg.fill(isDraggingVScrollHandle() ? GuisetGlobalValues.scrollHandleColor : GuisetGlobalValues.scrollHandlePressColor);
				pg.strokeWeight(1);
				pg.stroke(GuisetGlobalValues.scrollHandleBorderColor);
				pg.rect(getWidth() - getScrollbarStrength(), scrollhandle_posY(), scrollHandleStrength, scrollhandle_height(),
						GuisetGlobalValues.scrollHandleBorderRadius);
			}
		}
	}


	// draw horizontal scrollbar if needed
	protected void drawScrollbarH() {
		if (needsScrollbarH()) { // don't display scroll-bar when there's nothing to scroll

			pg.fill(GuisetGlobalValues.scrollBarColor);
			pg.noStroke();

			if (slim_scrollhandle) {
				pg.rect(scrollhandle_posX(), getHeight() - 1 - Constants.SCROLL_HANDLE_STRENGTH_SLIM, scrollhandle_width(), Constants.SCROLL_HANDLE_STRENGTH_SLIM, 15);
			} else {
				pg.rect(0, getHeight() - getScrollbarStrength(), scrollbar_width(), getScrollbarStrength());
				pg.fill(isDraggingHScrollHandle() ? GuisetGlobalValues.scrollHandleColor : GuisetGlobalValues.scrollHandlePressColor);
				pg.strokeWeight(1);
				pg.stroke(GuisetGlobalValues.scrollHandleBorderColor);
				pg.rect(scrollhandle_posX(), getHeight() - getScrollbarStrength(), scrollhandle_width(), scrollHandleStrength,
						GuisetGlobalValues.scrollHandleBorderRadius);

			}
		}
	}


	/*
	 * some methods needed for controlling and drawing the scrollbars
	 */

	// vertical scrollbar needed?
	protected boolean needsScrollbarV() {
		return getHeight() < fullScrollHeight;
	}

	// horizontal scrollbar needed?
	protected boolean needsScrollbarH() {
		return getWidth() < fullScrollWidth;
	}

	// get height of entire vertical scrollbar (usually full height of element
	// except when also having a horizontal scrollbar
	protected int scrollbar_height() {
		return getHeight() - (needsScrollbarH() ? getScrollbarStrength() : 0);
	}

	// get width of entire horizontal scrollbar (usually full width of element
	// except when also having a vertical scrollbar
	protected int scrollbar_width() {
		return getWidth() - (needsScrollbarV() ? getScrollbarStrength() : 0);
	}

	int getScrollbarStrength() {
		return scrollHandleStrength + 2;
	}


	// get height of handle (of the vertical scrollbar)
	protected int scrollhandle_height() {
		return Math.max(Constants.MinimalScrollHandleLength, getHeight() * scrollbar_height() / fullScrollHeight);
	}

	// get width of handle (of the horizontal scrollbar)
	protected int scrollhandle_width() {
		return Math.max(Constants.MinimalScrollHandleLength, getWidth() * scrollbar_width() / fullScrollWidth);
	}

	// get position of handle (of the vertical scrollbar)
	protected int scrollhandle_posY() {
		int scrollbar_height = scrollbar_height();
		float scrollhandle_height = scrollhandle_height();

		return (int) PApplet.constrain(scrollPositionY * (scrollbar_height - scrollhandle_height) / (fullScrollHeight - getHeight()), 1,
				scrollbar_height - scrollhandle_height - 2);
	}

	// get position of handle (of the horizontal scrollbar)
	protected int scrollhandle_posX() {
		int scrollbar_width = scrollbar_width();
		float scrollhandle_width = scrollhandle_width();

		return (int) PApplet.constrain(scrollPositionX * (scrollbar_width - scrollhandle_width) / (fullScrollWidth - getWidth()), 1,
				scrollbar_width - scrollhandle_width - 2);
	}





	/*
	 * SETTER
	 */
	/**
	 * Amount of pixels to scroll for each step with the mouse wheel in horizontal direction.
	 * 
	 * @param scrollSpeedX scrollSpeedX
	 */
	public void setScrollSpeedX(int scrollSpeedX) {
		this.scrollSpeedX = scrollSpeedX;
	}

	/**
	 * Amount of pixels to scroll for each step with the mouse wheel in vertical direction.
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
			scrollHandleStrength = Constants.SCROLL_HANDLE_STRENGTH_SLIM;
		} else {
			scrollHandleStrength = Constants.SCROLL_HANDLE_STRENGTH_STD;
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
		if (getFrame().isShiftDown()) {
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

	protected int scrollHandleStrength = Constants.SCROLL_HANDLE_STRENGTH_STD; // thickness


	protected boolean isDraggingScrollHandle() {
		return startHandleDragPos != -1;
	}

	protected boolean isDraggingVScrollHandle() {
		return isDraggingScrollHandle() && whichScrollBar == V_SCROLLBAR;
	}

	protected boolean isDraggingHScrollHandle() {
		return isDraggingScrollHandle() && whichScrollBar == H_SCROLLBAR;
	}

	@Override
	protected void drag(MouseEvent e) {
		if (isDraggingScrollHandle()) {
			if (whichScrollBar == H_SCROLLBAR) {

				int newScrollHandle_Pos = e.getX() - getOffsetXWindow() - startHandleDragPos;
				int newScrollPosition = newScrollHandle_Pos * (fullScrollWidth - getWidth()) / (scrollbar_width() - scrollhandle_width());
				setScrollPositionX(newScrollPosition);

			} else if (whichScrollBar == V_SCROLLBAR) {

				int newScrollHandle_Pos = e.getY() - getOffsetYWindow() - startHandleDragPos;
				int newScrollPosition = newScrollHandle_Pos * (fullScrollHeight - getHeight()) / (scrollbar_height() - scrollhandle_height());
				setScrollPositionY(newScrollPosition);

			}
		}
	}

	// Do this here and not in containerPreItemsMouseEvent called, because the
	// latter is not called when released outside this element.
	@Override
	protected void release(MouseEvent e) {
		super.release(e);
		if (isDraggingScrollHandle()) {
			startHandleDragPos = -1;
			update();
		}
	}

	/**
	 * Need to check if mouse is over one of the scroll bars. If so, then content items should not
	 * receive this mouse event (thus return false here in this case).
	 * 
	 * 
	 * (non-Javadoc)
	 * 
	 * @see guiSET.core.Container#containerPreItemsMouseEvent(int, int)
	 */
	@Override
	protected boolean containerPreItemsMouseEvent(int x, int y) {
		boolean mouseIsOverScrollBarV = needsScrollbarV() && x > getWidth() - getScrollbarStrength() && x < getWidth() && y > 0
				&& y < getHeight() - (needsScrollbarV() ? getScrollbarStrength() : 0);
		boolean mouseIsOverScrollBarH = needsScrollbarH() && y > getHeight() - getScrollbarStrength() && y < getHeight() && x > 0
				&& x < getWidth() - (needsScrollbarH() ? getScrollbarStrength() : 0);


		if (currentMouseEvent.getAction() == MouseEvent.PRESS) {

			if (mouseIsOverScrollBarH) {
				whichScrollBar = H_SCROLLBAR;
				int scrollhandle_posX = scrollhandle_posX();
				update();

				// if clicked on scrollhandle itself (instead of entire scroll area) the
				// dragging is started
				if (x > scrollhandle_posX && x < scrollhandle_posX + scrollhandle_width()) {
					startHandleDragPos = x - scrollhandle_posX;
				}

			} else if (mouseIsOverScrollBarV) {
				whichScrollBar = V_SCROLLBAR;
				int scrollhandle_posY = scrollhandle_posY();
				update();

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