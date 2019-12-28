package pGUI.core; //<>// //<>// //<>// //<>// //<>// //<>// //<>// //<>//

/*
 * A multi-line textbox for inserting text via keyboard.
 * Does not scroll horizontally but breaks lines when exceeding width. 
 * Other than TextBox a return is accepted as input. 
 *  
 * Comes with expected features like cursor, selection, some keyboard shortcuts and standard combinations,
 * scrolling etc...
 * 
 */



import processing.core.*;
import processing.event.*;
import processing.data.StringList;
import processing.data.IntList;

//clipboard
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.awt.datatransfer.DataFlavor;



public class MultilineTextbox extends VScrollContainer {


	protected StringList lines; 		// text in lines
	protected IntList lineBreaks; 		// stores all automatic and entered line-breaks



	protected int cursorColor = 70;
	protected int selectionColor = -13395457;
	protected String hint = "";

	protected int cursorPosition = 0;
	protected boolean clickSetsCursor = true;

	protected int lineHeight;
	protected boolean autoScroll; 		// autoscroll to cursor when text changed, dont set it

	// measure time to create blink animation
	protected int cursorTime;
	// cursor currently displayed or not in animation cycle
	protected boolean currentDisplayCursor;

	protected boolean initialized = false;




	public MultilineTextbox() {
		this(100, 100, 20);
	}

	public MultilineTextbox(int width, int height) {
		this(width, height, 15);
	}

	public MultilineTextbox(int width, int height, int fontSize) {
		super();
		this.width = width;
		this.height = height;

		this.fontSize = fontSize;

		lines = new StringList();
		lineBreaks = new IntList();

		foregroundColor = 0;
		setBackgroundColor(230);
		setPadding(3);
		borderWidth = 0;
		textAlign = PApplet.LEFT;

		lineHeight = fontSize * 3 / 2;

		// overridesParentsShortcuts = true;
		cursor = PApplet.TEXT;

		setSlimScrollHandle(true);

		setupListeners(2); // 2 additional listeners (textchanged, key)

		// cursor animation
		Frame.frame0.papplet.registerMethod("pre", this);
	}


	// from VScrollContainer inherited version messes up with fullScrollHeight
	@Override
	protected void calcBounds() {
	};




	/*
	 * Graphics
	 */

	@Override
	protected void render() {
		if (!initialized) {
			cursorTime = Frame.frame0.papplet.millis();
			initialized = true;
		}

		pg.textSize(fontSize);
		pg.textAlign(PApplet.LEFT, PApplet.TOP);
		drawDefaultBackground();

		if (borderWidth == 0) {
			// draw 3D-Border
			pg.strokeWeight(1);
			pg.stroke(70);
			pg.line(0, 0, width, 0);
			pg.line(0, 0, 0, height);
		}

		/*
		 * turn 'Text'-String to lines-list. Also compute line breaks
		 */
		boxedText(text); // actually takes the most time with long text

		/*
		 * do this before drawing cursor
		 */
		fullScrollHeight = lines.size() * lineHeight + paddingTop + paddingBottom;
		scrollPosition = PApplet.constrain(scrollPosition, 0, PApplet.max(0, fullScrollHeight - height));

		/*
		 * DRAW CURSOR, before drawing text!
		 */
		if (this.focused && currentDisplayCursor) {
			drawCursor();
		}

		/*
		 * DRAW SELECTION
		 */
		if (selectionStart < selectionEnd && focused) {
			if (selectionStart <= text.length() && selectionEnd <= text.length()) {

				getLineToCursor(selectionStart);
				getLineToCursor(selectionStart);

				pg.fill(selectionColor);
				pg.noStroke();

				for (int i = getLineToCursor(selectionStart); i <= getLineToCursor(selectionEnd); i++) {
					int start = Math.max(selectionStart, lineBreaks.get(i));
					int end = Math.min(selectionEnd, lineBreaks.get(i + 1));

					int selectionX = (int) (pg.textWidth(lines.get(i).substring(0, start - lineBreaks.get(i))) + fontSize / 40f);

					int selectionWidth = (int) pg.textWidth(lines.get(i).substring(start - lineBreaks.get(i), end - lineBreaks.get(i)));
					pg.rect(paddingLeft + selectionX, i * lineHeight + paddingTop - scrollPosition, selectionWidth, fontSize + pg.textDescent());
				}
			}
		}

		/*
		 * DRAW TEXT
		 */
		pg.fill(foregroundColor);
		if (!text.equals("")) {
			pg.textAlign(textAlign, PApplet.TOP);

			// int posX = textAlign == PApplet.LEFT ? paddingLeft
			// : (textAlign == PApplet.RIGHT ? width - paddingRight : (width - paddingLeft -
			// paddingRight) / 2 + paddingLeft);
			float posX = paddingLeft;

			for (int i = 0; i < lines.size(); i++) {
				pg.text(lines.get(i), posX, i * lineHeight + paddingTop - scrollPosition);
			}
		} else { // draw hint
			pg.fill(120);
			pg.text(hint, paddingLeft, paddingTop);
		}

		/*
		 * DRAW SCROLLBAR
		 */
		drawScrollbar();

		standardDisabled();

	}






