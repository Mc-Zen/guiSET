package guiSET.core;

/*
 * VScrollContainer is a container that allows vertical scrolling. 
 * This way items can exceed the displayed height of the container. 
 * VScrollContainer ignores x, y and z-coordinate of the items and rows them up in order as added 
 * (but respecting margins).  
 * 
 * VScrollContainer also provides an ordinary draggable scroll handle which can be replaced with a slim version
 * for i.e inline-textboxes etc. (reminding of mobile phone scroll bars). 
 */

import processing.core.*;
import processing.event.*;


/**
 * A Container that layouts its content vertically and enables a scrollbar if
 * the content overflows.
 * 
 * @author Mc-Zen
 *
 */
public class VScrollContainer extends VFlowContainer {

	// Width the scrollContainer would have summing up all its content
	// (fullScrollHeight can actually be smaller that height!)
	protected int fullScrollHeight;

	// Scroll position (only vertical), starts at 0
	protected int scrollPosition;

	// speed at which container will be scrolled, can be set externally
	protected int scrollSpeed = 40;

	// enable a thin version of scroll handle for small containers (i.e. smaller
	// textboxes)
	protected boolean slim_scrollhandle = false;




	public VScrollContainer() {
		this(100, 100);
	}

	public VScrollContainer(int width, int height) {
		super(width, height);
	}







	@Override
	protected void render() {

		drawDefaultBackground();

		fullScrollHeight = paddingTop + paddingBottom;

		for (Control c : items) {
			if (c.visible) {
				fullScrollHeight += c.marginTop + c.height + c.marginBottom;
			}
		}
		// do this here and not in setScrollPosition() as fullscrollHeight might have
		// changed.
		scrollPosition = PApplet.constrain(scrollPosition, 0, PApplet.max(0, fullScrollHeight - height));

		int usedSpace = paddingTop;
		for (Control c : items) {
			if (c.visible) {
				// don't draw and render if control is not visible

				int cx0 = c.marginLeft + paddingLeft;
				int cy0 = usedSpace + c.marginTop - scrollPosition;

				if (cy0 > height || cy0 + c.height < 0) { // out of the containers bounds due to scrolling
					c.offsetX = width; // one should suffice
					c.offsetY = height;
				} else {
					renderItem(c, cx0, cy0);
				}

				usedSpace += (c.height + c.marginTop + c.marginBottom);
			}
		}

		drawScrollbar();
		drawDefaultDisabled();
	}


	// draw vertical scrollbar if needed
	protected void drawScrollbar() {
		if (needsScrollbarV()) { // don't display scroll-bar when there's nothing to scroll

			pg.fill(130);
			pg.noStroke();

			if (slim_scrollhandle) {
				pg.rect(width - 1 - scrollHandleStrength, scrollhandle_posY(), scrollHandleStrength, scrollhandle_height(), 15);
			} else {
				pg.rect(width - 2 - scrollHandleStrength, 0, scrollHandleStrength + 3, scrollbar_height());
				pg.fill(startHandleDragPos > -1 ? 170 : 190);
				pg.rect(width - 1 - scrollHandleStrength, scrollhandle_posY(), scrollHandleStrength, scrollhandle_height(), 3);
			}
		}
	}

	/*
	 * some methods needed for controlling and drawing the scrollbars
	 */

	protected boolean needsScrollbarV() {
		return height < fullScrollHeight;
	}

	// get height of entire vertical scrollbar, seems trivial but is not if
	// container has vertical and horizontal scrollbar
	protected int scrollbar_height() {
		return height;
	}

	// get height of handle
	protected int scrollhandle_height() {
		return height * scrollbar_height() / fullScrollHeight;
	}

	// get position of handle
	protected int scrollhandle_posY() {
		int scrollbar_height = scrollbar_height();
		float scrollhandle_height = scrollhandle_height();

		return (int) PApplet.constrain(scrollPosition * (scrollbar_height - scrollhandle_height) / (fullScrollHeight - height), 1,
				scrollbar_height - scrollhandle_height - 2);
	}








	/*
	 * useful function to ensure the given item or item to given index is displayed
	 * within the visible part of the container
	 */

	public void scrollToItem(int index) {
		if (index >= 0 && index < items.size()) {
			int y = paddingTop;
			for (int i = 0; i < index; i++) {
				y += items.get(i).marginTop + items.get(i).height + items.get(i).marginBottom;
			}
			Control item = items.get(index);
			if (scrollPosition > y) {
				setScrollPosition(y);
			} else if (scrollPosition + height < y + item.height) {
				setScrollPosition(y - height + item.height + item.marginTop + item.marginBottom);
			}
		}
	}

	public void scrollToItem(Control item) {
		if (items.indexOf(item) == -1)
			return;

		int y = paddingTop;
		for (int i = 0; i < items.size(); i++) {
			if (item == items.get(i))
				break;
			y += items.get(i).marginTop + items.get(i).height + items.get(i).marginBottom;
		}
		if (scrollPosition > y) {
			setScrollPosition(y);
		} else if (scrollPosition + height < y + item.height) {
			setScrollPosition(y - height + item.height + item.marginTop + item.marginBottom);
		}
	}








	/*
	 * SETTER
	 */
	/**
	 * Set scroll position in pixel from top.
	 * 
	 * @param scrollPosition scrollPosition
	 */
	public void setScrollPosition(int scrollPosition) {
		// will be constrained in render(), because since fullScrollHeight has been
		// calculated last time there might have been added a new item
		this.scrollPosition = scrollPosition;
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
	 * @param slim_scrollhandle slim scrollhandle
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

	public int getFullScrollHeight() {
		return fullScrollHeight;
	}

	public boolean isSlimScrollHandle() {
		return slim_scrollhandle;
	}

	@Override
	public int getAvailableWidth() {
		return width - paddingLeft - paddingRight - scrollHandleStrength - 1;
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
	 * ScrollHandle:
	 * 
	 * The entire bar at the side, filling out the available space is called "scrollBar", 
	 * the actual handle whose size depends on fullScrollHeight is the "scrollHandle". 
	 * 
	 * When scrollHandle is dragged, it sets scrollposition to corresponding
	 * position. Therefore, the position of mouse when dragging started needs to be
	 * captured at press (in startHandleDragPos). Of course, this is only done when
	 * clicking on the handle. A release will result in resetting this to -1.
	 * 
	 * When drag() is called by Frame the new scrollPosition is calculated.
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
			int newScrollHandle_Pos = e.getY() - getOffsetYWindow() - startHandleDragPos;
			int newScrollPosition = newScrollHandle_Pos * (fullScrollHeight - height) / (scrollbar_height() - scrollhandle_height());
			setScrollPosition(newScrollPosition);
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
		boolean mouseIsOverScrollBar = x > width - scrollHandleStrength - 3 && x < width && y > 0 && y < height;

		if (MouseEvent.PRESS == currentMouseEvent.getAction() && mouseIsOverScrollBar) {

			int scrollhandle_posY = scrollhandle_posY();

			// if clicked on scrollhandle itself (instead of entire scroll area) the
			// dragging is started
			if (y > scrollhandle_posY && y < scrollhandle_posY + scrollhandle_height()) {
				startHandleDragPos = y - scrollhandle_posY;
			}
		}
		return !mouseIsOverScrollBar;
	}


}

