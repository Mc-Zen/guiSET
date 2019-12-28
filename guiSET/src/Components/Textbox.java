package pGUI.core;

/*
 * A one-line textbox for inserting text via keyboard.
 *  
 * Comes with expected features like cursor, selection, some keyboard shortcuts and standard combinations,
 * scrolling etc...
 * 
 * If submitOnEnter isn't set to false the return key will trigger the submit-event and blur the focus on 
 * the textbox. 
 * 
 */


import processing.core.*;
import processing.event.*;

//clipboard
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.StringSelection;
import java.io.*;

import java.awt.datatransfer.DataFlavor;

public class Textbox extends HScrollContainer {

	protected int selectionColor = -13395457;
	protected int cursorColor = 70;
	protected String hint = ""; // text to display when textbox is empty

	protected int cursorPosition;
	protected int selectionStart;
	protected int selectionEnd;

	protected boolean autoScroll; // autoscroll to cursor when text changed, dont set it

	// measure time to create blink animation
	protected int cursorTime;
	// cursor currently displayed or not in animation cycle
	protected boolean currentDisplayCurs;

	// loose focus and call submit event when hit enter
	public boolean submitOnEnter = true;




	public Textbox() {
		this(100, 20, 13);

	}

	public Textbox(int width, int height) {
		this(width, height, 13);
	}

	public Textbox(int width, int height, int fontSize) {

		foregroundColor = 0;
		backgroundColor = 220;
		hoverColor = 220;
		visualBackgroundColor = backgroundColor;

		this.width = width;
		this.fontSize = fontSize;
		cursor = PApplet.TEXT;
		setPadding(5);
		autosize();
		setSlimScrollHandle(true);

		overridesParentsShortcuts = true;

		setupListeners(3); // 3 additional listeners (textchanged, key, submit(enter))

		// cursor animation:
		Frame.frame0.papplet.registerMethod("pre", this);
	}








	@Override
	protected void render() {

		drawDefaultBackground();

		if (borderWidth == 0) {
			// draw 3D-Border
			pg.strokeWeight(1);
			pg.stroke(70);
			pg.line(0, 0, width, 0);
			pg.line(0, 0, 0, height);
		}

		/*
		 * prepare text style
		 */
		pg.textSize(fontSize);
		pg.textAlign(PApplet.LEFT, PApplet.TOP);

		// do this before drawing cursor!! - needs new fullScrollWidth
		fullScrollWidth = (int) pg.textWidth(text) + paddingLeft + paddingRight;
		scrollPosition = Math.max(0, Math.min(scrollPosition, Math.max(0, fullScrollWidth - width)));

		/*
		 * draw cursor if textbox is focused and animation currently is in display cycle
		 */
		if (this.focused && currentDisplayCurs) {
			drawCursor();
		}

		/*
		 * draw selection
		 */
		if (selectionStart < selectionEnd && focused) {
			if (selectionStart <= text.length() && selectionEnd <= text.length()) {
				int selectionX = (int) pg.textWidth(text.substring(0, selectionStart));
				int selectionWidth = (int) pg.textWidth(text.substring(selectionStart, selectionEnd));
				pg.fill(selectionColor);
				pg.noStroke();
				pg.rect(paddingLeft - scrollPosition + selectionX + fontSize / 40f, paddingTop, selectionWidth + fontSize / 40f,
						height - paddingBottom - paddingTop + pg.textDescent());
			}
		}

		/*
		 * draw text
		 */
		if (!text.equals("")) {
			pg.fill(foregroundColor);
			pg.text(text, paddingLeft - scrollPosition, paddingTop);
		} else {
			pg.fill(120);
			pg.text(hint, paddingLeft, paddingTop);
		}

		/*
		 * draw scrollbar
		 */
		drawScrollbar();


		standardDisabled();
		

	}




	/*
	 * Cursor managing
	 */

	// cursor blink animation
	public void pre() {
		if (this.focused) {
			int t = Frame.frame0.papplet.millis();
			if (t - cursorTime > 500) {
				currentDisplayCurs = !currentDisplayCurs;
				cursorTime = t;
				update();
			}
		}
	}

