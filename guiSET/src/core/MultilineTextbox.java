package guiSET.core; //<>// //<>// //<>// //<>// //<>// //<>// //<>// //<>//

/*
 * A multi-line textbox for inserting text via keyboard.
 * Does not scroll horizontally but breaks lines when exceeding width. 
 * Other than TextBox a return is accepted as input. 
 *  
 * Comes with expected features like cursor, selection, some keyboard shortcuts and standard combinations,
 * scrolling etc...
 * 
 */



import processing.core.PApplet;
import processing.event.*;
import processing.data.StringList;
import processing.data.IntList;

//clipboard
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.DataFlavor;



/**
 * The MultilineTextbox allows entering and pasting multi-line text. If the width of a line exceeds
 * the MultilineTextboxs width, line-breaks are performed. Features copy/paste/cut, select and
 * special events.
 * 
 * @author Mc-Zen
 *
 */
public class MultilineTextbox extends VScrollContainer {

	protected int selectionColor = GuisetColor.SELECTION_BLUE;
	protected int cursorColor = GuisetColor.TEXT_CURSOR_COLOR;

	// Text to display when textbox is empty
	protected String hint = "";

	protected int cursorPosition = 0;
	protected int selectionStart;
	protected int selectionEnd;


	// cursor currently displayed or not in animation cycle
	protected boolean currentDisplayCursor;

	// if false, then user can select text and copy but not insert or type
	protected boolean inputEnabled = true;

	protected boolean clickSetsCursor = true;

	protected boolean initialized = false;


	protected StringList lines; 		// text in lines
	protected IntList breakPositions; 		// stores all automatic and entered line-breaks



	public MultilineTextbox() {
		this(100, 100, 20);
	}

	public MultilineTextbox(int width, int height) {
		this(width, height, 12);
	}

	public MultilineTextbox(String hint, int width, int height) {
		this(width, height, 12);
		setHint(hint);
	}

	public MultilineTextbox(int width, int height, int fontSize) {
		super(width, height);

		lines = new StringList();
		breakPositions = new IntList();

		setBackgroundColor(230);
		setPadding(3);
		setFontSize(fontSize);
		setTextAlign(Constants.LEFT);
		setLineHeightPercent(120);
		setCursor(PApplet.TEXT);
		setSlimScrollHandle(true);
	}


	/*
	 * Graphics
	 */

	@Override
	protected void render() {
		if (!initialized) {
			initialized = true;
			boxedText(text);
		}


		pg.textSize(getFontSize());
		drawDefaultBackground();

		if (getBorderWidth() == 0 && getBorderRadius() == 0) {
			// draw 3D-Border
			pg.strokeWeight(1);
			pg.stroke(70);
			pg.line(0, 0, getWidth(), 0);
			pg.line(0, 0, 0, getHeight());
		}

		int lineHeight = getLineHeight();

		/*
		 * turn 'Text'-String to lines-list. Also compute line breaks.
		 * 
		 * looks like its enough to call this in textChanged() (got all cases covered?)
		 */
		// boxedText(text); // actually takes the most time with long text

		/*
		 * do this before drawing cursor
		 */
		fullScrollHeight = lines.size() * lineHeight + getPaddingTop() + getPaddingBottom();
		scrollPosition = PApplet.constrain(scrollPosition, 0, PApplet.max(0, fullScrollHeight - getHeight()));

		/*
		 * DRAW CURSOR, before drawing text!
		 */
		if (focused && currentDisplayCursor) {
			drawCursor();
		}

		/*
		 * DRAW SELECTION
		 */
		if (focused && selectionStart < selectionEnd) {
			if (selectionStart <= text.length() && selectionEnd <= text.length()) {

				pg.fill(selectionColor);
				pg.noStroke();

				for (int i = getLineToSelectionStart(); i <= getLineToSelectionEnd(); i++) {
					int start = Math.max(selectionStart, breakPositions.get(i));
					int end = Math.min(selectionEnd, breakPositions.get(i + 1));

					float selectionX = textWidth(lines.get(i).substring(0, start - breakPositions.get(i))) + getFontSize() / 40f;

					float selectionWidth = textWidth(lines.get(i).substring(start - breakPositions.get(i), end - breakPositions.get(i)));
					pg.rect(lineStart(lines.get(i)) + selectionX, i * lineHeight + getPaddingTop() - scrollPosition, selectionWidth, getFontSize() + textDescent());
				}
			}
		}

		/*
		 * DRAW TEXT
		 */
		pg.fill(getTextColor());
		pg.textAlign(getTextAlign(), PApplet.TOP);
		if (!text.equals("")) {

			float posX = getTextAlign() == PApplet.LEFT ? getPaddingLeft()
					: (getTextAlign() == PApplet.RIGHT ? getAvailableWidth() + getPaddingLeft() : getAvailableWidth() / 2 + getPaddingLeft());
			int i0 = ((-getPaddingTop() + scrollPosition) / lineHeight); // first (partly) visible line

			for (int i = Math.max(i0, 0); i < lines.size(); i++) {
				float posY = i * (lineHeight) + getPaddingTop() - scrollPosition;
				if (posY > getHeight()) // all further lines not visible
					break;
				pg.text(lines.get(i), posX, posY);
			}
		} else { // draw hint
			// as a side effect we can just apply boxedText() to the hint and get "lines"
			// for the hint, perfect to draw (seems this does not interfere with anything
			// else
			float posX = getTextAlign() == PApplet.LEFT ? getPaddingLeft()
					: (getTextAlign() == PApplet.RIGHT ? getAvailableWidth() + getPaddingLeft(): getAvailableWidth() / 2 + getPaddingLeft());
			pg.fill(120);
			boxedText(hint);
			for (int i = 0; i < lines.size(); ++i) {
				pg.text(lines.get(i), posX, i * lineHeight + getPaddingTop() - scrollPosition);
			}
			boxedText(""); // important
		}

		/*
		 * DRAW SCROLLBAR
		 */
		drawScrollbar();

		drawDefaultDisabled();

	}

