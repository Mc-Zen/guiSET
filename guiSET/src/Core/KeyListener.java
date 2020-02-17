

package guiSET.core;

import guiSET.classes.*;
import processing.core.*;
import processing.event.*;

/**
 * Nothing the otto-normal-user should worry about. Serves internal key event
 * processing.
 * 
 * @author Mc-Zen
 *
 */
public class KeyListener {


	public int currentKeyCode;
	public char currentKey;


	public KeyEvent lastEvent = new KeyEvent(null, 0, 0, 0, (char) 0, 0); // need to create dummy


	Frame frame;

	public KeyListener(Frame frame) {
		this.frame = frame;
	}



	/*
	 * Just some reminders
	 * 
	 * 
	 * 
	 * modifiers: tells which of the 4 modifiers are currently pressed down: check
	 * for specify modifier: return modifiers & (1 << 0) != 0; SHIFT return
	 * modifiers & (1 << 1) != 0; CTRL return modifiers & (1 << 2) != 0; META return
	 * modifiers & (1 << 3) != 0; ALT
	 * 
	 * 
	 * Processing evaluates certain keys as CODED (key = 0xffff) These include:
	 * Ctrl, Shift, Alt, Meta, Windows key, Num key, arrow keys function keys
	 * (F1-F12), end, home, page up, page down, insert, scroll lock, pause, and dead
	 * keys like [accent],^
	 * 
	 * ENTER, RETURN, SPACE, BACKSPACE, DELETE, ESCAPE are not "coded"
	 * 
	 * 
	 * German characters like ae,oe,ue,s behave a bit strangely as their keyCode is 0
	 * but the key is not.
	 * 
	 * 
	 * 
	 * 
	 * get keyCode for char: sun.awt.ExtendedKeyCodes.getExtendedKeyCodeForChar( int
	 * c )
	 */


	// control c is the control which gets the event afterwards - usually the
	// element that currently has focus
	public void handleKeyEvent(KeyEvent event, Control c) {

		lastEvent = event;

		currentKey = event.getKey();
		currentKeyCode = event.getKeyCode();





		// Check if current key-pressed-configuration matches a shortcut
		// shortcut check only if currently down-pressed key is not a modifier
		if (event.getAction() == KeyEvent.PRESS) {
			if (currentKeyCode == PApplet.SHIFT || currentKeyCode == PApplet.CONTROL || currentKeyCode == PApplet.ALT) {

			} else {
				Shortcut current = Shortcut.shortcutFromKeyCode(currentKeyCode, event.getModifiers());
				frame.checkShortcut(current);
			}
		}



		if (c.enabled) {
			switch (event.getAction()) {
			case KeyEvent.PRESS:
				c.keyPress(event);
				break;
			case KeyEvent.TYPE:
				c.keyTyped(event);
				break;
			case KeyEvent.RELEASE:
				c.keyRelease(event);
				break;
			}
		}
	}


	public boolean isShiftDown() {
		return lastEvent.isShiftDown();
	}

	public boolean isControlDown() {
		return lastEvent.isControlDown();
	}

	public boolean isAltDown() {
		return lastEvent.isAltDown();
	}

	public boolean isMetaDown() {
		return lastEvent.isMetaDown();
	}
}

