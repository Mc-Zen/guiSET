package guiSET.core;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

/**
 * This is a textbox where all chars will be displayed with the passwordChar
 * character. Copying (or cutting) text is not possible or better it will return
 * a bunch of password chars. Pasting is enabled. DISCLAIMER: This class is not
 * the least bit a really secure textbox as the content is stored in memory as a
 * plain string.
 */
public class PasswordTextbox extends Textbox {

	protected char passwordChar = '*';


	public PasswordTextbox() {
	}

	public PasswordTextbox(String hint) {
		super(hint);
	}

	public PasswordTextbox(String hint, int width) {
		super(hint, width);
	}

	public PasswordTextbox(String hint, int width, int fontSize) {
		super(hint, width, fontSize);
	}

	public PasswordTextbox(int width) {
		super(width);
	}

	public PasswordTextbox(int width, int fontSize) {
		super(width, fontSize);
	}


	public char getPasswordChar() {
		return passwordChar;
	}

	/**
	 * Set the character that will be displayed instead of
	 * 
	 * @param passwordChar placeholder char
	 */
	public void setPasswordChar(char passwordChar) {
		this.passwordChar = passwordChar;
		update();
	}


	@Override
	protected void render() {
		String temp = text;
		text = "";
		for (int i = 0; i < temp.length(); i++)
			text += passwordChar;
		super.render();
		text = temp;
	}



	/*
	 * disable copying the real text
	 */

	@Override
	public void copy() {
		if (selectionStart < selectionEnd) {
			String placeholder = "";
			for (int i = 0; i < getSelectedText().length(); i++)
				placeholder += passwordChar;

			StringSelection selection = new StringSelection(placeholder);
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(selection, selection);
		}
	}

	@Override
	public void cut() {
		if (selectionStart < selectionEnd) {
			copy();
			int selStart = selectionStart;
			deleteRange(selectionStart, selectionEnd);
			moveCursorTo(selStart);
			selectionStart = 0;
			selectionEnd = 0;
		}
	}
}