	protected int compensateAlign(int x, int lineWidth) {
		switch (getTextAlign()) {
		case PApplet.LEFT:
			return x;
		case PApplet.RIGHT:
			return getAvailableWidth() - lineWidth;
		case PApplet.CENTER:
			return (getAvailableWidth() - lineWidth) / 2;
		default:
			return x;
		}
	}

	// return the xpos where the line with given width starts, regardless of the
	// alignment
	protected float lineStart(String line) {
		switch (getTextAlign()) {
		case PApplet.LEFT:
			return getPaddingLeft();
		case PApplet.RIGHT:
			return getPaddingLeft() + getAvailableWidth() - textWidth(line);
		case PApplet.CENTER:
			return getPaddingLeft() + (getAvailableWidth() - textWidth(line)) / 2f;
		default:
			return 0;
		}
	}



	/*
	 * Turn text to string list and compute all line break indices
	 * 
	 * Afterwards the list lines contains all computed lines while the list
	 * breakPositions will contain all indices of chars that START a new line
	 * (including 0) At last (length of str + 1) is appended to breakPositions.
	 * 
	 * The result depends on: width, paddingLeft, paddingRight, slimScrollHandle, text, fontSize, 
	 * Thus a change of these makes a call to boxedText() necessary. 
	 * 
	 * We can't use Processings implementation of boxed text here because we need the break indices as we 
	 * interact with the text afterwards. Also the standard implementation in PGraphics is not unbelievably fast.   
	 */
	protected void boxedText(String str) {

		// first clear the lists
		lines.clear();
		breakPositions.clear();


		String lineBuffer = "";		// we're building a line with this buffer
		float lineBufferWidth = 0;  // this float keeps track of the textwidth of the lineBuffer

		int spaceIndex = -1;		// absolute index set everytime a space is detected so we can see where the last
									// word was

		int availableWidth = getAvailableWidth();
		breakPositions.append(0);		// start off with a zero

		// iterate through all chars
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);

			// if char is newline then apply current lineBuffer and reset all vars that keep
			// track of the line
			if (c == '\n') {
				breakPositions.append(i + 1);
				lines.append(lineBuffer + "\n");
				lineBuffer = "";
				lineBufferWidth = 0;
				spaceIndex = -1;
				continue;
			}

			// get width of this char
			float charWidth = textWidth(c); // if we were using pg.textWidth(), we'd need to set pg.textSize()!
			// check if this char is a word-breaking char. Do this before next step, so we
			// can break when a space is at the end of the text
			if (c == ' ') {
				spaceIndex = i;
			}

