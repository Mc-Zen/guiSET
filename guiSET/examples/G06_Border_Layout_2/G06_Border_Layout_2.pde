import guiSET.core.*;

/*
 * Same result as the example Border_Layout but this time achieved by using
 * setAnchor() instead of using addAutoAnchor()
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
    size(500, 400); // choose any size
    
    f = new Frame(this);
    f.setResizable(true);
        
    north = new Container(width, northHeight); // I left this one the same
    south = new Container();
    west = new Container();
    east = new Container();
    center = new Container();
    f.add(north, south, west, east, center); // add them all to f before creating any anchors !


    north.addAutoAnchors(LEFT, RIGHT);

    west.setWidth(westWidth);
    west.setAnchor(TOP, northHeight);
    west.setAnchor(BOTTOM, southHeight);

    east.setWidth(eastWidth);
    east.setAnchor(RIGHT, 0);
    east.setAnchor(TOP, northHeight);
    east.setAnchor(BOTTOM, southHeight);

    south.setHeight(southHeight);
    south.setAnchor(LEFT, 0);
    south.setAnchor(RIGHT, 0);
    south.setAnchor(BOTTOM, 0);

    center.setAnchor(TOP, northHeight);
    center.setAnchor(LEFT,westWidth);
    center.setAnchor(RIGHT, eastWidth);
    center.setAnchor(BOTTOM, southHeight);  

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
