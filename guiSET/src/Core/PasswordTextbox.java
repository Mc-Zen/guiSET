package pGUI.core;


/*
 * ! This Class is not the least bit a secure textbox !
 * 
 * It's just a textfield where all chars will be displayed with the passwordChar character. 
 * Copying (or cutting) text is not possible or better it will return a bunch of password chars. 
 * Pasting is enabled. 
 * 
 * The content is stored as a plain string as usual
 */


public class PasswordTextbox extends Textbox {

	protected char passwordChar = '*';


	public char getPasswordChar() {
		return passwordChar;
	}


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
	 * @see pGUI.core.Textbox#copy()
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
