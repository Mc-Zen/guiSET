package guiSET.classes;

import processing.core.*;

/**
 * Class for storing keyboard shortcuts. Can store value for modifier keys
 * (Control, Shift Meta and Alt) and and an additional normal key.
 * 
 * Shortcuts can be created like Shortcut('A', true, true, false) for
 * Ctrl-Shift-A or like Shortcut(A, CONTROL, SHIFT);
 * 
 * @author Mc-Zen
 *
 */
public class Shortcut {

	private int keyCode;

	private int modifiers = 0;

	public Shortcut() {

	}


	public Shortcut(char key, boolean control, boolean shift, boolean alt) {
		this.keyCode = sun.awt.ExtendedKeyCodes.getExtendedKeyCodeForChar(key);
		setShift(shift);
		setControl(control);
		setAlt(alt);

	}

	/**
	 * Constructor for creating a shortcut with a key and some modifiers (order of
	 * modifiers is not important).
	 * 
	 * @param key
	 * @param modifiers
	 */
	public Shortcut(char key, int... modifiers) {
		this.keyCode = sun.awt.ExtendedKeyCodes.getExtendedKeyCodeForChar(key);
		for (int modifier : modifiers) {
			switch (modifier) {
			case 17:
				setControl(true);
				break;
			case 16:
				setShift(true);
				break;
			case 18:
				setAlt(true);
				break;
			}
		}
	}


	/*
	 *
	 */
	/**
	 * A method to create a shortcut with a given keyCode instead of a key and by
	 * passing the modifiers coded as one single int (as uesd in internally in java
	 * KeyEvents).
	 * 
	 * Need a static method here as not to confuse the constructor overloading too
	 * much. This is only used by {@link KeyListener} as a performance-optimal
	 * constructor.
	 * 
	 * @param keyCode key code
	 * @param modifiers modifiers
	 * @return created shortcut
	 */
	public static Shortcut shortcutFromKeyCode(int keyCode, int modifiers) {
		Shortcut s = new Shortcut();
		s.keyCode = keyCode;
		s.modifiers = modifiers;
		return s;
	}



	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (getClass() != other.getClass())
			return false;

		Shortcut shortcut = (Shortcut) other;

		if (modifiers == shortcut.modifiers && keyCode == shortcut.keyCode) {
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (modifiers) | (keyCode >> 4);
	}



	public void setKey(char key) {
		this.keyCode = sun.awt.ExtendedKeyCodes.getExtendedKeyCodeForChar(key);
	}

	public void setKeyCode(int keyCode) {
		this.keyCode = keyCode;
	}


	public void setShift(boolean state) {
		if (state)
			modifiers |= processing.event.Event.SHIFT;
		else
			modifiers &= ~processing.event.Event.SHIFT;
	}

	public void setControl(boolean state) {
		if (state)
			modifiers |= processing.event.Event.CTRL;
		else
			modifiers &= ~processing.event.Event.CTRL;
	}

	public void setAlt(boolean state) {
		if (state)
			modifiers |= processing.event.Event.ALT;
		else
			modifiers &= ~processing.event.Event.ALT;
	}


	public char getKey() {
		return (char) keyCode;
	}

	public int getKeyCode() {
		return keyCode;
	}

	public boolean getShift() {
		return (modifiers & processing.event.Event.SHIFT) != 0;
	}

	public boolean getControl() {
		return (modifiers & processing.event.Event.CTRL) != 0;
	}

	public boolean getAlt() {
		return (modifiers & processing.event.Event.ALT) != 0;
	}


	/**
	 * Turn shortcut into a string in a format like: "Ctrl+Shift+Alt+K" or
	 * "Shift+Alt+3".
	 */
	public String toString() {
		char c = Character.toUpperCase((char) this.keyCode);
		String s;
		switch (c) {
		case PApplet.DELETE:
			s = "DELETE";
			break;
		case PApplet.BACKSPACE:
			s = "BACKSPACE";
			break;
		case ' ':
			s = "SPACE";
			break;
		case PApplet.ENTER:
			s = "ENTER";
			break;
		case PApplet.RETURN:
			s = "RETURN";
			break;
		case PApplet.UP:
			s = "Arrow Up";
			break;
		case PApplet.DOWN:
			s = "Arrow Down";
			break;
		case PApplet.LEFT:
			s = "Arrow Left";
			break;
		case PApplet.RIGHT:
			s = "Arrow Right";
			break;
		case 35:
			s = "END";
			break;
		default:
			s = String.valueOf(c);
		}
		if (c >= 112 && c <= 123) // F!-F12
			s = "F" + (char) (c - 111);
		return (getControl() ? "Ctrl+" : "") + (getShift() ? "Shift+" : "") + (getAlt() ? "Alt+" : "") + s;
	}

}


/*
 * public class Shortcut {
 * 
 * public char key;
 * 
 * private int modifiers = 0;
 * 
 * public Shortcut(char key, boolean control, boolean shift, boolean alt) {
 * this.key = key; setShift(shift); setControl(control); setAlt(alt);
 * 
 * }
 * 
 * public Shortcut(char Key, int... Modifiers) { this.key =
 * java.lang.Character.toUpperCase(Key); for (int modifier : Modifiers) { switch
 * (modifier) { case 17: setControl(true); break; case 16: setShift(true);
 * break; case 18: setAlt(true); break; } } }
 * 
 * public Shortcut(int modifiers, char key) { this.key =
 * java.lang.Character.toUpperCase(key); this.modifiers = modifiers; }
 * 
 * @Override public boolean equals(Object other) { if (this == other) return
 * true; if (getClass() != other.getClass()) return false;
 * 
 * Shortcut shortcut = (Shortcut) other;
 * 
 * if (modifiers == shortcut.modifiers && key == shortcut.key) { return true; }
 * return false; }
 * 
 * @Override public int hashCode() { return (modifiers) | (key >> 4); }
 * 
 * 
 * public void setShift(boolean state) { if (state) modifiers |=
 * processing.event.Event.SHIFT; else modifiers &=
 * ~processing.event.Event.SHIFT; }
 * 
 * public void setControl(boolean state) { if (state) modifiers |=
 * processing.event.Event.CTRL; else modifiers &= ~processing.event.Event.CTRL;
 * }
 * 
 * public void setAlt(boolean state) { if (state) modifiers |=
 * processing.event.Event.ALT; else modifiers &= ~processing.event.Event.ALT; }
 * 
 * public boolean getShift() { return (modifiers & processing.event.Event.SHIFT)
 * != 0; }
 * 
 * public boolean getControl() { return (modifiers &
 * processing.event.Event.CTRL) != 0; }
 * 
 * public boolean getAlt() { return (modifiers & processing.event.Event.ALT) !=
 * 0; } public String toString() { return (getControl() ? "Ctrl+" : "") +
 * (getShift() ? "Shift+" : "") + (getAlt() ? "Alt+" : "") + this.key; }
 * 
 * }
 */