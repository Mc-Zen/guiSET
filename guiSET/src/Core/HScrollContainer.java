package guiSET.core;

/*
 * HScrollContainer is a container that allows horizontal scrolling. 
 * This way items can exceed the displayed width of the container. 
 * HScrollContainer ignores x, y and z-coordinate of the items and rows them up in order as added 
 * (but respecting margins).  
 * 
 * HScrollContainer also provides an ordinary draggable scroll handle which can be replaced with a slim version
 * for i.e inline-textboxes etc. (reminding of mobile phone scroll bars). 
 */

import processing.core.*;
import processing.event.*;


/**
 * A Container that layouts its content horizontally and enables a scrollbar if
 * the content overflows.
 * 
 * @author Mc-Zen
 *
 */
public class HScrollContainer extends HFlowContainer {

	// Width the scrollContainer would have summing up all its content
	// (fullScrollWidth can actually be smaller that width!)
	protected int fullScrollWidth;

	// Scroll position (only horizontal), starts at 0
	protected int scrollPosition;

	// speed at which container will be scrolled, can be set externally
	protected int scrollSpeed = DEFAULT_SCROLL_SPEED;



	// enable a thin version of scroll handle for small containers (i.e. smaller
	// textboxes)
	protected boolean slim_scrollhandle = false;




	public HScrollContainer() {
		super();
	}

	public HScrollContainer(int width, int height) {
		super(width, height);
	}





	@Override
	protected void render() {
		drawDefaultBackground();

		fullScrollWidth = paddingLeft;
		for (Control c : items) {
			if (c.visible) {
				fullScrollWidth += c.marginRight + c.width + c.marginLeft;
			}
		}

		scrollPosition = PApplet.constrain(scrollPosition, 0, PApplet.max(0, fullScrollWidth - width));

		int usedSpace = paddingLeft;
		for (Control c : items) {
			if (c.visible) {

				int cx0 = usedSpace + c.marginLeft - scrollPosition;
				int cy0 = c.marginTop + paddingTop;

				if (cx0 > width || cx0 + c.width < 0) {  // out of the containers bounds due to scrolling
					c.offsetX = width; // one should suffice
					c.offsetY = height;
				} else {
					renderItem(c, cx0, cy0);
				}

				usedSpace += (c.width + c.marginLeft + c.marginRight);
			}
		}

		drawScrollbar();
		drawDefaultDisabled();
	}









	// draw horizontal scrollbar if needed
	protected void drawScrollbar() {
		if (needsScrollbarH()) { // don't display scroll-bar when there's nothing to scroll

			pg.fill(SCROLL_BAR_COLOR);
			pg.noStroke();

			if (slim_scrollhandle) {
				pg.rect(scrollhandle_posX(), height - 1 - scrollHandleStrength, scrollhandle_width(), scrollHandleStrength, 15);
			} else {
//				pg.rect(0, height - 2 - scrollHandleStrength, scrollbar_width(), scrollHandleStrength + 3); // height is one more than necessary (just)
//				pg.fill(startHandleDragPos > -1 ? SCROLL_HANDLE_COLOR : SCROLL_HANDLE_PRESSED_COLOR);
//				pg.rect(scrollhandle_posX(), height - 1 - scrollHandleStrength, scrollhandle_width(), scrollHandleStrength, SCROLL_HANDLE_BORDER_RADIUS);

				pg.rect(0, height - 2 - scrollHandleStrength, scrollbar_width(), scrollHandleStrength + 3);
				pg.fill(startHandleDragPos > -1 ? SCROLL_HANDLE_COLOR : SCROLL_HANDLE_PRESSED_COLOR);
				pg.stroke(SCROLL_HANDLE_BORDER_COLOR);
				pg.rect(scrollhandle_posX(), height - 2 - scrollHandleStrength, scrollhandle_width(), scrollHandleStrength, SCROLL_HANDLE_BORDER_RADIUS);

			}
		}
	}



	/*
	 * some methods needed for controlling and drawing the scrollbars
	 */

	// horizontal scrollbar needed?
	protected boolean needsScrollbarH() {
		return width < fullScrollWidth;
	}


	// get width of entire horizontal scrollbar
	protected int scrollbar_width() {
		return width;
	}

	// get width of handle (of the horizontal scrollbar)
	protected int scrollhandle_width() {
		return Math.max(minScrollHandleLength, width * scrollbar_width() / fullScrollWidth);
	}

	// get position of handle (of the horizontal scrollbar)
	protected int scrollhandle_posX() {
		int scrollbar_width = scrollbar_width();
		float scrollhandle_width = scrollhandle_width();

		return (int) PApplet.constrain(scrollPosition * (scrollbar_width - scrollhandle_width) / (fullScrollWidth - width), 1,
				scrollbar_width - scrollhandle_width - 2);
	}


