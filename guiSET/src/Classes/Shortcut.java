package pGUI.classes;


/*
   * 
 * Class for storing keyboard shortcuts. 
 * Can store value for Control-, Shift- and Alt-Key and and an additional key. 
 * 
 * Shortcuts can be created like Shortcut('A', true, true, false) for Ctrl-Shift-A
 * or like Shortcut(A, CONTROL, SHIFT);
 *
 *
 */





public class Shortcut {

	public int keyCode;

	private int modifiers = 0;

	public Shortcut() {
		
	}
	
	public Shortcut(int key, boolean control, boolean shift, boolean alt) {
		this.keyCode = key;
		setShift(shift);
		setControl(control);
		setAlt(alt);

	}

	public Shortcut(int key, int... modifiers) {
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
	
	
	// need a static method here as not to confuse the method overloading to much
	// this is anyway only used by KeyListener as a performance-optimal Constructor
	public static Shortcut directShortcut(int key, int modifiers) {
		Shortcut s = new Shortcut();
		s.keyCode = key;
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

	public boolean getShift() {
		return (modifiers & processing.event.Event.SHIFT) != 0;
	}

	public boolean getControl() {
		return (modifiers & processing.event.Event.CTRL) != 0;
	}

	public boolean getAlt() {
		return (modifiers & processing.event.Event.ALT) != 0;
	}
	public String toString() {
		return (getControl() ? "Ctrl+" : "") + (getShift() ? "Shift+" : "") + (getAlt() ? "Alt+" : "") + Character.toUpperCase((char)this.keyCode);
	}
	
}


/*
public class Shortcut {

	public char key;

	private int modifiers = 0;

	public Shortcut(char key, boolean control, boolean shift, boolean alt) {
		this.key = key;
		setShift(shift);
		setControl(control);
		setAlt(alt);

	}

	public Shortcut(char Key, int... Modifiers) {
		this.key = java.lang.Character.toUpperCase(Key);
		for (int modifier : Modifiers) {
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

	public Shortcut(int modifiers, char key) {
		this.key = java.lang.Character.toUpperCase(key);
		this.modifiers = modifiers;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (getClass() != other.getClass())
			return false;

		Shortcut shortcut = (Shortcut) other;

		if (modifiers == shortcut.modifiers && key == shortcut.key) {
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (modifiers) | (key >> 4);
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

	public boolean getShift() {
		return (modifiers & processing.event.Event.SHIFT) != 0;
	}

	public boolean getControl() {
		return (modifiers & processing.event.Event.CTRL) != 0;
	}

	public boolean getAlt() {
		return (modifiers & processing.event.Event.ALT) != 0;
	}
	public String toString() {
		return (getControl() ? "Ctrl+" : "") + (getShift() ? "Shift+" : "") + (getAlt() ? "Alt+" : "") + this.key;
	}
	
}*/