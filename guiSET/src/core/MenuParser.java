package guiSET.core;

import java.util.ArrayList;

import processing.core.PApplet;

/**
 * Parse a string as menu. This can be a lot easier than creating a menu by hand. Items are wrapped
 * by "&#60;&#62;" and start with their text, followed by an optional ":" after which the callback
 * method comes. Finally a shortcut can be specified by starting with "-". The MenuParser is still a
 * bit experimental, but seems to work so far. TODO: add examples. 
 * 
 * @author E-Bow
 *
 */


public class MenuParser {

	private Node root;


	public MenuParser(String s, MenuBar menubar) {
		root = new Node();
		current = root;

		try {
			parse(s);
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (Node n : root.children) {
			n.finish();
			menubar.add(n.item);
		}


	}

	private class Node {
		Node() {
			item = new MenuItem();
		}

		ArrayList<Node> children = new ArrayList<Node>(0);
		Node parent;

		void add(Node n) {
			children.add(n);
			n.parent = this;
		}

		MenuItem item;

		// add real items of child nodes to item of this node
		void finish() {
			for (Node n : children) {
				n.finish();
				item.add(n.item);
			}
		}

	}

	private void parse(String s) throws Exception {
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
			case '<':
				beginItem();
				break;
			case '>':
				endItem();
				break;
			default:
				content += c;
			}
		}
	}

	private Node current;
	private String content = "";

	private boolean beganItem = false;
	private int level;
	private boolean debug = false;

	// begin a new item (and might need to finish an opened one)
	private void beginItem() {
		finishItem();

		Node newNode = new Node();
		printDebug("begin item");
		current.add(newNode);
		current = newNode;
		beganItem = true;
		level++;
	}


	// finish an item if one has been started by filling in the info from the
	// content string
	private void finishItem() {
		if (beganItem)
			fillInItem();
	}


	private void endItem() throws Exception {
		finishItem();
		level--;

		printDebug("end item", current.children);

		current = current.parent;
		if (current == null)
			throw new Exception("parse error");
		beganItem = false;
	}



	// take the current content string and parse text, callback method name and
	// shortcut
	void fillInItem() {
		if (content.isEmpty())
			return;

		String methodName = null;
		Shortcut shortcut = null;

		int shortcutIndex = content.indexOf('-');
		int methodIndex = content.indexOf(':');

		if (shortcutIndex > -1) {
			String shortcutText = content.substring(shortcutIndex + 1).trim();
			shortcut = parseShortcut(shortcutText);
			content = content.substring(0, shortcutIndex);
		}
		if (methodIndex > -1) {
			methodName = content.substring(methodIndex + 1).trim();
			content = content.substring(0, methodIndex);
		}

		if (methodName != null) {
			if (shortcut != null) {
				current.item.registerShortcutAndMethod(methodName, shortcut);
			} else {
				//PApplet.println(methodName);
				current.item.setSelectListener(methodName);
			}
		} else {
			current.item.setShortcut(shortcut);
		}

		current.item.setText(content.trim());
		content = "";
		printDebug("Fill in", current.item.text, current.item.getShortcut() != null ? current.item.getShortcut().toString() : "");
	}

	// parse a shortcut
	private Shortcut parseShortcut(String str) {
		String[] pieces = str.split("-");
		boolean ctrl = false, alt = false, shift = false;
		char key = 0;

		for (String piece : pieces) {
			if (piece.length() == 0)
				continue;
			switch (piece.toLowerCase()) {
			case "control":
			case "ctrl":
				ctrl = true;
				break;
			case "shift":
			case "shft":
				shift = true;
				break;
			case "alt":
				alt = true;
				break;
			default:
				key = piece.charAt(0);
			}
		}
		if (key != 0) {
			Shortcut s = new Shortcut(key, ctrl, shift, alt);
			return s;
		}
		return null;
	}



	// according to level, return level*2 whitespaces (for debugging)
	private String ws() {
		String s = "";
		for (int i = 0; i < level; i++) {
			s += "  ";
		}
		return s;
	}

	private void printDebug(Object... items) {
		if (debug) {
			PApplet.println(ws(), items);
		}
	}
}