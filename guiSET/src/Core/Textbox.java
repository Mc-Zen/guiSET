package guiSET.core;

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



/**
 * A single-line Textbox that behaves just as you would expect one to behave.
 * When focused (by click) text can be inserted via keyboard. Comes with
 * expected features like cursor, selection, some keyboard shortcuts for
 * cut/copy/paste, scrolling etc...
 * 
 * 
 * 
 * @author Mc-Zen
 *
 */
public class Textbox extends HScrollContainer {

	protected int selectionColor = -13395457;
	protected int cursorColor = -12171706; // color(70)
	protected String hint = ""; // text to display when textbox is empty

	protected int cursorPosition;
	protected int selectionStart;
	protected int selectionEnd;


	// measure time to create blink animation
	protected int cursorTime;
	// cursor currently displayed or not in animation cycle
	protected boolean currentDisplayCurs;

	// loose focus and call submit event when hit enter
	/**
	 * If submitOnEnter isn't set to false the return/enter key will trigger the
	 * submit-event and blur the focus on the textbox.
	 */
	public boolean submitOnEnter = true;

	// if false, then user can select text and copy but not insert or type
	protected boolean inputEnabled = true;



	public Textbox() {
		this(100, 13);
	}

	public Textbox(int width) {
		this(width, 13);
	}

	public Textbox(int width, int fontSize) {
		super(width, 20); // height does not matter

		foregroundColor = -16777216;
		setBackgroundColor(-2302756);

		setFontSize(fontSize);
		cursor = PApplet.TEXT;
		setPadding(5);
		setSlimScrollHandle(true);

		overridesFrameShortcuts = true;

		// cursor animation:
		getPApplet().registerMethod("pre", this);
	}





	protected boolean needsScrolling; // autoscroll once to cursor when text changed, dont set it