	/*
	 * Turn text to string list and compute all line break indices
	 */

	protected void boxedText(String str) {

		// reset both lists
		lines = new StringList();
		lineBreaks = new IntList();


		// width of space in pixels
		float spaceWidth = pg.textWidth(" ");

		// count of used space in the currently processed line
		float usedLineSpace = 0;

		// content of the current line
		String currentLine = "";

		// count of chars already processed. This goes up to text.length() in the end
		int countChars = 0;

		// append the "zero break" which is needed for some counting in other methods
		lineBreaks.append(0);

		// width that can be occupied by text
		int availableSpace = width - paddingLeft - paddingRight - scrollHandleStrength - 1;


		// array containing all paragraphs (paragraphs are generated when the user adds
		// a newline character).
		// These are additional line breaks to the automatic ones generated next
		String[] paragraphs = PApplet.split(str, '\n');


		for (int j = 0; j < paragraphs.length; j++) {

			// create array of all words in this paragraph (breaks should be preferred
			// between words and without splitting any words)
			String[] words = PApplet.split(paragraphs[j], ' ');

			/*
			 * iterate over all words
			 */
			for (int i = 0; i < words.length; i++) {

				// width of the current word in pixels
				float wordWidth = pg.textWidth(words[i]);

				/*
				 * If this word alone is longer than the available space then it needs
				 * splitting. Else just proceed by checking if the content of currentLine so far
				 * is exceeding available space and if not then append it to the currentline and
				 * else store the currentline and add the word to the new line.
				 */

				if (wordWidth < availableSpace) {
					if (usedLineSpace + wordWidth < availableSpace) { // no new line

						usedLineSpace += spaceWidth + wordWidth;
						// also add a space character:
						currentLine += words[i] + " ";
						countChars += 1 + words[i].length();
					} else { // break line

						lineBreaks.append(countChars); // store position of break
						lines.append(currentLine); // store current line

						// create new line and add current word (which didn't fit into last line)
						currentLine = words[i] + " ";
						countChars += 1 + words[i].length();
						usedLineSpace = wordWidth + spaceWidth;
					}
				} else { // if the only word in this line is already too long then split it

					// first store currentLine if it isn't empty
					if (!currentLine.equals("")) {
						lineBreaks.append(countChars); // store position of break
						lines.append(currentLine); // store previous line

						currentLine = ""; // new word is first word of currentLine

						usedLineSpace = 0;
					}

					// now determine at which point the word needs splitting by going back step by
					// step

					for (int k = words[i].length(); k > 0; k--) {

						wordWidth -= pg.textWidth(words[i].substring(k - 1, k));

						if (wordWidth < availableSpace) { // found substring that fits line
							currentLine += words[i].substring(0, k - 1);
							countChars += words[i].substring(0, k - 1).length();

							lineBreaks.append(countChars); // store position of break
							lines.append(currentLine); // store previous line

							words[i] = words[i].substring(k - 1); // set current word to remainder of it

							// .. and go back a step in the iteration over the words to process this word
							// again
							i--;

							// reset new line
							currentLine = "";
							usedLineSpace = 0;

							break;
						}
					}
				}
			}

			// after each paragraph insert linebreak
			currentLine += "\n";

			// append position of (non-automatic) linebreak
			lineBreaks.append(countChars);
			// append the recent line as it hasn't been stored yet
			lines.append(currentLine);

			// clear currentline
			currentLine = "";
			usedLineSpace = 0;
		}

		// append last linebreak again (needed for some scrolling issues)
		lineBreaks.append(countChars);
	}







