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


import processing.core.PApplet;
import processing.event.*;

//clipboard
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.DataFlavor;



/**
 * A single-line Textbox that behaves just as you would expect one to behave. When focused (by
 * click) text can be inserted via keyboard. Comes with expected features like cursor, selection,
 * some keyboard shortcuts for cut/copy/paste, scrolling etc...
 * 
 * 
 * 
 * @author Mc-Zen
 *
 */
public class Textbox extends HScrollContainer {

	protected int selectionColor = GuisetColor.SELECTION_BLUE;
	protected int cursorColor = GuisetColor.TEXT_CURSOR_COLOR;

	// Text to display when textbox is empty
	protected String hint = "";

	protected int cursorPosition;
	protected int selectionStart;
	protected int selectionEnd;

	// Cursor currently displayed or not in animation cycle
	protected boolean currentDisplayCursor;

	// Loose focus and call submit event when hit Enter.
	public boolean submitOnEnter = true;

	// If false, then user can select text and copy but not insert or type
	protected boolean inputEnabled = true;

	volatile protected static int globalCursorCycleTime = 1000; // 1000 ms for cursor to blink off and on // read from timer thread


	public Textbox() {
		this(100, GuisetDefaultValues.fontSize);
	}

	public Textbox(String hint) {
		this(100, GuisetDefaultValues.fontSize);
		setHint(hint);
	}

	public Textbox(String hint, int width) {
		this(width, GuisetDefaultValues.fontSize);
		setHint(hint);
	}

	public Textbox(String hint, int width, int fontSize) {
		this(width, fontSize);
		setHint(hint);
	}

	public Textbox(int width) {
		this(width, GuisetDefaultValues.fontSize);
	}

	public Textbox(int width, int fontSize) {
		super(width, 20); // height does not matter

		setBackgroundColor(-2302756);
		setPadding(5);
		setFontSize(fontSize);
		setSlimScrollHandle(true);
		setTextAlign(Constants.LEFT);
		setLineHeightPercent(120);
		setCursor(PApplet.TEXT);
	}





	protected boolean needsScrolling; // autoscroll once to cursor when text changed, dont set it


	@Override
	protected void render() {
		drawDefaultBackground();

		if (getBorderWidth() == 0 && getBorderRadius() == 0) {
			// draw "3D"-Border
			pg.strokeWeight(1);
			pg.stroke(70);
			pg.line(0, 0, getWidth(), 0);
			pg.line(0, 0, 0, getHeight());
		}

		/*
		 * prepare text style
		 */
		pg.textSize(getFontSize());
		pg.textAlign(PApplet.LEFT, PApplet.TOP);

		// do this before drawing cursor!! - needs new fullScrollWidth
		fullScrollWidth = (int) textWidth(getText()) + getPaddingLeft() + getPaddingRight();
		scrollPosition = Math.max(0, Math.min(scrollPosition, Math.max(0, fullScrollWidth - getWidth())));

		/*
		 * draw cursor if textbox is focused and animation currently is in display cycle
		 */
		if (focused && currentDisplayCursor) {
			drawCursor();
		}

		/*
		 * draw selection
		 */
		if (focused && selectionStart < selectionEnd) {
			if (selectionStart <= getText().length() && selectionEnd <= getText().length()) {
				float selectionX = textWidth(getText().substring(0, selectionStart));
				float selectionWidth = textWidth(getText().substring(selectionStart, selectionEnd));
				pg.fill(selectionColor);
				pg.noStroke();
				pg.rect(getPaddingLeft() - getScrollPosition() + selectionX + getFontSize() / 40f, getPaddingTop(), selectionWidth + getFontSize() / 40f,
						getFontSize() + textDescent());
			}
		}

		/*
		 * draw text
		 */
		if (!text.equals("")) {
			pg.fill(getTextColor());
			pg.text(getText(), getPaddingLeft() - getScrollPosition(), getPaddingTop());
		} else {
			pg.fill(120);
			pg.text(getHint(), getPaddingLeft(), getPaddingTop());
		}

		/*
		 * draw scrollbar
		 */
		drawScrollbar();

		drawDefaultDisabled();
	}




	/*
	 * CURSOR
	 */