	// draw cursor to the graphics
	protected void drawCursor() {
		cursorPosition = Math.max(0, Math.min(text.length(), cursorPosition));

		// get width of text before cursor in pixels; add little extra space
		float wordWidth = pg.textWidth(text.substring(0, cursorPosition)) + fontSize / 40f;

		int cursorHeight = fontSize;

		// do this before drawing the cursor - scrollPositionX has to be set first!!
		if (autoScroll) {
			if (wordWidth - scrollPosition > width - paddingRight - paddingLeft) { // cursor left visible box at the
				// right
				setScrollPosition((int) (wordWidth - width + paddingRight + paddingLeft));
			} else if (wordWidth < scrollPosition + paddingLeft) { // cursor left visible box at the left
				setScrollPosition((int) wordWidth);
			}
			autoScroll = false;
		}
		pg.stroke(cursorColor);
		pg.line(paddingLeft + wordWidth - scrollPosition, paddingTop, wordWidth + paddingLeft - scrollPosition,
				cursorHeight + paddingTop /* + pg.textDescent() */);
	}

	@Override
	public void focus() {
		super.focus();
		cursorTime = Frame.frame0.papplet.millis();
		currentDisplayCurs = true;
	}




	/*
	 * INTERNAL TEXT EDITING METHODS
	 */

	// append character at cursorPosition
	protected void append(char c) {
		if (c != '\n') { // don't allow line breaks
			text = text.substring(0, cursorPosition) + c + text.substring(cursorPosition);
			cursorPosition += 1;
			textChanged();
		}
	}

	// append string at cursorPosition
	protected void append(String s) {
		s = s.replaceAll("\\r\\n|\\r|\\n", " ");
		text = text.substring(0, cursorPosition) + s + text.substring(cursorPosition);
		cursorPosition += s.length();
		textChanged();
	}

	// del char after cursor
	protected void backspace() {
		if (text.length() > 0 && cursorPosition >= 1) {
			text = text.substring(0, cursorPosition - 1) + text.substring(cursorPosition);
			cursorPosition--;
			textChanged();
		}
	}

	// del char before cursor
	protected void deleteKey() {
		if (text.length() > cursorPosition) {
			text = text.substring(0, cursorPosition) + text.substring(cursorPosition + 1);
			textChanged();
		}
	}

	// delete between start and end index
	protected void deleteRange(int start, int end) {
		start = Math.max(0, Math.min(text.length(), start));
		end = Math.max(0, Math.min(text.length(), end));

		if (start > end) {
			int temp = start;
			start = end;
			end = temp;
		}
		text = text.substring(0, start) + text.substring(end);
		textChanged();
	}



	// called whenether the text has been altered through user interaction
	protected void textChanged() {
		handleRegisteredEventMethod(TEXTCHANGED_EVENT, null);
		cursorPositionChanged();
	}

	// called whenether the cursor position changed due to user interaction or text
	// edits
	protected void cursorPositionChanged() {
		cursorTime = Frame.frame0.papplet.millis();
		currentDisplayCurs = true;
		selectionStart = cursorPosition;
		selectionEnd = cursorPosition;
		autoScroll = true;
		update();
	}

	protected void moveCursorBy(int ammount) {
		cursorPosition = PApplet.constrain(cursorPosition + ammount, 0, text.length());
		cursorPositionChanged();
	}

	protected void moveCursorTo(int position) {
		cursorPosition = PApplet.constrain(position, 0, text.length());
		cursorPositionChanged();
	}





	/*
	 * GETTER AND SETTER
	 */

	public void setCursorColor(int cursorColor) {
		this.cursorColor = cursorColor;
		update();
	}

	public void setCursorPosition(int cursorPosition) {
		moveCursorTo(cursorPosition);
	}

	public void setSelectionStart(int selectionStart) {
		this.selectionStart = Math.max(0, Math.min(text.length(), selectionStart));
	}

	public void setSelectionEnd(int selectionEnd) {
		this.selectionEnd = Math.max(0, Math.min(text.length(), selectionEnd));

	}

	public void setSelectionColor(int selectionColor) {
		this.selectionColor = selectionColor;
	}

	public void setHint(String hint) {
		this.hint = hint;
	}

	@Override
	public void setText(String text) {
		this.text = text.replaceAll("\\r\\n|\\r|\\n", " ");
		cursorPosition = PApplet.constrain(cursorPosition, 0, text.length());
		update();
	}





	public int getCursorColor() {
		return cursorColor;
	}

	public int getCursorPosition() {
		return cursorPosition;
	}

	public int getSelectionColor() {
		return selectionColor;
	}

	public String getHint() {
		return hint;
	}






	@Override
	protected void autosize() {
		height = fontSize + paddingTop + paddingBottom;
	}


