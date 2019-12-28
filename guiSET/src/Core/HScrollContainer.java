package pGUI.core;

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

public class HScrollContainer extends HFlowContainer {

	// Width the scrollContainer would have summing up all its content
	// (fullScrollWidth can actually be smaller that width!)
	protected int fullScrollWidth;

	// Scroll position (only horizontal), starts at 0
	protected int scrollPosition;

	// speed at which container will be scrolled, can be set externally
	protected int scrollSpeed = 10;



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
	protected void calcBounds() {

		// get total virtual width of all elements rowed up
		fullScrollWidth = paddingLeft;
		for (Control c : content) {
			if (c.visible) {
				fullScrollWidth += c.marginRight + c.width + c.marginLeft;
			}
		}

		int usedSpace = paddingLeft;

		for (Control c : content) {

			if (c.visible) {
				c.bounds.X0 = this.bounds.X0 + usedSpace + c.marginLeft - scrollPosition;
				c.bounds.Y0 = this.bounds.Y0 + c.marginTop + paddingTop;

				c.bounds.X = Math.min(c.bounds.X0 + c.width, this.bounds.X);
				// if scrollbar displayed constrain childrens bounds some more
				c.bounds.Y = Math.min(c.bounds.Y0 + c.height, this.bounds.Y - (((float) width / fullScrollWidth < 1) ? scrollHandleStrength + 3 : 0));

				// constrain after computing X,Y so no data will be lost by constraining
				c.bounds.X0 = Math.max(c.bounds.X0, this.bounds.X0);
				c.bounds.Y0 = Math.max(c.bounds.Y0, this.bounds.Y0);

				usedSpace += (c.width + c.marginLeft + c.marginRight);
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

		scrollPosition = PApplet.constrain(scrollPosition, 0, PApplet.max(0, fullScrollWidth - width));

		int usedSpace = paddingLeft;

		for (Control c : content) {

			if (c.visible) {
				// don't draw and render if control is not visible (out of the containers bounds
				// due to scrolling)
				if (!(c.bounds.X0 > bounds.X || c.bounds.X < bounds.X0)) {
					containerRenderItem(c, usedSpace + c.marginLeft - scrollPosition, c.marginTop + paddingTop);
				}

				usedSpace += (c.width + c.marginLeft + c.marginRight);
			}
		}



		drawScrollbar();
	}









	// draw horizontal scrollbar if needed
	protected void drawScrollbar() {
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

	// horizontal scrollbar needed?
	protected boolean needsScrollbarH() {
		return width < fullScrollWidth;
	}


	// get width of entire horizontal scrollbar
	protected int scrollbar_width() {
		return width;
	}

	// get width of handle (of the horizontal scrollbar)
	protected float scrollhandle_width() {
		return (float) width / fullScrollWidth * scrollbar_width();
	}

	// get position of handle (of the horizontal scrollbar)
	protected float scrollhandle_posX() {
		int scrollbar_width = scrollbar_width();
		float scrollhandle_width = scrollhandle_width();

		return PApplet.constrain(scrollPosition * (scrollbar_width - scrollhandle_width) / (fullScrollWidth - width), 1,
				scrollbar_width - scrollhandle_width - 2);
	}

	/*
	 * SETTER
	 */
	public void setScrollPosition(int scrollPosition) {
		this.scrollPosition = scrollPosition; // will be constrained in render()
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

	public int getFullScrollWidth() {
		return fullScrollWidth;
	}


	public boolean isSlimScrollHandle() {
		return slim_scrollhandle;
	}


	/*
	 * MOUSE EVENTS
	 */

	@Override
	protected void mouseWheel(MouseEvent e) {
		// int temp = ScrollPosition;
		setScrollPosition(scrollPosition + e.getCount() * scrollSpeed);
		/*
		 * if (ScrollPosition != temp) { frame.stopPropagation(true) ;}
		 */
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
			float newScrollHandle_Pos = e.getX() - bounds.X0 - startHandleDragPos;
			float newScrollPosition = newScrollHandle_Pos * (float) (fullScrollWidth - width) / (scrollbar_width() - scrollhandle_width());
			setScrollPosition((int) newScrollPosition);
		}
	}




	@Override
	protected void mouseEvent(MouseEvent e) {
		if (visible) {

			/*
			 * handle the scrollbar dragging
			 */

			boolean mouseIsOverScrollArea = e.getY() > bounds.Y - scrollHandleStrength - 3 && e.getY() < bounds.Y && e.getX() > bounds.X0
					&& e.getX() < bounds.X;


			switch (e.getAction()) {
			case MouseEvent.PRESS:
				if (mouseIsOverScrollArea) {

					int scrollhandle_height = width * width / fullScrollWidth;
					float scrollhandle_posX = PApplet.constrain(scrollPosition * (width - scrollhandle_height) / (float) (fullScrollWidth - width), 1,
							width - scrollhandle_height - 2);

					// if clicked on scrollhandle itself (instead of entire scroll area) the
					// dragging is started
					if (e.getX() > scrollhandle_posX + bounds.X0 && e.getX() < scrollhandle_posX + bounds.X0 + scrollhandle_height) {
						startHandleDragPos = e.getX() - bounds.X0 - scrollhandle_posX;
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