	// draw cursor to the graphics
	protected void drawCursor() {
		cursorPosition = Math.max(0, Math.min(text.length(), cursorPosition));

		// get width of text before cursor in pixels; add little extra space
		float wordWidth = textWidth(text.substring(0, cursorPosition)) + getFontSize() / 40f;

		float cursorHeight = getFontSize();

		// do this before drawing the cursor - scrollPositionX has to be set first!!
		if (needsScrolling) {
			if (wordWidth - scrollPosition > getWidth() - getPaddingRight() - getPaddingLeft()) { // cursor has left visible box at the right
				setScrollPosition((int) (wordWidth - getWidth() + getPaddingRight() + getPaddingLeft()));
				scrollPosition = Math.min(scrollPosition, getFullScrollWidth() - getWidth()); // cursor moves a bit strange otherwise
			} else if (wordWidth < scrollPosition + getPaddingLeft()) { // cursor has left visible box at the left
				setScrollPosition((int) wordWidth);
			}
			needsScrolling = false;
		}
		float x = getPaddingLeft() + wordWidth - scrollPosition;
		pg.stroke(cursorColor);
		pg.line(x, getPaddingTop(), x, cursorHeight + getPaddingTop());
	}


	/**
	 * Simple thread that sleeps for one half cursor cycle and toggles cursor visibilty. An interruption
	 * just restarts the thread, except the variable finish has been set to false previously. Everything
	 * alright with thread saftey?
	 * 
	 * @author Mc-Zen
	 *
	 */
	protected class CursorThread extends Thread {
		boolean finish = false;

		public void run() {
			while (true) {
				try {
					Thread.sleep(globalCursorCycleTime / 2);
					currentDisplayCursor = !currentDisplayCursor;
					update();
				} catch (InterruptedException e) {
					if (finish)
						return;
					continue;
				}
			}
		}
	}

	protected static CursorThread t;

	@Override
	protected void focused() {
		t = new CursorThread();
		t.start();
	}

	@Override
	protected void blurred() {
		t.finish = true;
		t.interrupt();
	}

	protected void restartCursorAnimation() {
		if (focused) {
			currentDisplayCursor = true;
			t.interrupt();
		}
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
		handleEvent(textChangeListener);
		cursorPositionChanged();
	}