	/*
	 * start at cursorPosition and iterate over the text to find the next
	 * space/bracket/comma etc...
	 */

	protected int findNextStop() {
		// in first phase search for next space, in second search for first letter
		int phase = 0;

		String delimiters = " \n+-()[] {}().,:;_*\"\'§$%&/=?!";

		for (int i = cursorPosition + 1; i < text.length(); i++) {
			if (phase == 0) {

				if (delimiters.indexOf(text.charAt(i)) > -1) {
					if (text.charAt(i) != ' ') {
						return i;// i == cursorPosition ? i+1 : i;
					}
					phase = 1;
				}

			} else {
				if (text.charAt(i) != ' ' && text.charAt(i) != '\n')
					return i;
			}
		}
		// reached ending of text
		return text.length();
	}


	/*
	 * start at cursorPosition and iterate over the text to find the first
	 * space/bracket/comma etc. in reverse direction
	 */

	protected int findPreviousStop() {

		String delimiters = " \n+-()[] {}().,:;_*\"\'§$%&/=?!";

		for (int i = cursorPosition - 2; i > 0; i--) {

			if (delimiters.indexOf(text.charAt(i)) > -1) {
				return i + 1;
			}

		}
		// reached beginning of text
		return 0;
	}






	/*
	 * EVENT METHODS
	 */

	protected static final int KEY_EVENT = Frame.numberMouseListeners;
	protected static final int TEXTCHANGED_EVENT = Frame.numberMouseListeners + 1;
	protected static final int SUBMIT_EVENT = Frame.numberMouseListeners + 2;


	public void addKeyListener(String methodName, Object target) {
		registerEventRMethod(KEY_EVENT, methodName, target, KeyEvent.class);
	}

	public void addKeyListener(String methodName) {
		addKeyListener(methodName, Frame.frame0.papplet);
	}

	public void removeKeyListener() {
		deregisterEventRMethod(KEY_EVENT);
	}

	public void addTextChangedListener(String methodName, Object target) {
		registerEventRMethod(TEXTCHANGED_EVENT, methodName, target, null);
	}

	public void addTextChangedListener(String methodName) {
		addTextChangedListener(methodName, Frame.frame0.papplet);
	}

	public void removeTextChangedListener() {
		deregisterEventRMethod(TEXTCHANGED_EVENT);
	}

	public void addSubmitListener(String methodName, Object target) {
		registerEventRMethod(SUBMIT_EVENT, methodName, target, null);
	}

	public void addSubmitListener(String methodName) {
		addSubmitListener(methodName, Frame.frame0.papplet);
	}

	public void removeSubmitListener() {
		deregisterEventRMethod(SUBMIT_EVENT);
	}



	@Override
	protected void enter(MouseEvent e) {
		visualBackgroundColor = hoverColor;
		update();
	}

	@Override
	protected void exit(MouseEvent e) {
		visualBackgroundColor = backgroundColor;
		update();
	}

	protected int selectionInitial; // cursor at the time the dragging started



	@Override
	public void press(MouseEvent e) {

		if (e.getCount() < 2) {
			setCursorByClick(e.getX());

			selectionInitial = cursorPosition;
			selectionStart = cursorPosition;
			selectionEnd = cursorPosition;

			// should not be necessary as every control gets focus upon pressing it
			//this.focus();

		} else { // double click
			setCursorByClick(e.getX());

			selectionStart = findPreviousStop();
			selectionEnd = findNextStop();

		}
	}

	protected void setCursorByClick(int mX) {
		// relative to textbox origin and ind respect to fullScrollWidth
		int clickedPos = mX - bounds.X0 + scrollPosition - paddingLeft;
		float wide = 0;
		for (int i = 0; i < text.length(); i++) {
			float letterWidth = pg.textWidth(text.substring(i, i + 1));
			wide += letterWidth;
			if (wide - letterWidth / 2 > clickedPos) { // set decision point to the center of the letter
				moveCursorTo(i);
				break;
			}
		}
		if (wide < clickedPos) { // in case clicked beyond last letter - set cursor to end
			moveCursorTo(text.length());
		}
	}


	@Override
	protected void drag(MouseEvent e) {
		super.drag(e); // need to handle scrollbar stuff

		if (startHandleDragPos == -1) { // only select text if not dragging scrollbar
			setCursorByClick(e.getX());

			if (cursorPosition > selectionInitial) {
				selectionStart = selectionInitial;
				selectionEnd = cursorPosition;
			} else {
				selectionStart = cursorPosition;
				selectionEnd = selectionInitial;
			}

			// scroll a bit when at left or right edge
			if (e.getX() - bounds.X0 < 10) {
				setScrollPosition(scrollPosition - 10);
			} else if (bounds.X - e.getX() < 10) {
				setScrollPosition(scrollPosition + 10);
			}
		}
	}