			// If lineBuffer would exceed available width if this new char were included, we
			// need to break the line.
			if (lineBufferWidth + charWidth > availableWidth) {

				// look if there is a space in the line, then we can break there
				if (spaceIndex != -1) {

					// append current line until spaceIndex (include space)
					lines.append(str.substring(breakPositions.get(breakPositions.size() - 1), spaceIndex + 1));
					breakPositions.append(spaceIndex + 1);

					// set lineBuffer to reset
					lineBuffer = str.substring(spaceIndex + 1, i + 1); // can still get error here
					lineBufferWidth = textWidth(lineBuffer);
					spaceIndex = -1;
					continue;
				} else {
					// no space to break at - just break here
					lines.append(lineBuffer);
					breakPositions.append(i);
					lineBuffer = "";
					lineBufferWidth = 0;
					// spaceIndex = -1, // unnecessary
				}
			}
			// in any case continue building the buffer
			lineBufferWidth += charWidth;
			lineBuffer += c;
		}
		breakPositions.append(str.length() + 1);
		lines.append(lineBuffer + "\n");
	}





	// int pressed = 0;

	/*
	 * CURSOR
	 */


	/*
	 * Take a cursor position and get the line the cursor should be in.
	 * Also takes into account if endCursor is set. This is usually called with 
	 * cursorPosition, but once with selectionStart/end
	 */
	protected int getLineToCursor(int cursor) {
		int cursorLine = 0;

		for (int i = 1; i < breakPositions.size(); i++) {
			if (cursor < breakPositions.get(i)) {
				cursorLine = i - 1;
				break;
			}
		}
		if (endCursor)
			cursorLine--;

		return cursorLine;
	}

	protected int getLineToCursor() {
		return getLineToCursor(cursorPosition);
	}

	// special methods for selectionStart and selectionEnd (not just
	// getLineToCursor()), because they are not both at cursorEnd
	protected int getLineToSelectionStart() {
		return getLineToCursor(selectionStart) + ((endCursor && selectionStart != cursorPosition) ? 1 : 0);
	}

	protected int getLineToSelectionEnd() {
		return getLineToCursor(selectionEnd) + ((endCursor && selectionEnd != cursorPosition) ? 1 : 0);
	}

	protected float lineWidthUntilCursor() {
		int lineNumber = getLineToCursor();
		String lineUntiCursor = lines.get(lineNumber).substring(0, cursorPosition - breakPositions.get(lineNumber));
		return textWidth(lineUntiCursor);
	}



	protected boolean needsScrolling; 		// autoscroll to cursor when text changed, dont set it

	protected void drawCursor() {
		int lineNumber = getLineToCursor();
		float posX = lineWidthUntilCursor() + lineStart(lines.get(lineNumber)) + getFontSize() / 40f;
		// position of upper left corner of cursor relative to first character of text
		int posY = lineNumber * getLineHeight();
		float cursorHeight = getFontSize();

		// perform autoscroll (i.e. when created new line)
		if (needsScrolling) {
			// cursor has left the element at the bottom
			if (posY - scrollPosition + cursorHeight > getHeight() - getPaddingBottom() - getPaddingTop()) {
				setScrollPosition((int) (posY - getHeight() + getFontSize() + getPaddingTop() + getPaddingBottom()));
			} else if (posY < scrollPosition + getPaddingTop()) { // cursor has left the element at the top
				setScrollPosition(posY);
			}
			needsScrolling = false;
		}

		pg.stroke(cursorColor);
		pg.line(posX, posY - scrollPosition + getPaddingTop(), posX, posY + cursorHeight - scrollPosition + getPaddingTop());
	}

	// On forced line breaks, when clicking on the end of the line, this cursor
	// position is really the first position of the next line. The canonical textbox
	// still displays the cursor at the end ONCE. When moving the cursor, we see
	// that the other position (at front of next line is always favoured).
	// This phenomenon happens in two cases: either a through a mouse click, or
	// through pressing the END button on the keyboard.
	boolean endCursor = false;

	protected void setCursorByXAndLine(int relativeX, int lineNumber) {
		if (lineNumber < lines.size()) {
			String line = lines.get(lineNumber);

			// determine where clicked on this line
			float textX = relativeX - lineStart(line);

			float textwidth = 0;
			for (int i = 0; i < line.length(); i++) {
				float letterWidth = textWidth(line.charAt(i));

				textwidth += letterWidth;
				if (textwidth > textX) { // determine clicked on character.
					if (textX > textwidth - letterWidth / 2) { // determine whether clicked on right or left half of character.
						i++;
					}
					moveCursorTo(i + breakPositions.get(lineNumber));
					if (i == line.length() && line.charAt(line.length() - 1) != '\n') {
						// end of line -> set cursor to end which is the same as beginning of next line
						// but user expects to see cursor where he clicked.
						endCursor = true; // also see field description
					}

					return;
				}
			}
			// In case clicked beyond last letter - set cursor to end.
			// Discern between lines that have a forced break (at a whitespace or anywhere)
			// vs. ones that haven't (have a \n).

			// On lines with real break we want to set the cursor to the end (before '\n')
			// but that would be one short of what the other types of lines need.

			if (line.charAt(line.length() - 1) == '\n') {
				moveCursorTo(line.length() + breakPositions.get(lineNumber) - 1);
			} else {
				moveCursorTo(line.length() + breakPositions.get(lineNumber));
				endCursor = true; // see field description
			}

		} else { // if line number is exceeded, set cursor to end
			moveCursorTo(text.length());
		}
	}

	protected void setCursorByClick(int mX, int mY) { // mX, mY being coordinates relative to parent
		// determine which line clicked on
		int textY = mY - getPaddingTop() + scrollPosition; // clicked y position on text (max(), because when drag, lines can be exceeded)
		int lineHeight = getLineHeight();
		int lineNumber = (int) ((textY + (lineHeight - getFontSize()) / 2) / (lineHeight/* + getFontSize()*/));

		setCursorByXAndLine(mX, Math.max(lineNumber, 0));
	}


	protected void moveCursorBy(int ammount) {
		cursorPosition = PApplet.constrain(cursorPosition + ammount, 0, text.length());
		cursorChanged();
	}

	protected void moveCursorTo(int position) {
		cursorPosition = PApplet.constrain(position, 0, text.length());
		cursorChanged();
	}

	/*
	 * Move cursor up and down, trying to keep the x position of the cursor best as possible
	 */
	protected void moveCursorVertically(int direction) { // negative down, positive up
		int lineNumber = getLineToCursor();

		if (lineNumber - direction >= 0 && lineNumber - direction < lines.size()) {
			float x = lineWidthUntilCursor() + lineStart(lines.get(lineNumber));
			setCursorByXAndLine((int) x, lineNumber - direction);
		}
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
					Thread.sleep(Textbox.globalCursorCycleTime / 2);
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
		if (inputEnabled) {
			cursorPosition += 1;
			text = text.substring(0, cursorPosition - 1) + c + text.substring(cursorPosition - 1);

			textChanged();
		}
	}

	// append string at cursorPosition
	protected void append(String s) {
		if (inputEnabled) {
			text = text.substring(0, cursorPosition) + s + text.substring(cursorPosition);
			cursorPosition += s.length();

			textChanged();
		}
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
		handleEvent(textChangeListener);
		boxedText(text);
		cursorChanged();
	}

	// called whenether the cursor position changed due to user interaction or text
	// edits
	protected void cursorChanged() {
		restartCursorAnimation();
		selectionStart = cursorPosition;
		selectionEnd = cursorPosition;
		needsScrolling = true;
		endCursor = false;
		update();
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
	 * @param cursorPosition cursor positon
	 */
	public void setCursorPosition(int cursorPosition) {
		moveCursorTo(cursorPosition);
	}

	/**
	 * Set the start for the text selection. Selection start index can never be higher than end index
	 * 
	 * @param selectionStart selection start position
	 */
	public void setSelectionStart(int selectionStart) {
		this.selectionStart = Math.max(0, Math.min(text.length(), selectionStart));
		update();
	}

	/**
	 * Set the end for the text selection. Selection end index can never be less than start index
	 * 
	 * @param selectionEnd selection end position
	 */
	public void setSelectionEnd(int selectionEnd) {
		this.selectionEnd = Math.max(0, Math.min(text.length(), selectionEnd));
		update();
	}

	/**
	 * Set selection. If end is less than start, they will we swapped.
	 * 
	 * @param start selection start position
	 * @param end   selection end position
	 */
	public void setSelection(int start, int end) {
		int s = Math.min(start, end);
		int e = Math.max(start, end);
		setSelectionStart(s);
		setSelectionEnd(e);
	}

	/**
	 * Set highlight color of selection
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

	/**
	 * if false then clicking on the Textbox does not set the cursor to where the user clicked. Not
	 * really necessary but I needed it once.
	 * 
	 * @param clickSetsCursor clickSetsCursor
	 */
	public void setClickSetsCursor(boolean clickSetsCursor) {
		this.clickSetsCursor = clickSetsCursor;
	}




	@Override
	public void setSize(int width, int height) {
		super.setSize(width, height);
		boxedText(text);
	}

	@Override
	public void setWidth(int width) {
		super.setWidth(width);
		boxedText(text);
	}

	@Override
	public void setPadding(int all) {
		super.setPadding(all);
		boxedText(text);
	}

	@Override
	public void setPadding(int top_bottom, int left_right) {
		super.setPadding(top_bottom, left_right);
		boxedText(text);
	}

	@Override
	public void setPadding(int top, int right, int bottom, int left) {
		super.setPadding(top, right, bottom, left);
		boxedText(text);
	}

	@Override
	public void setPaddingRight(int right) {
		super.setPaddingRight(right);
		boxedText(text);
	}

	@Override
	public void setPaddingLeft(int left) {
		super.setPaddingLeft(left);
		boxedText(text);
	}

	public void setSlimScrollHandle(boolean slimScrollHandle) {
		super.setSlimScrollHandle(slimScrollHandle);
		boxedText(text);
	}

	@Override
	public void setText(String text) {
		this.text = text;
		cursorPosition = PApplet.constrain(cursorPosition, 0, text.length());
		textChanged(); // calls boxedText and update
	}

	@Override
	public void setFontSize(float fontSize) {
		super.setFontSize(fontSize);
		boxedText(text);
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

	public int getSelectionStart() {
		return selectionStart;
	}

	public int getSelectionEnd() {
		return selectionEnd;
	}

	public String getSelection() {
		return text.substring(selectionStart, selectionEnd);
	}

	public boolean getClickSetsCursor() {
		return clickSetsCursor;
	}







	/*
	 * EVENTS
	 */

	protected EventListener keyPressListener;
	protected EventListener textChangeListener;

	/**
	 * Set a key listener to the textbox. The event is triggered each time any key is pressed when the
	 * textbox has focus.
	 * 
	 * Event arguments: {@link KeyEvent}
	 * 
	 * @param methodName name of callback method
	 * @param target     object that declares callback method.
	 */
	public void setKeyPressListener(String methodName, Object target) {
		keyPressListener = createEventListener(methodName, target, KeyEvent.class);
	}

	public void setKeyPressListener(String methodName) {
		setKeyPressListener(methodName, getPApplet());
	}

	/**
	 * Set a lambda key listener to the textbox. The event is triggered each time any key is pressed
	 * when the textbox has focus.
	 * 
	 * Event arguments: none
	 * 
	 * @param lambda lambda expression
	 */
	public void setKeyPressListener(Predicate lambda) {
		keyPressListener = new LambdaEventListener(lambda);
	}

	/**
	 * Set a lambda key listener to the textbox. The event is triggered each time any key is pressed
	 * when the textbox has focus.
	 * 
	 * Event arguments: {@link KeyEvent}
	 * 
	 * @param lambda lambda expression with {@link KeyEvent} as parameter
	 */
	public void setKeyPressListener(Predicate1<KeyEvent> lambda) {
		keyPressListener = new LambdaEventListener1<KeyEvent>(lambda);
	}

	public void removeKeyPressListener() {
		keyPressListener = null;
	}

	/**
	 * Set a listener that fires when the text has actually changed (not just the cursor or selection).
	 * 
	 * @param methodName name of callback method
	 * @param target     object that declares callback method.
	 */
	public void setTextChangeListener(String methodName, Object target) {
		textChangeListener = createEventListener(methodName, target);
	}

	public void setTextChangeListener(String methodName) {
		setTextChangeListener(methodName, getPApplet());
	}

	/**
	 * Set a lambda listener that fires when the text has actually changed (not just the cursor or
	 * selection).
	 * 
	 * Event arguments: none
	 * 
	 * @param lambda lambda expression
	 */
	public void setTextChangeListener(Predicate lambda) {
		textChangeListener = new LambdaEventListener(lambda);
	}

	public void removeTextChangeListener() {
		textChangeListener = null;
	}





	protected int selectionInitial = 0; // used for dragging

	@Override
	protected void press(MouseEvent e) {
		super.press(e);
		if (startHandleDragPos > -1) // only set cursor if not dragging scrollbar
			return;

		if (e.getCount() < 2) {
			// set cursor by clicking
			if (clickSetsCursor) {

				if (e.isShiftDown()) {
					int selStart = cursorPosition > selectionStart ? selectionStart : selectionEnd;
					setCursorByClick(e.getX() - getOffsetXToWindow(), e.getY() - getOffsetYToWindow());
					setSelection(selStart, cursorPosition);
				} else {
					setCursorByClick(e.getX() - getOffsetXToWindow(), e.getY() - getOffsetYToWindow());

					selectionInitial = cursorPosition;
					// selectionStart = cursorPosition;
					// selectionEnd = cursorPosition;
				}
			}
		} else { // double click
			setCursorByClick(e.getX() - getOffsetXToWindow(), e.getY() - getOffsetYToWindow());

			selectionStart = findPreviousStop();
			selectionEnd = findNextStop();

		}
	}


	@Override
	protected void drag(MouseEvent e) {
		super.drag(e); // need to handle scrollbar stuff
		if (startHandleDragPos > -1) // only select text if not dragging scrollbar
			return;

		setCursorByClick(e.getX() - getOffsetXToWindow(), e.getY() - getOffsetYToWindow());
		if (cursorPosition > selectionInitial) {
			selectionStart = selectionInitial;
			selectionEnd = cursorPosition;
		} else {
			selectionStart = cursorPosition;
			selectionEnd = selectionInitial;
		}
	}

	@Override
	protected void release(MouseEvent e) {
		super.release(e);
		Control.drop = false; // induce no drop events on Frame
	}

	@Override
	protected void mouseWheel(MouseEvent e) {
		if (getFrame().isControlDown()) {
			setFontSize(getFontSize() * (e.getCount() < 0 ? 1.1f : 1 / 1.1f));
		} else {
			super.mouseWheel(e);
		}
		stopPropagation();
	}








	/*
	 * KEY EVENTS and Clipboard operations
	 */

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

		if (e.getKey() == PApplet.CODED) {
			switch (code) {

			case PApplet.LEFT:
				int selEnd = selectionEnd;

				if (ctrl) {
					moveCursorTo(findPreviousStop()); // jump to previous word
				} else {
					moveCursorBy(-1);
				}
				if (shft) {
					selectionEnd = selEnd; // select if shift is pressed
				}
				break;

			case PApplet.RIGHT:
				int selStart = selectionStart;

				if (ctrl) {
					moveCursorTo(findNextStop()); // jump to next word
				} else {
					moveCursorBy(1);
				}
				if (shft) {
					selectionStart = selStart; // select if shift is pressed
				}
				break;

			case PApplet.UP:
				selEnd = selectionEnd;
				moveCursorVertically(1);
				if (shft) {
					selectionEnd = selEnd; // select if shift is pressed
				}
				break;

			case PApplet.DOWN:
				selStart = selectionStart;
				moveCursorVertically(-1);
				if (shft) {
					selectionStart = selStart; // select if shift is pressed
				}
				break;
			case 36: // POS 1
				int currentLine = getLineToCursor();
				moveCursorTo(breakPositions.get(currentLine));
				break;
			case 35: // END
				currentLine = getLineToCursor();
				if ((text + '\n').charAt(breakPositions.get(currentLine + 1) - 1) != '\n') {
					moveCursorTo(breakPositions.get(currentLine + 1));
					endCursor = true; // always after moveCursorTo
				} else {
					moveCursorTo(breakPositions.get(currentLine + 1) - 1);
				}
			}
		} // if key == CODED


		/*
		 * Manage other inputs.
		 */

		else if (!ctrl || alt) { // don't allow coded keys (control), but enable Alt Gr (Alt + Control)
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

	/*
	 * (non-Javadoc)
	 * @see guiSET.core.Control#overridesFrameShortcuts()
	 * 
	 * Normal shortcuts are not executed if this textbox has focus. 
	 */
	@Override
	protected boolean overridesRegisteredShortcuts() {
		return true;
	}


	/**
	 * Copy selection to clipboard
	 */
	public void copy() {
		if (selectionStart < selectionEnd) {
			StringSelection selection = new StringSelection(text.substring(selectionStart, selectionEnd));
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
				if (selectionStart < selectionEnd) { // if there is a selection then replace
					int selStart = selectionStart; // store this because delete will call cursorChanged
													 // which resets selectionStart
					deleteRange(selectionStart, selectionEnd);
					moveCursorTo(selStart);
				}
				append(content);
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
	public void cut() {
		copy();

		// store this because delete will call cursorChanged which resets selectionStart
		int selStart = selectionStart;
		deleteRange(selectionStart, selectionEnd);
		moveCursorTo(selStart);
		selectionStart = 0;
		selectionEnd = 0;
	}
}