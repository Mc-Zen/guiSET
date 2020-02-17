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



/**
 * The MultilineTextbox allows entering and pasting multi-line text. If the
 * width of a line exceeds the MultilineTextboxs width a line-break is
 * performed.
 * 
 * @author Mc-Zen
 *
 */
public class MultilineTextbox extends VScrollContainer {


	protected StringList lines; 		// text in lines
	protected IntList breakPositions; 		// stores all automatic and entered line-breaks



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
		this(width, height, 12);
	}

	public MultilineTextbox(int width, int height, int fontSize) {
		super();
		this.width = width;
		this.height = height;

		this.fontSize = fontSize;

		lines = new StringList();
		breakPositions = new IntList();

		foregroundColor = 0;
		setBackgroundColor(230);
		setPadding(3);
		borderWidth = 0;
		textAlign = PApplet.LEFT;

		lineHeight = (int) (fontSize * 0.2f);

		// overridesFrameShortcuts = true;
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
			boxedText(text);
<<<<<<< HEAD
			boxedText(text);
=======
>>>>>>> branch 'master' of https://github.com/Mc-Zen/guiSET.git
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
		 * turn 'Text'-String to lines-list. Also compute line breaks.
		 * 
		 * looks like its enough to call this in textChanged() (got all cases covered?)
		 */
		// boxedText(text); // actually takes the most time with long text

		/*
		 * do this before drawing cursor
		 */
		fullScrollHeight = (int) (lines.size() * (fontSize + lineHeight) + paddingTop + paddingBottom);
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
					int start = Math.max(selectionStart, breakPositions.get(i));
					int end = Math.min(selectionEnd, breakPositions.get(i + 1));

					int selectionX = (int) (pg.textWidth(lines.get(i).substring(0, start - breakPositions.get(i))) + fontSize / 40f);

<<<<<<< HEAD
					int selectionWidth = (int) pg.textWidth(lines.get(i).substring(start - breakPositions.get(i), end - breakPositions.get(i)));
=======
					int selectionWidth = (int) pg.textWidth(lines.get(i).substring(start - lineBreaks.get(i), end - lineBreaks.get(i)));
>>>>>>> branch 'master' of https://github.com/Mc-Zen/guiSET.git
					pg.rect(paddingLeft + selectionX, i * (lineHeight + fontSize) + paddingTop - scrollPosition, selectionWidth,
							fontSize + pg.textDescent());
				}
			}
		}

		/*
		 * DRAW TEXT
		 */
		pg.fill(foregroundColor);
		if (!text.equals("")) {
			// pg.textAlign(textAlign, PApplet.TOP);

			// int posX = textAlign == PApplet.LEFT ? paddingLeft
			// : (textAlign == PApplet.RIGHT ? width - paddingRight : (width - paddingLeft -
			// paddingRight) / 2 + paddingLeft);
			float posX = paddingLeft;

			for (int i = 0; i < lines.size(); i++) {
				pg.text(lines.get(i), posX, i * (lineHeight + fontSize) + paddingTop - scrollPosition);
			}
		} else { // draw hint
			// as a side effect we can just apply boxedText() to the hint and get "lines"
			// for the hint, perfect to draw (seems this does not interfere with anything
			// else
			pg.fill(120);
			/* pg.text(hint, paddingLeft, paddingTop); */
			boxedText(hint);
			float posX = paddingLeft;
			for (int i = 0; i < lines.size(); i++) {
				pg.text(lines.get(i), posX, i * (lineHeight + fontSize) + paddingTop - scrollPosition);
			}
		}

		/*
		 * DRAW SCROLLBAR
		 */
		drawScrollbar();

		standardDisabled();

	}



	protected void boxedText(String str) {
		boxedText1(str);
	}



	/*
	 * Turn text to string list and compute all line break indices
	 * 
	 * Afterwards the list lines contains all computed lines while the list 
	 * breakPositions will contain all indices of chars that START a new line (including 0)
	 * At last (length of str + 1) is appended to breakPositions. 
	 * 
	 */
	protected void boxedText1(String str) {

<<<<<<< HEAD
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
				lines.append(lineBuffer + " \n");
				lineBuffer = "";
				lineBufferWidth = 0;
				spaceIndex = -1;
				continue;
			}

			// get width of this char
			float cWidth = textWidth("" + c);

			// check if this char is a word-breaking char
			// do this before next step, so we can break when a space is at the end of the
			// text
			if (c == ' ') {
				spaceIndex = i;
			}

			// If lineBuffer would exceed available width if this new char were included, we
			// need to break the line.
			if (lineBufferWidth + cWidth > availableWidth) {

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
			lineBufferWidth += cWidth;
			lineBuffer += c;

		}
		breakPositions.append(str.length() + 1);
		lines.append(lineBuffer + " \n");
	}

	protected void boxedText2(String str) {
=======
	protected void boxedText(String str) {
>>>>>>> branch 'master' of https://github.com/Mc-Zen/guiSET.git
		if (!initialized)
			return;

		// reset both lists
		lines.clear();
		breakPositions.clear();


		// width of space in pixels

		// count of used space in the currently processed line
		float usedLineSpace = 0;

		// content of the current line
		String currentLine = "";

		// count of chars already processed. This goes up to text.length() in the end
		int countChars = 0;

		// append the "zero break" which is needed for some counting in other methods
		breakPositions.append(0);

		// width that can be occupied by text
		int availableSpace = getAvailableWidth();

		float spaceWidth = textWidth(" ");

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
				float wordWidth = textWidth(words[i]);

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

						breakPositions.append(countChars); // store position of break
						lines.append(currentLine); // store current line

						// create new line and add current word (which didn't fit into last line)
						currentLine = words[i] + " ";
						countChars += 1 + words[i].length();
						usedLineSpace = wordWidth + spaceWidth;
					}
				} else { // if the only word in this line is already too long then split it

					// first store currentLine if it isn't empty
					if (!currentLine.equals("")) {
						breakPositions.append(countChars); // store position of break
						lines.append(currentLine); // store previous line

						currentLine = ""; // new word is first word of currentLine

						usedLineSpace = 0;
					}

					// now determine at which point the word needs splitting by going back step by
					// step
					//PApplet.println(words[i].length());
					for (int k = words[i].length(); k > 0; k--) {

						wordWidth -= pg.textWidth(words[i].substring(k - 1, k));
						//PApplet.println(k, wordWidth, words[i].substring(k - 1, k));

						if (wordWidth < availableSpace) { // found substring that fits line
							currentLine += words[i].substring(0, k - 1);
							countChars += words[i].substring(0, k - 1).length();

							breakPositions.append(countChars); // store position of break
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
			breakPositions.append(countChars);
			// append the recent line as it hasn't been stored yet
			lines.append(currentLine);

			// clear currentline
			currentLine = "";
			usedLineSpace = 0;
		}

		// append last linebreak again (needed for some scrolling issues)
		breakPositions.append(countChars);
		//PApplet.println(breakPositions);
	}






	// int pressed = 0;

	/*
	 * CURSOR
	 */

	// cursor blink animation
	/**
	 * DO NOT CALL THIS METHOD. It is for internal purposes only and unfortunately
	 * needs to be public.
	 */
	public void pre() {
		if (this.focused) {
			int t = Frame.frame0.papplet.millis();
			if (t - cursorTime > 500) {
				currentDisplayCursor = !currentDisplayCursor;
				cursorTime = t;
				update();
			}
		}
		/*
		 * if(pressed >=1) { System.out.println(pressed); if(pressed == 2)
		 * setScrollPosition(scrollPosition-2); if(pressed == 3)
		 * setScrollPosition(scrollPosition+2); }
		 */
	}

	protected void drawCursor() {
		int index = getLineToCursor(cursorPosition);
		float cursorHeight = fontSize;
		if (index >= 0 && cursorPosition > 0) {
			int start = breakPositions.get(index);
			int stop = cursorPosition;

			String a = text.substring(start, stop);

			//PApplet.println("drawCursor:", index, lines);
			a = lines.get(index).substring(0, cursorPosition - start);
			float wordWidth = pg.textWidth(a);

			// position of upper left corner of cursor relative to first character of text
			int cursorY = (int) ((index) * (lineHeight + fontSize));


			// perform autoscroll (i.e. when created new line)
			if (autoScroll) {
				if (cursorY - scrollPosition + cursorHeight > height - paddingBottom - paddingTop) { // cursor left
																									 // visible box
																									 // at the bottom
					setScrollPosition((int) (cursorY - height + fontSize + paddingTop + paddingBottom));
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

		for (int i = 1; i < breakPositions.size(); i++) {
			if (cursor < breakPositions.get(i)) {
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
		text = text.substring(0, start) + text.substring(end);
		textChanged();
	}



	// called whenether the text has been altered through user interaction
	protected void textChanged() {
		handleRegisteredEventMethod(TEXTCHANGED_EVENT, null);
		boxedText(text);
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
			String oldSubstring = text.substring(breakPositions.get(oldCursorLine), cursorPosition);
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
					moveCursorTo(breakPositions.get(oldCursorLine - direction) + i);
					break;
				}
			}


			// in case previous line has been longer than aimed line
			if (wide - letterWidth / 2 < widthPreviousLineToCursor) {
				if (direction > 0) {
					moveCursorTo(breakPositions.get(oldCursorLine) - 1); // for up moving
				} else {
					moveCursorTo(breakPositions.get(oldCursorLine - 2 * direction) - 1); // for down moving
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

		String delimiters = " \n+-()[] {}().,:;_*\"\'$%&/=?!";

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

		String delimiters = " \n+-()[] {}().,:;_*\"\'$%&/=?!";

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
	 * Set the start for the text selection. Selection start index can never be
	 * higher than end index
	 * 
	 * @param selectionStart selection start position
	 */
	public void setSelectionStart(int selectionStart) {
		this.selectionStart = Math.max(0, Math.min(text.length(), selectionStart));
		update();
	}

	/**
	 * Set the end for the text selection. Selection end index can never be less
	 * than start index
	 * 
	 * @param selectionEnd selection end position
	 */
	public void setSelectionEnd(int selectionEnd) {
		this.selectionEnd = Math.max(0, Math.min(text.length(), selectionEnd));
		update();
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
	 * if false then clicking on the Textbox does not set the cursor to where the
	 * user clicked. Not really necessary but I needed it once.
	 * 
	 * @param clickSetsCursor clickSetsCursor
	 */
	public void setClickSetsCursor(boolean clickSetsCursor) {
		this.clickSetsCursor = clickSetsCursor;
	}

	/**
	 * Set the line height (distance between to lines).
	 * 
	 * @param lineHeight line height
	 */
	public void setLineHeight(int lineHeight) {
		this.lineHeight = lineHeight;
		update();
	}



	/*
	 * !!! Setting width or padding (or scrollhandlewidth) affects how the text
	 * breaks need to be set. So call boxedText() here.
	 * 
	 * There will be <no> trouble with animations.
	 */
	@Override
	protected void animated() {
		super.animated();

		// might look wasteful but isn't as it would be called a lot more often if
		// called in render()
		boxedText(text);
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

	public void setSlimScrollHandle(boolean light_scrollhandle) {
		super.setSlimScrollHandle(light_scrollhandle);
		boxedText(text);
	}

	@Override
	public void setText(String text) {
		this.text = text;
		cursorPosition = PApplet.constrain(cursorPosition, 0, text.length());
<<<<<<< HEAD
		boxedText(this.text);
=======
		boxedText(text);
>>>>>>> branch 'master' of https://github.com/Mc-Zen/guiSET.git
		update();
	}

	@Override
	public void setFontSize(float fontSize) {
		super.setFontSize(fontSize);
		boxedText(text);
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








	/*
	 * EVENTS
	 */

	protected static final int KEY_EVENT = Frame.numberMouseListeners;
	protected static final int TEXTCHANGED_EVENT = Frame.numberMouseListeners + 1;


	/**
	 * Add a key listener to the textbox. Event is triggered each time any key is
	 * pressed when the textbox has focus.
	 * 
	 * @param methodName name of callback method
	 * @param target     object that declares callback method.
	 */
	public void addKeyListener(String methodName, Object target) {
		registerEventRMethod(KEY_EVENT, methodName, target, KeyEvent.class);
	}

	/**
	 * @see #addKeyListener(String, Object)
	 * @param methodName name of callback method
	 */
	public void addKeyListener(String methodName) {
		addKeyListener(methodName, Frame.frame0.papplet);
	}

	public void removeKeyListener() {
		deregisterEventRMethod(KEY_EVENT);
	}

	/**
	 * Add a listener that fires when the text has actually changed (not the cursor
	 * or selection).
	 * 
	 * @param methodName name of callback method
	 * @param target     object that declares callback method.
	 */
	public void addTextChangedListener(String methodName, Object target) {
		registerEventRMethod(TEXTCHANGED_EVENT, methodName, target, null);
	}

	/**
	 * @see #addTextChangedListener(String, Object)
	 * @param methodName name of callback method
	 */
	public void addTextChangedListener(String methodName) {
		addTextChangedListener(methodName, Frame.frame0.papplet);
	}

	public void removeTextChangedListener() {
		deregisterEventRMethod(TEXTCHANGED_EVENT);
	}


	int selectionInitial = 0;
	int selectionStart, selectionEnd;

	@Override
	protected void press(MouseEvent e) {
		super.press(e);
		// pressed = 1;
		if (e.getCount() < 2) {

			// set cursor by clicking
			if (clickSetsCursor) {
				setCursorByClick(e.getX(), e.getY());

				selectionInitial = cursorPosition;
				selectionStart = cursorPosition;
				selectionEnd = cursorPosition;
			}

			// shouldnt be necessary anymore as pressing always sets focus
			// this.focus();
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
		/*
		 * if ( e.getY() < bounds.Y0 ) { // if not over element
		 * 
		 * pressed = 2; }else if (e.getY() > bounds.Y) {
		 * 
		 * pressed = 3; }
		 */
	}

	@Override
	protected void release(MouseEvent e) {
		super.release(e);
		// pressed = 0;
	}

	protected void setCursorByClick(int mX, int mY) {
		int clickedPosY = Math.max(mY, bounds.Y0) - bounds.Y0 - paddingTop + scrollPosition;
		int newCursorLine = (int) ((clickedPosY + lineHeight / 2) / (lineHeight + fontSize)); // clickedPosY+lineHeight/2, to switch between two lines
																								 // just in the middle between them
		String line = "";

		if (lines.size() > newCursorLine) {
			line = lines.get(newCursorLine);
			int clickedPosX = mX - bounds.X0 - paddingLeft; // relative to textbox origin and considering
															 // fullScrollWidth

			float wide = 0;
			for (int i = 0; i < line.length(); i++) {
				float letterWidth = pg.textWidth(line.charAt(i));
				wide += letterWidth;
				if (wide - letterWidth / 2 > clickedPosX) { // set decision point to the center of the letter
					moveCursorTo(i + breakPositions.get(newCursorLine));
					break;
				}
			}

			if (wide < clickedPosX) { // in case clicked beyond last letter - set cursor to end
				// discern between lines that have a forced break vs ones that havn't.
				// On lines with real break we want to set the cursor to the end (before '\n')
				// but that would be on short of what the other types of lines need
				if (line.charAt(line.length() - 1) == '\n')
<<<<<<< HEAD
					moveCursorTo(line.length() + breakPositions.get(newCursorLine) - 2);
				else
					moveCursorTo(line.length() + breakPositions.get(newCursorLine) - 1);
=======
					moveCursorTo(line.length() + lineBreaks.get(newCursorLine) - 2);
				else
					moveCursorTo(line.length() + lineBreaks.get(newCursorLine) - 1);
>>>>>>> branch 'master' of https://github.com/Mc-Zen/guiSET.git
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
	protected void keyPress(KeyEvent e) {
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
	}

	/**
	 * Cut selection and copy to clipboard
	 */
	protected void cut() {
		copy();

		// store this because delete will call cursorChanged which resets selectionStart
		int selStart = selectionStart;
		deleteRange(selectionStart, selectionEnd);
		moveCursorTo(selStart);
		selectionStart = 0;
		selectionEnd = 0;
	}
}