	@Override
	protected void mouseWheel(MouseEvent e) {
		if (focused) {
			super.mouseWheel(e);
			// never allow parent scrolling when textbox has scrolled -> nicer user handling
			Frame.stopPropagation();
		}
	}





	@Override
	public void onKeyPress(KeyEvent e) {
		if (enabled) {
			char key = e.getKey();
			int code = e.getKeyCode();

			boolean ctrl = e.isControlDown();
			boolean shft = e.isShiftDown();
			boolean alt = e.isAltDown();


			/*
			 * Manage copying, pasting, cutting and selecting everything
			 */

			if (ctrl && !shft && !alt) {
				switch ((char) code) {

				case 'C':
					copy();
					break;

				case 'V':
					paste();
					break;

				// select everything
				case 'A':
					selectionStart = 0;
					selectionEnd = text.length();
					update();
					break;

				case 'X':
					cut();
					break;
				}
			}



			/*
			 * Manage other inputs.
			 */

			if (key == PApplet.CODED) {
				switch (code) {

				case PApplet.LEFT:
					int selEnd = selectionEnd;

					if (ctrl) {
						moveCursorTo(findPreviousStop());
					} else {
						moveCursorBy(-1);
					}
					if (shft) {
						selectionEnd = selEnd;
					}
					break;

				case PApplet.RIGHT:
					int selStart = selectionStart;

					if (ctrl) {
						moveCursorTo(findNextStop());
					} else {
						moveCursorBy(1);
					}
					if (shft) {
						selectionStart = selStart;
					}
					break;

				case 35: // END key
					moveCursorTo(text.length());
					break;

				case 36: // Pos1 key
					moveCursorTo(0);
					break;
				}
			}


			/*
			 * No coded keys (control), but enable Alt Gr (Alt + Control)
			 */


			else if (!ctrl || alt) {
				switch (key) {
				case PApplet.BACKSPACE:
					if (selectionStart < selectionEnd) {

						// store this because delete will call cursorChanged which resets selectionStart
						int selStart = selectionStart;
						deleteRange(selectionStart, selectionEnd);
						moveCursorTo(selStart);
					} else {
						backspace();
					}
					break;

				case PApplet.DELETE:
					if (selectionStart < selectionEnd) {

						// store this because delete will call cursorChanged which resets selectionStart
						int selStart = selectionStart;
						deleteRange(selectionStart, selectionEnd);
						moveCursorTo(selStart);
					} else {
						deleteKey();
					}
					break;

				case PApplet.RETURN: // for macinthosh
				case PApplet.ENTER:
					if (submitOnEnter) {
						this.blur();
						handleRegisteredEventMethod(SUBMIT_EVENT, null);
						update();
					}
					break;

				default:
					if (selectionStart < selectionEnd) { // if there is a selection then replace

						// store this because delete will call cursorChanged which resets selectionStart
						int selStart = selectionStart;
						deleteRange(selectionStart, selectionEnd);
						moveCursorTo(selStart);
					}
					append(key);
				}
			} else if (ctrl && key == PApplet.BACKSPACE) { // delete last word
				int xx = findPreviousStop();
				deleteRange(xx, selectionEnd);
				moveCursorTo(xx);

			}
		}
		handleRegisteredEventMethod(KEY_EVENT, e);
	}


	protected void copy() {
		if (selectionStart < selectionEnd) {
			StringSelection selection = new StringSelection(text.substring(selectionStart, selectionEnd));
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(selection, selection);
		}

	}

	protected void paste() {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable contents = clipboard.getContents(null);
		if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			try {
				String content = (String) contents.getTransferData(DataFlavor.stringFlavor);
				if (selectionStart < selectionEnd) { // if there is a selection then replace

					// store this because delete will call cursorChanged which resets selectionStart
					int selStart = selectionStart;
					deleteRange(selectionStart, selectionEnd);
					moveCursorTo(selStart);
				}
				this.append(content);
			} catch (UnsupportedFlavorException ex) {
				System.out.println(ex);
				ex.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	protected void cut() {
		copy();
		deleteRange(selectionStart, selectionEnd);
		selectionStart = 0;
		selectionEnd = 0;


	}
}