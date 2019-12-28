package pGUI.core;

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

public class VScrollContainer extends VFlowContainer {

	// Width the scrollContainer would have summing up all its content
	// (fullScrollHeight can actually be smaller that height!)
	protected int fullScrollHeight;

	// Scroll position (only vertical), starts at 0
	protected int scrollPosition;

	// speed at which container will be scrolled, can be set externally
	protected int scrollSpeed = 10;

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
	protected void calcBounds() {
		int usedSpace = paddingTop;

		fullScrollHeight = paddingTop;

		for (Control c : content) {
			if (c.visible) {
				fullScrollHeight += c.marginTop + c.height + c.marginBottom;
			}
		}

		// need to do this here because when trying to scroll further than possible
		// needs to be stopped
		scrollPosition = PApplet.constrain(scrollPosition, 0, PApplet.max(0, fullScrollHeight - height));

		for (Control c : content) {

			if (c.visible) {
				Frame.calcBoundsCount++;

				c.bounds.X0 = this.bounds.X0 + c.marginLeft + paddingLeft;
				c.bounds.Y0 = this.bounds.Y0 + usedSpace + c.marginTop - scrollPosition;

				// the 3 is the buffer that is included in the actually displayed scroll handle
				c.bounds.X = Math.min(c.bounds.X0 + c.width,
						this.bounds.X - (((float) height / fullScrollHeight < 1) ? scrollHandleStrength + 3 : 0));
				c.bounds.Y = Math.min(c.bounds.Y0 + c.height, this.bounds.Y);

				// constrain after computing X,Y so no data will be lost by constraining
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
		// if AutoSize is on, first get minimal dimensions
		if (autoSize) {
			setAutoSize();
		}

		drawDefaultBackground();


		scrollPosition = PApplet.constrain(scrollPosition, 0, PApplet.max(0, fullScrollHeight - height));

		int usedSpace = paddingTop;

		for (Control c : content) {


			if (c.visible) {

				// don't draw and render if control is not visible (out of the containers bounds
				// due to scrolling)

				if (!(c.bounds.Y0 > bounds.Y || c.bounds.Y < bounds.Y0)) {
					containerRenderItem(c, c.marginLeft + paddingLeft, usedSpace + c.marginTop - scrollPosition);
				}

				usedSpace += (c.height + c.marginTop + c.marginBottom);
			}
		}

		drawScrollbar();

	}


	// draw vertical scrollbar if needed
	protected void drawScrollbar() {
		if (needsScrollbarH()) { // don't display scroll-bar when there's nothing to scroll

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

	/*
	 * some methods needed for controlling and drawing the scrollbars
	 */

	protected boolean needsScrollbarH() {
		return height < fullScrollHeight;
	}

	// get height of entire vertical scrollbar
	protected int scrollbar_height() {
		return height;
	}

	// get height of handle (of the vertical scrollbar)
	protected float scrollhandle_height() {
		return (float) height / fullScrollHeight * scrollbar_height();
	}

	// get position of handle (of the vertical scrollbar)
	protected float scrollhandle_posY() {
		int scrollbar_height = scrollbar_height();
		float scrollhandle_height = scrollhandle_height();

		return PApplet.constrain(scrollPosition * (scrollbar_height - scrollhandle_height) / (fullScrollHeight - height), 1,
				scrollbar_height - scrollhandle_height - 2);
	}








	/*
	 * useful function to ensure the given item or item to given index is displayed
	 * within the visible part of the container
	 */

	public void scrollToItem(int index) {
		if (index >= 0 && index < content.size()) {
			float y = 0;
			for (int i = 0; i < index; i++) {
				y += content.get(i).marginTop + content.get(i).height + content.get(i).marginBottom;
			}
			if (scrollPosition > y) {
				setScrollPosition((int) y);
			} else if (scrollPosition + height < y + content.get(index).height) {
				setScrollPosition((int) (y - height + content.get(index).height + content.get(index).marginTop + content.get(index).marginBottom));
			}
		}
	}

	public void scrollToItem(Control item) {
		float y = 0;
		for (int i = 0; i < content.size(); i++) {
			if (item == content.get(i))
				break;
			y += content.get(i).marginTop + content.get(i).height + content.get(i).marginBottom;
		}
		if (scrollPosition > y) {
			setScrollPosition((int) y);
		} else if (scrollPosition + height < y + item.height) {
			setScrollPosition((int) (y - height + item.height + item.marginTop + item.marginBottom));
		}
	}








	/*
	 * SETTER
	 */

	public void setScrollPosition(int scrollPosition) {
		// will be constrained in render(), because since fullScrollHeight has been
		// calculated last time there might have been added a new item
		this.scrollPosition = scrollPosition;
		update();
	}

	public void setScrollSpeed(int scrollSpeed) {
		this.scrollSpeed = scrollSpeed;
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





	/*
	 * MOUSE EVENTS
	 */

	@Override
	protected void mouseWheel(MouseEvent e) {
		int temp = scrollPosition;
		setScrollPosition(scrollPosition + e.getCount() * scrollSpeed);
		if (scrollPosition != temp) {
			Frame.frame0.stopPropagation = true;
		}
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

	protected float startHandleDragPos = -1;

	protected int scrollHandleStrength = SCROLL_HANDLE_STRENGTH_STD;
	protected static final int SCROLL_HANDLE_STRENGTH_STD = 12;
	protected static final int SCROLL_HANDLE_STRENGTH_SLIM = 3;



	@Override
	protected void drag(MouseEvent e) {
		if (startHandleDragPos > -1) {
			float newScrollHandle_Pos = e.getY() - bounds.Y0 - startHandleDragPos;
			float newScrollPosition = newScrollHandle_Pos * (float) (fullScrollHeight - height) / (scrollbar_height() - scrollhandle_height());
			setScrollPosition((int) newScrollPosition);
		}
	}






	@Override
	protected void mouseEvent(MouseEvent e) {
		if (visible) {

			/*
			 * handle the scrollbar dragging
			 */

			boolean mouseIsOverScrollArea = e.getX() > bounds.X - scrollHandleStrength - 3 && e.getX() < bounds.X && e.getY() > bounds.Y0
					&& e.getY() < bounds.Y;


			switch (e.getAction()) {
			case MouseEvent.PRESS:
				if (mouseIsOverScrollArea) {

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