	@Override
	protected void render() {
		drawDefaultBackground();

		if (borderWidth == 0) {
			// draw "3D"-Border
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
		fullScrollWidth = (int) textWidth(text) + paddingLeft + paddingRight;
		scrollPosition = Math.max(0, Math.min(scrollPosition, Math.max(0, fullScrollWidth - width)));

		/*
		 * draw cursor if textbox is focused and animation currently is in display cycle
		 */
		if (focused && currentDisplayCurs) {
			drawCursor();
		}

		/*
		 * draw selection
		 */
		if (focused && selectionStart < selectionEnd) {
			if (selectionStart <= text.length() && selectionEnd <= text.length()) {
				int selectionX = (int) pg.textWidth(text.substring(0, selectionStart));
				int selectionWidth = (int) pg.textWidth(text.substring(selectionStart, selectionEnd));
				pg.fill(selectionColor);
				pg.noStroke();
				pg.rect(paddingLeft - scrollPosition + selectionX + fontSize / 40f, paddingTop, selectionWidth + fontSize / 40f,
						fontSize + pg.textDescent());
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

		drawDefaultDisabled();
	}




	/*
	 * Cursor managing
	 */

	// cursor blink animation
	/**
	 * DO NOT CALL THIS METHOD. It is for internal purposes only and unfortunately
	 * needs to be public.
	 */
	public void pre() {
		if (this.focused) {
			int t = getPApplet().millis();
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

		float cursorHeight = fontSize;

		// do this before drawing the cursor - scrollPositionX has to be set first!!
		if (needsScrolling) {
			if (wordWidth - scrollPosition > width - paddingRight - paddingLeft) { // cursor has left visible box at the right
				setScrollPosition((int) (wordWidth - width + paddingRight + paddingLeft));
			} else if (wordWidth < scrollPosition + paddingLeft) { // cursor has left visible box at the left
				setScrollPosition((int) wordWidth);
			}
			needsScrolling = false;
		}
		pg.stroke(cursorColor);
		pg.line(paddingLeft + wordWidth - scrollPosition, paddingTop, wordWidth + paddingLeft - scrollPosition,
				cursorHeight + paddingTop /* + pg.textDescent() */);
	}

	@Override
	public void focus() {
		super.focus();
		cursorTime = getPApplet().millis();
		currentDisplayCurs = true;
	}




	/*
	 * INTERNAL TEXT EDITING METHODS
	 */

	// append character at cursorPosition
	protected void append(char c) {
		if (!inputEnabled)
			return;
		if (c != '\n' && c != '\r') { // don't allow line breaks
			text = text.substring(0, cursorPosition) + c + text.substring(cursorPosition);
			cursorPosition += 1;
			textChanged();
		}
	}

	// append string at cursorPosition
	protected void append(String s) {
		if (!inputEnabled)
			return;
		s = s.replaceAll("\\r\\n|\\r|\\n", " ");
		text = text.substring(0, cursorPosition) + s + text.substring(cursorPosition);
		cursorPosition += s.length();
		textChanged();
	}

	// del char after cursor
	protected void backspace() {
		if (text.length() > 0 && cursorPosition > 0) {
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
		handleEvent(textChangeListener, null);
		cursorPositionChanged();
	}

	// called whenether the cursor position changed due to user interaction or text
	// edits
	protected void cursorPositionChanged() {
		cursorTime = getPApplet().millis();
		currentDisplayCurs = true;
		selectionStart = cursorPosition;
		selectionEnd = cursorPosition;
		needsScrolling = true;
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


	/**
	 * Set the color of the cursor.
	 * 
	 * @param cursorColor rgb integer color
	 */
	public void setCursorColor(int cursorColor) {
		this.cursorColor = cursorColor;
		update();
	}

	/**
	 * Set the cursor to a specific index position in the text.
	 * 
	 * @param cursorPosition cusor position
	 */
	public void setCursorPosition(int cursorPosition) {
		moveCursorTo(cursorPosition);
	}

	/**
	 * Set the start for the text selection. Selection start index can never be
	 * higher than end index.
	 * 
	 * @param selectionStart selection start position
	 */
	public void setSelectionStart(int selectionStart) {
		this.selectionStart = Math.max(0, Math.min(text.length(), selectionStart));
		update();
	}

	/**
	 * Set the end for the text selection. Selection end index can never be less
	 * than start index.
	 * 
	 * @param selectionEnd selection end position
	 */
	public void setSelectionEnd(int selectionEnd) {
		this.selectionEnd = Math.max(0, Math.min(text.length(), selectionEnd));
		update();
	}

	/**
	 * Set highlight color of selection.
	 * 
	 * @param selectionColor rgb integer color
	 */
	public void setSelectionColor(int selectionColor) {
		this.selectionColor = selectionColor;
		update();
	}

	/**
	 * Set a hint for the textbox that is displayed when the textbox is empty.
	 * 
	 * @param hint hint
	 */
	public void setHint(String hint) {
		this.hint = hint;
		update();
	}

	@Override
	public void setText(String text) {
		this.text = text.replaceAll("\\r\\n|\\r|\\n", " ");
		cursorPosition = PApplet.constrain(cursorPosition, 0, text.length());
		update();
	}

	/**
	 * Prevent user from typing or pasting text while maintaining the ability of
	 * selecting and copying.
	 */
	public void disableInput() {
		inputEnabled = false;
	}

	/**
	 * Undo {@link #disableInput()}
	 */
	public void enableInput() {
		inputEnabled = true;
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
	protected void autosizeRule() {
		setHeight((int) (fontSize + paddingTop + paddingBottom));
	}


	protected static final String wordDelimiters = " \n+-()[] {}().,:;_*\"\'$%&/=?!";

	/*
	 * start at cursorPosition and iterate over the text to find the next
	 * space/bracket/comma etc...
	 */
	protected int findNextStop() {
		// in first phase search for next space, in second search for first letter
		int phase = 0;

		for (int i = cursorPosition + 1; i < text.length(); i++) {
			if (phase == 0) {

				if (wordDelimiters.indexOf(text.charAt(i)) > -1) {
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
		for (int i = cursorPosition - 2; i > 0; i--) {
			if (wordDelimiters.indexOf(text.charAt(i)) > -1) {
				return i + 1;
			}
		}
		// reached beginning of text
		return 0;
	}






	/*
	 * EVENT METHODS
	 */

	protected EventListener keyPressListener;
	protected EventListener textChangeListener;
	protected EventListener submitListener;

	/**
	 * Add a key listener to the textbox. The event is triggered each time any key
	 * is pressed when the textbox has focus.
	 * 
	 * @param methodName name of callback method
	 * @param target     object that declares callback method.
	 */
	public void addKeyPressListener(String methodName, Object target) {
		keyPressListener = createEventListener(methodName, target, KeyEvent.class);
	}

	public void addKeyPressListener(String methodName) {
		addKeyPressListener(methodName, getPApplet());
	}

	public void removeKeyPressListener() {
		keyPressListener = null;
	}



	/**
	 * Add a listener that fires when the text has actually changed (not just the
	 * cursor or selection).
	 * 
	 * @param methodName name of callback method
	 * @param target     object that declares callback method.
	 */
	public void addTextChangeListener(String methodName, Object target) {
		textChangeListener = createEventListener(methodName, target, null);
	}

	public void addTextChangeListener(String methodName) {
		addTextChangeListener(methodName, getPApplet());
	}

	public void removeTextChangeListener() {
		textChangeListener = null;
	}


	/**
	 * So long as the property {@link #submitOnEnter} is set to true, pressing enter
	 * or return will remove the focus of the textbox and call this event.
	 * 
	 * @param methodName name of callback method
	 * @param target     object that declares callback method.
	 */
	public void addSubmitListener(String methodName, Object target) {
		submitListener = createEventListener(methodName, target, null);
	}

	public void addSubmitListener(String methodName) {
		addSubmitListener(methodName, getPApplet());
	}

	public void removeSubmitListener() {
		submitListener = null;
	}








	protected int selectionInitial; // cursor at the time the dragging started

	@Override
	protected void press(MouseEvent e) {
		if (e.getCount() < 2) {
			setCursorByClick(e.getX());

			selectionInitial = cursorPosition;
			selectionStart = cursorPosition;
			selectionEnd = cursorPosition;

			// should not be necessary as every control gets focus upon pressing it
			// this.focus();
		} else { // double click
			setCursorByClick(e.getX());

			selectionStart = findPreviousStop();
			selectionEnd = findNextStop();
		}
	}

	protected void setCursorByClick(int mX) {
		// relative to textbox origin and ind respect to fullScrollWidth
		int clickedPos = mX - getOffsetXWindow() + scrollPosition - paddingLeft;
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

			int x0 = getOffsetXWindow();

			// scroll a bit when at left or right edge
			if (e.getX() - x0 < 10) {
				setScrollPosition(scrollPosition - 10);
			} else if (x0 + width - e.getX() < 10) {
				setScrollPosition(scrollPosition + 10);
			}
		}
	}

	@Override
	protected void mouseWheel(MouseEvent e) {
		if (focused) {
			super.mouseWheel(e);
			// never allow parent scrolling when textbox has scrolled -> better UX
			stopPropagation();
		}
	}





	@Override
	protected void keyPress(KeyEvent e) {
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
			case 'X':
				cut();
				break;
			case 'A':
				selectionStart = 0;
				selectionEnd = text.length();
				update();
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
					moveCursorTo(findPreviousStop()); // jump to previous word
				} else {
					moveCursorBy(-1); // select if shift is pressed
				}
				if (shft) {
					selectionEnd = selEnd;
				}
				break;

			case PApplet.RIGHT:
				int selStart = selectionStart;

				if (ctrl) {
					moveCursorTo(findNextStop()); // jump to next word
				} else {
					moveCursorBy(1); // select if shift is pressed
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
					blur(); // this first, in case the event callback wants to focus this again 
					handleEvent(submitListener, null);
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
		handleEvent(keyPressListener, e);
	}

	/**
	 * Copy selection to clipboard
	 */
	protected void copy() {
		if (selectionStart < selectionEnd) {
			StringSelection selection = new StringSelection(text.substring(selectionStart, selectionEnd));
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(selection, selection);
		}
	}

	/**
	 * Paste from clipboard (and delete selection)
	 */
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

	/**
	 * Cut selection and copy to clipboard
	 */
	protected void cut() {
		copy();
		int selStart = selectionStart;
		deleteRange(selectionStart, selectionEnd);
		moveCursorTo(selStart);
		selectionStart = 0;
		selectionEnd = 0;
	}
}