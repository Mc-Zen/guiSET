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
 * A Container that layouts its content horizontally and enables a scrollbar if the content
 * overflows.
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
	protected int scrollSpeed = GuisetDefaultValues.scrollSpeed;

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

		fullScrollWidth = getPaddingLeft();
		for (Control c : items) {
			if (c.isVisible()) {
				fullScrollWidth += c.getMarginRight() + c.getWidth() + c.getMarginLeft();
			}
		}

		scrollPosition = PApplet.constrain(scrollPosition, 0, PApplet.max(0, fullScrollWidth - getWidth()));

		int usedSpace = getPaddingLeft();
		for (Control c : items) {
			if (c.isVisible()) {

				int cx0 = usedSpace + c.getMarginLeft() - scrollPosition;
				int cy0 = c.getMarginTop() + getPaddingTop();

				if (cx0 > getWidth() || cx0 + c.getWidth() < 0) {  // out of the containers bounds due to scrolling
					c.offsetX = getWidth(); // one should suffice
					c.offsetY = getHeight();
				} else {
					renderItem(c, cx0, cy0);
				}

				usedSpace += (c.getWidth() + c.getMarginLeft() + c.getMarginRight());
			}
		}

		drawScrollbar();
		drawDefaultDisabled();
	}









	// draw horizontal scrollbar if needed
	protected void drawScrollbar() {
		if (needsScrollbarH()) { // don't display scroll-bar when there's nothing to scroll

			pg.fill(GuisetGlobalValues.scrollBarColor);
			pg.noStroke();

			if (slim_scrollhandle) {
				pg.rect(scrollhandle_posX(), getHeight() - 1 - Constants.SCROLL_HANDLE_STRENGTH_SLIM, scrollhandle_width(), Constants.SCROLL_HANDLE_STRENGTH_SLIM, 15);
			} else {
				pg.rect(0, getHeight() - getScrollbarStrength(), scrollbar_width(), getScrollbarStrength());
				pg.fill(isDraggingScrollHandle() ? GuisetGlobalValues.scrollHandleColor : GuisetGlobalValues.scrollHandlePressColor);
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

	// horizontal scrollbar needed?
	protected boolean needsScrollbarH() {
		return getWidth() < fullScrollWidth;
	}


	// get width of entire horizontal scrollbar
	protected int scrollbar_width() {
		return getWidth();
	}

	// get width of handle (of the horizontal scrollbar)
	protected int scrollhandle_width() {
		return Math.max(Constants.MinimalScrollHandleLength, getWidth() * scrollbar_width() / fullScrollWidth);
	}

	// get position of handle (of the horizontal scrollbar)
	protected int scrollhandle_posX() {
		int scrollbar_width = scrollbar_width();
		float scrollhandle_width = scrollhandle_width();

		return (int) PApplet.constrain(scrollPosition * (scrollbar_width - scrollhandle_width) / (fullScrollWidth - getWidth()), 1,
				scrollbar_width - scrollhandle_width - 2);
	}

	int getScrollbarStrength() {
		return scrollHandleStrength + 2;
	}

	/*
	 * useful function to ensure the given item or item to given index is displayed
	 * within the visible part of the container
	 */

	public void scrollToItem(int index) {
		if (index >= 0 && index < items.size()) {
			int x = getPaddingLeft();
			for (int i = 0; i < index; i++) {
				x += items.get(i).getMarginLeft() + items.get(i).getWidth() + items.get(i).getMarginRight();
			}
			Control item = items.get(index);
			if (scrollPosition > x) {
				setScrollPosition(x);
			} else if (scrollPosition + getWidth() < x + item.getWidth()) {
				setScrollPosition(x - getWidth() + item.getWidth() + item.getMarginLeft() + item.getMarginRight());
			}
		}
	}

	public void scrollToItem(Control item) {
		if (items.indexOf(item) == -1)
			return;

		int x = getPaddingLeft();
		for (int i = 0; i < items.size(); i++) {
			if (item == items.get(i))
				break;
			x += items.get(i).getMarginLeft() + items.get(i).getWidth() + items.get(i).getMarginRight();
		}
		if (scrollPosition > x) {
			setScrollPosition(x);
		} else if (scrollPosition + getWidth() < x + item.getWidth()) {
			setScrollPosition(x - getWidth() + item.getWidth() + item.getMarginLeft() + item.getMarginRight());
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
			scrollHandleStrength = Constants.SCROLL_HANDLE_STRENGTH_SLIM;
		} else {
			scrollHandleStrength = Constants.SCROLL_HANDLE_STRENGTH_STD;
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
		return getHeight() - getPaddingTop() - getPaddingBottom() - scrollHandleStrength - 1;
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

	protected int scrollHandleStrength = Constants.SCROLL_HANDLE_STRENGTH_STD;

	@Override
	protected void drag(MouseEvent e) {
		if (isDraggingScrollHandle()) {
			int newScrollHandle_Pos = e.getX() - getOffsetXWindow() - startHandleDragPos;
			float newScrollPosition = newScrollHandle_Pos * (float) (fullScrollWidth - getWidth()) / (scrollbar_width() - scrollhandle_width());
			setScrollPosition((int) newScrollPosition);
		}
	}


	protected boolean isDraggingScrollHandle() {
		return startHandleDragPos != -1;
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
		boolean mouseIsOverScrollBar = needsScrollbarH() && y > getHeight() - getScrollbarStrength() && y < getHeight() && x > 0 && x < getWidth();

		if (currentMouseEvent.getAction() == MouseEvent.PRESS && mouseIsOverScrollBar) {
			int scrollhandle_posX = scrollhandle_posX();
			update();

			// if clicked on scrollhandle itself (instead of entire scroll area) the
			// dragging is started
			if (x > scrollhandle_posX && x < scrollhandle_posX + scrollhandle_width()) {
				startHandleDragPos = x - scrollhandle_posX;
			}
		}
		return !mouseIsOverScrollBar;
	}



}