	/*
	 * CURSOR
	 */

	// cursor blink animation
	public void pre() {
		if (this.focused) {
			int t = Frame.frame0.papplet.millis();
			if (t - cursorTime > 500) {
				currentDisplayCursor = !currentDisplayCursor;
				cursorTime = t;
				update();
			}
		}
	}

	protected void drawCursor() {
		int index = getLineToCursor(cursorPosition);
		int cursorHeight = fontSize;
		if (index >= 0 && cursorPosition > 0) {
			int start = lineBreaks.get(index);
			int stop = cursorPosition;

			String a = text.substring(start, stop);

			a = lines.get(index).substring(0, cursorPosition - start);
			float wordWidth = pg.textWidth(a);

			// position of upper left corner of cursor relative to first character of text
			int cursorY = (index) * lineHeight;


			// perform autoscroll (i.e. when created new line)
			if (autoScroll) {
				if (cursorY - scrollPosition + cursorHeight > height - paddingBottom - paddingTop) { // cursor left
																									 // visible box
																									 // at the bottom
					setScrollPosition(cursorY - height + fontSize + paddingTop + paddingBottom);
				} else if (cursorY < scrollPosition + paddingTop) { // cursor left visible box at the top
					setScrollPosition(cursorY);
				}
				autoScroll = false;
			}

			// draw cursor
			pg.stroke(cursorColor);

			float posX = paddingLeft + wordWidth + fontSize / 40f;
			/*
			 * if (textAlign == PApplet.LEFT) { posX = paddingLeft + wordWidth + fontSize /
			 * 40f; } else if (textAlign == PApplet.RIGHT) { posX = width - paddingRight -
			 * paddingLeft - pg.textWidth(lines.get(index)) + wordWidth - fontSize / 40f; }
			 * else { posX = (width - paddingRight - paddingLeft -
			 * pg.textWidth(lines.get(index)))/2 + wordWidth; }
			 */


			pg.line(posX, cursorY - scrollPosition + paddingTop, posX, cursorY + cursorHeight - scrollPosition + paddingTop);
		} else {
			pg.stroke(cursorColor);
			pg.line(paddingLeft, paddingTop, paddingLeft, cursorHeight + paddingTop);
		}
	}

	// take a cursor position and get the line the cursor should be in
	protected int getLineToCursor(int cursor) {
		int cursorLine = 1;

		for (int i = 1; i < lineBreaks.size(); i++) {
			if (cursor < lineBreaks.get(i)) {
				cursorLine = i - 1;
				break;
			}
		}
		return cursorLine;
	}

	@Override
	public void focus() {
		super.focus();
		cursorTime = Frame.frame0.papplet.millis();
		currentDisplayCursor = true;
	}





	/*
	 * INTERNAL TEXT EDITING METHODS
	 */

	// append character at cursorPosition
	protected void append(char c) {
		cursorPosition += 1;
		text = text.substring(0, cursorPosition - 1) + c + text.substring(cursorPosition - 1);

		textChanged();
	}

