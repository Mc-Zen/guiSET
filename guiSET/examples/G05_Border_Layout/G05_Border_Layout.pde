import guiSET.core.*;

/*
 * Example for a border layout and a demonstration for 
 * The window is split into a header, footer, two sidebars and the main area - a.k.a. 
 * north, south, west, east, and center.
 *
 * Their size is adjusted automatically when the window is resized. 
 */

Frame f;
Container north;
Container west;
Container east;
Container south;
Container center;

int northHeight = 25; // (fixed) height of header ("north" section)
int southHeight = 30; // (fixed) height of footer ("south" section)
int eastWidth = 50;   // (fixed) width of left sidebar ("west" section)
int westWidth = 50;   // (fixed) width of right sidebar ("east" section)

void setup() {
  size(400, 300);       // choose any size
  f = new Frame(this);
  f.setResizable(true); // enable window resizing (default is off)

  north = new Container(width, northHeight);
  south = new Container(width, southHeight);
  east = new Container(eastWidth, height - northHeight - southHeight);
  west = new Container(westWidth, height - northHeight - southHeight);
  center = new Container(width - eastWidth - westWidth, height - northHeight - southHeight);
  f.add(north, south, west, east, center);  // add them all to f before creating any anchors !


  south.setY(height - southHeight);
  west.setY(northHeight);
  center.setPosition(westWidth, northHeight);
  east.setPosition(width - eastWidth, northHeight);


  north.addAutoAnchors(LEFT, RIGHT);
  west.addAutoAnchors(TOP, BOTTOM);
  east.addAutoAnchors(TOP, RIGHT, BOTTOM);
  south.addAutoAnchors(LEFT, RIGHT, BOTTOM);
  center.addAutoAnchors(LEFT, RIGHT, TOP, BOTTOM);


  // Set background colors and add labels for demonstration purposes
 
  north.setBackgroundColor(color(200));
  north.add(new Label("North"));
  west.setBackgroundColor(color(220, 50, 67));
  west.add(new Label("West"));
  east.setBackgroundColor(color(200, 83, 67));
  east.add(new Label("East"));
  south.setBackgroundColor(color(150));
  south.add(new Label("South"));
  center.setBackgroundColor(color(255, 252, 229));
  center.add(new Label("Center"));
}

void draw() {
}