	// called whenether the cursor position changed due to user interaction or text
	// edits
	protected void cursorPositionChanged() {
		restartCursorAnimation();
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
	 * Set the start for the text selection. Selection start index can never be higher than end index.
	 * 
	 * @param selectionStart selection start position
	 */
	public void setSelectionStart(int selectionStart) {
		this.selectionStart = Math.max(0, Math.min(text.length(), selectionStart));
		update();
	}

	/**
	 * Set the end for the text selection. Selection end index can never be less than start index.
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
		textChanged();
	}

	/**
	 * Prevent user from typing or pasting text while maintaining the ability of selecting and copying.
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

	/**
	 * Set the time the cursor needs to blink on and off in milliseconds, default is 1000 (1 second).
	 * 
	 * @param milliseconds time in milliseconds
	 */
	public static void setGlobalCursorCycleTime(int milliseconds) {
		globalCursorCycleTime = milliseconds;
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

	public int getSelectionStart() {
		return selectionStart;
	}

	public int getSelectionEnd() {
		return selectionEnd;
	}

	public String getSelectedText() {
		return text.substring(selectionStart, selectionEnd);
	}

	public String getHint() {
		return hint;
	}

	public static int getGlobalCursorCycleTime() {
		return globalCursorCycleTime;
	}




	@Override
	protected int autoHeight() {
		return (int) getFontSize() + paddingTop + paddingBottom;
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
	 * Add a key listener to the textbox. The event is triggered each time any key is pressed when the
	 * textbox has focus.
	 * 
	 * Event arguments: {@link KeyEvent}
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

	/**
	 * Add a lambda key listener to the textbox. The event is triggered each time any key is pressed
	 * when the textbox has focus.
	 * 
	 * Event arguments: none
	 * 
	 * @param lambda lambda expression
	 */
	public void addKeyPressListener(Predicate lambda) {
		keyPressListener = new LambdaEventListener(lambda);
	}

	/**
	 * Add a lambda key listener to the textbox. The event is triggered each time any key is pressed
	 * when the textbox has focus.
	 * 
	 * Event arguments: {@link KeyEvent}
	 * 
	 * @param lambda lambda expression with {@link KeyEvent} as parameter
	 */
	public void addKeyPressListener(Predicate1<KeyEvent> lambda) {
		keyPressListener = new LambdaEventListener1<KeyEvent>(lambda);
	}

	public void removeKeyPressListener() {
		keyPressListener = null;
	}



	/**
	 * Add a listener that fires when the text has actually changed (not just the cursor or selection).
	 * 
	 * @param methodName name of callback method
	 * @param target     object that declares callback method.
	 */
	public void addTextChangeListener(String methodName, Object target) {
		textChangeListener = createEventListener(methodName, target);
	}

	public void addTextChangeListener(String methodName) {
		addTextChangeListener(methodName, getPApplet());
	}

	/**
	 * Add a lambda listener that fires when the text has actually changed (not just the cursor or
	 * selection).
	 * 
	 * Event arguments: none
	 * 
	 * @param lambda lambda expression
	 */
	public void addTextChangeListener(Predicate lambda) {
		textChangeListener = new LambdaEventListener(lambda);
	}

	public void removeTextChangeListener() {
		textChangeListener = null;
	}


	/**
	 * So long as the property {@link #submitOnEnter} is set to true, pressing enter or return will
	 * remove the focus of the textbox and call this event.
	 * 
	 * @param methodName name of callback method
	 * @param target     object that declares callback method.
	 */
	public void addSubmitListener(String methodName, Object target) {
		submitListener = createEventListener(methodName, target);
	}

	public void addSubmitListener(String methodName) {
		addSubmitListener(methodName, getPApplet());
	}

	/**
	 * So long as the property {@link #submitOnEnter} is set to true, pressing enter or return will
	 * remove the focus of the textbox and call this event.
	 * 
	 * Event arguments: none
	 * 
	 * @param lambda lambda expression
	 */
	public void addSubmitListener(Predicate lambda) {
		submitListener = new LambdaEventListener(lambda);
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
		int clickedPos = mX - getOffsetXToWindow() + scrollPosition - paddingLeft;
		float wide = 0;
		for (int i = 0; i < text.length(); i++) {
			float letterWidth = textWidth(text.substring(i, i + 1));
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

			int x0 = getOffsetXToWindow();

			// scroll a bit when at left or right edge
			if (e.getX() - x0 < 10) {
				setScrollPosition(scrollPosition - 10);
			} else if (x0 + getWidth() - e.getX() < 10) {
				setScrollPosition(scrollPosition + 10);
			}
		}
	}

	@Override
	protected void release(MouseEvent e) {
		super.release(e);
		Control.drop = false; // induce no drop events on Frame
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

			case PApplet.RETURN: // for macintosh
			case PApplet.ENTER:
				if (submitOnEnter) {
					blur(); // this first, in case the event callback wants to focus this again
					handleEvent(submitListener);
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
			int pos = findPreviousStop();
			deleteRange(pos, selectionEnd);
			moveCursorTo(pos);
		}
		handleEvent(keyPressListener, e);
	}



	/*
	 * (non-Javadoc)
	 * @see guiSET.core.Control#overridesFrameShortcuts()
	 * 
	 * Normal shortcuts are not executed if this textbox has focus. 
	 */
	@Override
	protected boolean overridesFrameShortcuts() {
		return true;
	}


	/**
	 * Copy selection to clipboard
	 */
	public void copy() {
		if (selectionStart < selectionEnd) {
			StringSelection selection = new StringSelection(getSelectedText());
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(selection, selection);
		}
	}

	/**
	 * Paste from clipboard (and delete selection)
	 */
	public void paste() {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable contents = clipboard.getContents(null);
		if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			try {
				String content = (String) contents.getTransferData(DataFlavor.stringFlavor);
				if (selectionStart < selectionEnd) { // if there is a selection, then replace

					// store this because delete will call cursorChanged which resets selectionStart
					int selStart = selectionStart;
					deleteRange(selectionStart, selectionEnd);
					moveCursorTo(selStart);
				}
				this.append(content);
			} catch (UnsupportedFlavorException ufe) {
				System.out.println(ufe);
				ufe.printStackTrace();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	/**
	 * Cut selection and copy to clipboard
	 */
	public void cut() {
		copy();
		int selStart = selectionStart;
		deleteRange(selectionStart, selectionEnd);
		moveCursorTo(selStart);
		selectionStart = 0;
		selectionEnd = 0;
	}

}