	/*
	 * useful function to ensure the given item or item to given index is displayed
	 * within the visible part of the container
	 */

	public void scrollToItem(int index) {
		if (index >= 0 && index < items.size()) {
			int x = paddingLeft;
			for (int i = 0; i < index; i++) {
				x += items.get(i).marginLeft + items.get(i).width + items.get(i).marginRight;
			}
			Control item = items.get(index);
			if (scrollPosition > x) {
				setScrollPosition(x);
			} else if (scrollPosition + width < x + item.width) {
				setScrollPosition(x - width + item.width + item.marginLeft + item.marginRight);
			}
		}
	}

	public void scrollToItem(Control item) {
		if (items.indexOf(item) == -1)
			return;

		int x = paddingLeft;
		for (int i = 0; i < items.size(); i++) {
			if (item == items.get(i))
				break;
			x += items.get(i).marginLeft + items.get(i).width + items.get(i).marginRight;
		}
		if (scrollPosition > x) {
			setScrollPosition(x);
		} else if (scrollPosition + width < x + item.width) {
			setScrollPosition(x - width + item.width + item.marginLeft + item.marginRight);
		}
	}


	/*
	 * SETTER
	 */

	/**
	 * Set scroll position in pixel from left.
	 * 
	 * @param scrollPosition scrollPosition
	 */
	public void setScrollPosition(int scrollPosition) {
		this.scrollPosition = scrollPosition; // will be constrained in render()
		update();
	}

	/**
	 * Amount of pixels to scroll for each step with the mouse wheel.
	 * 
	 * @param scrollSpeed scrollSpeed
	 */
	public void setScrollSpeed(int scrollSpeed) {
		this.scrollSpeed = scrollSpeed;
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

	public int getScrollPosition() {
		return scrollPosition;
	}

	public int getScrollSpeed() {
		return scrollSpeed;
	}

	public int getFullScrollWidth() {
		return fullScrollWidth;
	}

	public boolean isSlimScrollHandle() {
		return slim_scrollhandle;
	}

	@Override
	public int getAvailableHeight() {
		return height - paddingTop - paddingBottom - scrollHandleStrength - 1;
	}




	/*
	 * MOUSE EVENTS
	 */

	@Override
	protected void mouseWheel(MouseEvent e) {
		int temp = scrollPosition;
		setScrollPosition(scrollPosition + e.getCount() * scrollSpeed);
		if (scrollPosition != temp)
			stopPropagation();
	}

	/*
	 * ScrollHandle
	 * 
	 * When scrollHandle is dragged it sets scrollposition to corresponding
	 * position. Therefore the position of mouse when dragging started needs to be
	 * captured at press (in startHandleDragPos). Of course this is only done when
	 * clicking on the handle. A release will result in resetting this to -1.
	 * 
	 * When drag() is called by Frame the new scrollPosition is calculated.
	 * 
	 * 
	 * 
	 */

	protected int startHandleDragPos = -1;

	protected int scrollHandleStrength = SCROLL_HANDLE_STRENGTH_STD;
	protected static final int SCROLL_HANDLE_STRENGTH_STD = 12;
	protected static final int SCROLL_HANDLE_STRENGTH_SLIM = 3;

	@Override
	protected void drag(MouseEvent e) {
		if (startHandleDragPos > -1) {
			int newScrollHandle_Pos = e.getX() - getOffsetXWindow() - startHandleDragPos;
			float newScrollPosition = newScrollHandle_Pos * (float) (fullScrollWidth - width) / (scrollbar_width() - scrollhandle_width());
			setScrollPosition((int) newScrollPosition);
		}
	}



	// Do this here and not in containerPreItemsMouseEvent called, because the
	// latter is not called when released outside this element.
	@Override
	protected void release(MouseEvent e) {
		super.release(e);
		startHandleDragPos = -1;
	}


	/*
	 * Need to check if mouse is over the scroll bar. If so, then content items
	 * should not receive this mouse event (thus return false here in this case).
	 * 
	 * 
	 * (non-Javadoc)
	 * 
	 * @see guiSET.core.Container#containerPreItemsMouseEvent(int, int)
	 */
	@Override
	protected boolean containerPreItemsMouseEvent(int x, int y) {
		boolean mouseIsOverScrollBar = needsScrollbarH() && y > height - scrollHandleStrength - 3 && y < height && x > 0 && x < width;

		if (currentMouseEvent.getAction() == MouseEvent.PRESS && mouseIsOverScrollBar) {
			int scrollhandle_posX = scrollhandle_posX();

			// if clicked on scrollhandle itself (instead of entire scroll area) the
			// dragging is started
			if (x > scrollhandle_posX && x < scrollhandle_posX + scrollhandle_width()) {
				startHandleDragPos = x - scrollhandle_posX;
			}
		}
		return !mouseIsOverScrollBar;
	}



}
