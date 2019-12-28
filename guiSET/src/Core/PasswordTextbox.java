package guiSET.core;


/**
 * This is a textbox where all chars will be displayed with the passwordChar
 * character. Copying (or cutting) text is not possible or better it will return
 * a bunch of password chars. Pasting is enabled. DISCLAIMER: This class is not the least
 * bit a really secure textbox as the content is stored in memory as a plain
 * string.
 */
public class PasswordTextbox extends Textbox {

	protected char passwordChar = '*';


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
		String placeholder = text;
		text = "";
		for (int i = 0; i < placeholder.length(); i++)
			text += passwordChar;
		super.render();
		text = placeholder;
	}

	/*
	 * disable copying the real text
	 * 
	 * (non-Javadoc)
	 * 
	 * @see guiSET.core.Textbox#copy()
	 */

	@Override
	protected void copy() {
		String placeholder = text;
		text = "";
		for (int i = 0; i < placeholder.length(); i++)
			text += passwordChar;
		super.copy();
		text = placeholder;
	}
}