	// append string at cursorPosition
	protected void append(String s) {
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
		textChanged();
		text = text.substring(0, start) + text.substring(end);
	}



	// called whenether the text has been altered through user interaction
	protected void textChanged() {
		handleRegisteredEventMethod(TEXTCHANGED_EVENT, null);
		// boxedText(text, width);
		cursorChanged();
	}

	// called whenether the cursor position changed due to user interaction or text
	// edits
	protected void cursorChanged() {
		cursorTime = Frame.frame0.papplet.millis();
		currentDisplayCursor = true;
		selectionStart = cursorPosition;
		selectionEnd = cursorPosition;
		autoScroll = true;
		update();
	}

	protected void moveCursorBy(int ammount) {
		cursorPosition = PApplet.constrain(cursorPosition + ammount, 0, text.length());
		cursorChanged();
	}

	protected void moveCursorTo(int ammount) {
		cursorPosition = PApplet.constrain(ammount, 0, text.length());
		cursorChanged();
	}

	protected void moveCursorVertically(int direction) { // negative down, positive up
		int oldCursorLine = getLineToCursor(cursorPosition); // textline, in which cursor has been so far

		if (oldCursorLine - direction >= 0 && oldCursorLine - direction < lines.size()) {
			// get line up to cursor where cursor has been so far
			String oldSubstring = text.substring(lineBreaks.get(oldCursorLine), cursorPosition);
			float widthPreviousLineToCursor = pg.textWidth(oldSubstring); // and the width in pixels

			String newLineText = lines.get(oldCursorLine - direction); // text of the aimed line

			float wide = 0; // variable for counting the width in next step

			float letterWidth = 0;

			// find out at what letter of the new line the approximately same width in
			// pixels is reached
			for (int i = 0; i < newLineText.length() - 1; i++) {
				String a = newLineText.substring(i, i + 1);
				letterWidth = pg.textWidth(a);
				wide += letterWidth;

				// set decision point to the center of the letter
				if (wide - letterWidth / 2 >= widthPreviousLineToCursor) {
					moveCursorTo(lineBreaks.get(oldCursorLine - direction) + i);
					break;
				}
			}


			// in case previous line has been longer than aimed line
			if (wide - letterWidth / 2 < widthPreviousLineToCursor) {
				if (direction > 0) {
					moveCursorTo(lineBreaks.get(oldCursorLine) - 1); // for up moving
				} else {
					moveCursorTo(lineBreaks.get(oldCursorLine - 2 * direction) - 1); // for down moving
				}
			}
		}
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
	 * GETTER AND SETTER
	 */

	public void setCursorColor(int cursorColor) {
		this.cursorColor = cursorColor;
		update();
	}

	public void setCursorPosition(int cursorPosition) {
		moveCursorTo(cursorPosition);
	}

	public void setSelectionColor(int selectionColor) {
		this.selectionColor = selectionColor;
	}

	public void setHint(String hint) {
		this.hint = hint;
	}

	public void setClickSetsCursor(boolean clickSetsCursor) {
		this.clickSetsCursor = clickSetsCursor;
	}

	public void setLineHeight(int lineHeight) {
		this.lineHeight = lineHeight;
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

	public boolean getClickSetsCursor() {
		return clickSetsCursor;
	}

	public int getLineHeight() {
		return this.lineHeight;
	}

	@Override
	public void setText(String text) {
		this.text = text;
		cursorPosition = PApplet.constrain(cursorPosition, 0, text.length());
		update();
	}






	/*
	 * EVENTS
	 */

	protected static final int KEY_EVENT = Frame.numberMouseListeners;
	protected static final int TEXTCHANGED_EVENT = Frame.numberMouseListeners + 1;


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

	public void addTextChangedListener(String methodName) { // Beware! At the moment this also reacts to setting the
															 // cursor
		addTextChangedListener(methodName, Frame.frame0.papplet);
	}

	public void removeTextChangedListener() {
		deregisterEventRMethod(TEXTCHANGED_EVENT);
	}


	int selectionInitial = 0;
	int selectionStart, selectionEnd;

	@Override
	public void press(MouseEvent e) {
		if (e.getCount() < 2) {

			// set cursor by clicking
			if (clickSetsCursor) {
				setCursorByClick(e.getX(), e.getY());

				selectionInitial = cursorPosition;
				selectionStart = cursorPosition;
				selectionEnd = cursorPosition;
			}

			// shouldnt be necessary anymore as pressing always sets focus
			//this.focus();
		} else { // double click
			setCursorByClick(e.getX(), e.getY());

			selectionStart = findPreviousStop();
			selectionEnd = findNextStop();

		}
	}

	@Override
	protected void drag(MouseEvent e) {
		super.drag(e); // need to handle scrollbar stuff

		if (startHandleDragPos == -1) { // only select text if not dragging scrollbar
			setCursorByClick(e.getX(), e.getY());
			if (cursorPosition > selectionInitial) {
				selectionStart = selectionInitial;
				selectionEnd = cursorPosition;
			} else {
				selectionStart = cursorPosition;
				selectionEnd = selectionInitial;
			}
		}
	}

	protected void setCursorByClick(int mX, int mY) {
		int clickedPosY = Math.max(mY, bounds.Y0) - bounds.Y0 - paddingTop + scrollPosition;
		int newCursorLine = clickedPosY / lineHeight;
		String line = "";

		if (lines.size() > newCursorLine) {
			line = lines.get(newCursorLine);
			int clickedPosX = mX - bounds.X0 - paddingLeft; // relative to textbox origin and considering
															 // fullScrollWidth

			float wide = 0;
			for (int i = 0; i < line.length(); i++) {
				float letterWidth = pg.textWidth(line.substring(i, i + 1));
				wide += letterWidth;
				if (wide - letterWidth / 2 > clickedPosX) { // set decision point to the center of the letter
					moveCursorTo(i + lineBreaks.get(newCursorLine));
					break;
				}
			}

			if (wide < clickedPosX) { // in case clicked beyond last letter - set cursor to end
				moveCursorTo(line.length() + lineBreaks.get(newCursorLine) - 1);
			}

		} else { // if line number is exceeded, set cursor to end
			moveCursorTo(text.length());
		}
	}

	@Override
	protected void mouseWheel(MouseEvent e) {
		if (focused) {
			super.mouseWheel(e);
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

				// copy
				case 'C':
					if (selectionStart < selectionEnd) {
						StringSelection selection = new StringSelection(text.substring(selectionStart, selectionEnd));
						Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
						clipboard.setContents(selection, selection);
					}
					break;

				// paste
				case 'V':
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
							this.append(content);
						} catch (UnsupportedFlavorException ex) {
							System.out.println(ex);
							ex.printStackTrace();
						} catch (IOException ex) {
							ex.printStackTrace();
						}
					}

					break;

				// select everything
				case 'A':
					selectionStart = 0;
					selectionEnd = text.length();
					update();
					break;

				// cut selection
				case 'X':
					if (selectionStart < selectionEnd) {
						StringSelection selection = new StringSelection(text.substring(selectionStart, selectionEnd));
						Clipboard clipBoard = Toolkit.getDefaultToolkit().getSystemClipboard();
						clipBoard.setContents(selection, selection);

						int selStart = selectionStart; // store this because delete will call cursorChanged which resets
														 // selectionStart
						deleteRange(selectionStart, selectionEnd);
						moveCursorTo(selStart);
						selectionStart = 0;
						selectionEnd = 0;
					}

					break;
				}
			}

			if (e.getKey() == PApplet.CODED) {
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

				case PApplet.UP:
					moveCursorVertically(1);
					break;

				case PApplet.DOWN:
					moveCursorVertically(-1);
					break;
				}


			}


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
		}
		handleRegisteredEventMethod(KEY_EVENT, e);
	}
}